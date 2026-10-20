package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
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
        tag(ModTags.blacklisted).add(EntityType.PAINTING);
        // 平原与针叶林宠物商店笼子的刷怪笼蛋
        this.tag(ModTags.petstore_cage_0).add(EntityType.WOLF).add(EntityType.CAT).add(EntityType.RABBIT);
// 沙漠宠物商店笼子的刷怪笼蛋（当前未启用）
        this.tag(ModTags.petstore_cage_1).add(EntityType.FROG).add(EntityType.RABBIT);
// 雪原宠物商店笼子的刷怪笼蛋（当前未启用）
        this.tag(ModTags.petstore_cage_2).add(EntityType.FOX).add(EntityType.RABBIT);
        // 热带草原宠物商店笼子的刷怪笼蛋
        this.tag(ModTags.petstore_cage_3).add(EntityType.FROG).add(EntityType.PARROT);
        // 平原宠物商店鱼缸的刷怪笼蛋
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
