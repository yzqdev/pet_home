package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.item.CustomTabBehavior;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.lang.reflect.Field;
import java.util.List;

public class DICreativeTabRegistry {


    public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(PetHomeMod.MODID, PetHomeMod.MODID), FabricItemGroup.builder()
            .title(Component.translatable("itemGroup." + PetHomeMod.MODID))
            .icon(() -> new ItemStack(PHItemRegistry.COLLAR_TAG))
            .displayItems((enabledFeatures, output) -> {
                BuiltInRegistries.ITEM.stream()
                        .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(PetHomeMod.MODID))
                        .forEach(item -> {
                    // 如果实现了自定义行为接口（如填充不同状态的物品）
                    if (item instanceof CustomTabBehavior customBehavior) {
                        customBehavior.fillItemCategory(output);
                    }
                    // 排除掉不需要显示的特定物品
                    else if (item != PHItemRegistry.NET_HAS_ITEM) {
                        output.accept(item);
                    }
                });

                BuiltInRegistries.ENCHANTMENT.stream()
                        .filter(enchant -> BuiltInRegistries.ENCHANTMENT.getKey(enchant).getNamespace().equals(PetHomeMod.MODID))
                        .forEach(enchant -> {
                    if (PetHomeMod.CONFIG.isEnchantEnabled(enchant)) {
                        output.accept(EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(enchant, enchant.getMaxLevel()))
                        );
                    }
                });
//                try {
//                    for (Field f : DIEnchantmentRegistry.class.getDeclaredFields()) {
//                        Object obj = null;
//                        obj = f.get(null);
//                        if (obj instanceof Enchantment) {
//                            Enchantment enchant = (Enchantment) obj;
//                            if (PetHomeMod.CONFIG.isEnchantEnabled(enchant)) {
//                                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchant, enchant.getMaxLevel())));
//                            }
//                        }
//                    }
//                } catch (IllegalAccessException e) {
//                    throw new RuntimeException(e);
//                }
            })
            .build());

    public static void init() {
    }
}