package com.github.yzqdev.pethome.server.block;


import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.server.misc.PHWorldData;
import com.github.yzqdev.pethome.server.misc.RespawnRequest;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * 宠物床复活执行者：死亡宠物的 RespawnRequest（完整 NBT）在次日白天第一 tick
 * 于床上重建实体（复用原 UUID/PetId、满血、朝向、停留态）。
 * 请求存于 {@link PHWorldData}（主世界共享，跨维度有效）。
 */
public class PetBedBlockEntity extends BlockEntity {

    public PetBedBlockEntity(BlockPos pos, BlockState state) {
        super(PHTileEntityRegistry.PET_BED.get(), pos, state);
    }

    /** BlockEntity ticker：每天 dayTime%24000==1 的那一 tick 处理本床的全部复活请求，成功即移除并标脏 */
    public static void tick(Level level, BlockPos pos, BlockState state, PetBedBlockEntity blockEntity) {
        long time = level.dayTime() % 24000L;
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

    /** 清除本床的全部复活请求（床被拆/放生时调用），并对每个请求向 message 玩家发告别消息 */
    public void removeAllRequestsFor(@Nullable Player message) {
        PHWorldData data = PHWorldData.get(level);
        if (data != null) {
            List<RespawnRequest> requestList = data.getRespawnRequestsFor(level, this.getBlockPos());
            for (RespawnRequest request : requestList) {
                data.removeRespawnRequest(request);
                if (message != null) {
                    message.displayClientMessage(Component.translatable("message.pet_home.goodbye", request.nametag()), false);
                }
            }
        }
    }

    /**
     * 用请求里的完整 NBT 重建宠物：load 恢复（含 UUID/PetId）→ 满血/名字/朝向 →
     * 指令置停留(1)、原版 TamableAnimal 坐下 → 放入世界并通知在线主人。
     * 返回是否成功（成功调用方才移除请求）。
     */
    private static boolean addAndRemoveEntity(Level level, BlockPos pos, Direction dir, RespawnRequest request) {
        EntityType type = request.getEntityType();
        if (type != null) {
            Entity entity = type.create(level);
            if (entity instanceof LivingEntity living) {
                living.readAdditionalSaveData(request.entityData());
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
                    ((Player) owner).displayClientMessage(Component.translatable("message.pet_home.respawn", entity.getName()), false);
                }
                return true;
            }
        }
        return false;
    }

    /** 重绑 10×5×10 范围内绑定到本床坐标的宠物：清除其床绑定并通知主人（床被拆时调用） */
    public void resetBedsForNearbyPets() {
        Predicate<Entity> pet = (animal) -> TameableUtils.isTamed(animal) && TameableUtils.getPetBedPos((LivingEntity) animal) != null && TameableUtils.getPetBedPos((LivingEntity) animal).equals(this.getBlockPos());
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(this.getBlockPos().offset(-10, -5, -10).getCenter(), this.getBlockPos().offset(10, 5, 10).getCenter()), EntitySelector.NO_SPECTATORS.and(pet));
        for (LivingEntity entity : list) {
            Entity owner = TameableUtils.getOwnerOf(entity);
            if (owner instanceof Player) {
                ((Player) owner).displayClientMessage(Component.translatable("message.pet_home.remove_respawn", entity.getName()), false);
                TameableUtils.removePetBedPos(entity);
            }
        }
    }
}
