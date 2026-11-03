package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.PHPOIRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;

/**
 * 村民职业注册表："Animal Tamer"（驯兽师）——村庄宠物商店配套的职业。
 *
 * <p>工作站点为宠物床兴趣点（{@code PHPOIRegistry.PET_BED}），即靠近宠物床的村民可转职为驯兽师； 工作音效复用宠物床使用音效。
 */
public class PHVillagerRegistry {

  /** 驯兽师职业（工作站点：宠物床兴趣点）。 */
  public static final VillagerProfession ANIMAL_TAMER =
      Registry.register(
          BuiltInRegistries.VILLAGER_PROFESSION,
          ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "animal_tamer"),
          buildVillagerProfession());

  /** 结构注入完成标记；当前代码库无读写引用（遗留字段）。 */
  public static boolean registeredHouses = false;

  /** 构建驯兽师职业：持有与获取工作站点的判定均指向宠物床 POI，无副职业物品集。 */
  private static VillagerProfession buildVillagerProfession() {
    Predicate<Holder<PoiType>> heldJobSite =
        (poiType) -> {
          return poiType.is(PHPOIRegistry.PET_BED.key());
        };
    Predicate<Holder<PoiType>> acquirableJobSite =
        (poiType) -> {
          return poiType.is(PHPOIRegistry.PET_BED.key());
        };
    return new VillagerProfession(
        "animal_tamer",
        heldJobSite,
        acquirableJobSite,
        ImmutableSet.of(),
        ImmutableSet.of(),
        PHSoundRegistry.PET_BED_USE);
  }

  /** 空实现：由主入口调用以触发类加载、执行上方静态注册。 */
  public static void init() {}
}
