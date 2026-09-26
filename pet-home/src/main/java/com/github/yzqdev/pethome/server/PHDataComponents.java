package com.github.yzqdev.pethome.server;


import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

import java.util.function.UnaryOperator;


public class PHDataComponents {

    public static final DataComponentType<CompoundTag> ENTITY_HOLDER = register("entity_holder",
            builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    public static final DataComponentType<Boolean> RELEASE_MODE = register("release_mode",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    // 宠物罗盘绑定目标：persistent 存档 + networkSynchronized 同步到客户端（指针/tooltip 直读）
    public static final DataComponentType<CompoundTag> PET_COMPASS_TARGET = register("pet_compass_target",
            builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.TRUSTED_COMPOUND_TAG));

    public static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builder) {
        Identifier location = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, id);
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                location,
                builder.apply(DataComponentType.builder())
                        .build()
        );
    }

    /** 触发类加载（数据组件必须在物品注册之前完成注册） */
    public static void init() {
    }
}
