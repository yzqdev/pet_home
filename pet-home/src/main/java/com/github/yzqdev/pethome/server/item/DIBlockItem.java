package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.server.block.DrumBlock;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import com.github.yzqdev.pethome.server.block.WaywardLanternBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class DIBlockItem extends BlockItem {

    private final RegistryObject<Block> blockSupplier;

    public DIBlockItem(RegistryObject<Block> blockSupplier, Item.Properties props) {
        super(null, props);
        this.blockSupplier = blockSupplier;
    }

    @Override
    public Block getBlock() {
        return blockSupplier.get();
    }

    // 方块物品与 1.21 PetbedItem 行为一致：附 desc 提示行（按方块类型区分；DIBlockItem 用于宠物床/指挥鼓/迷途灯笼）
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
        return !(blockSupplier.get() instanceof ShulkerBoxBlock);
    }

    public void onDestroyed(ItemEntity p_150700_) {
        if (this.blockSupplier.get() instanceof ShulkerBoxBlock) {
            ItemStack itemstack = p_150700_.getItem();
            CompoundTag compoundtag = getBlockEntityData(itemstack);
            if (compoundtag != null && compoundtag.contains("Items", 9)) {
                ListTag listtag = compoundtag.getList("Items", 10);
                ItemUtils.onContainerDestroyed(p_150700_, listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of));
            }
        }
    }
}
