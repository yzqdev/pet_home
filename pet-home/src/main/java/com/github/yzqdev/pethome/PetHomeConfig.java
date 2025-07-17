package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.enchantment.PetEnchantment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = PetHomeMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PetHomeConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public final ForgeConfigSpec.BooleanValue trinaryCommandSystem;
    public final ForgeConfigSpec.BooleanValue tameableAxolotl;
    public final ForgeConfigSpec.BooleanValue tameableHorse;
    public final ForgeConfigSpec.BooleanValue tameableFox;
    public final ForgeConfigSpec.BooleanValue tameableRabbit;
    public final ForgeConfigSpec.BooleanValue tameableFrog;
    public final ForgeConfigSpec.BooleanValue swingThroughPets;
    public final ForgeConfigSpec.BooleanValue rottenApple;
    public final ForgeConfigSpec.BooleanValue petBedRespawns;
    public final ForgeConfigSpec.BooleanValue collarTag;
    public final ForgeConfigSpec.BooleanValue rabbitsScareRavagers;
    public final ForgeConfigSpec.BooleanValue animalTamerVillager;
    public final ForgeConfigSpec.IntValue petstoreVillageWeight;

    public final ForgeConfigSpec.BooleanValue petCurseEnchantmentsLootOnly;
    public final ForgeConfigSpec.DoubleValue sinisterCarrotLootChance;
    public final ForgeConfigSpec.DoubleValue bubblingLootChance;
    public final ForgeConfigSpec.DoubleValue vampirismLootChance;
    public final ForgeConfigSpec.DoubleValue voidCloudLootChance;
    public final ForgeConfigSpec.DoubleValue oreScentingLootChance;
    public final ForgeConfigSpec.DoubleValue muffledLootChance;
    public final ForgeConfigSpec.DoubleValue blazingProtectionLootChance;
    private static final ForgeConfigSpec.BooleanValue MOBCATCHER_ONLY_TAMABLE_ANIMAL = BUILDER.comment("Mob catcher only catches tamable animal").define("mobcatcherOnlyTamableAnimal", true);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOBCATCHER_BLACKLIST = BUILDER.comment("entities that can't be caught").defineListAllowEmpty("mobcatcherBlacklist", List.of("minecraft:painting"),   PetHomeConfig::validateEntityTypesName);

    // friendly fire

    static {
        BUILDER.push("friendly_fire");
    }
    private static final ForgeConfigSpec.BooleanValue PROTECT_PETS_FROM_OWNER = BUILDER
            .comment("owner cannot hurt pet")
            .define("protectPetsFromOwner", true);
    private static final ForgeConfigSpec.BooleanValue PROTECT_PETS_FROM_PETS = BUILDER
            .comment("pet cannot hurt pet")
            .define("protectPetsFromPets", true);
    private static final ForgeConfigSpec.BooleanValue PROTECT_CHILDREN = BUILDER
            .comment("protect children animal")
            .define("protectChildren", true);
    private static final ForgeConfigSpec.BooleanValue REFLECT_DAMAGE = BUILDER
            .comment("protect pet from owner")
            .define("reflectDamage", false);
    private static final ForgeConfigSpec.BooleanValue DISPLAY_HIT_WARNING = BUILDER
            .comment("owner cannot hurt pet")
            .define("displayHitWarning", true);
    private static final ForgeConfigSpec.BooleanValue PROTECT_TEAM_MEMBERS = BUILDER
            .comment("PROTECT_TEAM_MEMBERS")
            .define("protectTeamMembers", true);
    private static final ForgeConfigSpec.BooleanValue RESPECT_TEAM_RULES = BUILDER
            .comment("RESPECT_TEAM_RULES")
            .define("respectTeamRules", true);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CAN_HURT_PET_ITEM = BUILDER.comment("can hurt pet item").defineListAllowEmpty("can_hurt_pet_item", List.of(), PetHomeConfig::validateItemName);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CAN_HURT_ALL_ITEM = BUILDER.comment("can always hurt item").defineListAllowEmpty("can_hurt_all", List.of(), PetHomeConfig::validateItemName);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> NO_PROTECTION_ENTITY = BUILDER.comment("can always hurt").defineListAllowEmpty("no_protection_entity", List.of(), PetHomeConfig::validateEntityTypesName);
    ;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> OTHER_SHOULD_PROTECT_ENTITY = BUILDER.comment("other entities that can be protected").defineListAllowEmpty("other_should_protect_entity", List.of(), PetHomeConfig::validateEntityTypesName);
    ;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PLAYER_CANT_HURT_ENTITY = BUILDER.comment("entities player cant hurt").defineListAllowEmpty("player_cant_hurt_entity", List.of(), PetHomeConfig::validateEntityTypesName);




    static {
        BUILDER.pop();
    }

    private final Map<String, ForgeConfigSpec.BooleanValue> enabledEnchantments = new HashMap<>();

    private static boolean validateEntityTypesName(Object obj) {

        return obj instanceof String itemName&&BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(itemName));
    }
    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(  ResourceLocation.parse(itemName));
    }


    public PetHomeConfig(final ForgeConfigSpec.Builder builder) {
        builder.push("general");
        trinaryCommandSystem = builder.comment("true if wolves, cats, parrots, foxes, axolotls, etc can be set to wander, sit or follow").translation("trinary_command_system").define("trinary_command_system", true);
        tameableAxolotl = builder.comment("true if axolotls are fully tameable (axolotl must be tamed with tropical fish)").translation("tameable_axolotls").define("tameable_axolotls", true);
        tameableHorse = builder.comment("true if horses, donkeys, llamas, etc can be given enchants, beds, etc").translation("tameable_horse").define("tameable_horse", true);
        tameableFox = builder.comment("true if foxes are fully tameable (fox must be tamed via breeding)").translation("tameable_fox").define("tameable_fox", true);
        tameableRabbit = builder.comment("true if rabbits are fully tameable (rabbit must be tamed with carrots)").translation("tameable_rabbit").define("tameable_rabbit", true);
        tameableFrog = builder.comment("true if frogs are fully tameable (rabbit must be tamed with spider eyes)").translation("tameable_rabbit").define("tameable_frog", true);
        swingThroughPets = builder.comment("true if attacks do not register on pets from their owners and go through them to attack a mob behind them").translation("swing_through_pets").define("swing_through_pets", true);
        rottenApple = builder.comment("true if apples can turn into rotten apples if they despawn").translation("rotten_apple").define("rotten_apple", true);
        petBedRespawns = builder.comment("true if mobs can respawn in pet beds the next morning after they die").translation("pet_bed_respawns").define("pet_bed_respawns", true);
        collarTag = builder.comment("true if collar tag functionality are enabled. If this is disabled, there is no way to enchant mobs!").translation("collar_tags").define("collar_tags", true);
        rabbitsScareRavagers = builder.comment("true if rabbits scare ravagers like they used to do").translation("rabbits_scare_ravagers").define("rabbits_scare_ravagers", true);
        animalTamerVillager = builder.comment("true if animal tamer villagers are enabled. Their work station is a pet bed").translation("animal_tamer_villager").define("animal_tamer_villager", true);
        petstoreVillageWeight = builder.comment("the spawn weight of the pet store in villages, set to 0 to disable it entirely").translation("petstore_village_weight").defineInRange("petstore_village_weight", 17, 0, 1000);
        builder.pop();
        builder.push("loot");
        petCurseEnchantmentsLootOnly = builder.comment("true if pet curse enchantments should only appear in loot, and not the enchanting table.").translation("pet_curse_enchantments_loot_only").define("pet_curse_enchantments_loot_only", true);
        sinisterCarrotLootChance = builder.comment("percent chance of woodland mansion loot table containing sinister carrot:").translation("sinister_carrot_loot_chance").defineInRange("sinister_carrot_loot_chance", 0.3D, 0.0, 1.0D);
        bubblingLootChance = builder.comment("percent chance of burried treasure loot table containing Bubbling book:").translation("bubbling_loot_chance").defineInRange("bubbling_loot_chance", 0.65D, 0.0, 1.0D);
        vampirismLootChance = builder.comment("percent chance of woodland mansion loot table containing Vampire book:").translation("vampirism_loot_chance").defineInRange("vampirism_loot_chance", 0.22D, 0.0, 1.0D);
        voidCloudLootChance = builder.comment("percent chance of end city loot table containing Void Cloud book:").translation("void_cloud_loot_chance").defineInRange("void_cloud_loot_chance", 0.19D, 0.0, 1.0D);
        oreScentingLootChance = builder.comment("percent chance of mineshaft loot table containing Ore Scenting book:").translation("ore_scenting_loot_chance").defineInRange("ore_scenting_loot_chance", 0.15D, 0.0, 1.0D);
        muffledLootChance = builder.comment("percent chance of ancient city loot table containing Muffled book:").translation("muffled_loot_chance").defineInRange("muffled_loot_chance", 0.19D, 0.0, 1.0D);
        blazingProtectionLootChance = builder.comment("percent chance of nether fortress loot table containing Blazing Protection book:").translation("ore_scenting_loot_chance").defineInRange("blazing_protection_loot_chance", 0.2D, 0.0, 1.0D);
        builder.pop();
        builder.push("enchantments");
        try {
            for (Field f : DIEnchantmentRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof PetEnchantment) {
                    String registryName = ((PetEnchantment) obj).getName();
                    String name = registryName + "_enabled";
                    enabledEnchantments.put(registryName, builder.comment("true if " + registryName.replace("_", " ") + " enchant is enabled, false if disabled").translation(name).define(name, true));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        builder.pop();
    }

    public boolean isEnchantEnabled(Enchantment enchantment) {
        return enchantment instanceof PetEnchantment && isEnchantEnabled(((PetEnchantment) enchantment).getName());
    }

    public boolean isEnchantEnabled(String enchantment) {
        ForgeConfigSpec.BooleanValue entry = enabledEnchantments.get(enchantment);
        return entry == null || entry.get();
    }
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

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        mobcatcherBlackList=(MOBCATCHER_BLACKLIST.get()).stream().map((entityTypeName) -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityTypeName))).collect(Collectors.toSet());;
        mobcatcherOnlyTamableAnimal=MOBCATCHER_ONLY_TAMABLE_ANIMAL.get();
        respectTeamRules = RESPECT_TEAM_RULES.get();
        protectPetsFromOwner = PROTECT_PETS_FROM_OWNER.get();
        protectChildren = PROTECT_CHILDREN.get();
        protectPetsFromPets = PROTECT_PETS_FROM_PETS.get();
        reflectDamage = REFLECT_DAMAGE.get();
        displayHitWarning = DISPLAY_HIT_WARNING.get();
        protectTeamMembers = PROTECT_TEAM_MEMBERS.get();

        // convert the list of strings into a set of items
        noProtectionEntity = (NO_PROTECTION_ENTITY.get()).stream().map((entityTypeName) -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityTypeName))).collect(Collectors.toSet());
        otherShouldProtectionEntity = (OTHER_SHOULD_PROTECT_ENTITY.get()).stream().map((entityTypeName) -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityTypeName))).collect(Collectors.toSet());
        playerCantHurtEntity = (PLAYER_CANT_HURT_ENTITY.get()).stream().map((entityTypeName) -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityTypeName))).collect(Collectors.toSet());
        canHurtPetItem = (CAN_HURT_PET_ITEM.get()).stream().map(i -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(i))).collect(Collectors.toSet());
        canHurtAllItem = (CAN_HURT_ALL_ITEM.get()).stream().map(i -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(i))).collect(Collectors.toSet());
    }
}
