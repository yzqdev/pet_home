package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.datagen.loot.PHLootRegistry;
import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.platform.ServerRef;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.block.PHTileEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PHActivityRegistry;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PHVillagerRegistry;
import com.github.yzqdev.pethome.server.event.EntityHurtHandler;
import com.github.yzqdev.pethome.server.event.EntityTickHandler;
import com.github.yzqdev.pethome.server.event.PlayerInteractEntityHandler;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.misc.*;
import com.github.yzqdev.pethome.worldgen.PHVillagePieceRegistry;
import com.github.yzqdev.pethome.worldgen.VillageHouseManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public class PetHomeMod implements ModInitializer {

    public static final String MODID = "pet_home";

    public static final PHModLogger LOGGER = PHModLogger.getInstance();

    @Override
    public void onInitialize() {
        ServerRef.init();


        PetHomeConfig.load();


        PHVillagePieceRegistry.init();
        PHBlockRegistry.init();
        PHPOIRegistry.init();
        PHVillagerRegistry.init();
        PHActivityRegistry.init();
        PHSoundRegistry.init();
        PHDataComponents.init();
        PHTileEntityRegistry.init();
        PHEntityRegistry.init();
        PHItemRegistry.init();
        PHParticleRegistry.init();
        ModEffects.init();
        PHCreativeTabRegistry.init();
        PHLootRegistry.init();


        PoiTypes.registerBlockStates(PHPOIRegistry.PET_BED, PHPOIRegistry.PET_BED.value().matchingStates());


        ServerEvent.registerVillagerTrades(TradeOfferHelper::registerVillagerOffers);


        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> ServerEvent.onLivingDie(entity));
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> ServerEvent.onEntityJoinWorldEvent(entity));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> ServerEvent.onEntityLeaveWorld(entity));
        ServerTickEvents.END_WORLD_TICK.register(ServerEvent::onServerTick);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> VillageHouseManager.addAllHouses(server.registryAccess()));
        // 退出世界/服务器时保存
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PetHomeConfig.save());
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (!level.isClientSide() && state.getBlock() instanceof com.github.yzqdev.pethome.server.block.PetBedBlock) {
                if (level.getBlockEntity(pos) instanceof com.github.yzqdev.pethome.server.block.PetBedBlockEntity petBedBlockEntity) {
                    petBedBlockEntity.removeAllRequestsFor(player);
                    petBedBlockEntity.resetBedsForNearbyPets();
                }
            }
            return true;
        });
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                PlayerInteractEntityHandler.onInteractWithEntity(player, hand, entity));


        EntityTickHandler.init();
        EntityHurtHandler.init();

        Networking.initServer();
    }
}
