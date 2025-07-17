package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.item.CustomTabBehavior;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;
import java.util.List;

public class DICreativeTabRegistry {

    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PetHomeMod.MODID);

    public static final RegistryObject<CreativeModeTab> TAB = DEF_REG.register(PetHomeMod.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + PetHomeMod.MODID))
            .icon(() -> new ItemStack(PHItemRegistry.COLLAR_TAG.get()))
            .displayItems((enabledFeatures, output) -> {
                PHItemRegistry.DEF_REG.getEntries().forEach(itemObj -> {
                    Item item = itemObj.get();

                    // 如果实现了自定义行为接口（如填充不同状态的物品）
                    if (item instanceof CustomTabBehavior customBehavior) {
                        customBehavior.fillItemCategory(output);
                    }
                    // 排除掉不需要显示的特定物品 (建议改为判断是否属于某个标记接口或RegistryObject对比)
                    else if (!isExcluded(itemObj)) {
                        output.accept(item);
                    }
                });

                DIEnchantmentRegistry.DEF_REG.getEntries().forEach(enchantObj -> {
                    Enchantment enchant = enchantObj.get();

                    if (enchant.isAllowedOnBooks() && PetHomeMod.CONFIG.isEnchantEnabled(enchant)) {
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
//                            if (enchant.isAllowedOnBooks() && PetHomeMod.CONFIG.isEnchantEnabled(enchant)) {
//                                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchant, enchant.getMaxLevel())));
//                            }
//                        }
//                    }
//                } catch (IllegalAccessException e) {
//                    throw new RuntimeException(e);
//                }
            })
            .build());
    private static boolean isExcluded(RegistryObject<Item> itemObj) {
        return itemObj == PHItemRegistry.NET_HAS_ITEM;
        // 或者使用 Set.of(ID1, ID2).contains(itemObj.getId())
    }
}
