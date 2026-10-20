package com.github.yzqdev.pethome.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.Level;

/**
 * 宠物床复活请求（存于 {@link PHWorldData}，Codec 持久化）：宠物死亡且绑定宠物床时
 * 由 ServerEvent.onLivingDie 用<b>完整实体 NBT</b>（含 UUID/PetId/装备/自定义名）建档，
 * 次日白天第一 tick 由 {@code PetBedBlockEntity} 重建实体（复用原 UUID 与 PetId）。
 */
public record RespawnRequest(
        String entityType,
        String dimension,
        CompoundTag entityData,
        BlockPos bedPosition,
        long timestamp,
        String nametag
) {


    public static final Codec<RespawnRequest> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("entity_type").forGetter(RespawnRequest::entityType),
                    Codec.STRING.fieldOf("dimension").forGetter(RespawnRequest::dimension),
                    CompoundTag.CODEC.fieldOf("entity_data").forGetter(RespawnRequest::entityData),
                    BlockPos.CODEC.fieldOf("bed_position").forGetter(RespawnRequest::bedPosition),
                    Codec.LONG.fieldOf("timestamp").forGetter(RespawnRequest::timestamp),
                    Codec.STRING.fieldOf("nametag").forGetter(RespawnRequest::nametag)
            ).apply(instance, RespawnRequest::new)
    );


    /** 解析实体类型 id；id 失效时退回注册表默认值（minecraft:pig），调用方不会得到 null */
    public EntityType<?> getEntityType() {
        return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(this.entityType));
    }

    /** 维度 ResourceKey → 存档字符串（如 "minecraft:overworld"） */
    public static String processDimension(ResourceKey<Level> key) {
        return key.location().toString();
    }
}