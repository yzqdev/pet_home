package com.github.yzqdev.pet_home.datagen.loot;


import com.github.yzqdev.pet_home.PetHomeConfig;
import com.github.yzqdev.pet_home.datagen.ModEnchantments;
import com.github.yzqdev.pet_home.server.item.PHItemRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;


import javax.annotation.Nonnull;

public class PHLootModifier extends LootModifier {
    public static final MapCodec<PHLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst ->
                    inst.group(
                                    LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions),
                                    Codec.INT.fieldOf("loot_type").orElse(0).forGetter((configuration) -> configuration.lootType)
                            )
                            .apply(inst, PHLootModifier::new));

    private final int lootType;

    protected PHLootModifier(LootItemCondition[] conditionsIn, int lootType) {
        super(conditionsIn);
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
        }
        return generatedLoot;
    }

    private ItemStack enchantedBook(ResourceKey<Enchantment> enchantmentKey, RandomSource randomSource, LootContext context) {

        var reg = context.getLevel()
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT);
        var enchant = reg.get(enchantmentKey);

        int maxLevels = enchant.getMaxLevel();
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(reg.wrapAsHolder(enchant), maxLevels > 1 ? 1 + randomSource.nextInt(maxLevels - 1) : 1));

    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}