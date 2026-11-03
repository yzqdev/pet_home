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


/**
 * 数据组件（DataComponentType）注册表：捕网与宠物罗盘的物品级数据载体。
 * 26.1+ 的 NBT 正被组件化，这里用 {@code CompoundTag} 组件承接原实体数据。
 * {@code init()} 仅用于触发类加载完成注册（必须先于物品注册）。
 */
public class PHDataComponents {

    /** 捕网内生物的完整 NBT（{@code NetItem.getNBTfromEntity} 写入、释放时读出重建实体）。 */
    public static final DataComponentType<CompoundTag> ENTITY_HOLDER = register("entity_holder",
            builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    /** 捕网发射器的释放模式开关（true=落地即释放生物）。 */
    public static final DataComponentType<Boolean> RELEASE_MODE = register("release_mode",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    // 宠物罗盘绑定目标：persistent 存档 + networkSynchronized 同步到客户端（指针/tooltip 直读）
    public static final DataComponentType<CompoundTag> PET_COMPASS_TARGET = register("pet_compass_target",
            builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.TRUSTED_COMPOUND_TAG));

    /** 契约绑定的宠物（UUID + 名字，NBT 形态）：空 = 未绑定；绑定后对同一宠物使用即转让所有权 */
    public static final DataComponentType<CompoundTag> BOUND_PET = register("bound_pet",
            builder -> builder.persistent(CompoundTag.CODEC));

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
