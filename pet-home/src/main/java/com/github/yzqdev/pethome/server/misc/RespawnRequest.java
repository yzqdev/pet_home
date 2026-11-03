package com.github.yzqdev.pethome.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.logging.Level;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;


import java.util.Optional;

/**
 * 待复活的宠物条目（宠物死亡且有宠物床时由 {@code ServerEvent.onLivingDie} 写入
 * {@code PHWorldData}，次日清晨被对应宠物床消费）：entityData 是宠物的<b>完整实体 NBT</b>
 * （含 UUID、主人、citadel 数据），复活时按它重建实体。
 *
 * @param entityType  实体类型 id 字符串（如 {@code minecraft:wolf}）
 * @param dimension   宠物床所在维度 id
 * @param entityData  完整实体 NBT（注意存档里 UUID 是 int[4]）
 * @param bedPosition 宠物床坐标（死亡时的绑定坐标）
 * @param timestamp   死亡时的主世界时钟时间
 * @param nametag     自定义名（无则空串）
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


    public EntityType<?> getEntityType() {
        return BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(this.entityType));
    }

    public static String processDimension(ResourceKey<Level> key) {
        return key.identifier().toString();
    }
}