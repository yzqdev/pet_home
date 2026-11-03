package com.github.yzqdev.pethome.client;


import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.particle.*;
import com.github.yzqdev.pethome.client.render.*;
import com.github.yzqdev.pethome.server.entity.*;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.item.DeedOfOwnershipItem;
import com.github.yzqdev.pethome.server.item.FeatherOnAStickItem;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = PetHomeMod.MODID, value = Dist.CLIENT)
public class ClientEvents {

    public static final Map<Integer, DiscJockeySound> DISC_JOCKEY_SOUND_MAP = new HashMap<>();
    // 弱引用键：实体被卸载/GC 后条目自动清理，避免长会话内存泄漏
    public static final Map<Entity, int[]> shadowPunchRenderData = new WeakHashMap<>();
    /** 名牌渲染用的项圈物品（懒加载：注册表就绪后才会被渲染路径触达） */
    private static ItemStack collarTagRenderStack = null;

    /** mod 事件总线上的客户端监听；需在 mod 构造阶段（仅客户端）调用 */
    public static void registerModListeners(IEventBus modEventBus) {
        modEventBus.addListener(ClientEvents::clientSetup);
        modEventBus.addListener(ClientEvents::onAddLayers);
        modEventBus.addListener(ClientEvents::setupParticles);
    }

