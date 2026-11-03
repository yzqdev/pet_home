package com.github.yzqdev.pethome.server.block;


import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.server.misc.PHWorldData;
import com.github.yzqdev.pethome.server.misc.RespawnRequest;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * 宠物床方块实体：tick 处理复活流。复活请求来自 {@code PHWorldData.respawnRequestList}
 * （按本床维度+坐标过滤），次日清晨（主世界时钟 time==1）消费。
 */
public class PetBedBlockEntity extends BlockEntity {

    public PetBedBlockEntity(BlockPos pos, BlockState state) {
        super(PHTileEntityRegistry.PET_BED, pos, state);
    }

    /** 方块实体 tick（仅服务端挂 ticker）：主世界时钟 {@code time == 1}（次日清晨第一 tick）时，取出本床的全部复活请求逐个 {@link #addAndRemoveEntity} 复活，成功的移除请求。 */
    public static void tick(Level level, BlockPos pos, BlockState state, PetBedBlockEntity blockEntity) {
        long time = level.getOverworldClockTime() % 24000L;
        if (time == 1) {
            PHWorldData data = PHWorldData.get(level);
            if (data != null) {
                List<RespawnRequest> requestList = data.getRespawnRequestsFor(level, pos);
                for (RespawnRequest request : requestList) {
                    if (addAndRemoveEntity(level, pos, state.getValue(PetBedBlock.FACING), request)) {
                        data.removeRespawnRequest(request);
                    }
                }
            }
        }
    }

    /** 清空本床的全部复活请求（主人拆床时由 {@code ServerEvent.onBlockBreak} 调用），每条给玩家发"永别"提示。 */
    public void removeAllRequestsFor(@Nullable Player message) {
        PHWorldData data = PHWorldData.get(level);
        if (data != null) {
            List<RespawnRequest> requestList = data.getRespawnRequestsFor(level, this.getBlockPos());
            for (RespawnRequest request : requestList) {
                data.removeRespawnRequest(request);
                if (message != null) {
                    message.sendSystemMessage(Component.translatable("message.pet_home.goodbye", request.nametag()));
                }
            }
        }
    }

    /**
     * 按存档 NBT 复活一只宠物：{@code EntityType.create} 新实体 → {@code Entity#load} 整份 NBT
     * （26.1 走 {@code TagValueInput}，PetId 随之带回）→ 满血/自定义名/按床朝向转身/置停留坐姿
     * → 生成并通知主人。失败（类型不存在等）返回 false，请求保留。
     */
    private static boolean addAndRemoveEntity(Level level, BlockPos pos, Direction dir, RespawnRequest request) {
        EntityType type = request.getEntityType();
        if (type != null) {
            Entity entity = type.create(level, EntitySpawnReason.TRIGGERED);
            if (entity instanceof LivingEntity living) {
                // 26.1: readAdditionalSaveData 为 protected，改用 TagValueInput + Entity#load
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(living.problemPath(), com.github.yzqdev.pethome.PHConstants.LOG)) {
                    ValueInput input = TagValueInput.create(reporter, living.registryAccess(), request.entityData());
                    living.load(input);
                }
                living.setPos(Vec3.upFromBottomCenterOf(pos, 0.8F));
                living.setHealth(living.getMaxHealth());
                if (!request.nametag().isEmpty()) {
                    living.setCustomName(Component.translatable(request.nametag()));
                }
                switch (dir) {
                    case NORTH:
                        living.setYRot(180);
                        break;
                    case EAST:
                        living.setYRot(-90);
                        break;
                    case SOUTH:
                        living.setYRot(0);
                        break;
                    case WEST:
                        living.setYRot(90);
                        break;
                }
                if (living instanceof IComandableMob) {
                    ((IComandableMob) living).setCommand(1);
                }
                if (living instanceof TamableAnimal) {
                    ((TamableAnimal) living).setOrderedToSit(true);
                }
                level.addFreshEntity(living);
                Entity owner = TameableUtils.getOwnerOf(entity);
                if (owner instanceof Player) {
                    ((Player) owner).sendSystemMessage(Component.translatable("message.pet_home.respawn", entity.getName()));
                }
                return true;
            }
        }
        return false;
    }

    /** 床被破坏时：清掉附近 10 格内绑定到本床的宠物的床绑定，并通知各主人。 */
    public void resetBedsForNearbyPets() {
        Predicate<Entity> pet = (animal) -> TameableUtils.isTamed(animal) && TameableUtils.getPetBedPos((LivingEntity) animal) != null && TameableUtils.getPetBedPos((LivingEntity) animal).equals(this.getBlockPos());
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(this.getBlockPos().offset(-10, -5, -10).getCenter(), this.getBlockPos().offset(10, 5, 10).getCenter()), EntitySelector.NO_SPECTATORS.and(pet));
        for (LivingEntity entity : list) {
            Entity owner = TameableUtils.getOwnerOf(entity);
            if (owner instanceof Player) {
                ((Player) owner).sendSystemMessage(Component.translatable("message.pet_home.remove_respawn", entity.getName()));
                TameableUtils.removePetBedPos(entity);
            }
        }
    }
}
