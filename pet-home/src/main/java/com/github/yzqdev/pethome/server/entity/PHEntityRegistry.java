package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * 实体类型注册表：链锁闪电、巨型气泡、念力墙、矿石高亮框、羽毛、召回球与捕网投掷物。
 *
 * <p>注册走 Fabric 原生 {@code Registry.register}，均声明为 MobCategory.MISC 的特效类实体并免疫火焰； {@code init()}
 * 由主入口调用以触发类加载、执行静态注册。
 */
public class PHEntityRegistry {

  /** 拼接 {@code pet_home:<name>} 资源路径。 */
  private static ResourceLocation id(String name) {
    return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, name);
  }

  /** 链锁闪电特效实体（连锁闪电附魔）。 */
  public static final EntityType<ChainLightningEntity> CHAIN_LIGHTNING =
      Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          id("chain_lightning"),
          build(
              EntityType.Builder.of(ChainLightningEntity::new, MobCategory.MISC)
                  .sized(0.5F, 0.5F)
                  .fireImmune(),
              "chain_lightning"));

  /** 巨型泡泡实体（泡泡控制附魔的载体）。 */
  public static final EntityType<GiantBubbleEntity> GIANT_BUBBLE =
      Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          id("giant_bubble"),
          build(
              EntityType.Builder.of(GiantBubbleEntity::new, MobCategory.MISC)
                  .sized(1.2F, 1.8F)
                  .fireImmune(),
              "giant_bubble"));

  /** 念力墙实体（念力墙附魔的载体）。 */
  public static final EntityType<PsychicWallEntity> PSYCHIC_WALL =
      Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          id("psychic_wall"),
          build(
              EntityType.Builder.of(PsychicWallEntity::new, MobCategory.MISC)
                  .sized(1F, 1F)
                  .fireImmune(),
              "psychic_wall"));

  /** 矿石高亮框实体（矿脉嗅探附魔的标记特效）。 */
  public static final EntityType<HighlightedBlockEntity> HIGHLIGHTED_BLOCK =
      Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          id("highlighted_block"),
          build(
              EntityType.Builder.of(HighlightedBlockEntity::new, MobCategory.MISC)
                  .sized(1.0F, 1.0F)
                  .fireImmune(),
              "highlighted_block"));

  /** 羽毛钓竿投掷物（落地吸引宠物聚拢）。 */
  public static final EntityType<FeatherEntity> FEATHER =
      Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          id("feather"),
          build(
              EntityType.Builder.<FeatherEntity>of(FeatherEntity::new, MobCategory.MISC)
                  .sized(0.2F, 0.2F)
                  .fireImmune(),
              "feather"));

  /** 往生球实体（紧急收回附魔的载体，收纳宠物后由主人打开释放）。 */
  public static final EntityType<RecallBallEntity> RECALL_BALL =
      Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          id("recall_ball"),
          build(
              EntityType.Builder.of(RecallBallEntity::new, MobCategory.MISC)
                  .sized(0.8F, 0.8F)
                  .fireImmune(),
              "recall_ball"));

  /** 捕网投掷物（捕网发射器的弹射物，客户端追踪距离加大到 128）。 */
  public static final EntityType<NetEntity> NET_ENTITY =
      Registry.register(
          BuiltInRegistries.ENTITY_TYPE,
          id("net_entity"),
          EntityType.Builder.<NetEntity>of(NetEntity::new, MobCategory.MISC)
              .clientTrackingRange(128)
              .updateInterval(1)
              .sized(.6f, .6f)
              .build("net"));

  /** 统一构建入口（当前仅透传 {@code builder.build}）。 */
  private static <T extends Entity> EntityType<T> build(
      EntityType.Builder<T> builder, String entityName) {
    return builder.build(entityName);
  }

  /** 空实现：由主入口调用以触发类加载、执行上方静态注册。 */
  public static void init() {}
}