    /** 模组列表"配置"按钮入口；需在 mod 构造阶段（仅客户端）调用 */
    public static void registerConfigGui() {
        ModLoadingContext.get().registerExtensionPoint(
                net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new com.github.yzqdev.pethome.client.gui.PetHomeConfigScreen(parent)));
    }

    /** 名牌纵向偏移：安装了 Neat（自带血量条名牌）时抬高 0.5 格避免遮挡。 */
    public static float getNametagOffset() {
        return ModList.get().isLoaded("neat") ? 0.5F : 0;
    }

    /** 由 tick 逻辑写入的影子拳渲染数据（仅客户端实际生效） */
    public static void updateVisualDataForMob(Entity entity, int[] arr) {
        shadowPunchRenderData.put(entity, arr);
    }

    /** 随从点唱机的音效状态更新，仅在实体事件包到达客户端时调用 */
    public static void updateEntityStatus(Entity entity, byte updateKind) {
        if (entity instanceof FollowingJukeboxEntity) {
            SoundEvent record = ((FollowingJukeboxEntity) entity).getRecordSound();
            if (entity.isAlive() && updateKind == 66) {
                DiscJockeySound sound;
                if (record != null && (DISC_JOCKEY_SOUND_MAP.get(entity.getId()) == null || DISC_JOCKEY_SOUND_MAP.get(entity.getId()).getRecordSound() != record)) {
                    sound = new DiscJockeySound(record, (FollowingJukeboxEntity) entity);
                    DISC_JOCKEY_SOUND_MAP.put(entity.getId(), sound);
                } else {
                    sound = DISC_JOCKEY_SOUND_MAP.get(entity.getId());
                }
                if (sound != null && !Minecraft.getInstance().getSoundManager().isActive(sound) && sound.canPlaySound() && sound.isNearest()) {
                    Minecraft.getInstance().getSoundManager().play(sound);
                }
            }
            if (updateKind == 67 || record == null) {
                if (DISC_JOCKEY_SOUND_MAP.containsKey(entity.getId())) {
                    DiscJockeySound sound = DISC_JOCKEY_SOUND_MAP.get(entity.getId());
                    DISC_JOCKEY_SOUND_MAP.remove(entity.getId());
                    Minecraft.getInstance().getSoundManager().stop(sound);
                }
            }
        }
    }

    /** 客户端专属注册：实体渲染器与物品模型属性 */
    public static void clientInit() {
        EntityRenderers.register(PHEntityRegistry.CHAIN_LIGHTNING.get(), ChainLightningRender::new);
        EntityRenderers.register(PHEntityRegistry.RECALL_BALL.get(), RecallBallRender::new);
        EntityRenderers.register(PHEntityRegistry.FEATHER.get(), RenderFeather::new);
        EntityRenderers.register(PHEntityRegistry.GIANT_BUBBLE.get(), RenderGiantBubble::new);
        EntityRenderers.register(PHEntityRegistry.FOLLOWING_JUKEBOX.get(), RenderJukeboxFollower::new);
        EntityRenderers.register(PHEntityRegistry.HIGHLIGHTED_BLOCK.get(), RenderHighlightedBlock::new);
        EntityRenderers.register(PHEntityRegistry.PSYCHIC_WALL.get(), RenderPsychicWall::new);
        ItemProperties.register(PHItemRegistry.FEATHER_ON_A_STICK.get(),   ResourceLocation.withDefaultNamespace("cast"), (stack, lvl, holder, i) -> {
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
        ItemProperties.register(PHItemRegistry.DEED_OF_OWNERSHIP.get(), ResourceLocation.withDefaultNamespace("bound"), (stack, lvl, holder, i) -> {
            return DeedOfOwnershipItem.isBound(stack) ? 1 : 0;
        });
        // 捕网发射器蓄力谓词（模型 overrides 按 pet_home:charge 切换能量核心贴图）
        ItemProperties.register(PHItemRegistry.NET_LAUNCHER_ITEM.get(), new ResourceLocation(PetHomeMod.MODID, "charge"), (stack, lvl, holder, i) -> {
            if (holder == null || !holder.isUsingItem() || holder.getUseItem() != stack) {
                return 0.0F;
            }
            return Math.min((stack.getUseDuration() - holder.getUseItemRemainingTicks()) / 20.0F, 1.0F);
        });
    }

    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientEvents::clientInit);
    }

    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        List<EntityType<? extends LivingEntity>> entityTypes = ImmutableList.copyOf(
                ForgeRegistries.ENTITY_TYPES.getValues().stream()
                        .filter(LayerManager::canApply)
                        .filter(DefaultAttributes::hasSupplier)
                        .map(entityType -> (EntityType<? extends LivingEntity>) entityType)
                        .collect(Collectors.toList()));
        entityTypes.forEach((entityType -> {
            LayerManager.addLayerIfApplicable(entityType, event);
        }));
    }

    /** mod 事件总线：粒子工厂注册 */
    public static void setupParticles(RegisterParticleProvidersEvent event) {
        PetHomeMod.LOGGER.debug("Registered particle factories");
        event.registerSpecial(PHParticleRegistry.DEFLECTION_SHIELD.get(), new ParticleDeflectionShield.Factory());
        event.registerSpriteSet(PHParticleRegistry.MAGNET.get(), ParticleMagnet.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.ZZZ.get(), ParticleZZZ.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.GIANT_POP.get(), ParticleGiantPop.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.SIMPLE_BUBBLE.get(), ParticleSimpleBubble.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.VAMPIRE.get(), ParticleVampire.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.SNIFF.get(), ParticleSniff.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.PSYCHIC_WALL.get(), ParticlePsychicWall.Factory::new);
        event.registerSpecial(PHParticleRegistry.INTIMIDATION.get(), new ParticleIntimidation.Factory());
        event.registerSpriteSet(PHParticleRegistry.BLIGHT.get(), ParticleBlight.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.LANTERN_BUGS.get(), ParticleLanternBugs.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.QUESTION_MARK.get(), ParticleQuestionMark.Factory::new);
    }

    /** 游戏事件总线：客户端命令注册 */
    @SubscribeEvent
    public static void onRegisterClientCommands(net.minecraftforge.client.event.RegisterClientCommandsEvent event) {
        event.getDispatcher().register(com.github.yzqdev.pethome.client.gui.PetHomeConfigCommand.build());
    }

    /** 游戏事件总线：方块高亮描边颜色 */
    @SubscribeEvent
    public static void onOutlineColor(EventGetOutlineColor event) {
        if (event.getEntityIn() instanceof HighlightedBlockEntity) {
            event.setColor(OreColorRegistry.getBlockColor(((HighlightedBlockEntity) event.getEntityIn()).getBlockState()));
            event.setResult(Event.Result.ALLOW);
        }
    }

    /** 游戏事件总线：潜行时把宠物铭牌替换为附魔/生命信息 */
    @SubscribeEvent
    public static void renderNametagEvent(RenderNameTagEvent event) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            return;
        }
        if (TameableUtils.isTamed(event.getEntity()) && TameableUtils.isPetOf(localPlayer, event.getEntity()) && TameableUtils.hasAnyEnchants((LivingEntity) event.getEntity()) && localPlayer.isShiftKeyDown()) {
            event.setResult(Event.Result.DENY);
            renderNametagEnchantments(event.getEntity(), event.getContent(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }
    }

    /** 自绘增强名牌：名字追加血量，上方渲染项圈图标与附魔列表（暗影底 + 高亮描边两遍绘制）。 */
    private static void renderNametagEnchantments(Entity entity, Component nameTag, PoseStack pose, MultiBufferSource buffer, int lightIn) {
        LivingEntity living = (LivingEntity) entity;
            List<Component> list = TameableUtils.getEnchantDescriptions(living);
            double d0 = Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(entity);
            if (net.minecraftforge.client.ForgeHooksClient.isNameplateInRenderDistance(entity, d0)) {
                if (nameTag instanceof MutableComponent) {
                    int health = Math.round(living.getHealth());
                    int maxHealth = Math.round(living.getMaxHealth());
                    nameTag = ((MutableComponent) nameTag).append(" (" + health + "/" + maxHealth + ")");
                }
                Font font = Minecraft.getInstance().font;
                boolean flag = !entity.isDiscrete();
                float f = entity.getBbHeight() + 0.5F;
                int i = -10 * list.size();
                pose.pushPose();
                pose.translate(0.0D, f + getNametagOffset(), 0.0D);
                pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
                pose.scale(-0.025F, -0.025F, 0.025F);

                float f3 = !list.isEmpty() ? (float) (-font.width(list.get(0)) / 2) : (float) (-font.width(nameTag) / 2);
                pose.pushPose();
                pose.translate(f3 + 12, (-10 * list.size()) + 16, 0);
                pose.mulPose(Axis.XP.rotationDegrees(180.0F));
                pose.scale(22F, 22F, 22F);
                Minecraft.getInstance().getItemRenderer().renderStatic(getCollarTagRenderStack(), ItemDisplayContext.GROUND, lightIn, OverlayTexture.NO_OVERLAY, pose, buffer, entity.level(), entity.getId());
                pose.popPose();

                Matrix4f matrix4f = pose.last().pose();
                float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                int j = (int) (f1 * 255.0F) << 24;
                float f2 = (float) (-font.width(nameTag) / 2);
                font.drawInBatch(nameTag, f2, (float) i - 0.25F, 553648127, false, matrix4f, buffer, Font.DisplayMode.NORMAL, j, lightIn);
                if (flag) {
                    font.drawInBatch(nameTag, f2, (float) i - 0.25F, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, lightIn);
                }
                pose.pushPose();
                pose.scale(0.8F, 0.8F, 0.8F);
                matrix4f = pose.last().pose();
                for (int k = 0; k < list.size(); k++) {
                    float f4 = (float) (-font.width(list.get(k)) / 2);
                    font.drawInBatch(list.get(k), f4, i * 1.25F + k * 10 + 12, 553648127, false, matrix4f, buffer, Font.DisplayMode.NORMAL, j, lightIn);
                    if (flag) {
                        font.drawInBatch(list.get(k), f4, i * 1.25F + k * 10 + 12, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, lightIn);
                    }
                }
                pose.popPose();
                pose.popPose();
            }
    }

    private static ItemStack getCollarTagRenderStack() {
        if (collarTagRenderStack == null) {
            collarTagRenderStack = new ItemStack(PHItemRegistry.COLLAR_TAG.get());
        }
        return collarTagRenderStack;
    }
}
