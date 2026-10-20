package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.entity.NetEntity;
import com.github.yzqdev.pethome.util.ItemMobTooltip;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 捕网：空网右键生物经 canCatchMob 校验后把完整实体 NBT 存入
 * {@link PHDataComponents#ENTITY_HOLDER} 数据组件、原实体 discard、5 tick 冷却；
 * 有内容的网 useOn 放出实体。掉落物形态发光/无敌/防坠虚空（onEntityItemUpdate）。
 */
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

    /** 有内容的网放到方块上：从 NBT 重建实体到点击点上方一格，清空组件；创造模式还给空网 */
    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = context.getItemInHand();
        if (world.isClientSide || !containsEntity(stack)) {
            return InteractionResult.FAIL;
        }

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

    /** 空网右键生物：黑名单/存活校验后抓捕（NBT 入组件、discard 实体、入背包或掉落），仅服务端生效 */
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

    // 已警告过的解析失败实体名，避免每帧 getName 刷日志；三侧原版均未初始化（NPE），此处补上
    static Set<String> warned = new HashSet<>();

    /** 网掉落物每 tick：常亮+无敌；掉出世界底部时悬停冻结在 minY（存档里的宠物不丢失） */
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
                tooltip.add(Component.translatable(LangDefinition.health_text).append(": " + String.format("%.1f", (getEntityData(stack).getDouble("Health")))));
            }
        } else {
            super.appendHoverText(stack, context, tooltip, tooltipFlag);
            tooltip.add(Component.translatable(LangDefinition.net_launcher_default_only_tamable).withStyle(ChatFormatting.GRAY));
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

    /** registryAccess：优先服务端；无服务端（客户端 tooltip 场景）回退客户端 world，避免专用类依赖 */
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
        if (!containsEntity(stack)) {
            return nameC;
        } else {
            return ((MutableComponent) nameC)
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

    // 辅助方法

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

    /**
     * 抓捕白名单：默认全生物可抓（玩家恒排除 + 配置黑名单）；
     * mobcatcherOnlyTamableAnimal 开启时仅 TamableAnimal 可抓。
     */
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

    /** 从 NBT 重建实体；{@code withInfo=false} 只按 id 创建不 load（调用方需自担 null 实体风险） */
    public static Entity getEntityFromNBT(CompoundTag nbt, Level world, boolean withInfo) {
        Entity entity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(getEntityID(nbt))).create(world);
        if (withInfo) {
            entity.load(nbt);
        }
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
