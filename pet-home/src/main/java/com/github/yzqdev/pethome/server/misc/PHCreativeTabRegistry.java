package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.CustomTabBehavior;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * 创造模式物品栏：Fabric 原生 {@code Registry.register}。
 * 内容遍历直接按命名空间过滤物品注册表（与 {@code PHItemRegistry#allItems()} 一致）。
 */
public class PHCreativeTabRegistry {

    public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, PetHomeMod.MODID),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup." + PetHomeMod.MODID))
                    .icon(() -> new ItemStack(PHItemRegistry.COLLAR_TAG))
                    .displayItems((enabledFeatures, output) -> {
                        BuiltInRegistries.ITEM.stream()
                                .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(PetHomeMod.MODID))
                                .forEach(item -> {
                                    if (item instanceof CustomTabBehavior customTabBehavior) {
                                        customTabBehavior.fillItemCategory(output);
                                    } else if (item != PHItemRegistry.NET_HAS_ITEM) {
                                        output.accept(item);
                                    }
                                });
                        enabledFeatures.holders().lookup(Registries.ENCHANTMENT).ifPresent(enchantmentRegistry -> {
                            enchantmentRegistry.listElements()
                                    .filter(holder -> holder.key().location().getNamespace().equals(PetHomeMod.MODID))
                                    .forEach(holder -> {
                                        output.accept(EnchantedBookItem.createForEnchantment(
                                                new EnchantmentInstance(holder, holder.value().getMaxLevel())
                                        ));
                                    });
                        });
                    })
                    .build());

    public static void init() {
    }
}
