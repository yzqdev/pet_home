package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * 网络包通道 id 与编解码工具（1.20.1 fabric-api 无 CustomPacketPayload payload 机制，
 * 走 {@code ServerPlayNetworking/ClientPlayNetworking.registerGlobalReceiver(ResourceLocation, ...)}
 * 通道形态；五个通道 id 与 NeoForge 侧 payload id 一一对应）。
 */
public final class PHNetworkIds {

    /** C2S：右键罗盘请求打开列表（PetCompassItem） */
    public static final ResourceLocation PET_COMPASS_OPEN = id("pet_compass_open");
    /** C2S：罗盘动作（传送/召回，tag：PetId/Action） */
    public static final ResourceLocation PET_COMPASS_ACTION = id("pet_compass_action");
    /** C2S：罗盘删除档案（tag：PetId） */
    public static final ResourceLocation PET_COMPASS_FORGET = id("pet_compass_forget");
    /** S2C：罗盘列表数据（服务端权威下发，含按钮显隐配置） */
    public static final ResourceLocation PET_COMPASS_DATA = id("pet_compass_data");
    /** 双向：citadel tag 单实体更新（客户端本地写 → 服务端校验；服务端 → 客户端回写） */
    public static final ResourceLocation ENTITY_DATA_TAG_UPDATE = id("pet_entity_tag_update");

    private PHNetworkIds() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(PetHomeMod.MODID, path);
    }
}
