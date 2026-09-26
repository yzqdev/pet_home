package com.github.yzqdev.pethome.datagen.loot;


import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nonnull;

public class PHLootModifier extends LootModifier {
    public static final MapCodec<PHLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst ->
                    codecStart(inst)
                            .and(Codec.INT.optionalFieldOf("loot_type", 0).forGetter((configuration) -> configuration.lootType))
                            .apply(inst, PHLootModifier::new));

    private final int lootType;

    protected PHLootModifier(LootItemCondition[] conditionsIn, int priority, int lootType) {
        super(conditionsIn, priority);
        this.lootType = lootType;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        switch (lootType) {
            case 0 -> {
                if (context.getRandom().nextFloat() < PetHomeConfig.sinisterCarrotLootChance) {
                    generatedLoot.add(new ItemStack(PHItemRegistry.SINISTER_CARROT.get(), context.getRandom().nextInt(1, 2)));
                }
            }
            case 1 -> {
                if (context.getRandom().nextFloat() < PetHomeConfig.bubblingLootChance) {
                    generatedLoot.add(enchantedBook(ModEnchantments.BUBBLING, context.getRandom(), context));
                }
            }
            case 2 -> {
                if (context.getRandom().nextFloat() < PetHomeConfig.vampirismLootChance) {
                    generatedLoot.add(enchantedBook(ModEnchantments.VAMPIRE, context.getRandom(), context));
                }
            }
            case 3 -> {
                if (context.getRandom().nextFloat() < PetHomeConfig.shareLootChance) {
                    generatedLoot.add(enchantedBook(ModEnchantments.SHARE, context.getRandom(), context));
                }
            }
            case 4 -> {
                if (context.getRandom().nextFloat() < PetHomeConfig.oreScentingLootChance) {
                    generatedLoot.add(enchantedBook(ModEnchantments.ORE_SCENTING, context.getRandom(), context));
                }
            }
            case 5 -> {
                if (context.getRandom().nextFloat() < PetHomeConfig.sonicBoomLootChance) {
                    generatedLoot.add(enchantedBook(ModEnchantments.SonicBoom, context.getRandom(), context));
                }
            }
            case 6 -> {
                if (context.getRandom().nextFloat() < PetHomeConfig.blazingProtectionLootChance) {
                    generatedLoot.add(enchantedBook(ModEnchantments.BLAZING_PROTECTION, context.getRandom(), context));
                }
            }
            case 7 -> {
                if (context.getRandom().nextFloat() < PetHomeConfig.paralysisLootChance) {
                    generatedLoot.add(enchantedBook(ModEnchantments.PARALYSIS, context.getRandom(), context));
                }
            }
            case 8 -> {
                if (context.getRandom().nextFloat() < PetHomeConfig.toughLootChance) {
                    generatedLoot.add(enchantedBook(ModEnchantments.TOUGH, context.getRandom(), context));
                }
            }

            default -> throw new IllegalStateException("Unexpected value: " + lootType);
        }
        return generatedLoot;
    }

    private ItemStack enchantedBook(ResourceKey<Enchantment> enchantmentKey, RandomSource randomSource, LootContext context) {

        var reg = context.getLevel()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);
        var enchant = reg.getValue(enchantmentKey);
        if (enchant == null) {
            return new ItemStack(Items.ENCHANTED_BOOK);
        }
        Holder<Enchantment> holder = reg.wrapAsHolder(enchant);
        int maxLevels = enchant.getMaxLevel();
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.enchant(holder, maxLevels > 1 ? 1 + randomSource.nextInt(maxLevels - 1) : 1);
        return book;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}