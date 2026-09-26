package com.github.yzqdev.pethome.server.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class ServerEventContexts {

    private ServerEventContexts() {
    }

    /** 对应 {@code LivingDropsEvent}：由 {@code LivingEntityDropsMixin} 在 dropAllDeathLoot 头部触发 */
    public static final class LivingDropsEvent {
        private final LivingEntity entity;
        private boolean canceled;

        public LivingDropsEvent(LivingEntity entity) {
            this.entity = entity;
        }

        public LivingEntity getEntity() {
            return entity;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    /** 对应 {@code ServerAboutToStartEvent} */
    public static final class ServerAboutToStartEvent {
        private final MinecraftServer server;

        public ServerAboutToStartEvent(MinecraftServer server) {
            this.server = server;
        }

        public MinecraftServer getServer() {
            return server;
        }
    }

    /** 对应 {@code LevelTickEvent.Post} */
    public static final class LevelTickEvent {
        private LevelTickEvent() {
        }

        public static final class Post {
            private final Level level;

            public Post(Level level) {
                this.level = level;
            }

            public Level getLevel() {
                return level;
            }
        }
    }

    /** 对应 {@code EntityTravelToDimensionEvent}：由 {@code ServerEntityWorldChangeEvents} 触发 */
    public static final class EntityTravelToDimensionEvent {
        private final Entity entity;
        private final ResourceKey<Level> dimension;
        private boolean canceled;

        public EntityTravelToDimensionEvent(Entity entity, ResourceKey<Level> dimension) {
            this.entity = entity;
            this.dimension = dimension;
        }

        public Entity getEntity() {
            return entity;
        }

        public ResourceKey<Level> getDimension() {
            return dimension;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    /** 对应 {@code EntityTeleportEvent}：由 {@code EntityTeleportMixin} 在 teleportTo 头部触发 */
    public static final class EntityTeleportEvent {
        private final Entity entity;
        private final Vec3 prev;
        private final Vec3 target;

        public EntityTeleportEvent(Entity entity, Vec3 prev, Vec3 target) {
            this.entity = entity;
            this.prev = prev;
            this.target = target;
        }

        public Entity getEntity() {
            return entity;
        }

        public Vec3 getPrev() {
            return prev;
        }

        public Vec3 getTarget() {
            return target;
        }
    }

    /** 对应 {@code ProjectileImpactEvent}：由 {@code ProjectileImpactMixin} 在 onHit 头部触发 */
    public static final class ProjectileImpactEvent {
        private final Projectile projectile;
        private final HitResult rayTraceResult;
        private boolean canceled;

        public ProjectileImpactEvent(Projectile projectile, HitResult rayTraceResult) {
            this.projectile = projectile;
            this.rayTraceResult = rayTraceResult;
        }

        public Projectile getProjectile() {
            return projectile;
        }

        /** NeoForge 的 ProjectileImpactEvent 继承自 EntityEvent，getEntity() 就是投掷物本身 */
        public Entity getEntity() {
            return projectile;
        }

        public HitResult getRayTraceResult() {
            return rayTraceResult;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    /** 对应 {@code EntityMountEvent}：由 {@code EntityMountMixin} 在 stopRiding 头部触发 */
    public static final class EntityMountEvent {
        private final Entity entity;
        private final Entity entityBeingMounted;
        private final boolean dismounting;
        private boolean canceled;

        public EntityMountEvent(Entity entity, Entity entityBeingMounted, boolean dismounting) {
            this.entity = entity;
            this.entityBeingMounted = entityBeingMounted;
            this.dismounting = dismounting;
        }

        public Entity getEntity() {
            return entity;
        }

        public Entity getEntityBeingMounted() {
            return entityBeingMounted;
        }

        public boolean isDismounting() {
            return dismounting;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    /** 对应 {@code EntityJoinLevelEvent}：由 {@code ServerEntityEvents.ENTITY_LOAD} 触发 */
    public static final class EntityJoinLevelEvent {
        private final Entity entity;
        private final Level level;

        public EntityJoinLevelEvent(Entity entity, Level level) {
            this.entity = entity;
            this.level = level;
        }

        public Entity getEntity() {
            return entity;
        }

        public Level getLevel() {
            return level;
        }
    }

    /** 对应 {@code EntityLeaveLevelEvent}：由 {@code ServerEntityEvents.ENTITY_UNLOAD} 触发 */
    public static final class EntityLeaveLevelEvent {
        private final Entity entity;

        public EntityLeaveLevelEvent(Entity entity) {
            this.entity = entity;
        }

        public Entity getEntity() {
            return entity;
        }
    }

    /** 对应 {@code LivingDeathEvent}：由 {@code ServerLivingEntityEvents.AFTER_DEATH} 触发 */
    public static final class LivingDeathEvent {
        private final LivingEntity entity;
        private final DamageSource source;

        public LivingDeathEvent(LivingEntity entity, DamageSource source) {
            this.entity = entity;
            this.source = source;
        }

        public LivingEntity getEntity() {
            return entity;
        }

        public DamageSource getSource() {
            return source;
        }
    }

    /** 对应 {@code ExplosionEvent.Start}：由 {@code ExplosionStartMixin} 触发 */
    public static final class ExplosionEvent {
        private ExplosionEvent() {
        }

        public static final class Start {
            private final Explosion explosion;
            private boolean canceled;

            public Start(Explosion explosion) {
                this.explosion = explosion;
            }

            public Explosion getExplosion() {
                return explosion;
            }

            /** NeoForge 的 ExplosionEvent 提供 getLevel()，这里从爆炸实例上取 */
            public Level getLevel() {
                return explosion.level();
            }

            public boolean isCanceled() {
                return canceled;
            }

            public void setCanceled(boolean canceled) {
                this.canceled = canceled;
            }
        }
    }

    /** 对应 {@code BreakBlockEvent}：由 {@code PlayerBlockBreakEvents.AFTER} 触发 */
    public static final class BreakBlockEvent {
        private final Player player;
        private final Level level;
        private final BlockPos pos;
        private final BlockState state;

        public BreakBlockEvent(Player player, Level level, BlockPos pos, BlockState state) {
            this.player = player;
            this.level = level;
            this.pos = pos;
            this.state = state;
        }

        public Player getPlayer() {
            return player;
        }

        public Level getLevel() {
            return level;
        }

        public BlockPos getPos() {
            return pos;
        }

        public BlockState getState() {
            return state;
        }
    }

    /**
     * 对应 {@code ItemExpireEvent}：由 {@code ItemEntityExpireMixin} 在掉落物寿命到期时触发。
     *
     * <p>{@link #setExtraLife(int)} 与 NeoForge 语义一致：额外延长寿命，并让调用方知道
     * 「本次过期已被接管」，从而不再执行原版的移除逻辑。</p>
     */
    public static final class ItemExpireEvent {
        private final ItemEntity entity;
        private int extraLife;

        public ItemExpireEvent(ItemEntity entity) {
            this.entity = entity;
        }

        public ItemEntity getEntity() {
            return entity;
        }

        public int getExtraLife() {
            return extraLife;
        }

        public void setExtraLife(int extraLife) {
            this.extraLife = extraLife;
        }
    }

    /** 对应 {@code ItemTooltipEvent}（客户端） */
    public static final class ItemTooltipEvent {
        private final ItemStack itemStack;
        private final List<Component> toolTip;

        public ItemTooltipEvent(ItemStack itemStack, List<Component> toolTip) {
            this.itemStack = itemStack;
            this.toolTip = toolTip;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public List<Component> getToolTip() {
            return toolTip;
        }

        /** 便于把不可变列表转成可写列表后构造 */
        public static List<Component> mutableTooltip(List<Component> lines) {
            return lines instanceof ArrayList ? lines : new ArrayList<>(lines);
        }
    }
}
