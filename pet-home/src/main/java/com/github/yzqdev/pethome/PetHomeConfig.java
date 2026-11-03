package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.config.BooleanValue;
import com.github.yzqdev.pethome.config.ConfigBuilder;
import com.github.yzqdev.pethome.config.ConfigSpec;
import com.github.yzqdev.pethome.config.DoubleValue;
import com.github.yzqdev.pethome.config.IntValue;
import com.github.yzqdev.pethome.config.StringListValue;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 全部配置项定义（静态块经 {@code BUILDER.define*} 注册到 {@link ConfigSpec}）+ 静态缓存字段。
 *
 * <p>分组：顶层（捕网）、{@code [friendly_fire]}（友军伤害保护）、{@code [general]}（三态指令、
 * 各物种驯服开关、宠物床/项圈/罗盘/HUD、村庄商店权重）、{@code [loot]}（战利品概率）、 {@code [enchantments]}（每个附魔一个 {@code
 * <name>_enabled} 开关）。
 *
 * <p>配置键与 NeoForge 26.1 侧保持一致（对外契约）。热路径不走 {@code ConfigValue.get()}， 而是读下方 {@code public static}
 * 缓存字段——由 {@link #refreshCachedValues()} 在加载与 配置 GUI 修改后统一刷新。
 */
public class PetHomeConfig {

  public static final ConfigSpec SPEC = new ConfigSpec();
  private static final ConfigBuilder BUILDER = SPEC.builder();

  // ===================== 顶层：生物球（捕捉网） =====================

  public static final BooleanValue MOBCATCHER_ONLY_TAMABLE_ANIMAL;
  public static final StringListValue MOBCATCHER_BLACKLIST;

  // ===================== [friendly_fire] 友好保护 =====================
  public static final BooleanValue PROTECT_PETS_FROM_OWNER;
  public static final BooleanValue PROTECT_PETS_FROM_PETS;
  public static final BooleanValue PROTECT_CHILDREN;
  public static final BooleanValue REFLECT_DAMAGE;
  public static final BooleanValue DISPLAY_HIT_WARNING;
  public static final BooleanValue PROTECT_TEAM_MEMBERS;
  public static final BooleanValue RESPECT_TEAM_RULES;
  public static final StringListValue CAN_HURT_PET_ITEM;
  public static final StringListValue CAN_HURT_ALL_ITEM;
  public static final StringListValue NO_PROTECTION_ENTITY;
  public static final StringListValue OTHER_SHOULD_PROTECT_ENTITY;
  public static final StringListValue PLAYER_CANT_HURT_ENTITY;

  // ===================== [general] 通用功能 =====================
  public static final BooleanValue TRINARY_COMMAND_SYSTEM;
  public static final BooleanValue TAMEABLE_AXOLOTL;
  public static final BooleanValue TAMEABLE_HORSE;
  public static final BooleanValue TAMEABLE_FOX;
  public static final BooleanValue TAMEABLE_RABBIT;
  public static final BooleanValue TAMEABLE_FROG;
  public static final BooleanValue ROTTEN_APPLE;
  public static final BooleanValue PET_BED_RESPAWNS;
  public static final BooleanValue COLLAR_TAGS;
  public static final BooleanValue RABBITS_SCARE_RAVAGERS;
  public static final BooleanValue PET_INFO_OVERLAY;
  public static final BooleanValue PET_INFO_OVERLAY_REQUIRE_SHIFT;
  public static final BooleanValue PET_INFO_OVERLAY_IGNORE_JADE;
  // —— Pet Compass（ enable / showPetInfo / 两个传送完全独立）——
  public static final BooleanValue DEBUG_MODE;
  public static final BooleanValue PET_COMPASS_ENABLE;
  public static final BooleanValue PET_COMPASS_TELEPORT_PLAYER_TO_PET;
  public static final BooleanValue PET_COMPASS_TELEPORT_PET_TO_PLAYER;
  public static final IntValue PETSTORE_VILLAGE_WEIGHT;

  // ===================== [loot] 战利品概率 =====================
  public static final BooleanValue PET_CURSE_ENCHANTMENTS_LOOT_ONLY;
  public static final DoubleValue SINISTER_CARROT_LOOT_CHANCE;
  public static final DoubleValue BUBBLING_LOOT_CHANCE;
  public static final DoubleValue VAMPIRISM_LOOT_CHANCE;
  public static final DoubleValue VOID_CLOUD_LOOT_CHANCE;
  public static final DoubleValue ORE_SCENTING_LOOT_CHANCE;
  public static final DoubleValue BLAZING_PROTECTION_LOOT_CHANCE;
  public static final DoubleValue SHARE_LOOT_CHANCE;
  public static final DoubleValue SONIC_BOOM_LOOT_CHANCE;
  public static final DoubleValue PARALYSIS_LOOT_CHANCE;
  public static final DoubleValue TOUGH_LOOT_CHANCE;
  public static final DoubleValue MUFFLED_LOOT_CHANCE;

  public static final Map<String, BooleanValue> ENABLED_ENCHANTMENTS;

  static {
    MOBCATCHER_ONLY_TAMABLE_ANIMAL =
        BUILDER
            .comment("Mob catcher only catches tamable animal")
            .define("mobcatcherOnlyTamableAnimal", true);
    MOBCATCHER_BLACKLIST =
        BUILDER
            .comment("entities that can't be caught")
            .defineListAllowEmpty(
                "mobcatcherBlacklist",
                List.of("minecraft:painting"),
                PetHomeConfig::validateEntityTypesName);

    BUILDER.push("friendly_fire");
    PROTECT_PETS_FROM_OWNER =
        BUILDER.comment("owner cannot hurt pet").define("protectPetsFromOwner", true);
    PROTECT_PETS_FROM_PETS =
        BUILDER.comment("pet cannot hurt pet").define("protectPetsFromPets", true);
    PROTECT_CHILDREN = BUILDER.comment("protect children animal").define("protectChildren", true);
    REFLECT_DAMAGE = BUILDER.comment("protect pet from owner").define("reflectDamage", false);
    DISPLAY_HIT_WARNING =
        BUILDER.comment("owner cannot hurt pet").define("displayHitWarning", true);
    PROTECT_TEAM_MEMBERS =
        BUILDER.comment("PROTECT_TEAM_MEMBERS").define("protectTeamMembers", true);
    RESPECT_TEAM_RULES = BUILDER.comment("RESPECT_TEAM_RULES").define("respectTeamRules", true);
    CAN_HURT_PET_ITEM =
        BUILDER
            .comment("can hurt pet item")
            .defineListAllowEmpty("can_hurt_pet_item", List.of(), PetHomeConfig::validateItemName);
    CAN_HURT_ALL_ITEM =
        BUILDER
            .comment("can always hurt item")
            .defineListAllowEmpty("can_hurt_all", List.of(), PetHomeConfig::validateItemName);
    NO_PROTECTION_ENTITY =
        BUILDER
            .comment("can always hurt")
            .defineListAllowEmpty(
                "no_protection_entity", List.of(), PetHomeConfig::validateEntityTypesName);
    OTHER_SHOULD_PROTECT_ENTITY =
        BUILDER
            .comment("other entities that can be protected")
            .defineListAllowEmpty(
                "other_should_protect_entity", List.of(), PetHomeConfig::validateEntityTypesName);
    PLAYER_CANT_HURT_ENTITY =
        BUILDER
            .comment("entities player cant hurt")
            .defineListAllowEmpty(
                "player_cant_hurt_entity", List.of(), PetHomeConfig::validateEntityTypesName);
    BUILDER.pop();

    BUILDER.push("general");
    TRINARY_COMMAND_SYSTEM =
        BUILDER
            .comment(
                "true if wolves, cats, parrots, foxes, axolotls, etc can be set to wander, sit or follow")
            .define("trinary_command_system", true);
    TAMEABLE_AXOLOTL =
        BUILDER
            .comment(
                "true if axolotls are fully tameable (axolotl must be tamed with tropical fish)")
            .define("tameable_axolotls", true);
    TAMEABLE_HORSE =
        BUILDER
            .comment("true if horses, donkeys, llamas, etc can be given enchants, beds, etc")
            .define("tameable_horse", true);
    TAMEABLE_FOX =
        BUILDER
            .comment("true if foxes are fully tameable (fox must be tamed via breeding)")
            .define("tameable_fox", true);
    TAMEABLE_RABBIT =
        BUILDER
            .comment("true if rabbits are fully tameable (rabbit must be tamed with carrots)")
            .define("tameable_rabbit", true);
    TAMEABLE_FROG =
        BUILDER
            .comment("true if frogs are fully tameable (frog must be tamed with spider eyes)")
            .define("tameable_frog", true);
    ROTTEN_APPLE =
        BUILDER
            .comment("true if apples can turn into rotten apples if they despawn")
            .define("rotten_apple", true);
    PET_BED_RESPAWNS =
        BUILDER
            .comment("true if mobs can respawn in pet beds the next morning after they die")
            .define("pet_bed_respawns", true);
    COLLAR_TAGS =
        BUILDER
            .comment(
                "true if collar tag functionality are enabled. If this is disabled, there is no way to enchant mobs!")
            .define("collar_tags", true);
    RABBITS_SCARE_RAVAGERS =
        BUILDER
            .comment("true if rabbits scare ravagers like they used to do")
            .define("rabbits_scare_ravagers", true);
    PET_INFO_OVERLAY =
        BUILDER
            .comment(
                "true if aiming the crosshair at your pet and holding shift shows the pet info overlay")
            .define("petInfoOverlay", true);
    PET_INFO_OVERLAY_REQUIRE_SHIFT =
        BUILDER
            .comment(
                "true if the pet info overlay is only shown while holding shift (default: shown whenever the crosshair is on your pet)")
            .define("petInfoOverlayRequireShift", false);
    PET_INFO_OVERLAY_IGNORE_JADE =
        BUILDER
            .comment("true if the pet info overlay is shown even when Jade is installed")
            .define("petInfoOverlayIgnoreJade", false);
    // 调试日志总开关（PHModLogger.debugLog 门控）
    DEBUG_MODE =
        BUILDER
            .comment(
                "enable debug logs for troubleshooting (key systems print diagnostic info when on)")
            .define("debugMode", false);
    // 键名与 26.1（NeoForge/Fabric）侧一致：petCompassEnable/teleportPlayerToPet/teleportPetToPlayer
    PET_COMPASS_ENABLE =
        BUILDER
            .comment("master switch for the pet compass system")
            .define("petCompassEnable", true);
    PET_COMPASS_TELEPORT_PLAYER_TO_PET =
        BUILDER
            .comment("allow teleporting the player to the pet")
            .define("teleportPlayerToPet", true);
    PET_COMPASS_TELEPORT_PET_TO_PLAYER =
        BUILDER
            .comment("allow teleporting (recalling) the pet to the player")
            .define("teleportPetToPlayer", false);
    PETSTORE_VILLAGE_WEIGHT =
        BUILDER
            .comment(
                "the spawn weight of the pet store in villages, set to 0 to disable it entirely")
            .defineInRange("petstore_village_weight", 17, 0, 1000);
    BUILDER.pop();

    BUILDER.push("loot");
    PET_CURSE_ENCHANTMENTS_LOOT_ONLY =
        BUILDER
            .comment(
                "true if pet curse enchantments should only appear in loot, and not the enchanting table.")
            .define("pet_curse_enchantments_loot_only", true);
    SINISTER_CARROT_LOOT_CHANCE =
        BUILDER
            .comment("percent chance of woodland mansion loot table containing sinister carrot:")
            .defineInRange("sinister_carrot_loot_chance", 0.3D, 0.0, 1.0D);
    BUBBLING_LOOT_CHANCE =
        BUILDER
            .comment("percent chance of burried treasure loot table containing Bubbling book:")
            .defineInRange("bubbling_loot_chance", 0.65D, 0.0, 1.0D);
    VAMPIRISM_LOOT_CHANCE =
        BUILDER
            .comment("percent chance of woodland mansion loot table containing Vampire book:")
            .defineInRange("vampirism_loot_chance", 0.22D, 0.0, 1.0D);
    VOID_CLOUD_LOOT_CHANCE =
        BUILDER
            .comment("percent chance of end city loot table containing Void Cloud book:")
            .defineInRange("void_cloud_loot_chance", 0.19D, 0.0, 1.0D);
    ORE_SCENTING_LOOT_CHANCE =
        BUILDER
            .comment("percent chance of mineshaft loot table containing Ore Scenting book:")
            .defineInRange("ore_scenting_loot_chance", 0.15D, 0.0, 1.0D);
    BLAZING_PROTECTION_LOOT_CHANCE =
        BUILDER
            .comment(
                "percent chance of nether fortress loot table containing Blazing Protection book:")
            .defineInRange("blazing_protection_loot_chance", 0.2D, 0.0, 1.0D);
    SHARE_LOOT_CHANCE =
        BUILDER
            .comment("percent chance of ender city loot table containing share book:")
            .defineInRange("share_loot_chance", 0.5D, 0.0, 1.0D);
    SONIC_BOOM_LOOT_CHANCE =
        BUILDER
            .comment("percent chance of ancient city loot table containing Sonic boom book:")
            .defineInRange("sonic_boom_loot_chance", 0.6D, 0.0, 1.0D);
    PARALYSIS_LOOT_CHANCE =
        BUILDER
            .comment("percent chance of chest loot table containing paralysis book:")
            .defineInRange("paralysis_loot_chance", 0.1D, 0.0, 1.0D);
    TOUGH_LOOT_CHANCE =
        BUILDER
            .comment("percent chance of chest loot table containing tough book:")
            .defineInRange("tough_loot_chance", 0.1D, 0.0, 1.0D);
    MUFFLED_LOOT_CHANCE =
        BUILDER
            .comment("percent chance of ancient city loot table containing Muffled book:")
            .defineInRange("muffled_loot_chance", 0.19D, 0.0, 1.0D);
    BUILDER.pop();

    BUILDER.push("enchantments");
    Map<String, BooleanValue> enchantments = new TreeMap<>();
    for (String name : enchantmentNames()) {
      String key = name + "_enabled";
      enchantments.put(
          name,
          BUILDER
              .comment(
                  "true if " + name.replace("_", " ") + " enchant is enabled, false if disabled")
              .define(key, true));
    }
    BUILDER.pop();
    ENABLED_ENCHANTMENTS = Collections.unmodifiableMap(enchantments);
  }

  /**
   * 反射收集 {@link PHEnchantments} 全部 {@code ResourceKey<Enchantment>} 常量名， 为每个附魔生成 {@code
   * <name>_enabled} 配置开关。附魔是数据驱动注册表， 配置静态块构建期注册表尚未就绪，只能反射编译期常量。
   */
  private static Set<String> enchantmentNames() {
    Map<String, Boolean> names = new TreeMap<>();
    for (Field field : PHEnchantments.class.getDeclaredFields()) {
      if (!ResourceKey.class.isAssignableFrom(field.getType())) {
        continue;
      }
      try {
        Object value = field.get(null);
        if (value instanceof ResourceKey<?> key) {
          names.put(key.location().getPath(), Boolean.TRUE);
        }
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("无法读取附魔常量 " + field.getName(), e);
      }
    }
    return names.keySet();
  }

  /**
   * 把各 {@code ConfigValue.get()} 刷进下方静态缓存字段（字符串配置在此解析为 {@code EntityType}/{@code Item} 集合）；{@link
   * #load()} 与配置 GUI 修改后调用。
   */
  public static void refreshCachedValues() {
    mobcatcherBlackList =
        MOBCATCHER_BLACKLIST.get().stream()
            .map(name -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(name)))
            .collect(Collectors.toSet());
    mobcatcherOnlyTamableAnimal = MOBCATCHER_ONLY_TAMABLE_ANIMAL.get();

    protectPetsFromOwner = PROTECT_PETS_FROM_OWNER.get();
    protectPetsFromPets = PROTECT_PETS_FROM_PETS.get();
    protectChildren = PROTECT_CHILDREN.get();
    reflectDamage = REFLECT_DAMAGE.get();
    displayHitWarning = DISPLAY_HIT_WARNING.get();
    protectTeamMembers = PROTECT_TEAM_MEMBERS.get();
    respectTeamRules = RESPECT_TEAM_RULES.get();

    noProtectionEntity =
        NO_PROTECTION_ENTITY.get().stream()
            .map(name -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(name)))
            .collect(Collectors.toSet());
    otherShouldProtectionEntity =
        OTHER_SHOULD_PROTECT_ENTITY.get().stream()
            .map(name -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(name)))
            .collect(Collectors.toSet());
    playerCantHurtEntity =
        PLAYER_CANT_HURT_ENTITY.get().stream()
            .map(name -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(name)))
            .collect(Collectors.toSet());
    canHurtPetItem =
        CAN_HURT_PET_ITEM.get().stream()
            .map(name -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(name)))
            .collect(Collectors.toSet());
    canHurtAllItem =
        CAN_HURT_ALL_ITEM.get().stream()
            .map(name -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(name)))
            .collect(Collectors.toSet());

    trinaryCommandSystem = TRINARY_COMMAND_SYSTEM.get();
    tameableAxolotl = TAMEABLE_AXOLOTL.get();
    tameableHorse = TAMEABLE_HORSE.get();
    tameableFox = TAMEABLE_FOX.get();
    tameableRabbit = TAMEABLE_RABBIT.get();
    tameableFrog = TAMEABLE_FROG.get();
    rottenApple = ROTTEN_APPLE.get();
    petBedRespawns = PET_BED_RESPAWNS.get();
    collarTag = COLLAR_TAGS.get();
    rabbitsScareRavagers = RABBITS_SCARE_RAVAGERS.get();
    petInfoOverlay = PET_INFO_OVERLAY.get();
    petInfoOverlayRequireShift = PET_INFO_OVERLAY_REQUIRE_SHIFT.get();
    petInfoOverlayIgnoreJade = PET_INFO_OVERLAY_IGNORE_JADE.get();
    petCompassEnable = PET_COMPASS_ENABLE.get();
    petCompassTeleportPlayerToPet = PET_COMPASS_TELEPORT_PLAYER_TO_PET.get();
    petCompassTeleportPetToPlayer = PET_COMPASS_TELEPORT_PET_TO_PLAYER.get();
    petstoreVillageWeight = PETSTORE_VILLAGE_WEIGHT.get();

    petCurseEnchantmentsLootOnly = PET_CURSE_ENCHANTMENTS_LOOT_ONLY.get();
    sinisterCarrotLootChance = SINISTER_CARROT_LOOT_CHANCE.get();
    bubblingLootChance = BUBBLING_LOOT_CHANCE.get();
    vampirismLootChance = VAMPIRISM_LOOT_CHANCE.get();
    voidCloudLootChance = VOID_CLOUD_LOOT_CHANCE.get();
    oreScentingLootChance = ORE_SCENTING_LOOT_CHANCE.get();
    blazingProtectionLootChance = BLAZING_PROTECTION_LOOT_CHANCE.get();
    shareLootChance = SHARE_LOOT_CHANCE.get();
    sonicBoomLootChance = SONIC_BOOM_LOOT_CHANCE.get();
    paralysisLootChance = PARALYSIS_LOOT_CHANCE.get();
    toughLootChance = TOUGH_LOOT_CHANCE.get();
    muffledLootChance = MUFFLED_LOOT_CHANCE.get();
    debugMode = DEBUG_MODE.get();

    // 改造物种驯服开关缓存进 TameableUtils 的门控表，配置刷新后 isTamed 立即按新开关生效
    TameableUtils.refreshSpeciesGates();
  }

  /** 读取 {@code config/pet_home.toml}（失败保默认值）并刷新静态缓存；模组初始化时调用。 */
  public static void load() {
    SPEC.load();
    refreshCachedValues();
  }

  /** 落盘（仅 dirty 时写入）；服务端 SERVER_STOPPING / 客户端 CLIENT_STOPPING 与配置 GUI 关闭时调用。 */
  public static void save() {
    SPEC.save();
  }

  public static void onLoad() {
    load();
  }

  private static boolean validateEntityTypesName(Object obj) {
    return obj instanceof String itemName
        && BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(itemName));
  }

  private static boolean validateItemName(final Object obj) {
    return obj instanceof String itemName
        && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
  }

  /**
   * 附魔开关查询：未登记的附魔名视为启用；非 {@code pet_home} 命名空间直接放行。消费点为 {@code TameableUtils.getEnchantLevel}（禁用即 0
   * 级）。
   */
  public static boolean isEnchantEnabled(String enchantment) {
    BooleanValue value = ENABLED_ENCHANTMENTS.get(enchantment);
    return value == null || value.get();
  }

  /** 按 {@link ResourceLocation} 查询附魔开关（见 {@link #isEnchantEnabled(String)}）。 */
  public static boolean isEnchantEnabled(ResourceLocation enchantment) {
    return !enchantment.getNamespace().equals(PetHomeMod.MODID)
        || isEnchantEnabled(enchantment.getPath());
  }

  /** 按 {@link ResourceKey} 查询附魔开关（见 {@link #isEnchantEnabled(String)}）。 */
  public static boolean isEnchantEnabled(ResourceKey<Enchantment> enchantment) {
    return isEnchantEnabled(enchantment.location());
  }

  // ===================== 静态缓存（供热路径直接读取，避免频繁装箱） =====================
  public static boolean mobcatcherOnlyTamableAnimal = true;
  public static Set<EntityType<?>> mobcatcherBlackList = Set.of(EntityType.PAINTING);

  public static boolean protectPetsFromOwner = true;
  public static boolean protectPetsFromPets = true;
  public static boolean protectChildren = true;
  public static boolean reflectDamage;
  public static boolean displayHitWarning = true;
  public static boolean protectTeamMembers = true;
  public static boolean respectTeamRules = true;
  public static Set<EntityType<?>> noProtectionEntity = Set.of(EntityType.ZOMBIE);
  public static Set<EntityType<?>> otherShouldProtectionEntity = Set.of();
  public static Set<EntityType<?>> playerCantHurtEntity = Set.of();
  public static Set<Item> canHurtPetItem = Set.of();
  public static Set<Item> canHurtAllItem = Set.of();

  public static boolean trinaryCommandSystem = true;
  public static boolean tameableAxolotl = true;
  public static boolean tameableHorse = true;
  public static boolean tameableFox = true;
  public static boolean tameableRabbit = true;
  public static boolean tameableFrog = true;
  public static boolean rottenApple = true;
  public static boolean petBedRespawns = true;
  public static boolean collarTag = true;
  public static boolean rabbitsScareRavagers = true;
  public static boolean petInfoOverlay = true;
  public static boolean petInfoOverlayRequireShift = false;
  public static boolean petInfoOverlayIgnoreJade = false;
  public static boolean petCompassEnable = true;
  public static boolean petCompassTeleportPlayerToPet = true;
  public static boolean petCompassTeleportPetToPlayer = true;
  public static int petstoreVillageWeight = 17;

  public static boolean petCurseEnchantmentsLootOnly = true;
  public static double sinisterCarrotLootChance = 0.3D;
  public static double bubblingLootChance = 0.65D;
  public static double vampirismLootChance = 0.22D;
  public static double voidCloudLootChance = 0.19D;
  public static double oreScentingLootChance = 0.15D;
  public static double blazingProtectionLootChance = 0.2D;
  public static double shareLootChance = 0.5D;
  public static double sonicBoomLootChance = 0.6D;
  public static double paralysisLootChance = 0.1D;
  public static double toughLootChance = 0.1D;
  public static double muffledLootChance = 0.19D;
  public static boolean debugMode = false;
}
