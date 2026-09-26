package com.github.yzqdev.pethome.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import com.github.yzqdev.pethome.server.block.DrumBlock;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import com.github.yzqdev.pethome.server.block.WaywardLanternBlock;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class DIBlockItem extends BlockItem {

    private final Block block;

    public DIBlockItem(Block block, Item.Properties props) {
        super(block, props);
        this.block = block;
    }

    @Override
    public Block getBlock() {
        return block;
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
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
        tooltipComponents.add(Component.translatable(key).withStyle(ChatFormatting.GREEN));
    }

    public boolean canFitInsideContainerItems() {
        return !(block instanceof ShulkerBoxBlock);
    }

    public void onDestroyed(ItemEntity p_150700_) {
        if (this.block instanceof ShulkerBoxBlock) {
            ItemStack itemstack = p_150700_.getItem();
            CompoundTag compoundtag = getBlockEntityData(itemstack);
            if (compoundtag != null && compoundtag.contains("Items", 9)) {
                ListTag listtag = compoundtag.getList("Items", 10);
                ItemUtils.onContainerDestroyed(p_150700_, listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of));
            }
        }
    }
}
