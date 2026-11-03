package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.NbtKeys;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * disc_jockey（唱片骑师，自 1.20 移植）附魔实体：漂浮跟随宠物的迷你唱片机。 唱片内容存宠物 citadel tag（随宠物存档），本实体只同步当前唱片 ItemStack
 * 供客户端渲染/音效； 跟随目标 UUID 为服务端普通字段（仅NBT持久化，不同步客户端——客户端渲染不需要）。
 */
public class FollowingJukeboxEntity extends Entity {

    private static final EntityDataAccessor<ItemStack> JUKEBOX_ITEM =
            SynchedEntityData.defineId(FollowingJukeboxEntity.class, EntityDataSerializers.ITEM_STACK);

    /** 跟随的宠物 UUID（服务端字段，NBT 持久化）。 */
    @Nullable private UUID followingUUID;

    /** 上次广播的实体事件号，避免每 tick 重复广播 66/67。 */
    private byte lastBroadcastEvent = -1;

    public FollowingJukeboxEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    /**
     * 每 tick：自旋并按 delta 移动；距宠物远于 2+宽度格直接贴过去，否则绕宠物做圆周飘动。 服务端从宠物 citadel tag 拉取唱片（内容变化才重设数据槽），广播实体事件
     * 66（播放中）/67（空闲）驱动客户端 DiscJockeySound 起停； 宠物失去 disc_jockey 附魔或目标消失时解除绑定并 discard。
     */
    @Override
    public void tick() {
        super.tick();
        LivingEntity following = getFollowing();
        this.setPos(this.position().add(this.getDeltaMovement()));
        this.setYRot(this.getYRot() + 2F);
        if (!level().isClientSide()) {
            if (following != null) {
                float width = following.getBbWidth() + 0.6F;
                ItemStack disc = getDiscFromOwner();
                if (!ItemStack.matches(disc, this.getRecordItem())) {
                    this.setRecordItem(disc);
                }
                if (this.distanceTo(following) > 2 + width) {
                    this.copyPosition(following);
                } else {
                    double targetX = following.getX() + Math.sin(tickCount * 0.05F) * width;
                    double targetY = following.getY();
                    double targetZ = following.getZ() - Math.cos(tickCount * 0.05F) * width;
                    Vec3 vec3 =
                            new Vec3(
                                    targetX - this.getX(),
                                    targetY - this.getY() + Math.sin(tickCount * 0.3F + 2F) * 0.01F,
                                    targetZ - this.getZ());
                    this.setDeltaMovement(vec3);
                }
                byte event = (!this.isSilent() && !this.getRecordItem().isEmpty()) ? (byte) 66 : (byte) 67;
                if (event != lastBroadcastEvent || (event == 66 && tickCount % 100 == 0)) {
                    this.level().broadcastEntityEvent(this, event);
                    lastBroadcastEvent = event;
                }
                if (!TameableUtils.hasEnchant(following, PHEnchantments.DISC_JOCKEY)) {
                    this.followingUUID = null;
                }
            } else {
                this.setRecordItem(ItemStack.EMPTY);
                this.discard();
            }
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(JUKEBOX_ITEM, ItemStack.EMPTY);
    }

    /** 唱片机不可被伤害。 */
    @Override
    public boolean hurtServer(
            ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.followingUUID = input.read(NbtKeys.JUKEBOX_FOLLOWER_UUID, UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.followingUUID != null) {
            output.store(NbtKeys.JUKEBOX_FOLLOWER_UUID, UUIDUtil.CODEC, this.followingUUID);
        }
    }

    @Nullable
    public UUID getFollowerUUID() {
        return this.followingUUID;
    }

    public void setFollowingUUID(@Nullable UUID uniqueId) {
        this.followingUUID = uniqueId;
    }

    /** 跟随的宠物实例（仅服务端可查；客户端/未加载返回 null）。 */
    @Nullable
    public LivingEntity getFollowing() {
        if (this.followingUUID != null
                && !level().isClientSide()
                && level() instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(this.followingUUID) instanceof LivingEntity living
                    ? living
                    : null;
        }
        return null;
    }

    public ItemStack getRecordItem() {
        return this.entityData.get(JUKEBOX_ITEM);
    }

    public void setRecordItem(ItemStack item) {
        this.entityData.set(JUKEBOX_ITEM, item);
    }

    /** 写唱片到跟随宠物的 citadel tag（空 stack 即取下）；取下时广播 67 让客户端立即停音效。 */
    public void addDiscToOwner(ItemStack stack) {
        LivingEntity living = getFollowing();
        if (living != null) {
            TameableUtils.setPetJukeboxDisc(living, stack);
            if (stack.isEmpty()) {
                this.level().broadcastEntityEvent(this, (byte) 67);
            }
        }
    }

    public ItemStack getDiscFromOwner() {
        LivingEntity living = getFollowing();
        return living != null ? TameableUtils.getPetJukeboxDisc(living) : ItemStack.EMPTY;
    }

    public boolean isPickable() {
        return true;
    }

    /**
     * 右键统一走此方法（interactAt 已合并入 interact）：取下当前唱片掉落，或把手持唱片装入（生存消耗）。
     *
     * <p>{@link Player#interactOn} 双端执行，客户端线程的 level 是 ClientLevel——而 26.x 的 {@code spawnAtLocation} 只收 ServerLevel，
     * 此处所有世界修改副作用（掉落唱片、改宠物 citadel tag、消耗手持）一律只准服务端做，客户端返回 PASS 交由原版发包权威执行。
     */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (this.level().isClientSide()) {
            return InteractionResult.PASS;
        }
        boolean flag = false;
        ItemStack held = player.getItemInHand(hand);
        if (!this.getRecordItem().isEmpty()) {
            ItemStack copy = this.getRecordItem().copy();
            addDiscToOwner(ItemStack.EMPTY);
            this.spawnAtLocation((ServerLevel) this.level(), copy);
            flag = true;
        }
        if (isMusicDisc(held)) {
            addDiscToOwner(held);
            if (!player.isCreative()) {
                held.shrink(1);
            }
            flag = true;
        }
        return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /** 音乐唱片判定：物品带 JUKEBOX_PLAYABLE 组件。 */
    private static boolean isMusicDisc(ItemStack stack) {
        return !stack.isEmpty() && stack.has(DataComponents.JUKEBOX_PLAYABLE);
    }

    /** 当前唱片的音效（JUKEBOX_PLAYABLE → JukeboxSong → soundEvent）；无唱片返回 null。 */
    @Nullable
    public SoundEvent getRecordSound() {
        JukeboxPlayable playable = this.getRecordItem().get(DataComponents.JUKEBOX_PLAYABLE);
        return playable != null ? playable.song().value().soundEvent().value() : null;
    }

    /** 仅客户端：66/67 转交 {@link ClientGameEvents#updateEntityStatus} 驱动 DiscJockeySound 起停，播放中随机冒音符粒子。 */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 66 || id == 67) {
            if (this.level().isClientSide()) {
                if (this.getRecordSound() != null && this.random.nextFloat() < 0.1F) {
                    float f = this.random.nextFloat();
                    float f1 = this.random.nextFloat();
                    float f2 = this.random.nextFloat();
                    this.level()
                            .addParticle(ParticleTypes.NOTE, this.getX(), this.getY(1), this.getZ(), f, f1, f2);
                }
                ClientGameEvents.updateEntityStatus(this, id);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }
}
