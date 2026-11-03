package com.github.yzqdev.pethome.datagen.loot;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * 战利品表 key 常量。chest 表内容见 {@link PHLootProvider}（Fabric 走
 * SimpleFabricLootTableSubProvider）；26.3 的 LootTableSubProvider 已改为 Context + run() 形态。
 */
public class LootTableGen {
  /** 宠物商店箱子战利品表（{@code pet_home:chests/petshop_chest}）。 */
  public static final ResourceKey<LootTable> PET_LOOT_TABLE =
      ResourceKey.create(
          Registries.LOOT_TABLE,
          Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "chests/petshop_chest"));
  /** "宠物保护"附魔书战利品表（Tough；{@code pet_home:chests/pet_protection_loot}）。 */
  public static final ResourceKey<LootTable> PET_PROTECTION_TABLE =
      ResourceKey.create(
          Registries.LOOT_TABLE,
          Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "chests/pet_protection_loot"));
}
