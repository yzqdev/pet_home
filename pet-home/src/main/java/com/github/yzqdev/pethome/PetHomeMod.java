package com.github.yzqdev.pethome;


import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.platform.ServerRef;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.block.PHTileEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PHActivityRegistry;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PHVillagerRegistry;
import com.github.yzqdev.pethome.server.event.EntityTickHandler;
import com.github.yzqdev.pethome.server.event.PlayerInteractEntityHandler;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.datagen.loot.PetHomeLootInjector;
import com.github.yzqdev.pethome.server.misc.PHEffects;
import com.github.yzqdev.pethome.server.misc.PHCreativeTabRegistry;
import com.github.yzqdev.pethome.server.misc.PHPOIRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.PHAttachments;
import com.github.yzqdev.pethome.worldgen.PHVillagePieceRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 模组主入口（{@code ModInitializer}，双端）。
 *
 * <p>{@link #onInitialize()} 的调用顺序敏感：配置 → 附件/网络 → 注册 → 事件接线 → 战利品注入，
 * 与 NeoForge 版构造函数的注册顺序对齐。各 {@code *Registry.init()} 是空实现，
 * 唯一作用是按顺序触发注册类的静态初始化（内部为 Fabric 原生 {@code Registry.register} 立即注册）。</p>
 */
public class PetHomeMod implements ModInitializer {
    public static final String MODID = "pet_home";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    /**
     * 按固定顺序完成全部初始化。顺序要点：
     * <ul>
     *   <li>配置最先加载（后续注册与事件逻辑依赖静态缓存）；</li>
     *   <li>{@code PHAttachments.init()} 必须先于任何实体 {@code defineSynchedData}
     *       （自定义 {@code EntityDataSerializer} 在其静态块经 {@code FabricEntityDataRegistry.register} 登记）；</li>
     *   <li>{@code PHPOIRegistry.init()} 之后必须紧跟 {@code PoiTypes.registerBlockStates(...)}：
     *       原版 {@code PoiTypes.TYPE_BY_STATE} 只由原版 bootstrap 填充，漏登记则宠物床不是 POI、
     *       驯兽师村民没有工作站（该方法由 {@code pet_home.classtweaker} 放开）。</li>
     * </ul>
     */
    @Override
    public void onInitialize() {

        PetHomeConfig.load();


        PHAttachments.init();
        // 世界级数据 / 网络
        Networking.registerPayloadTypes();
        ServerRef.init();


        PHVillagePieceRegistry.init();
        PHItemRegistry.init();
        PHBlockRegistry.init();

        PHPOIRegistry.init();
        PHVillagerRegistry.init();

        PoiTypes.registerBlockStates(PHPOIRegistry.PET_BED, PHPOIRegistry.PET_BED.value().matchingStates());
        PHActivityRegistry.init();
        PHSoundRegistry.init();
        PHDataComponents.init();
        PHTileEntityRegistry.init();
        PHEntityRegistry.init();

        PHParticleRegistry.init();
        PHEffects.init();

        PHCreativeTabRegistry.init();


        EntityTickHandler.init();
        PlayerInteractEntityHandler.init();
        ServerEvent.init();

       PetHomeLootInjector.init();
    }
}
