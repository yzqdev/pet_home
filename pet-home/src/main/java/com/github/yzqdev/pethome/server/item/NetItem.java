package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.entity.NetEntity;
import com.github.yzqdev.pethome.util.ItemMobTooltip;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class NetItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Type type;

    public NetItem(Type type, Properties properties) {
        super(properties.stacksTo(1));
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = context.getItemInHand();
        if (world.isClientSide() || !containsEntity(stack)) {
            return InteractionResult.FAIL;
        }

        Entity entity = getEntityFromStack(stack, world, true);
        BlockPos blockPos = context.getClickedPos();
        entity.snapTo(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, 0, 0);
        stack.set(PHDataComponents.ENTITY_HOLDER, null);
        world.addFreshEntity(entity);
        context.getItemInHand().shrink(1);
        if (player.isCreative()) {
            player.setItemInHand(context.getHand(), new ItemStack(PHItemRegistry.NET_ITEM.get()));
        }


        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target.level().isClientSide() || target instanceof Player || !target.isAlive() || containsEntity(stack)) {
            return InteractionResult.FAIL;
        }
        if (this.type == Type.EMPTY) {
            EntityType<?> entityID = target.getType();
            if (!canCatchMob(target)) {
                return InteractionResult.FAIL;
            }
            ItemStack newStack = new ItemStack(PHItemRegistry.NET_HAS_ITEM.get());
            CompoundTag nbt = getNBTfromEntity(target);
            ItemStack newerStack = newStack.split(1);
            newerStack.set(PHDataComponents.ENTITY_HOLDER, nbt);

            player.swing(hand);
            player.setItemInHand(hand, newStack);
            if (!player.addItem(newerStack)) {
                ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), newerStack);
                player.level().addFreshEntity(itemEntity);
            }
            target.discard();
            player.getCooldowns().addCooldown(newStack, 5);
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    // 已警告过的解析失败实体名，避免每帧 getName 刷日志；三侧原版均未初始化（NPE），此处补上
    static Set<String> warned = new HashSet<>();

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.isCurrentlyGlowing()) {
            entity.setGlowingTag(true);
        }
        if (!entity.isInvulnerable()) {
            entity.setInvulnerable(true);
        }
        Vec3 position = entity.position();
        int minY = entity.level().getMinY();
        if (position.y < minY) {
            entity.setNoGravity(true);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setPos(position.x, minY, position.z);
        }
        return super.onEntityItemUpdate(stack, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltip, Consumer<Component> builder, TooltipFlag tooltipFlag) {

        if (type == Type.HAS_MOB) {
            if (containsEntity(stack)) {
                CompoundTag holder = getEntityData(stack);
                String id = holder.getStringOr("id", "");
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(id));
                builder.accept(type.getDescription());
                builder.accept(Component.translatable(LangDefinition.health_text)
                        .append(": " + String.format("%.1f", (getEntityData(stack).getDoubleOr("Health", 0.0)))));
            }
        } else {
            super.appendHoverText(stack, context, tooltip, builder, tooltipFlag);
            builder.accept(Component.translatable(LangDefinition.net_launcher_default_only_tamable).withStyle(ChatFormatting.GRAY));
        }

    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (this.type == Type.EMPTY) {
            return Optional.empty();

        } else {
            if (stack.has(PHDataComponents.ENTITY_HOLDER)) {
                var tag = stack.get(PHDataComponents.ENTITY_HOLDER);
                return Optional.of(new ItemMobTooltip(tag));
            } else {
                return Optional.empty();
            }

        }
    }

    public static Component getNameFromStoredEntity(ItemStack stack) {
        CompoundTag holder = getEntityData(stack);
        if (holder.contains("CustomName")) {
            String s = holder.getStringOr("CustomName", "");
            try {
                JsonElement jsonElement = JsonParser.parseString(s);
                return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow();
            } catch (Exception exception) {
                if (!warned.contains(s)) {
                    LOGGER.warn("Failed to parse entity custom name {}", s, exception);
                    warned.add(s);
                }
            }
        }
        String id = holder.getStringOr("id", "");
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(id));
        return type.getDescription().copy().withStyle(ChatFormatting.AQUA);
    }

    @Override
    @Nonnull
    public Component getName(@Nonnull ItemStack stack) {
        Component nameC = super.getName(stack);
        if (!containsEntity(stack)) {
            return nameC;
        } else {

            return ((MutableComponent) nameC.copy())
                    .append(" (")
                    .append(getNameFromStoredEntity(stack))
                    .append(")");
        }
    }

    public NetEntity createNet(Level worldIn, LivingEntity shooter, ItemStack stack) {
        ItemStack newStack = stack.copy();
        newStack.setCount(1);
        return new NetEntity(shooter.getX(), shooter.getY() + 1.25, shooter.getZ(), worldIn, newStack);
    }

    //helper methods

    public static boolean containsEntity(ItemStack stack) {
        return stack != null && stack.has(PHDataComponents.ENTITY_HOLDER);
    }

    public static CompoundTag getEntityData(ItemStack stack) {
        return containsEntity(stack) ? stack.get(PHDataComponents.ENTITY_HOLDER) : new CompoundTag();
    }

    public static String getEntityID(CompoundTag nbt) {
        return nbt.getStringOr("id", "");
    }

    public static boolean isBlacklisted(EntityType<?> type) {
        return type == EntityType.PLAYER || PetHomeConfig.mobcatcherBlackList.contains(type);
    }

    public static boolean canCatchMob(Entity livingEntity) {
        var type = livingEntity.getType();
        if (PetHomeConfig.mobcatcherOnlyTamableAnimal) {
            if (livingEntity instanceof TamableAnimal && !(isBlacklisted(type))) {
                return true;
            } else {
                return false;
            }
        } else {
            if (isBlacklisted(type)) {
                return false;
            } else {
                return true;
            }

        }

    }

    public static Entity getEntityFromNBT(CompoundTag nbt, Level world, boolean withInfo) {
        Entity entity = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(getEntityID(nbt))).create(world, EntitySpawnReason.COMMAND);
        if (withInfo && entity != null) {
            var tagValue = TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), nbt);
            entity.load(tagValue);
        }
        return entity;
    }

    public static Entity getEntityFromStack(ItemStack stack, Level world, boolean withInfo) {
        return getEntityFromNBT(stack.get(PHDataComponents.ENTITY_HOLDER), world, withInfo);
    }

    public static CompoundTag getNBTfromEntity(Entity entity) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), com.github.yzqdev.pethome.PHConstants.LOG)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
            entity.save(output);
            return output.buildResult();
        }
    }
}
