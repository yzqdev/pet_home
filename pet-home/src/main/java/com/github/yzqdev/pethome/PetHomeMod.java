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
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

/**
 * 模组通用端主入口（{@code fabric.mod.json} 的 {@code entrypoints.main}）。
 *
 * <p>{@link #onInitialize()} 的调用顺序即注册顺序，各 {@code *Registry.init()} 多为空实现，唯一作用是按顺序触发注册类的静态初始化（内部走
 * Fabric 原生 {@code Registry.register} 立即注册），调整顺序前需确认注册项之间的依赖。
 */
public class PetHomeMod implements ModInitializer {

  public static final String MODID = "pet_home";

  public static final PHModLogger LOGGER = PHModLogger.getInstance();

  /**
   * Fabric 入口回调，服务端与客户端共同端启动时各执行一次。顺序敏感：
   *
   * <ol>
   *   <li>{@link ServerRef#init()} 缓存服务器引用；
   *   <li>{@code PetHomeConfig.load()} 读 {@code config/pet_home.toml} 并刷新静态缓存；
   *   <li>按依赖顺序触发各注册类（方块 → POI/村民职业 → 方块实体/实体/物品等）；
   *   <li>POI 注册后立即 {@code PoiTypes.registerBlockStates} 把宠物床方块状态登记进 原版 {@code
   *       PoiTypes.TYPE_BY_STATE}（Fabric 不会自动收录，漏掉则驯兽师村民无工作站）；
   *   <li>注册村民交易与全部服务器事件回调；
   *   <li>{@code EntityTickHandler/EntityHurtHandler.init()} 与 {@code Networking.initServer()}。
   * </ol>
   */
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
    PHEffects.init();
    PHCreativeTabRegistry.init();
    PHLootRegistry.init();

    PoiTypes.registerBlockStates(
        PHPOIRegistry.PET_BED, PHPOIRegistry.PET_BED.value().matchingStates());

    ServerEvent.registerVillagerTrades(TradeOfferHelper::registerVillagerOffers);

    ServerLivingEntityEvents.AFTER_DEATH.register(
        (entity, source) -> ServerEvent.onLivingDie(entity));
    ServerEntityEvents.ENTITY_LOAD.register(
        (entity, level) -> ServerEvent.onEntityJoinWorldEvent(entity));
    ServerEntityEvents.ENTITY_UNLOAD.register(
        (entity, level) -> ServerEvent.onEntityLeaveWorld(entity));
    ServerTickEvents.END_WORLD_TICK.register(ServerEvent::onServerTick);
    ServerLifecycleEvents.SERVER_STARTING.register(
        server -> VillageHouseManager.addAllHouses(server.registryAccess()));
    // 退出世界/服务器时保存
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> PetHomeConfig.save());
    PlayerBlockBreakEvents.BEFORE.register(
        (level, player, pos, state, blockEntity) -> {
          if (!level.isClientSide()
              && state.getBlock() instanceof com.github.yzqdev.pethome.server.block.PetBedBlock) {
            if (level.getBlockEntity(pos)
                instanceof
                com.github.yzqdev.pethome.server.block.PetBedBlockEntity petBedBlockEntity) {
              petBedBlockEntity.removeAllRequestsFor(player);
              petBedBlockEntity.resetBedsForNearbyPets();
            }
          }
          return true;
        });
    UseEntityCallback.EVENT.register(
        (player, level, hand, entity, hitResult) ->
            PlayerInteractEntityHandler.onInteractWithEntity(player, hand, entity));

    EntityTickHandler.init();
    EntityHurtHandler.init();

    Networking.registerPayloadTypes();
  }
}
