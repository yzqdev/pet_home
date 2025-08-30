package com.github.yzqdev.pet_home;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@EventBusSubscriber(modid = PetHomeMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class PetHomeConfig {


    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ROTTEN_APPLE = BUILDER.comment("true if apples can turn into rotten apples if they despawn").define("rotten_apple", true);
    private static final ModConfigSpec.BooleanValue MOBCATCHER_ONLY_TAMABLE_ANIMAL = BUILDER.comment("Mob catcher only catches tamable animal").define("mobcatcherOnlyTamableAnimal", true);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> MOBCATCHER_BLACKLIST = BUILDER.comment("entities that can't be caught").defineListAllowEmpty("mobcatcherBlacklist", List.of("minecraft:painting"), () -> BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PAINTING).toString(), PetHomeConfig::validateEntityTypesName);
    private static final ModConfigSpec.DoubleValue BLAZING_PROTECTION_LOOT_CHANCE = BUILDER.comment("percent chance of nether fortress loot table containing Blazing Protection book:").defineInRange("blazing_protection_loot_chance", 0.2D, 0.0, 1.0D);
    private static final ModConfigSpec.IntValue PETSTORE_VILLAGE_WEIGHT = BUILDER.comment("the spawn weight of the pet store in villages, set to 0 to disable it entirely").defineInRange("petstore_village_weight", 17, 0, 1000);


    private static final ModConfigSpec.DoubleValue SINISTER_CARROT_LOOT_CHANCE = BUILDER.comment("percent chance of woodland mansion loot table containing sinister carrot:").defineInRange("sinister_carrot_loot_chance", 0.3D, 0.0, 1.0D);
    private static final ModConfigSpec.DoubleValue BUBBLING_LOOT_CHANCE = BUILDER.comment("percent chance of burried treasure loot table containing Bubbling book:").defineInRange("bubbling_loot_chance", 0.65D, 0.0, 1.0D);
    private static final ModConfigSpec.DoubleValue VAMPIRISM_LOOT_CHANCE = BUILDER.comment("percent chance of woodland mansion loot table containing Vampire book:").defineInRange("vampirism_loot_chance", 0.22D, 0.0, 1.0D);
    private static final ModConfigSpec.DoubleValue SONIC_BOOM_LOOT_CHANCE = BUILDER.comment("percent chance of woodland mansion loot table containing Sonic boom book:").defineInRange("sonic_boom_loot_chance", 0.6D, 0.0, 1.0D);
    private static final ModConfigSpec.DoubleValue PARALYSIS_LOOT_CHANCE = BUILDER.comment("percent chance of chest loot table containing paralysis book:").defineInRange("paralysis_loot_chance", 0.1D, 0.0, 1.0D);
    private static final ModConfigSpec.DoubleValue SHARE_LOOT_CHANCE = BUILDER.comment("percent chance of ender city loot table containing share book:").defineInRange("share_loot_chance", 0.5D, 0.0, 1.0D);

    private static final ModConfigSpec.DoubleValue ORE_SCENTING_LOOT_CHANCE = BUILDER.comment("percent chance of mineshaft loot table containing Ore Scenting book:").defineInRange("ore_scenting_loot_chance", 0.15D, 0.0, 1.0D);


    private static final ModConfigSpec.BooleanValue PROTECT_PETS_FROM_OWNER = BUILDER
            .comment("owner cannot hurt pet")
            .define("protectPetsFromOwner", true);
    private static final ModConfigSpec.BooleanValue PROTECT_PETS_FROM_PETS = BUILDER
            .comment("pet cannot hurt pet")
            .define("protectPetsFromPets", true);
    private static final ModConfigSpec.BooleanValue PROTECT_CHILDREN = BUILDER
            .comment("protect children animal")
            .define("protectChildren", true);
    private static final ModConfigSpec.BooleanValue REFLECT_DAMAGE = BUILDER
            .comment("protect pet from ownder")
            .define("reflectDamage", false);
    private static final ModConfigSpec.BooleanValue DISPLAY_HIT_WARNING = BUILDER
            .comment("owner cannot hurt pet")
            .define("displayHitWarning", true);
    private static final ModConfigSpec.BooleanValue PROTECT_TEAM_MEMBERS = BUILDER
            .comment("PROTECT_TEAM_MEMBERS")
            .define("protectTeamMembers", true);
    private static final ModConfigSpec.BooleanValue RESPECT_TEAM_RULES = BUILDER
            .comment("RESPECT_TEAM_RULES")
            .define("respectTeamRules", true);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> CAN_HURT_PET_ITEM = BUILDER.comment("can hurt pet item").defineListAllowEmpty("can_hurt_pet_item", List.of(), () -> BuiltInRegistries.ITEM.getKey(Items.EGG).toString(), PetHomeConfig::validateItemName);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> CAN_HURT_ALL_ITEM = BUILDER.comment("can always hurt item").defineListAllowEmpty("can_hurt_all", List.of(), () -> BuiltInRegistries.ITEM.getKey(Items.EGG).toString(), PetHomeConfig::validateItemName);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> NO_PROTECTION_ENTITY = BUILDER.comment("can always hurt").defineListAllowEmpty("no_protection_entity", List.of(), () -> BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE).toString(), PetHomeConfig::validateEntityTypesName);
    ;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> OTHER_SHOULD_PROTECT_ENTITY = BUILDER.comment("other entities that can be protected").defineListAllowEmpty("other_should_protect_entity", List.of(), () -> BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.VILLAGER).toString(), PetHomeConfig::validateEntityTypesName);
    ;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> PLAYER_CANT_HURT_ENTITY = BUILDER.comment("entities player cant hurt").defineListAllowEmpty("player_cant_hurt_entity", List.of(), () -> BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIFIED_PIGLIN).toString(), PetHomeConfig::validateEntityTypesName);
    ;
    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateEntityTypesName(Object obj) {
        boolean flag;
        if (obj instanceof String itemName) {
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(itemName))) {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }

    private static boolean validateItemName(Object obj) {
        boolean flag;
        if (obj instanceof String itemName) {
            if (BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName))) {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }




    public static double sinisterCarrotLootChance;
    public static double bubblingLootChance;
    public static double oreScentingLootChance;
    public static double vampirismLootChance;
    public static double shareLootChance;
    public static double sonicBoomLootChance;
    public static double blazingProtectionLootChance;
    public static double paralysisLootChance;
    public static boolean protectPetsFromOwner = true;
    public static int petstoreVillageWeight;
    public static boolean rottenApple;
    public static boolean protectPetsFromPets = true;

    public static boolean protectChildren = true;

    public static boolean reflectDamage;

    public static boolean displayHitWarning;

    public static boolean protectTeamMembers = true;
    public static boolean respectTeamRules = true;
    public static Set<EntityType<?>> noProtectionEntity;
    public static Set<EntityType<?>> otherShouldProtectionEntity;
    public static Set<EntityType<?>> playerCantHurtEntity;
    public static Set<Item> canHurtPetItem;
    public static Set<Item> canHurtAllItem;
    public static boolean mobcatcherOnlyTamableAnimal;
    public static Set<EntityType<?>> mobcatcherBlackList;
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        mobcatcherOnlyTamableAnimal=MOBCATCHER_ONLY_TAMABLE_ANIMAL.get();
        mobcatcherBlackList=(MOBCATCHER_BLACKLIST.get()).stream().map((entityTypeName) -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityTypeName))).collect(Collectors.toSet());
        respectTeamRules = RESPECT_TEAM_RULES.get();
        protectPetsFromOwner = PROTECT_PETS_FROM_OWNER.get();
        protectChildren = PROTECT_CHILDREN.get();
        protectPetsFromPets = PROTECT_PETS_FROM_PETS.get();
        reflectDamage = REFLECT_DAMAGE.get();
        displayHitWarning = DISPLAY_HIT_WARNING.get();
        protectTeamMembers = PROTECT_TEAM_MEMBERS.get();
        rottenApple = ROTTEN_APPLE.get();
        blazingProtectionLootChance = BLAZING_PROTECTION_LOOT_CHANCE.get();
        petstoreVillageWeight = PETSTORE_VILLAGE_WEIGHT.get();
        sinisterCarrotLootChance = SINISTER_CARROT_LOOT_CHANCE.get();
        bubblingLootChance = BUBBLING_LOOT_CHANCE.get();
        oreScentingLootChance = ORE_SCENTING_LOOT_CHANCE.get();
        vampirismLootChance = VAMPIRISM_LOOT_CHANCE.get();
        shareLootChance = SHARE_LOOT_CHANCE.get();
        sonicBoomLootChance = SONIC_BOOM_LOOT_CHANCE.get();
        paralysisLootChance = PARALYSIS_LOOT_CHANCE.get();
        blazingProtectionLootChance = BLAZING_PROTECTION_LOOT_CHANCE.get();
        // convert the list of strings into a set of items
        noProtectionEntity = (NO_PROTECTION_ENTITY.get()).stream().map((entityTypeName) -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityTypeName))).collect(Collectors.toSet());
        otherShouldProtectionEntity = (OTHER_SHOULD_PROTECT_ENTITY.get()).stream().map((entityTypeName) -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityTypeName))).collect(Collectors.toSet());
        playerCantHurtEntity = (PLAYER_CANT_HURT_ENTITY.get()).stream().map((entityTypeName) -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityTypeName))).collect(Collectors.toSet());
        canHurtPetItem = (CAN_HURT_PET_ITEM.get()).stream().map(i -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(i))).collect(Collectors.toSet());
        canHurtAllItem = (CAN_HURT_ALL_ITEM.get()).stream().map(i -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(i))).collect(Collectors.toSet());
    }
}
