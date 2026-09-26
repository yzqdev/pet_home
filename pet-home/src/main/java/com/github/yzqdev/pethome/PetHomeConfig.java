package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.config.BooleanValue;
import com.github.yzqdev.pethome.config.ConfigBuilder;
import com.github.yzqdev.pethome.config.ConfigSpec;
import com.github.yzqdev.pethome.config.DoubleValue;
import com.github.yzqdev.pethome.config.IntValue;
import com.github.yzqdev.pethome.config.StringListValue;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.enchantment.PetEnchantment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class PetHomeConfig {


    public static final ConfigSpec SPEC = new ConfigSpec();
    private static final ConfigBuilder BUILDER = SPEC.builder();

    // ===================== friendly_fire（静态，供其它类直接读取） =====================
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


    public static final BooleanValue MOBCATCHER_ONLY_TAMABLE_ANIMAL;
    public static final StringListValue MOBCATCHER_BLACKLIST;

    static {
        MOBCATCHER_ONLY_TAMABLE_ANIMAL = BUILDER
                .comment("Mob catcher only catches tamable animal")
                .define("mobcatcherOnlyTamableAnimal", true);
        MOBCATCHER_BLACKLIST = BUILDER
                .comment("entities that can't be caught")
                .defineListAllowEmpty("mobcatcherBlacklist", List.of("minecraft:painting"), PetHomeConfig::validateEntityTypesName);

        BUILDER.push("friendly_fire");
        PROTECT_PETS_FROM_OWNER = BUILDER
                .comment("owner cannot hurt pet")
                .define("protectPetsFromOwner", true);
        PROTECT_PETS_FROM_PETS = BUILDER
                .comment("pet cannot hurt pet")
                .define("protectPetsFromPets", true);
        PROTECT_CHILDREN = BUILDER
                .comment("protect children animal")
                .define("protectChildren", true);
        REFLECT_DAMAGE = BUILDER
                .comment("protect pet from owner")
                .define("reflectDamage", false);
        DISPLAY_HIT_WARNING = BUILDER
                .comment("owner cannot hurt pet")
                .define("displayHitWarning", true);
        PROTECT_TEAM_MEMBERS = BUILDER
                .comment("PROTECT_TEAM_MEMBERS")
                .define("protectTeamMembers", true);
        RESPECT_TEAM_RULES = BUILDER
                .comment("RESPECT_TEAM_RULES")
                .define("respectTeamRules", true);
        CAN_HURT_PET_ITEM = BUILDER
                .comment("can hurt pet item")
                .defineListAllowEmpty("can_hurt_pet_item", List.of(), PetHomeConfig::validateItemName);
        CAN_HURT_ALL_ITEM = BUILDER
                .comment("can always hurt item")
                .defineListAllowEmpty("can_hurt_all", List.of(), PetHomeConfig::validateItemName);
        NO_PROTECTION_ENTITY = BUILDER
                .comment("can always hurt")
                .defineListAllowEmpty("no_protection_entity", List.of(), PetHomeConfig::validateEntityTypesName);
        OTHER_SHOULD_PROTECT_ENTITY = BUILDER
                .comment("other entities that can be protected")
                .defineListAllowEmpty("other_should_protect_entity", List.of(), PetHomeConfig::validateEntityTypesName);
        PLAYER_CANT_HURT_ENTITY = BUILDER
                .comment("entities player cant hurt")
                .defineListAllowEmpty("player_cant_hurt_entity", List.of(), PetHomeConfig::validateEntityTypesName);
        BUILDER.pop();
    }

    // ===================== general / loot / enchantments（实例字段） =====================
    public final BooleanValue trinaryCommandSystem;
    public final BooleanValue tameableAxolotl;
    public final BooleanValue tameableHorse;
    public final BooleanValue tameableFox;
    public final BooleanValue tameableRabbit;
    public final BooleanValue tameableFrog;
    public final BooleanValue swingThroughPets;
    public final BooleanValue rottenApple;
    public final BooleanValue petBedRespawns;
    public final BooleanValue collarTag;
    public final BooleanValue rabbitsScareRavagers;
    public final BooleanValue petInfoOverlay;
    public final BooleanValue petInfoOverlayRequireShift;
    public final BooleanValue petInfoOverlayIgnoreJade;
    public final BooleanValue animalTamerVillager;
    public final IntValue petstoreVillageWeight;
    public final BooleanValue petCompassEnable;
    public final BooleanValue petCompassTeleportPlayerToPet;
    public final BooleanValue petCompassTeleportPetToPlayer;

    public final BooleanValue petCurseEnchantmentsLootOnly;
    public final DoubleValue sinisterCarrotLootChance;
    public final DoubleValue bubblingLootChance;
    public final DoubleValue vampirismLootChance;
    public final DoubleValue voidCloudLootChance;
    public final DoubleValue oreScentingLootChance;
    public final DoubleValue muffledLootChance;
    public final DoubleValue blazingProtectionLootChance;
    public final DoubleValue shareLootChance;
    public final DoubleValue sonicBoomLootChance;
    public final DoubleValue paralysisLootChance;
    public final DoubleValue toughLootChance;

    /** 每个宠物附魔的启用开关，键为附魔注册名 */
    public final Map<String, BooleanValue> enabledEnchantments = new LinkedHashMap<>();

    public PetHomeConfig() {
        BUILDER.push("general");
        trinaryCommandSystem = BUILDER.comment("true if wolves, cats, parrots, foxes, axolotls, etc can be set to wander, sit or follow").translation("trinary_command_system").define("trinary_command_system", true);
        tameableAxolotl = BUILDER.comment("true if axolotls are fully tameable (axolotl must be tamed with tropical fish)").translation("tameable_axolotls").define("tameable_axolotls", true);
        tameableHorse = BUILDER.comment("true if horses, donkeys, llamas, etc can be given enchants, beds, etc").translation("tameable_horse").define("tameable_horse", true);
        tameableFox = BUILDER.comment("true if foxes are fully tameable (fox must be tamed via breeding)").translation("tameable_fox").define("tameable_fox", true);
        tameableRabbit = BUILDER.comment("true if rabbits are fully tameable (rabbit must be tamed with carrots)").translation("tameable_rabbit").define("tameable_rabbit", true);
        tameableFrog = BUILDER.comment("true if frogs are fully tameable (frog must be tamed with spider eyes)").translation("tameable_frog").define("tameable_frog", true);
        swingThroughPets = BUILDER.comment("true if attacks do not register on pets from their owners and go through them to attack a mob behind them").translation("swing_through_pets").define("swing_through_pets", true);
        rottenApple = BUILDER.comment("true if apples can turn into rotten apples if they despawn").translation("rotten_apple").define("rotten_apple", true);
        petBedRespawns = BUILDER.comment("true if mobs can respawn in pet beds the next morning after they die").translation("pet_bed_respawns").define("pet_bed_respawns", true);
        collarTag = BUILDER.comment("true if collar tag functionality are enabled. If this is disabled, there is no way to enchant mobs!").translation("collar_tags").define("collar_tags", true);
        rabbitsScareRavagers = BUILDER.comment("true if rabbits scare ravagers like they used to do").translation("rabbits_scare_ravagers").define("rabbits_scare_ravagers", true);
        petInfoOverlay = BUILDER.comment("true if aiming the crosshair at your pet and holding shift shows the pet info overlay").translation("pet_info_overlay").define("pet_info_overlay", true);
        petInfoOverlayRequireShift = BUILDER.comment("true if the pet info overlay is only shown while holding shift (default: shown whenever the crosshair is on your pet)").translation("pet_info_overlay_require_shift").define("pet_info_overlay_require_shift", false);
        petInfoOverlayIgnoreJade = BUILDER.comment("true if the pet info overlay is shown even when Jade is installed").translation("pet_info_overlay_ignore_jade").define("pet_info_overlay_ignore_jade", false);
        animalTamerVillager = BUILDER.comment("true if animal tamer villagers are enabled. Their work station is a pet bed").translation("animal_tamer_villager").define("animal_tamer_villager", true);
        petstoreVillageWeight = BUILDER.comment("the spawn weight of the pet store in villages, set to 0 to disable it entirely").translation("petstore_village_weight").defineInRange("petstore_village_weight", 17, 0, 1000);
        petCompassEnable = BUILDER.comment("master switch for the pet compass system").translation("pet_compass_enable").define("pet_compass_enable", true);
        petCompassTeleportPlayerToPet = BUILDER.comment("allow teleporting the player to the pet").translation("teleport_player_to_pet").define("teleport_player_to_pet", true);
        petCompassTeleportPetToPlayer = BUILDER.comment("allow teleporting (recalling) the pet to the player").translation("teleport_pet_to_player").define("teleport_pet_to_player", true);
        BUILDER.pop();

        BUILDER.push("loot");
        petCurseEnchantmentsLootOnly = BUILDER.comment("true if pet curse enchantments should only appear in loot, and not the enchanting table.").translation("pet_curse_enchantments_loot_only").define("pet_curse_enchantments_loot_only", true);
        sinisterCarrotLootChance = BUILDER.comment("percent chance of woodland mansion loot table containing sinister carrot:").translation("sinister_carrot_loot_chance").defineInRange("sinister_carrot_loot_chance", 0.3D, 0.0, 1.0D);
        bubblingLootChance = BUILDER.comment("percent chance of burried treasure loot table containing Bubbling book:").translation("bubbling_loot_chance").defineInRange("bubbling_loot_chance", 0.65D, 0.0, 1.0D);
        vampirismLootChance = BUILDER.comment("percent chance of woodland mansion loot table containing Vampire book:").translation("vampirism_loot_chance").defineInRange("vampirism_loot_chance", 0.22D, 0.0, 1.0D);
        voidCloudLootChance = BUILDER.comment("percent chance of end city loot table containing Void Cloud book:").translation("void_cloud_loot_chance").defineInRange("void_cloud_loot_chance", 0.19D, 0.0, 1.0D);
        oreScentingLootChance = BUILDER.comment("percent chance of mineshaft loot table containing Ore Scenting book:").translation("ore_scenting_loot_chance").defineInRange("ore_scenting_loot_chance", 0.15D, 0.0, 1.0D);
        muffledLootChance = BUILDER.comment("percent chance of ancient city loot table containing Muffled book:").translation("muffled_loot_chance").defineInRange("muffled_loot_chance", 0.19D, 0.0, 1.0D);
        blazingProtectionLootChance = BUILDER.comment("percent chance of nether fortress loot table containing Blazing Protection book:").translation("blazing_protection_loot_chance").defineInRange("blazing_protection_loot_chance", 0.2D, 0.0, 1.0D);
        // 以下 4 个自 1.21 移植
        shareLootChance = BUILDER.comment("percent chance of ender city loot table containing share book:").translation("share_loot_chance").defineInRange("share_loot_chance", 0.5D, 0.0, 1.0D);
        sonicBoomLootChance = BUILDER.comment("percent chance of woodland mansion loot table containing Sonic boom book:").translation("sonic_boom_loot_chance").defineInRange("sonic_boom_loot_chance", 0.6D, 0.0, 1.0D);
        paralysisLootChance = BUILDER.comment("percent chance of chest loot table containing paralysis book:").translation("paralysis_loot_chance").defineInRange("paralysis_loot_chance", 0.1D, 0.0, 1.0D);
        toughLootChance = BUILDER.comment("percent chance of chest loot table containing tough book:").translation("tough_loot_chance").defineInRange("tough_loot_chance", 0.1D, 0.0, 1.0D);
        BUILDER.pop();

        BUILDER.push("enchantments");
        try {
            for (Field field : DIEnchantmentRegistry.class.getDeclaredFields()) {
                Object value = field.get(null);
                if (value instanceof PetEnchantment enchantment) {
                    String registryName = enchantment.getName();
                    String name = registryName + "_enabled";
                    enabledEnchantments.put(registryName, BUILDER
                            .comment("true if " + registryName.replace("_", " ") + " enchant is enabled, false if disabled")
                            .translation(name)
                            .define(name, true));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        BUILDER.pop();
    }

    /**
     * 加载配置文件并刷新静态缓存。由 {@link PetHomeMod#onInitialize()} 调用。
     */
    public static void load() {
        SPEC.load();
        refreshCachedValues();
    }

    public static void save() {
        SPEC.save();
    }

    public static void onLoad() {
        load();
    }

    private static boolean validateEntityTypesName(Object obj) {
        return obj instanceof String name && BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(name));
    }

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String name && BuiltInRegistries.ITEM.containsKey(new ResourceLocation(name));
    }

    public boolean isEnchantEnabled(Enchantment enchantment) {
        return enchantment instanceof PetEnchantment && isEnchantEnabled(((PetEnchantment) enchantment).getName());
    }

    public boolean isEnchantEnabled(String enchantment) {
        BooleanValue entry = enabledEnchantments.get(enchantment);
        return entry == null || entry.get();
    }

    // ===================== 静态缓存（供热路径直接读取，避免频繁装箱） =====================
    public static boolean mobcatcherOnlyTamableAnimal;
    public static Set<EntityType<?>> mobcatcherBlackList;
    public static boolean protectPetsFromPets = true;

    public static boolean protectChildren = true;

    public static boolean reflectDamage;

    public static boolean displayHitWarning;
    public static boolean protectPetsFromOwner = true;
    public static boolean protectTeamMembers = true;
    public static boolean respectTeamRules = true;
    public static Set<EntityType<?>> noProtectionEntity;
    public static Set<EntityType<?>> otherShouldProtectionEntity;
    public static Set<EntityType<?>> playerCantHurtEntity;
    public static Set<Item> canHurtPetItem;
    public static Set<Item> canHurtAllItem;

    /**
     * 把当前配置值刷新到静态缓存字段。
     * 设置界面修改配置后不会触发配置加载事件，需要主动调用本方法让新值立即生效。
     */
    public static void refreshCachedValues() {
        mobcatcherBlackList = MOBCATCHER_BLACKLIST.get().stream()
                .map(name -> BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(name)))
                .collect(Collectors.toSet());
        mobcatcherOnlyTamableAnimal = MOBCATCHER_ONLY_TAMABLE_ANIMAL.get();
        respectTeamRules = RESPECT_TEAM_RULES.get();
        protectPetsFromOwner = PROTECT_PETS_FROM_OWNER.get();
        protectChildren = PROTECT_CHILDREN.get();
        protectPetsFromPets = PROTECT_PETS_FROM_PETS.get();
        reflectDamage = REFLECT_DAMAGE.get();
        displayHitWarning = DISPLAY_HIT_WARNING.get();
        protectTeamMembers = PROTECT_TEAM_MEMBERS.get();

        noProtectionEntity = NO_PROTECTION_ENTITY.get().stream()
                .map(name -> BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(name)))
                .collect(Collectors.toSet());
        otherShouldProtectionEntity = OTHER_SHOULD_PROTECT_ENTITY.get().stream()
                .map(name -> BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(name)))
                .collect(Collectors.toSet());
        playerCantHurtEntity = PLAYER_CANT_HURT_ENTITY.get().stream()
                .map(name -> BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(name)))
                .collect(Collectors.toSet());
        canHurtPetItem = CAN_HURT_PET_ITEM.get().stream()
                .map(name -> BuiltInRegistries.ITEM.get(new ResourceLocation(name)))
                .collect(Collectors.toSet());
        canHurtAllItem = CAN_HURT_ALL_ITEM.get().stream()
                .map(name -> BuiltInRegistries.ITEM.get(new ResourceLocation(name)))
                .collect(Collectors.toSet());
    }
}
