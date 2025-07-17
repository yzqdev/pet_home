package com.github.yzqdev.pet_home.network;

import com.github.yzqdev.pet_home.ModConstants;
import com.github.yzqdev.pet_home.util.CitadelEntityData;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

public class ServerPayloadHandler {
    private static final ServerPayloadHandler INSTANCE = new ServerPayloadHandler();
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ServerPayloadHandler getInstance() {
        return INSTANCE;
    }

    public static void handleData(final PropertiesMessage data, final IPayloadContext context) {

        context.enqueueWork(() -> {
                    LOGGER.info(String.valueOf(data.entityID()));
                    var level=context.player().level();
                    Entity e = level.getEntity(data.entityID());
                    if (e instanceof LivingEntity && (  data. propertyID().equals(ModConstants.entityDataTagUpdate))) {
                        CitadelEntityData.setCitadelTag((LivingEntity)e, data.compound());
                    }
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable("my_mod.networking.failed", e.getMessage()));
                    return null;
                });
    }

}
