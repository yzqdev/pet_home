package com.github.yzqdev.pethome.server.misc;


import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.CustomTabBehavior;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;

public class PHCreativeTabRegistry {

    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PetHomeMod.MODID);



    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = DEF_REG.register(PetHomeMod.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + PetHomeMod.MODID))
            .icon(() -> new ItemStack(PHItemRegistry.COLLAR_TAG.get()))
            .displayItems((enabledFeatures, output) -> {
                var excludeItems = List.of(PHItemRegistry.NET_HAS_ITEM.getId());
                for (var item : PHItemRegistry.DEF_REG.getEntries()) {
                    if (item.get() instanceof CustomTabBehavior customTabBehavior) {
                        customTabBehavior.fillItemCategory(output);
                    } else {
                        if (!excludeItems.contains(item.getId())) {
                            output.accept(item.get());
                        }

                    }
                }
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

}
