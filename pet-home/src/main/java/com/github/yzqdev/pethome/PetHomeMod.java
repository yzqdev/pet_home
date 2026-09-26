package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.platform.ServerRef;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.block.DIBlockRegistry;
import com.github.yzqdev.pethome.server.block.DITileEntityRegistry;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.DIActivityRegistry;
import com.github.yzqdev.pethome.server.entity.DIVillagerRegistry;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.event.PlayerInteractEntityHandler;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.misc.*;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PetHomeMod implements ModInitializer {
    public static final String MODID = "pet_home";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final PetHomeConfig CONFIG = new PetHomeConfig();

    @Override
    public void onInitialize() {
         ServerRef.init();

        // 触发各注册表类静态初始化（Fabric 版为立即注册）
        PHItemRegistry.init();

        DIBlockRegistry.init();
        DIEnchantmentRegistry.registerEnchantments();
        DITileEntityRegistry.init();
        PHEntityRegistry.init();
        DIPOIRegistry.init();
        // 自定义 POI 必须手动登记「方块状态 -> POI」映射：原版 PoiTypes 的 TYPE_BY_STATE 只由它自己的
        // bootstrap 填充，Fabric 无自动处理（Forge 侧由加载器补丁兜底）。漏掉这行的话宠物床永远不会被
        // PoiManager 认成 POI，驯兽师村民拿不到工作站。
        PoiTypes.registerBlockStates(DIPOIRegistry.PET_BED_HOLDER, DIPOIRegistry.PET_BED.matchingStates());
        DIParticleRegistry.init();
        DIVillagerRegistry.init();
        DISoundRegistry.init();
        DIActivityRegistry.init();
        DIVillagePieceRegistry.init();
        DICreativeTabRegistry.init();
        ModEffects.init();

        // 配置：自研配置系统，读取 config/pet_home.toml（文件不存在时按默认值生成）
        PetHomeConfig.load();

        DILootModifier.init();

        Networking.initServer();

        // Fabric 事件接线（原 Forge EVENT_BUS 注册）
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> ServerEvent.onLivingDie(entity, source));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PetHomeConfig.save());
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> ServerEvent.onEntityJoinWorldEvent(entity));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> ServerEvent.onEntityLeaveWorld(entity));
        ServerTickEvents.START_WORLD_TICK.register(ServerEvent::onServerTick);
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (!level.isClientSide() && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                ServerEvent.onBlockBreak(serverLevel, player, pos, state);
            }
            return true;
        });
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                new PlayerInteractEntityHandler()
                        .onInteractWithEntity(player, hand, entity, player.getItemInHand(hand)));

        ServerEvent.serverStart();
    }
}
