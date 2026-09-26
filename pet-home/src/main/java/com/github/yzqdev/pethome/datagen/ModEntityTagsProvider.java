package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;


public class ModEntityTagsProvider extends FabricTagProvider<EntityType<?>> {

    public ModEntityTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ENTITY_TYPE, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        //For the plains & taiga pet store cage
        getOrCreateTagBuilder(ModTags.petstore_cage_0).add(EntityType.WOLF).add(EntityType.CAT).add(EntityType.RABBIT);
        //        For the desert pet store cage
        getOrCreateTagBuilder(ModTags.petstore_cage_1).add(EntityType.FROG).add(EntityType.RABBIT);
        //        For the snowy pet store cage
        getOrCreateTagBuilder(ModTags.petstore_cage_2).add(EntityType.FOX).add(EntityType.RABBIT);
        //For the savanna pet store cage
        getOrCreateTagBuilder(ModTags.petstore_cage_3).add(EntityType.FROG).add(EntityType.PARROT);
        //For the plain pet store fish tank
        getOrCreateTagBuilder(ModTags.petstore_fishtank).add(EntityType.TROPICAL_FISH);

        getOrCreateTagBuilder(ModTags.infamy_target_attracted).add(EntityType.DROWNED)
                .add(EntityType.HUSK).add(EntityType.ZOMBIE_VILLAGER).add(EntityType.ZOMBIE)
                .add(EntityType.VEX).add(EntityType.SPIDER).add(EntityType.SLIME).add(EntityType.GHAST)
                .add(EntityType.CAVE_SPIDER).add(EntityType.BLAZE).add(EntityType.MAGMA_CUBE)
                .add(EntityType.WITHER).add(EntityType.ENDERMITE).add(EntityType.SHULKER)
                .add(EntityType.PHANTOM).add(EntityType.RAVAGER).addOptionalTag(EntityTypeTags.SKELETONS)
                .addOptionalTag(EntityTypeTags.RAIDERS);
    }

    @Override
    public String getName() {
        return PetHomeMod.MODID + " entity tags";
    }
}
