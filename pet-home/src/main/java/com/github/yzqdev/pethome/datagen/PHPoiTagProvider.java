package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.PHPOIRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.github.yzqdev.pethome.server.misc.PHPOIRegistry.getBeds;

/**
 * POI 标签数据生成：把宠物床 POI 归入“可获取的工作点”，让村民能认领成
 * 驯兽师；另附宠物床 POI 的 bootstrap 注册（供动态注册表生成调用）。
 */
public class PHPoiTagProvider extends PoiTypeTagsProvider {
    public PHPoiTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(PHPOIRegistry.PET_BED.key());
    }

    public static void bootstrap(BootstrapContext<PoiType> bootstrap) {
        bootstrap.register(PHPOIRegistry.PET_BED.key(), new PoiType(getBeds(), 1, 1));
    }
}
