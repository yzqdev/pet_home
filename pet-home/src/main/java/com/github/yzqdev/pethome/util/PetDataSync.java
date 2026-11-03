package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.network.EntityDataTagUpdatePacket;
import java.util.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;

/**
 * citadel tag 写入的<b>唯一出口</b>（package-private，原 {@code TameableUtils#sync}）。
 *
 * <p>职责类只能通过本类写 citadel tag，不得绕过：本侧数据存在 {@code LivingEntity} 的 SynchedEntityData CompoundTag 槽内（非
 * 26.1 的附件），写入后走 force set 触发 vanilla 同步， 客户端侧另发 C2S 上行。保持包内可见而非 public——不对外暴露第二条写入路径。
 */
final class PetDataSync {

  private PetDataSync() {}

  /**
   * 写回宠物数据（本类所有写入的统一出口）： 服务端 {@code setCitadelTag} force 写 SynchedEntityData 槽，由原版把变化发给追踪者（不再全服广播）；
   * 客户端额外发 C2S entityDataTagUpdate 回写服务端（服务端 handleServer 校验所有权后才落槽）。
   */
  static void sync(LivingEntity entity, CompoundTag tag) {
    // 服务端不再手动全服广播：setCitadelEntityData 内部 force 写 entityData，
    // vanilla 只把变化发给追踪该实体的玩家（原先 sendToAllPlayers 发给全服所有玩家，双通道冗余）
    CitadelEntityData.setCitadelTag(entity, tag);
    if (entity.level().isClientSide()) {
      ClientPlayNetworking.send(new EntityDataTagUpdatePacket(entity.getId(), tag.copy()));
    }
  }
}
