package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.server.misc.PHWorldData;
import com.github.yzqdev.pethome.server.misc.RespawnRequest;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
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

/**
 * 宠物床方块实体：复活流的执行端。死亡宠物在 {@link PHWorldData} 里留有 {@code RespawnRequest} （按床坐标+维度归档），本 BE 每个 tick
 * 检查"新的一天第 1 tick"（{@code dayTime % 24000 == 1}） 一次性消费本床的全部请求，按存档 NBT 重建宠物并通知主人。
 */
public class PetBedBlockEntity extends BlockEntity {

  public PetBedBlockEntity(BlockPos pos, BlockState state) {
    super(PHTileEntityRegistry.PET_BED, pos, state);
  }

  /** 方块实体 tick（静态签名供 BlockEntityTickers 注册）：新一天的第 1 tick 复活绑定本床的宠物。 */
  public static void tick(
      Level level, BlockPos pos, BlockState state, PetBedBlockEntity blockEntity) {
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

  /** 清空本床的全部待复活请求，并向指定玩家（床的拆除者）逐条发送"宠物离开"提示；床被破坏/主入口 BEFORE 方块破坏回调时调用。 */
  public void removeAllRequestsFor(@Nullable Player message) {
    PHWorldData data = PHWorldData.get(level);
    if (data != null) {
      List<RespawnRequest> requestList = data.getRespawnRequestsFor(level, this.getBlockPos());
      for (RespawnRequest request : requestList) {
        data.removeRespawnRequest(request);
        if (message != null) {
          message.displayClientMessage(
              Component.translatable("message.pet_home.goodbye", request.nametag()), false);
        }
      }
    }
  }

  /**
   * 按请求的实体 NBT 在床前 0.8 格重建宠物：满血复活、朝向与床朝向一致、恢复自定义名、 复位为停留指令（可指挥生物 setCommand(1)，原版宠物坐下），成功后向主人发复活消息。
   *
   * @return 是否成功重建（失败则请求保留，次日再试）
   */
  private static boolean addAndRemoveEntity(
      Level level, BlockPos pos, Direction dir, RespawnRequest request) {
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
          ((Player) owner)
              .displayClientMessage(
                  Component.translatable("message.pet_home.respawn", entity.getName()), false);
        }
        return true;
      }
    }
    return false;
  }

  /** 床被破坏时：清掉 10×5×10 范围内仍绑定本床宠物的床坐标，并通知其主人。 */
  public void resetBedsForNearbyPets() {
    Predicate<Entity> pet =
        (animal) ->
            TameableUtils.isTamed(animal)
                && TameableUtils.getPetBedPos((LivingEntity) animal) != null
                && TameableUtils.getPetBedPos((LivingEntity) animal).equals(this.getBlockPos());
    List<LivingEntity> list =
        level.getEntitiesOfClass(
            LivingEntity.class,
            new AABB(
                this.getBlockPos().offset(-10, -5, -10).getCenter(),
                this.getBlockPos().offset(10, 5, 10).getCenter()),
            EntitySelector.NO_SPECTATORS.and(pet));
    for (LivingEntity entity : list) {
      Entity owner = TameableUtils.getOwnerOf(entity);
      if (owner instanceof Player) {
        ((Player) owner)
            .displayClientMessage(
                Component.translatable("message.pet_home.remove_respawn", entity.getName()), false);
        TameableUtils.removePetBedPos(entity);
      }
    }
  }
}
