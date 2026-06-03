package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.util.CitadelEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author yzqde
 * @date time 2025/1/9 11:32
 * @modified By:
 *
 */
public record PropertiesMessage(String propertyID, CompoundTag compound, int entityID) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PropertiesMessage> STREAM_CODEC  = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PropertiesMessage::propertyID,
            ByteBufCodecs.COMPOUND_TAG, PropertiesMessage::compound,
            ByteBufCodecs.VAR_INT, PropertiesMessage::entityID,
            PropertiesMessage::new
    );
    public static final Type<PropertiesMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "pet_entity_tag"));



    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handleServer(final PropertiesMessage data, final IPayloadContext context) {

        context.enqueueWork(() -> {
                   PetHomeMod. LOGGER.info(String.valueOf(data.entityID()));
                    var level = context.player().level();
                    Entity e = level.getEntity(data.entityID());
                    if (e instanceof LivingEntity && (data.propertyID().equals(PHConstants.entityDataTagUpdate))) {
                        CitadelEntityData.setCitadelTag((LivingEntity) e, data.compound());
                    }
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable(LangDefinition.network_failed, e.getMessage()));
                    return null;
                });
    }
    public static void handleClient(final PropertiesMessage data, final IPayloadContext context) {

        context.enqueueWork(() -> {

                    var compound = data.compound();
                    var entityID = data.entityID();
                    var propertyID = data.propertyID();
                    if (compound != null && Minecraft.getInstance().level != null) {
                        Entity entity = Minecraft.getInstance().level.getEntity(entityID);
                        if ((propertyID.equals(PHConstants.entityDataTagUpdate)) && entity instanceof LivingEntity) {
                            CitadelEntityData.setCitadelTag((LivingEntity) entity, compound);
                        }

                    }
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable("my_mod.networking.failed", e.getMessage()));
                    return null;
                });
    }
}
