package com.github.yzqdev.pet_home.client;

import com.github.yzqdev.pet_home.PetHomeMod;

import com.github.yzqdev.pet_home.client.render.ChainLightningRender;
import com.github.yzqdev.pet_home.client.render.LayerManager;
import com.github.yzqdev.pet_home.client.render.RecallBallRender;
import com.github.yzqdev.pet_home.client.render.RenderFeather;
import com.github.yzqdev.pet_home.client.render.RenderGiantBubble;
import com.github.yzqdev.pet_home.client.render.RenderHighlightedBlock;
import com.github.yzqdev.pet_home.client.render.RenderPsychicWall;
import com.github.yzqdev.pet_home.server.entity.PHEntityRegistry;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author yzqde
 * @date time 2024/12/1 23:00
 * @modified By:
 */
@EventBusSubscriber(modid = PetHomeMod.MODID, bus =  EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EntityRegEvent {


    @SubscribeEvent
    public static void registerEntityRender(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PHEntityRegistry.RECALL_BALL.get(), RecallBallRender::new);
        event.registerEntityRenderer(PHEntityRegistry.CHAIN_LIGHTNING.get(), ChainLightningRender::new);
        event.registerEntityRenderer(  PHEntityRegistry.GIANT_BUBBLE.get(), RenderGiantBubble::new);
        event.registerEntityRenderer(  PHEntityRegistry.PSYCHIC_WALL.get(), RenderPsychicWall::new);
        event.registerEntityRenderer( PHEntityRegistry.HIGHLIGHTED_BLOCK.get(), RenderHighlightedBlock::new);
        event.registerEntityRenderer( PHEntityRegistry.FEATHER.get(), RenderFeather::new);
    }
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {

        List<EntityType<? extends LivingEntity>> entityTypes = ImmutableList.copyOf(
                BuiltInRegistries.ENTITY_TYPE .stream()
                        .filter(LayerManager::canApply)
//                        .filter(DefaultAttributes::hasSupplier)
                        .map(entityType -> (EntityType<? extends LivingEntity>) entityType)
                        .collect(Collectors.toList()));
        entityTypes.forEach((entityType -> {
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            if (key != null) {
                String modId = key.getNamespace(); // 获取所属 Mod 的 ID（原版为 "minecraft"）
                if (!modId.equals("minecraft")){
                    System.out.println("EntityType: " + key + " | Mod: " + modId);
                }
            }
            LayerManager.addLayerIfApplicable(entityType, event);
        }));
    }
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
//        event.registerLayerDefinition(ElfModel.LAYER, ElfModel::createBodyLayer);

    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {


    }

    @SubscribeEvent
    public static void addEntityAttributes(EntityAttributeCreationEvent event) {

    }





}
