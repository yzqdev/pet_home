package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.datagen.LangDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 宠物罗盘：右键打开宠物列表 GUI（compass.md 第九节），列表展示玩家全部宠物，
 * 选中后可传送玩家到宠物 / 召回宠物（两个功能由配置独立开关）。
 */
public class PetCompassItem extends Item {

    public PetCompassItem() {
        super(new Item.Properties().stacksTo(1));
    }

    /** 右键：客户端向服务端请求按主人打包的宠物列表，数据回来后 PetCompassScreen.handleData 开屏 */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            com.github.yzqdev.pethome.network.Networking.sendMSGToServer(
                    new com.github.yzqdev.pethome.network.PropertiesMessage(com.github.yzqdev.pethome.ModConstants.petCompassOpen, new CompoundTag(), 0));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(LangDefinition.TOOLTIP_PET_COMPASS_DESC).withStyle(ChatFormatting.GREEN));
    }
}
