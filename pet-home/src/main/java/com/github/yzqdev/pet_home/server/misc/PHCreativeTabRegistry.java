package com.github.yzqdev.pet_home.server.misc;

 
import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.datagen.ModEnchantments;
import com.github.yzqdev.pet_home.server.item.CustomTabBehavior;
import com.github.yzqdev.pet_home.server.item.PHItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.lang.reflect.Field;
import java.util.Optional;

public class PHCreativeTabRegistry {

    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PetHomeMod.MODID);
    private static void addEnchantmentBook(Optional<Holder.Reference<Enchantment>> holder, CreativeModeTab.Output output) {
        holder.ifPresent(ref -> {
            EnchantmentInstance instance = new EnchantmentInstance(ref, ref.value().getMaxLevel());
            output.accept(EnchantedBookItem.createForEnchantment(instance));
        });
    }
    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> TAB = DEF_REG.register(PetHomeMod.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + PetHomeMod.MODID))
            .icon(() -> new ItemStack(PHItemRegistry.COLLAR_TAG.get()))
            .displayItems((enabledFeatures, output) -> {
                for (var item : PHItemRegistry.DEF_REG.getEntries()) {
                    if (item.get() instanceof CustomTabBehavior customTabBehavior) {
                        customTabBehavior.fillItemCategory(output);
                    } else {
                        output.accept(item.get());
                    }
                }
                enabledFeatures.holders().lookup(Registries.ENCHANTMENT).ifPresent(reg -> {
                    try {
                        for (Field f :ModEnchantments.class.getDeclaredFields()){
                            Object obj=null;
                            obj=f.get(null);
                            if (obj instanceof ResourceKey  key  ){
                                addEnchantmentBook(reg.get(key), output);
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

//                try {
//                    for (Field f : DIEnchantmentRegistry.class.getDeclaredFields()) {
//                        Object obj = null;
//                        obj = f.get(null);
//                        if (obj instanceof Enchantment) {
//                            Enchantment enchant = (Enchantment) obj;
//                            if (enchant.isAllowedOnBooks() && DomesticationMod.CONFIG.isEnchantEnabled(enchant)) {
//                                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchant, enchant.getMaxLevel())));
//                            }
//                        }
//                    }
//                } catch (IllegalAccessException e) {
//                    throw new RuntimeException(e);
//                }
            })
            .build());

}
