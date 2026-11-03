package com.github.yzqdev.pethome.network;


import com.github.yzqdev.pethome.server.entity.CitadelEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 旧单通道包 {@code PropertiesMessage(propertyID, compound, entityID)} 的业务逻辑留存：
 * 1b8ca907 拆分后通道由 {@link PHNetworkIds} 定义、传输由 {@link Networking} 承担，本类只保留
 * 两段无法内联的鉴权/写回逻辑（citadel tag 双向同步），方法名即旧分发分支语义。
 * <ul>
 *   <li>C2S {@code pet_entity_tag_update}：客户端把本地改动的宠物 citadel 数据写回服务端；
 *       服务端<strong>仅当实体主人 == 发包玩家</strong>时才写入，防伪造包篡改附魔 / 归属。
 *       tag 内约定键：{@code EntityId}（实体 id）+ 业务字段。</li>
 *   <li>S2C {@code pet_compass_data}：服务端打包的宠物档案 → {@code PetCompassScreen.handleData} 开屏；
 *       S2C {@code pet_entity_tag_update}：tag 内 {@code EntityId} 定位实体写回本地 citadel tag 副本。</li>
 * </ul>
 */
public class PropertiesMessage {

    /** 服务端收包入口（C2S citadel tag 更新；已由 {@link Networking} 保证主线程执行）：先做主人鉴权再写入。 */
    public static void handleEntityTagUpdateFromClient(ServerPlayer sender, CompoundTag compound) {
        int entityId = compound.contains("EntityId") ? compound.getInt("EntityId") : 0;
        Entity e = sender.level().getEntity(entityId);
        if (e instanceof LivingEntity living) {
            // 鉴权：客户端只能写入自己的宠物，否则伪造包可给任意实体刷附魔/篡改归属
            java.util.UUID ownerUUID = com.github.yzqdev.pethome.server.entity.TameableUtils.getOwnerUUIDOf(e);
            if (ownerUUID != null && ownerUUID.equals(sender.getUUID())) {
                CompoundTag payload = compound.copy();
                payload.remove("EntityId");
                CitadelEntityData.setCitadelTag(living, payload);
            }
        }
    }

    /** 客户端收包入口（S2C 罗盘数据）：服务端打包的宠物列表 → 打开/刷新 GUI。 */
    public static void handleCompassDataFromServer(CompoundTag compound) {
        com.github.yzqdev.pethome.client.PetCompassScreen.handleData(compound);
    }

    /** 客户端收包入口（S2C citadel tag 更新）：按 tag 内 EntityId 定位实体写回本地 citadel tag 副本。 */
    public static void handleEntityTagUpdateFromServer(CompoundTag compound) {
        if (compound == null) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int entityId = compound.contains("EntityId") ? compound.getInt("EntityId") : 0;
        Entity entity = player.level().getEntity(entityId);
        if (entity instanceof LivingEntity living) {
            CompoundTag payload = compound.copy();
            payload.remove("EntityId");
            CitadelEntityData.setCitadelTag(living, payload);
        }
    }
}
