package com.github.yzqdev.pethome.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.Level;

/**
 * 宠物床复活请求（PHWorldData 持久化）：宠物死亡时由 ServerEvent.onLivingDie 生成，
 * 次日白天由 PetBedBlockEntity 读取并按 {@code entityData}（完整实体 NBT，含原 Entity UUID 与 PetId）重建实体。
 * 注意 26.1 实体 NBT 的 UUID 是 int 数组（无 putUUID/getUUID）。
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


    /** 解析实体类型；id 非法时 {@code BuiltInRegistries.ENTITY_TYPE.getValue} 返回原版默认（pig），不会抛异常。 */
    public EntityType<?> getEntityType() {
        return BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(this.entityType));
    }

    /** 维度键 → 存档字符串（26.1 用 {@code ResourceKey#identifier}，1.21 是 {@code location()}）。 */
    public static String processDimension(ResourceKey<Level> key) {
        return key.identifier().toString();
    }
}