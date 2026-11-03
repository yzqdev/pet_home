package com.github.yzqdev.pethome.server.entity;

import net.minecraft.nbt.CompoundTag;
/**
 * @author Alexthe666
 * @since 1.7.0
 *
 * 实体级同步数据存取接口：由 {@code LivingEntityMixin} 注入到所有 LivingEntity，
 * 底层是 SynchedEntityData 的 CompoundTag 槽位（原版 Citadel 模组的替代实现），
 * 宠物附魔/项圈/宠物床坐标/指令状态等大块状态都存在这里。
 */
public interface ICitadelDataEntity {

    /** 取当前 citadel tag（活实例，客户端与服务端各持一份经原版 entityData 同步的副本）。 */
    CompoundTag getCitadelEntityData();

    /** force 写入并触发向追踪者的同步（内容判等无法发现原地修改，必须 force），同时使附魔缓存失效。 */
    void setCitadelEntityData(CompoundTag nbt);
}