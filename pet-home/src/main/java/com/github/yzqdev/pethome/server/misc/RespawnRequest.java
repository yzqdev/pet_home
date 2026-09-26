package com.github.yzqdev.pethome.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.logging.Level;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;


import java.util.Optional;

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
        return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(this.entityType));
    }

    public static String processDimension(ResourceKey<Level> key) {
        return key.location().toString();
    }
}