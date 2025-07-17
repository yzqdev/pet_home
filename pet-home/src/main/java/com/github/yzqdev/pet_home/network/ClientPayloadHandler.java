package com.github.yzqdev.pet_home.network;

import com.github.yzqdev.pet_home.ModConstants;
import com.github.yzqdev.pet_home.util.CitadelEntityData;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;


public class ClientPayloadHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ClientPayloadHandler INSTANCE = new ClientPayloadHandler();

    public static ClientPayloadHandler getInstance() {
        return INSTANCE;
    }

    public static void handleData(final PropertiesMessage data, final IPayloadContext context) {

        context.enqueueWork(() -> {

                 var compound=data.compound();
                 var entityID=data.entityID();
                 var propertyID=data.propertyID();
                    if (compound != null && Minecraft.getInstance().level != null) {
                        Entity entity = Minecraft.getInstance().level.getEntity(entityID);
                        if ((  propertyID.equals(ModConstants.entityDataTagUpdate)) && entity instanceof LivingEntity) {
                            CitadelEntityData.setCitadelTag((LivingEntity)entity, compound);
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
