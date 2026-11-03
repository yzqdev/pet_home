package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.misc.PHPOIRegistry;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;

/** POI tag datagen：把宠物床 POI 挂进 {@code minecraft:acquirable_job_site}——驯兽师村民靠它认领宠物床为工作站点。 */
public class PHPoiTagProvider extends FabricTagProvider<PoiType> {

  public PHPoiTagProvider(
      FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, Registries.POINT_OF_INTEREST_TYPE, registries);
  }

  @Override
  protected void addTags(HolderLookup.Provider provider) {
    getOrCreateTagBuilder(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(PHPOIRegistry.PET_BED.key());
  }

  @Override
  public String getName() {
    return "mod poi tags";
  }
}
