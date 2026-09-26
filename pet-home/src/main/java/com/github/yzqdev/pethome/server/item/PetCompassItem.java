package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

import java.util.function.Consumer;

/**
 * 宠物罗盘：右键（空气）打开宠物列表 GUI 。
 * 列表数据由服务端按主人打包（{@code PetCompassTracker.handleOpenRequest}），
 * 传送 / 召回操作由 {@code PetCompassTeleport} 在服务端验证所有权与配置后执行。
 */
public class PetCompassItem extends Item {

    public PetCompassItem(Properties properties) {
        super(properties);
    }

    /** 右键空气：客户端向服务端请求按主人打包的宠物列表，数据回来后 PetCompassScreen.handleData 开屏 */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            sendOpenRequest();
        }
        return InteractionResult.SUCCESS;
    }

    @Environment(EnvType.CLIENT)
    private static void sendOpenRequest() {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new com.github.yzqdev.pethome.network.PropertiesMessage(PHConstants.petCompassOpen, new CompoundTag(), 0));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltip, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable(LangDefinition.tooltips("pet_compass.desc")).withStyle(ChatFormatting.GREEN));
    }
}
