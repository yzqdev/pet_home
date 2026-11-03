package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.render.ChainLightningRender;
import com.github.yzqdev.pethome.client.render.LayerPetOverlays;
import com.github.yzqdev.pethome.client.render.RecallBallRender;
import com.github.yzqdev.pethome.client.render.RenderFeather;
import com.github.yzqdev.pethome.client.render.RenderGiantBubble;
import com.github.yzqdev.pethome.client.render.RenderHighlightedBlock;
import com.github.yzqdev.pethome.client.render.RenderPsychicWall;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.util.ClientMobTooltip;
import com.github.yzqdev.pethome.util.ItemMobTooltip;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端注册集合：实体渲染器、宠物叠加层、tooltip 组件与物品 tooltip。 各 {@code register*} 方法由 {@code
 * PetHomeModClient.onInitializeClient} 在客户端初始化时调用一次， 全部走 Fabric 客户端回调注册，不持有状态。
 */
public class ClientModEvents {

  /** 为本模组 7 种自定义实体注册渲染器（捕获物 NET_ENTITY 复用原版 {@code ThrownItemRenderer}）。 */
  public static void registerEntityRenderers() {
    EntityRendererRegistry.register(PHEntityRegistry.RECALL_BALL, RecallBallRender::new);
    EntityRendererRegistry.register(PHEntityRegistry.CHAIN_LIGHTNING, ChainLightningRender::new);
    EntityRendererRegistry.register(PHEntityRegistry.GIANT_BUBBLE, RenderGiantBubble::new);
    EntityRendererRegistry.register(PHEntityRegistry.PSYCHIC_WALL, RenderPsychicWall::new);
    EntityRendererRegistry.register(
        PHEntityRegistry.HIGHLIGHTED_BLOCK, RenderHighlightedBlock::new);
    EntityRendererRegistry.register(PHEntityRegistry.FEATHER, RenderFeather::new);
    EntityRendererRegistry.register(PHEntityRegistry.NET_ENTITY, ThrownItemRenderer::new);
  }

  /**
   * 经 {@code LivingEntityFeatureRendererRegistrationCallback} 给所有 {@code LivingEntityRenderer} 追加
   * {@link LayerPetOverlays}（渲染项圈附魔视觉：磁铁、暗影之手、烈焰条等）。 单个渲染器应用失败仅告警跳过，不中断其它实体的注册。
   */
  public static void registerLayers() {
    LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
        (entityType, entityRenderer, registrationHelper, context) -> {
          if (entityRenderer
              instanceof
              net.minecraft.client.renderer.entity.LivingEntityRenderer<?, ?>
                  livingEntityRenderer) {
            try {
              registrationHelper.register(new LayerPetOverlays(livingEntityRenderer));
            } catch (Exception e) {
              com.github.yzqdev.pethome.PetHomeMod.LOGGER.warn(
                  "Failed to apply pet overlays layer to {}: {}",
                  net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entityType),
                  e.getMessage());
            }
          }
        });
  }

  /** 捕网物品 tooltip 的客户端图像组件：把 {@code ItemMobTooltip} 数据转为可渲染的 {@code ClientMobTooltip}。 */
  public static void registerTooltipComponents() {
    TooltipComponentCallback.EVENT.register(
        data -> {
          if (data instanceof ItemMobTooltip itemMobTooltip) {
            return new ClientMobTooltip(itemMobTooltip);
          }
          return null;
        });
  }

  /** 物品 tooltip 回调，转发给 {@code ServerEvent.onItemTooltip} 补充捕网内生物／宠物相关物品的说明行（纯客户端追加，不发网络包）。 */
  public static void registerItemTooltips() {
    ItemTooltipCallback.EVENT.register(
        (stack, tooltipContext, tooltipType, lines) ->
            com.github.yzqdev.pethome.server.event.ServerEvent.onItemTooltip(
                stack, (List<net.minecraft.network.chat.Component>) lines));
  }

  /** 捕网发射器蓄力谓词（模型 overrides 按 pet_home:charge 切换能量核心贴图）。 */
  public static void registerItemProperties() {
    ItemProperties.register(
        PHItemRegistry.NET_LAUNCHER_ITEM,
        ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "charge"),
        (stack, level, holder, seed) -> {
          if (holder == null || !holder.isUsingItem() || holder.getUseItem() != stack) {
            return 0.0F;
          }
          return Math.min(
              (stack.getUseDuration(holder) - holder.getUseItemRemainingTicks()) / 20.0F, 1.0F);
        });
  }
}
