package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.server.block.DrumBlock;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import com.github.yzqdev.pethome.server.block.WaywardLanternBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import java.util.function.Consumer;

/**
 * 方块物品基类（宠物床/指挥鼓/迷途灯笼共用）：不可放回潜影盒、销毁时不掉落自身；
 * tooltip 按 {@code block.pet_home.<name>.desc} 类键补说明行（按方块类型三选一）。
 */
public class PHBlockItem extends BlockItem {

    private final Block block;

    public PHBlockItem(Block block, Item.Properties props) {
        super(block, props);
        this.block = block;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return !(block instanceof ShulkerBoxBlock);
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {

    }

    // 对齐 1.21 PetbedItem：方块物品附 desc 提示行（按方块类型区分）
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        String key;
        if (this.getBlock() instanceof PetBedBlock) {
            key = "tooltips.pet_home.substitute_pet_bed.desc";
        } else if (this.getBlock() instanceof DrumBlock) {
            key = "tooltips.pet_home.drum.desc";
        } else if (this.getBlock() instanceof WaywardLanternBlock) {
            key = "tooltips.pet_home.wayward_lantern.desc";
        } else {
            return;
        }
        builder.accept(Component.translatable(key).withStyle(ChatFormatting.GREEN));
    }
}
