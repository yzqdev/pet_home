package com.github.yzqdev.pethome.server;

import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class PHDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, PetHomeMod.MODID);


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> ENTITY_HOLDER = register("entity_holder",
            builder -> builder.persistent(CompoundTag.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> RELEASE_MODE = register("release_mode",
            builder -> builder.persistent(Codec.BOOL));

    /** 契约绑定的宠物（UUID + 名字，NBT 形态）：空 = 未绑定；绑定后对同一宠物使用即转让所有权 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> BOUND_PET = register("bound_pet",
            builder -> builder.persistent(CompoundTag.CODEC));


    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name,
                                                                                           UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}