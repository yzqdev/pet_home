package com.github.yzqdev.pet_home.datagen;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModEntityTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, PetHomeMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        //For the plains & taiga pet store cage
        this.tag(ModTags.petstore_cage_0).add(EntityType.WOLF).add(EntityType.CAT).add(EntityType.RABBIT);
//        For the desert pet store cage
        this.tag(ModTags.petstore_cage_1).add(EntityType.FROG).add(EntityType.RABBIT);
//        For the snowy pet store cage
        this.tag(ModTags.petstore_cage_2).add(EntityType.FOX).add(EntityType.RABBIT);
        //For the savanna pet store cage
        this.tag(ModTags.petstore_cage_3).add(EntityType.FROG).add(EntityType.PARROT);
        //For the plain pet store fish tank
        this.tag(ModTags.petstore_fishtank).add(EntityType.TROPICAL_FISH);

        tag(ModTags.infamy_target_attracted).add(EntityType.DROWNED)
                .add(EntityType.HUSK).add(EntityType.ZOMBIE_VILLAGER).add(EntityType.ZOMBIE)
                .add(EntityType.VEX).add(EntityType.SPIDER).add(EntityType.SLIME).add(EntityType.GHAST)
                .add(EntityType.CAVE_SPIDER).add(EntityType.BLAZE).add(EntityType.MAGMA_CUBE)
                .add(EntityType.WITHER).add(EntityType.ENDERMITE).add(EntityType.SHULKER)
                .add(EntityType.PHANTOM).add(EntityType.RAVAGER).addTag(EntityTypeTags.SKELETONS)
                .addTag(EntityTypeTags.RAIDERS)
        ;
    }
}
