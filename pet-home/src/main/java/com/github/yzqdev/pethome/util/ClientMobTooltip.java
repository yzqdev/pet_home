package com.github.yzqdev.pethome.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;

public class ClientMobTooltip implements ClientTooltipComponent {
    private final Component name;
    private final CompoundTag entityTag;
    private final String id;

    public ClientMobTooltip(ItemMobTooltip itemMobTooltip) {

        var tag = itemMobTooltip.compoundTag();
        String id = tag.getStringOr("id", "");
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(id));
        this.id = id;
        this.name = type.getDescription();
        this.entityTag = tag;
    }

    @Override
    public int getHeight(Font font) {

        return 75;
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(font.width(this.name), 50);
    }

    @Override
    public void extractImage(Font font, int pX, int pY, int w, int h, GuiGraphicsExtractor guiGraphics) {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }
        String id = this.entityTag.getStringOr("id", "");
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(id));
        LivingEntity livingEntity = (LivingEntity) type.create(world, EntitySpawnReason.COMMAND);
        if (livingEntity != null) {
            livingEntity.setOnGround(true);
            var tagValue = TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), this.entityTag);
            livingEntity.load(tagValue);


            float xAngle = (float) ((System.currentTimeMillis() / 25.0) % 720) / 20.0F;
            int width = this.getWidth(font);
            // 26.1: renderEntityInInventoryFollowsAngle 更名为 extractEntityInInventoryFollowsMouse
            InventoryScreen.extractEntityInInventoryFollowsMouse(guiGraphics,
                    pX, pY, pX + width, pY + this.getHeight(font), 25, 0.0625F, xAngle, 0.0F, livingEntity);
        }
    }
}
