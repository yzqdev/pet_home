package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.datagen.LangDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * 自由之铃（freedom_bell）：对自家宠物右键使用即放生（解除驯服、清附魔/宠物床/罗盘档案，消耗铃铛）。 服务端二次确认在 {@code
 * PlayerInteractEntityHandler#handleFreedomBell}：首次使用仅提示，10s 内对同一宠物 再次使用才真正放生，防误触。与哨子（{@link
 * WhistleItem} 召回全部宠物）互为反向操作。
 */
public class FreedomBellItem extends Item {

    public FreedomBellItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(LangDefinition.TOOLTIP_FREEDOM_BELL_DESC).withStyle(ChatFormatting.GREEN));
    }
}
