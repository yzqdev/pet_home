package com.github.yzqdev.pethome;



import com.github.yzqdev.pethome.client.ClientProxy;
import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.network.PropertiesMessage;
import com.github.yzqdev.pethome.server.CommonProxy;
import com.github.yzqdev.pethome.server.block.DIBlockRegistry;
import com.github.yzqdev.pethome.server.block.DITileEntityRegistry;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.DIActivityRegistry;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.DIVillagerRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;


import com.github.yzqdev.pethome.server.misc.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(PetHomeMod.MODID)
public class PetHomeMod {
    public static final String MODID = "pet_home";
    public static final Logger LOGGER = LogManager.getLogger();
    public static CommonProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    public static final PetHomeConfig CONFIG;
    private static final ForgeConfigSpec CONFIG_SPEC;

    static {
        final Pair<PetHomeConfig, ForgeConfigSpec> specPair = PetHomeConfig.BUILDER.configure(PetHomeConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public PetHomeMod(FMLJavaModLoadingContext context) {
        context.getModEventBus().addListener(this::setupClient);
        context.getModEventBus().addListener(this::setup);
        final IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener((LivingDeathEvent event) -> validateHealth(event));
        bus.addListener((LivingEvent.LivingTickEvent event) -> validateHealth(event));
        bus.addListener((LivingHurtEvent event) -> {
            validateHealth(event);
            if (Float.isNaN(event.getAmount())) {
                event.setCanceled(true);
            }
        });
       context.registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC, MODID+".toml");
        PHItemRegistry.DEF_REG.register(context.getModEventBus());
        DIBlockRegistry.DEF_REG.register(context.getModEventBus());
        DIEnchantmentRegistry.registerEnchantments(context.getModEventBus());
        DITileEntityRegistry.DEF_REG.register(context.getModEventBus());
        PHEntityRegistry.DEF_REG.register(context.getModEventBus());
        DIPOIRegistry.DEF_REG.register(context.getModEventBus());
        DIParticleRegistry.DEF_REG.register(context.getModEventBus());
        DIVillagerRegistry.DEF_REG.register(context.getModEventBus());
        DISoundRegistry.DEF_REG.register(context.getModEventBus());
        DIActivityRegistry.DEF_REG.register(context.getModEventBus());
        DIVillagePieceRegistry.DEF_REG.register(context.getModEventBus());
        DICreativeTabRegistry.DEF_REG.register(context.getModEventBus());
        DILootRegistry.DEF_REG.register(context.getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PROXY);
        PROXY.init(context);
    }
    private void validateHealth(@NotNull LivingEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        float health = entity.getHealth();
        if (Float.isNaN(health) || health < 0.0F) {
            LOGGER.error(entity.getName());
            entity.setHealth(0.0F);
        }
    }
    private void setupClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PROXY.clientInit());
    }

    private void setup(final FMLCommonSetupEvent event) {
        int packetsRegistered = 0;

        Networking. NETWORK_WRAPPER.registerMessage(packetsRegistered++, PropertiesMessage.class, PropertiesMessage::write, PropertiesMessage::read, PropertiesMessage.Handler::handle);
        event.enqueueWork(() -> PROXY.serverInit());
    }


}
