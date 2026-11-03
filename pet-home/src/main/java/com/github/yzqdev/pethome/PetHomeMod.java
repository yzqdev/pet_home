package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.platform.ServerRef;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.block.PHTileEntityRegistry;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.PHActivityRegistry;
import com.github.yzqdev.pethome.server.entity.PHVillagerRegistry;
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

/**
 * 模组主入口（{@link ModInitializer}）。
 *
 * <p>{@link #onInitialize()} 的顺序敏感：先缓存服务器引用（{@link ServerRef#init()}）→ 各注册类
 * 立即注册（{@code *Registry.init()} 为空方法，仅触发静态初始化，勿删）→ 宠物床 POI 登记
 * 「方块状态 → POI」映射（原版 {@code PoiTypes.TYPE_BY_STATE} 只由原版 bootstrap 填充，Fabric
 * 不会自动收录，漏掉则驯兽师村民拿不到工作站）→ 加载配置（{@code [enchantments]} 组在
 * {@link PetHomeConfig} 构造时反射枚举 {@link PHEnchantmentRegistry} 的静态字段生成开关，
 * 该反射会触发附魔类的初始化）→ 战利品注入与网络接线 →
 * Fabric 事件接线 → 直接调用 {@link ServerEvent#serverStart()} 完成村庄宠物商店注入与村民交易注册
 * （本版不挂在 {@code SERVER_STARTED} 回调上）。</p>
 */
public class PetHomeMod implements ModInitializer {
    public static final String MODID = "pet_home";
    public static final Logger LOGGER = LogManager.getLogger();
    /** 配置实例：{@code general} / {@code loot} / {@code enchantments} 三组配置经它访问（静态块那两组走 {@link PetHomeConfig} 的静态常量）。 */
    public static final PetHomeConfig CONFIG = new PetHomeConfig();

    @Override
    public void onInitialize() {
         ServerRef.init();

        // 触发各注册表类静态初始化（Fabric 版为立即注册）
        PHItemRegistry.init();

        PHBlockRegistry.init();
        PHEnchantmentRegistry.registerEnchantments();
        PHTileEntityRegistry.init();
        PHEntityRegistry.init();
        PHPOIRegistry.init();
        // 自定义 POI 必须手动登记「方块状态 -> POI」映射：原版 PoiTypes 的 TYPE_BY_STATE 只由它自己的
        // bootstrap 填充，Fabric 无自动处理（Forge 侧由加载器补丁兜底）。漏掉这行的话宠物床永远不会被
        // PoiManager 认成 POI，驯兽师村民拿不到工作站。
        PoiTypes.registerBlockStates(PHPOIRegistry.PET_BED_HOLDER, PHPOIRegistry.PET_BED.matchingStates());
        PHParticleRegistry.init();
        PHVillagerRegistry.init();
        PHSoundRegistry.init();
        PHActivityRegistry.init();
        PHVillagePieceRegistry.init();
        PHCreativeTabRegistry.init();
        PHEffects.init();

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
