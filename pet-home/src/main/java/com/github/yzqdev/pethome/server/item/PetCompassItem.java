package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.network.PropertiesMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.function.Consumer;

/**
 * 宠物罗盘：右键打开宠物列表 GUI（compass.md 第九节），列表展示玩家全部宠物，
 * 选中后可传送玩家到宠物 / 召回宠物（两个功能由配置独立开关）。
 */
public class PetCompassItem extends Item {

    public PetCompassItem(Properties properties) {
        super(properties);
    }

    /** 右键：客户端向服务端请求按主人打包的宠物列表，数据回来后 PetCompassScreen.handleData 开屏 */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            ClientPacketDistributor.sendToServer(
                    new PropertiesMessage(PHConstants.petCompassOpen, new CompoundTag(), 0));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltip, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable(LangDefinition.TOOLTIP_PET_COMPASS_DESC).withStyle(ChatFormatting.GREEN));
    }
}
