package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;


public class ModEntityTagsProvider extends FabricTagsProvider.EntityTypeTagsProvider {

    public ModEntityTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        valueLookupBuilder(ModTags.blacklisted).add(EntityType.PAINTING);
        //For the plains & taiga pet store cage
        valueLookupBuilder(ModTags.petstore_cage_0).add(EntityType.WOLF).add(EntityType.CAT).add(EntityType.RABBIT);
//        For the desert pet store cage
        valueLookupBuilder(ModTags.petstore_cage_1).add(EntityType.FROG).add(EntityType.RABBIT);
//        For the snowy pet store cage
        valueLookupBuilder(ModTags.petstore_cage_2).add(EntityType.FOX).add(EntityType.RABBIT);
        //For the savanna pet store cage
        valueLookupBuilder(ModTags.petstore_cage_3).add(EntityType.FROG).add(EntityType.PARROT);
        //For the plain pet store fish tank
        valueLookupBuilder(ModTags.petstore_fishtank).add(EntityType.TROPICAL_FISH);

        var infamy = valueLookupBuilder(ModTags.infamy_target_attracted);
        infamy.add(EntityType.DROWNED)
                .add(EntityType.HUSK).add(EntityType.ZOMBIE_VILLAGER).add(EntityType.ZOMBIE)
                .add(EntityType.VEX).add(EntityType.SPIDER).add(EntityType.SLIME).add(EntityType.GHAST)
                .add(EntityType.CAVE_SPIDER).add(EntityType.BLAZE).add(EntityType.MAGMA_CUBE)
                .add(EntityType.WITHER).add(EntityType.ENDERMITE).add(EntityType.SHULKER)
                .add(EntityType.PHANTOM).add(EntityType.RAVAGER);
        // 跨命名空间引用的原版标签：原版 provider 会要求被引用的标签在本次生成中出现，
        // 否则直接以 "missing following references" 失败（NeoForge 侧放宽了该校验，Fabric 侧不会）。
        // 因此这里用 addOptionalTag —— 语义上等价（原版这两个标签一定存在，required:false 只是不再强制校验），
        // 生成结果与 NeoForge 的差别仅在于该条引用多写一个 "required": false。
        infamy.addOptionalTag(EntityTypeTags.SKELETONS).addOptionalTag(EntityTypeTags.RAIDERS);
    }

    @Override
    public String getName() {
        return "mod entity tags - " + PetHomeMod.MODID;
    }
}
