package com.github.yzqdev.pet_home.server.item;

import com.github.yzqdev.pet_home.PetHomeConfig;
import com.github.yzqdev.pet_home.datagen.LangDefinition;
import com.github.yzqdev.pet_home.server.PHDataComponents;
import com.github.yzqdev.pet_home.server.entity.NetEntity;
import com.github.yzqdev.pet_home.util.ItemMobTooltip;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NetItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Type type;

    public NetItem(Type type) {
        super(new Properties().stacksTo(1));
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
        if (player == null) return InteractionResult.FAIL;
        ItemStack stack = context.getItemInHand();
        if (world.isClientSide || !containsEntity(stack)) return InteractionResult.FAIL;

        Entity entity = getEntityFromStack(stack, world, true);
        BlockPos blockPos = context.getClickedPos();
        entity.absMoveTo(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, 0, 0);
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
        if (target.getCommandSenderWorld().isClientSide || target instanceof Player || !target.isAlive() || containsEntity(stack)) {
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
            player.getCooldowns().addCooldown(this, 5);
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    static Set<String> warned;

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.isCurrentlyGlowing()) {
            entity.setGlowingTag(true);
        }
        if (!entity.isInvulnerable()) {
            entity.setInvulnerable(true);
        }
        Vec3 position = entity.position();
        int minY = entity.level().getMinBuildHeight();
        if (position.y < minY) {
            entity.setNoGravity(true);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setPos(position.x, minY, position.z);
        }
        return super.onEntityItemUpdate(stack, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {

        if (type == Type.HAS_MOB) {
            if (containsEntity(stack)) {
                CompoundTag holder = getEntityData(stack);
                String id = holder.getString("id");
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
                tooltip.add(type.getDescription());
                tooltip.add(Component.translatable(LangDefinition.ConstantMsg.health_text).append(": " + String.format("%.1f", (getEntityData(stack).getDouble("Health")))));
            }
        } else {
            super.appendHoverText(stack, context, tooltip, tooltipFlag);
           tooltip.add(Component.translatable(LangDefinition.ConstantMsg.net_launcher_default_only_tamable).withStyle(ChatFormatting.GRAY));
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

    public static RegistryAccess registryAccess() {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer().registryAccess();
        }
        return LogicalSidedProvider.CLIENTWORLD.get(LogicalSide.CLIENT).orElseThrow().registryAccess();
    }

    public static Component getNameFromStoredEntity(ItemStack stack) {
        CompoundTag holder = getEntityData(stack);
        if (holder.contains("CustomName", Tag.TAG_STRING)) {
            String s = holder.getString("CustomName");
            try {
                return Component.Serializer.fromJson(s, registryAccess());
            } catch (Exception exception) {
                if (!warned.contains(s)) {
                    LOGGER.warn("Failed to parse entity custom name {}", s, exception);
                    warned.add(s);
                }
            }
        }
        String id = holder.getString("id");
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
        return type.getDescription().copy().withStyle(ChatFormatting.AQUA);
    }

    @Override
    @Nonnull
    public Component getName(@Nonnull ItemStack stack) {
        Component nameC = super.getName(stack);
        if (!containsEntity(stack))
            return nameC;
        else return ((MutableComponent) nameC)
                .append(" (")
                .append(getNameFromStoredEntity(stack))
                .append(")");
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
        return nbt.getString("id");
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
        Entity entity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(getEntityID(nbt))).create(world);
        if (withInfo) entity.load(nbt);
        return entity;
    }

    public static Entity getEntityFromStack(ItemStack stack, Level world, boolean withInfo) {
        return getEntityFromNBT(stack.get(PHDataComponents.ENTITY_HOLDER), world, withInfo);
    }

    public static CompoundTag getNBTfromEntity(Entity entity) {
        CompoundTag nbt = new CompoundTag();
        entity.save(nbt);
        return nbt;
    }
}
