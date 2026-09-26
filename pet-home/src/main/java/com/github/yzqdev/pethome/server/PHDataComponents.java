package com.github.yzqdev.pethome.server;

import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.ByteBufCodecs;

/** 物品数据组件注册：Fabric 原生 {@code Registry.register}（立即注册）。 */
public class PHDataComponents {

    public static final DataComponentType<CompoundTag> ENTITY_HOLDER = register("entity_holder",
            builder -> builder.persistent(CompoundTag.CODEC));

    public static final DataComponentType<Boolean> RELEASE_MODE = register("release_mode",
            builder -> builder.persistent(Codec.BOOL));

    // 宠物罗盘绑定目标：persistent 存档 + networkSynchronized 同步到客户端（指针/tooltip 直读）
    public static final DataComponentType<CompoundTag> PET_COMPASS_TARGET = register("pet_compass_target",
            builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.TRUSTED_COMPOUND_TAG));

    private static <T> DataComponentType<T> register(String name, java.util.function.UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, name),
                builder.apply(DataComponentType.builder()).build());
    }

    public static void init() {
    }
}
