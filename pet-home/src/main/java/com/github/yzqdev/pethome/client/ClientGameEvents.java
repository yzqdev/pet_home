package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.server.entity.FollowingJukeboxEntity;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClientGameEvents {

    public static final Map<Integer, DiscJockeySound> DISC_JOCKEY_SOUND_MAP = new HashMap<>();
    public static Map<Entity, int[]> shadowPunchRenderData = new HashMap<>();

    public static float getNametagOffset() {
        return FabricLoader.getInstance().isModLoaded("neat") ? 0.5F : 0;
    }

    /**
     * 由 LivingEntityRendererMixin 在 renderNameTag 的 HEAD 调用。
     * 返回 true 表示已绘制自定义名牌（并取消原版名牌）。
     */
    public static boolean renderNametagEvent(Entity entity, Component nameTag, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        if (TameableUtils.isTamed(entity) && TameableUtils.isPetOf(Minecraft.getInstance().player, entity) && TameableUtils.hasAnyEnchants((LivingEntity) entity) && Minecraft.getInstance().player.isShiftKeyDown()) {
            renderNametagEnchantments(entity, nameTag, pose, buffer, packedLight);
            return true;
        }
        return false;
    }

    private static void renderNametagEnchantments(Entity entity, Component nameTag, PoseStack pose, MultiBufferSource buffer, int lightIn) {
        if (Minecraft.getInstance().player.isShiftKeyDown() && TameableUtils.isTamed(entity) && TameableUtils.hasAnyEnchants((LivingEntity) entity)) {
            LivingEntity living = (LivingEntity) entity;
            List<Component> list = TameableUtils.getEnchantDescriptions(living);
            double d0 = Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(entity);
            if (d0 < 4096.0D) {
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
                Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(PHItemRegistry.COLLAR_TAG), ItemDisplayContext.GROUND, lightIn, OverlayTexture.NO_OVERLAY, pose, buffer, entity.level(), entity.getId());
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
    }

    public static void updateVisualDataForMob(Entity entity, int[] arr) {
        shadowPunchRenderData.put(entity, arr);
    }

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
}
