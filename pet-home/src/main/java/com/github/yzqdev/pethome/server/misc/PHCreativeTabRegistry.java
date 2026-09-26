package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.CustomTabBehavior;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;


public class PHCreativeTabRegistry {

    public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(PetHomeMod.MODID, PetHomeMod.MODID),
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
                                    .filter(holder -> holder.key().identifier().getNamespace().equals(PetHomeMod.MODID))
                                    .forEach(holder -> {
                                        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                                        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                                        mutable.set(holder, holder.value().getMaxLevel());
                                        book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
                                        output.accept(book);
                                    });
                        });
                    })
                    .build());

    public static void init() {
    }
}
