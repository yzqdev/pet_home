package com.github.yzqdev.pet_home.network;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * @author yzqde
 * @date time 2025/1/9 11:32
 * @modified By:
 *
 */  public record PropertiesMessage(String propertyID, CompoundTag compound, int entityID)  implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf,PropertiesMessage> STREAM_CODEC =
            CustomPacketPayload.codec(PropertiesMessage::write,PropertiesMessage::new);
    public static final Type<PropertiesMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID,"pet_entity_tag"));



    public PropertiesMessage(final FriendlyByteBuf buf){
        this(buf.readUtf(),buf.readNbt(),buf.readInt());
    }

    public void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeUtf(propertyID());
        pBuffer.writeNbt(compound());
        pBuffer.writeInt(entityID());
    }
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
