package com.github.yzqdev.pethome.datagen.loot;


import com.github.yzqdev.pethome.server.item.PHItemRegistry;
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
                                    PHLootType.CODEC.fieldOf("loot_type").orElse(PHLootType.SINISTER_CARROT).forGetter((configuration) -> configuration.lootType)
                            )
                            .apply(inst, PHLootModifier::new));

    private final PHLootType lootType;

    protected PHLootModifier(LootItemCondition[] conditionsIn, PHLootType lootType) {
        super(conditionsIn);
        this.lootType = lootType;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() < this.lootType.chance()) {
            if (this.lootType.isEnchantedBook()) {
                generatedLoot.add(enchantedBook(this.lootType.enchantmentKey(), context.getRandom(), context));
            } else {
                // 阴森胡萝卜：林地府邸，1~2 根
                generatedLoot.add(new ItemStack(PHItemRegistry.SINISTER_CARROT.get(), context.getRandom().nextInt(1, 2)));
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