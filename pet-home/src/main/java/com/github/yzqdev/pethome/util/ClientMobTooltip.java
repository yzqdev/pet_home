package com.github.yzqdev.pethome.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;

/**
 * 捕网 tooltip 的生物渲染组件（客户端）：在 tooltip 高度内用 {@code InventoryScreen.renderEntityInInventory}
 * 绘制被装生物的 3D 形象（随时间自转，scissor 裁剪），实体按 NBT 现场重建。
 */
public class ClientMobTooltip implements ClientTooltipComponent {
    private final Component name;
    private final CompoundTag entityTag;
    private final String id;

    public ClientMobTooltip(ItemMobTooltip itemMobTooltip) {

        // 从同步来的 NBT 中解析实体 id，取注册表描述作为 tooltip 宽度基准
        var tag = itemMobTooltip.compoundTag();
        String id = tag.getString("id");
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(id));
        this.id = id;
        this.name = type.getDescription();
        this.entityTag = tag;
    }

    @Override
    public int getHeight() {

        return 75;
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(font.width(this.name), 50);
    }

    @Override
    public void renderImage(Font font, int pX, int pY, GuiGraphics guiGraphics) {
        int width = this.getWidth(font);
        int posX = pX + width / 2;
        int posY = pY + 64;
        double rot = ((System.currentTimeMillis() / 25.0) % 360);
        Quaternionf pose = (new Quaternionf()).rotateZ((float) Math.PI);
        Quaternionf rotation = (new Quaternionf()).rotateY((float) Math.toRadians(rot));
        pose.mul(rotation);
        guiGraphics.enableScissor(pX, posY - 60, pX + width, posY);
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }
        String id = this.entityTag.getString("id");
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(id));
        LivingEntity livingEntity = (LivingEntity) type.create(world);
        if (livingEntity != null) {
            livingEntity.setOnGround(true);
            livingEntity.load(this.entityTag);
            InventoryScreen.renderEntityInInventory(guiGraphics, posX, posY, (int) (25 * 1),  pose, null, livingEntity);
            guiGraphics.disableScissor();
        }
    }
}
