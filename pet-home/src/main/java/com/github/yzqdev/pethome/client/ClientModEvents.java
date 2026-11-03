package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.compat.geckolib.GeoOverlayCompat;
import com.github.yzqdev.pethome.client.render.ChainLightningRender;
import com.github.yzqdev.pethome.client.render.LayerPetOverlays;
import com.github.yzqdev.pethome.client.render.RecallBallRender;
import com.github.yzqdev.pethome.client.render.RenderFeather;
import com.github.yzqdev.pethome.client.render.RenderGiantBubble;
import com.github.yzqdev.pethome.client.render.RenderHighlightedBlock;
import com.github.yzqdev.pethome.client.render.RenderJukeboxFollower;
import com.github.yzqdev.pethome.client.render.RenderPsychicWall;
import com.github.yzqdev.pethome.server.entity.FeatherEntity;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.item.DeedOfOwnershipItem;
import com.github.yzqdev.pethome.server.item.FeatherOnAStickItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.util.ClientMobTooltip;
import com.github.yzqdev.pethome.util.ItemMobTooltip;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


/**
 * 客户端注册中心：实体渲染器、物品模型属性（钓鱼/绑定覆盖）、全生物宠物叠加层、
 * 捕网 tooltip 渲染组件与附魔书 tooltip 回调。由 {@code PetHomeModClient.onInitializeClient} 依序调用。
 */
public class ClientModEvents {

    /** 注册 7 个模组实体的渲染器（捕网用原版 ThrownItemRenderer）。 */
    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(PHEntityRegistry.CHAIN_LIGHTNING, ChainLightningRender::new);
        EntityRendererRegistry.register(PHEntityRegistry.RECALL_BALL, RecallBallRender::new);
        EntityRendererRegistry.register(PHEntityRegistry.FEATHER, RenderFeather::new);
        EntityRendererRegistry.register(PHEntityRegistry.GIANT_BUBBLE, RenderGiantBubble::new);
        EntityRendererRegistry.register(PHEntityRegistry.FOLLOWING_JUKEBOX, RenderJukeboxFollower::new);
        EntityRendererRegistry.register(PHEntityRegistry.HIGHLIGHTED_BLOCK, RenderHighlightedBlock::new);
        EntityRendererRegistry.register(PHEntityRegistry.PSYCHIC_WALL, RenderPsychicWall::new);
        EntityRendererRegistry.register(PHEntityRegistry.NET_ENTITY, ThrownItemRenderer::new);
    }

    /** 物品模型覆盖属性：羽毛钓竿的 cast（抛出/收回姿态）与契约的 bound（绑定发光贴图）。 */
    public static void registerItemProperties() {
        ItemProperties.register(PHItemRegistry.FEATHER_ON_A_STICK, new ResourceLocation("cast"), (stack, lvl, holder, i) -> {
            if (holder == null) {
                return 0.0F;
            } else {
                boolean flag = holder.getMainHandItem() == stack;
                boolean flag1 = holder.getOffhandItem() == stack;
                if (holder.getMainHandItem().getItem() instanceof FeatherOnAStickItem) {
                    flag1 = false;
                }
                return (flag || flag1) && holder instanceof Player && ((Player) holder).fishing instanceof FeatherEntity ? 1.0F : 0.0F;
            }
        });
        ItemProperties.register(PHItemRegistry.DEED_OF_OWNERSHIP, new ResourceLocation("bound"), (stack, lvl, holder, i) -> {
            return DeedOfOwnershipItem.isBound(stack) ? 1 : 0;
        });
        // 捕网发射器蓄力谓词（模型 overrides 按 pet_home:charge 切换能量核心贴图）
        ItemProperties.register(PHItemRegistry.NET_LAUNCHER_ITEM, new ResourceLocation(PetHomeMod.MODID, "charge"), (stack, lvl, holder, i) -> {
            if (holder == null || !holder.isUsingItem() || holder.getUseItem() != stack) {
                return 0.0F;
            }
            return Math.min((stack.getUseDuration() - holder.getUseItemRemainingTicks()) / 20.0F, 1.0F);
        });
    }

    /**
     * 通过 {@link LivingEntityFeatureRendererRegistrationCallback} 给所有生物渲染器追加宠物覆盖图层
     * （Fabric 版替代 Forge 的 EntityRenderersEvent.AddLayers）。
     */
    public static void registerLayers() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            addLayerIfApplicable((EntityType<? extends LivingEntity>) entityType, (LivingEntityRenderer) entityRenderer, registrationHelper);
        });
    }

    private static void addLayerIfApplicable(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer renderer,
                                             LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper registrationHelper) {
        if (entityType != EntityType.ENDER_DRAGON && renderer != null) {
            // GeckoLib 渲染器（原版 layer 挂不上）先走适配层；未装/版本不符时 tryApply 内部降级返回 false
            if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("geckolib")
                    && GeoOverlayCompat.tryApply(renderer)) {
                return;
            }
            try {
                registrationHelper.register(new LayerPetOverlays(renderer));
            } catch (Exception e) {
                PetHomeMod.LOGGER.warn("Could not apply pet overlays layer to " + BuiltInRegistries.ENTITY_TYPE.getKey(entityType) + ", has custom renderer that is not LivingEntityRenderer.");
            }
        }
    }

    public static void registerTooltipComponents() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof ItemMobTooltip itemMobTooltip) {
                return new ClientMobTooltip(itemMobTooltip);
            }
            return null;
        });
    }

    /**
     * 附魔 desc 提示行接进 Fabric 的 {@code ItemTooltipCallback}，实现与 1.21 一样放在
     * {@link ServerEvent#onItemTooltip(ItemStack, java.util.List)}。
     */
    public static void registerItemTooltips() {
        ItemTooltipCallback.EVENT.register((stack, context, tooltip) -> ServerEvent.onItemTooltip(stack, tooltip));
    }
}
