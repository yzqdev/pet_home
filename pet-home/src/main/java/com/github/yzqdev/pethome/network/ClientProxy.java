package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.server.entity.CitadelEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端代理：S2C {@link EntityDataTagUpdatePacket} 在客户端本地把服务端发来的 citadel tag
 * 写入实体数据槽（供渲染层/tooltip 读取），不做鉴权（信任服务端已校验）。
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientProxy extends ServerProxy {
    @Override
    public void handleEntityTagUpdate(CompoundTag tag, int entityID) {
        if (tag == null) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        Entity entity = player.level().getEntity(entityID);
        if (entity instanceof LivingEntity) {
            CitadelEntityData.setCitadelTag((LivingEntity) entity, tag);
        }
    }
}
