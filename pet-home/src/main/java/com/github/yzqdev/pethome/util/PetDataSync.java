package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.network.EntityDataTagUpdatePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * citadel tag 写入的<b>唯一出口</b>（package-private，原 {@code TameableUtils#sync}）。
 *
 * <p>职责类（{@link PetOwnershipUtils} / {@link PetEnchantmentUtils} / {@link PetStateUtils} / {@link
 * PetJukeboxUtils}）都只能通过本类写 citadel tag，不得直接 {@code setData} 绕过：
 *
 * <ul>
 *   <li>服务端：交给 {@link CitadelEntityData#setCitadelTag}——内容相等时 no-op，否则 setAttached 换实例；
 *       Fabric 侧 PETHOME_DATA 附件配置了 {@code syncWith(all)}，附件实例变更自动广播给所有追踪者
 *       （本侧无手工全服广播包）。
 *   <li>客户端：本地写入附件后再向服务端发 C2S {@link EntityDataTagUpdatePacket}（服务端有所有权校验），
 *       让服务端收下客户端侧的预测改动（如暗影手渲染状态）。
 * </ul>
 *
 * <p>保持包内可见而非 public：拆分类不得把同步出口暴露给业务调用方，避免出现第二条写入路径。
 */
final class PetDataSync {

    private PetDataSync() {
    }

    static void sync(LivingEntity entity, CompoundTag tag) {
        // S2C 同步由附件 syncWith 自带：setAttached 换实例后自动广播给所有追踪者，无需再手工全服广播。
        CitadelEntityData.setCitadelTag(entity, tag);
        if (entity.level().isClientSide()) {
            ClientPlayNetworking.send(new EntityDataTagUpdatePacket(entity.getId(), tag.copy()));
        }
    }
}
