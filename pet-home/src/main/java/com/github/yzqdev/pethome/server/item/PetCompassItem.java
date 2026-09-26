package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.ModConstants;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.network.Networking;
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

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * 宠物罗盘：右键（空气）打开宠物列表 GUI 。
 * 列表数据由服务端按主人打包（{@code PetCompassTracker.handleOpenRequest}），
 * 传送 / 召回操作由 {@code PetCompassTeleport} 在服务端验证所有权与配置后执行。
 */
public class PetCompassItem extends Item {

    public PetCompassItem() {
        super(new Item.Properties().stacksTo(1));
    }

    /** 右键空气：客户端向服务端请求按主人打包的宠物列表，数据回来后 PetCompassScreen.handleData 开屏 */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            Networking.sendMSGToServer(new com.github.yzqdev.pethome.network.PropertiesMessage(ModConstants.petCompassOpen, new CompoundTag(), 0));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(LangDefinition.tooltips("pet_compass.desc")).withStyle(ChatFormatting.GREEN));
    }
}
