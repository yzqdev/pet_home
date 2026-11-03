package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class PHLootModifier extends LootModifier {
    public static final Supplier<Codec<PHLootModifier>> CODEC = () ->
            RecordCodecBuilder.create(inst ->
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
                generatedLoot.add(enchantedBook(this.lootType.enchantment(), context.getRandom()));
            } else {
                // 阴森胡萝卜：林地府邸，1~2 根
                generatedLoot.add(new ItemStack(PHItemRegistry.SINISTER_CARROT.get(), context.getRandom().nextInt(1, 2)));
            }
        }
        return generatedLoot;
    }

    private ItemStack enchantedBook(Enchantment enchantment, RandomSource randomSource) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        int maxLevels = enchantment.getMaxLevel();
        EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(enchantment, maxLevels > 1 ? 1 + randomSource.nextInt(maxLevels - 1) : 1));
        return book;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}