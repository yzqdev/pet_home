package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

// 附魔 desc 提示行：1.21 原版会在附魔 tooltip 显示 description，Forge 1.20.1 无此机制，
// 此处对 pet_home 附魔手动追加 enchantment.pet_home.<path>.desc（对齐 1.21 的 tooltip 表现）。
// 若环境已安装 Enchantment Descriptions（enchdesc）模组，由它统一展示 desc，我们跳过以免重复
@Mod.EventBusSubscriber(modid = PetHomeMod.MODID, value = Dist.CLIENT)
public class ItemTooltipHandler {

    private static final boolean ENCHDESC_LOADED = ModList.get().isLoaded("enchdesc");

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (ENCHDESC_LOADED) {
            return;
        }
        ItemStack stack = event.getItemStack();
        ListTag enchantments;
        if (stack.is(Items.ENCHANTED_BOOK)) {
            enchantments = EnchantedBookItem.getEnchantments(stack);
        } else if (stack.hasTag() && stack.getTag().contains("Enchantments", CompoundTag.TAG_LIST)) {
            enchantments = stack.getTag().getList("Enchantments", CompoundTag.TAG_COMPOUND);
        } else {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag enchantmentTag = enchantments.getCompound(i);
            ResourceLocation id = EnchantmentHelper.getEnchantmentId(enchantmentTag);
            if (id != null && PetHomeMod.MODID.equals(id.getNamespace())) {
                tooltip.add(Component.translatable("enchantment." + PetHomeMod.MODID + "." + id.getPath() + ".desc").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
