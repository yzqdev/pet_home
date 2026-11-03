package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

/**
 * 实体 tag datagen：宠物商店各群系笼子/水族箱的展示生物、捕网黑名单（画上）与 {@code infamy_target_attracted}（恶名附魔吸引的怪物集合：不死者系 +
 * 骷髅/袭击者可选 tag）。
 */
public class PHEntityTagsProvider extends FabricTagProvider.EntityTypeTagProvider {

  public PHEntityTagsProvider(
      FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, registries);
  }

  @Override
  protected void addTags(HolderLookup.Provider provider) {
    getOrCreateTagBuilder(PHTags.blacklisted).add(EntityType.PAINTING);
    // For the plains & taiga pet store cage
    getOrCreateTagBuilder(PHTags.petstore_cage_0)
        .add(EntityType.WOLF)
        .add(EntityType.CAT)
        .add(EntityType.RABBIT);
    //        For the desert pet store cage
    getOrCreateTagBuilder(PHTags.petstore_cage_1).add(EntityType.FROG).add(EntityType.RABBIT);
    //        For the snowy pet store cage
    getOrCreateTagBuilder(PHTags.petstore_cage_2).add(EntityType.FOX).add(EntityType.RABBIT);
    // For the savanna pet store cage
    getOrCreateTagBuilder(PHTags.petstore_cage_3).add(EntityType.FROG).add(EntityType.PARROT);
    // For the plain pet store fish tank
    getOrCreateTagBuilder(PHTags.petstore_fishtank).add(EntityType.TROPICAL_FISH);

    getOrCreateTagBuilder(PHTags.infamy_target_attracted)
        .add(EntityType.DROWNED)
        .add(EntityType.HUSK)
        .add(EntityType.ZOMBIE_VILLAGER)
        .add(EntityType.ZOMBIE)
        .add(EntityType.VEX)
        .add(EntityType.SPIDER)
        .add(EntityType.SLIME)
        .add(EntityType.GHAST)
        .add(EntityType.CAVE_SPIDER)
        .add(EntityType.BLAZE)
        .add(EntityType.MAGMA_CUBE)
        .add(EntityType.WITHER)
        .add(EntityType.ENDERMITE)
        .add(EntityType.SHULKER)
        .add(EntityType.PHANTOM)
        .add(EntityType.RAVAGER)
        .addOptionalTag(EntityTypeTags.SKELETONS)
        .addOptionalTag(EntityTypeTags.RAIDERS);
  }

  @Override
  public String getName() {
    return "mod entity tags: " + PetHomeMod.MODID;
  }
}
