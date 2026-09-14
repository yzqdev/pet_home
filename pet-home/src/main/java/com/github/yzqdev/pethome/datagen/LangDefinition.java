package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;

public interface LangDefinition {
    public static String text(String name) {
        return "text." + PetHomeMod.MODID + "." + name;

    }

    public static String enchantment(String name) {
        return "enchantment." + PetHomeMod.MODID + "." + name;
    }

    public static String tooltips(String name) {
        return "tooltips." + PetHomeMod.MODID + "." + name;
    }

    public static String soundSubtitle(String name) {
        return PetHomeMod.MODID + ".sound.subtitle." + name;
    }

    public static String message(String name) {
        return "message." + PetHomeMod.MODID + "." + name;
    }

    public static String itemGroup() {
        return "itemGroup." + PetHomeMod.MODID;
    }

    public static String block(String name) {
        return "block." + PetHomeMod.MODID + "." + name;
    }

    public static String item(String name) {
        return "item." + PetHomeMod.MODID + "." + name;
    }

    public static String entity(String name) {
        return "entity." + PetHomeMod.MODID + "." + name;
    }

    public static String config(String name) {
        return PetHomeMod.MODID + ".configuration." + name;
    }

    public static String configTooltip(String name) {
        return PetHomeMod.MODID + ".configuration." + name + ".tooltip";
    }

    public static String configButton(String name) {
        return PetHomeMod.MODID + ".configuration." + name + ".button";
    }

    public static String gui(String number) {
        return "gui." + PetHomeMod.MODID + "." + number;
    }

    public static String event(String name) {
        return "event." + PetHomeMod.MODID + "." + name;
    }

    public static String effect(String name) {
        return "effect." + PetHomeMod.MODID + "." + name;
    }

    String has_pet_bed_at_pos = text("has_petbed_at_pos");
    String capturing_text = text("capturing");
    String release_text = text("releasing");
    String health_text = text("health");
    String no_net_entity_text = text("no_net_entity");
    String net_launcher_tip = text("net_launcher_tip");
    String net_launcher_default_only_tamable = text("net_launcher_default_only_tamable");
    String network_failed = text("network_failed");

    String animal_tamer_villager_conf = config("animal_tamer_villager");
    String mobcatcherOnlyTamableAnimal_conf = config("mobcatcherOnlyTamableAnimal");
    String mobcatcherOnlyTamableAnimal_conf_tooltip = configTooltip("mobcatcherOnlyTamableAnimal");
    String mobcatcherBlacklist_conf = config("mobcatcherBlacklist");
    String mobcatcherBlacklist_conf_tooltip = configTooltip("mobcatcherBlacklist");
    String protectChildren_conf = config("protectChildren");
    String protectChildren_conf_tooltip = configTooltip("protectChildren");
    String displayHitWarning_conf = config("displayHitWarning");
    String displayHitWarning_conf_tooltip = configTooltip("displayHitWarning");
    String rotten_apple_conf = config("rotten_apple");
    String rotten_apple_conf_tooltip = configTooltip("rotten_apple");
    String sinister_carrot_loot_chance_conf = config("sinister_carrot_loot_chance");
    String sinister_carrot_loot_chance_conf_tooltip = configTooltip("sinister_carrot_loot_chance");
    String petstore_village_weight_conf = config("petstore_village_weight");
    String petstore_village_weight_conf_tooltip = configTooltip("petstore_village_weight");
    String respectTeamRules_conf = config("respectTeamRules");
    String respectTeamRules_conf_tooltip = configTooltip("respectTeamRules");
    String protectPetsFromOwner_conf = config("protectPetsFromOwner");
    String protectPetsFromOwner_conf_tooltip = configTooltip("protectPetsFromOwner");
    String protectTeamMembers_conf = config("protectTeamMembers");
    String protectTeamMembers_conf_tooltip = configTooltip("protectTeamMembers");
    String protectPetsFromPets_conf = config("protectPetsFromPets");
    String protectPetsFromPets_conf_tooltip = configTooltip("protectPetsFromPets");
    String reflectDamage_conf = config("reflectDamage");
    String reflectDamage_conf_tooltip = configTooltip("reflectDamage");
    String ore_scenting_loot_chance_conf = config("ore_scenting_loot_chance");
    String ore_scenting_loot_chance_conf_tooltip = configTooltip("ore_scenting_loot_chance");
    String bubbling_loot_chance_conf = config("bubbling_loot_chance");
    String bubbling_loot_chance_conf_tooltip = configTooltip("bubbling_loot_chance");
    String blazing_protection_loot_chance_conf = config("blazing_protection_loot_chance");
    String blazing_protection_loot_chance_conf_tooltip = configTooltip("blazing_protection_loot_chance");
    String vampirism_loot_chance_conf = config("vampirism_loot_chance");
    String vampirism_loot_chance_conf_tooltip = configTooltip("vampirism_loot_chance");
    String other_should_protect_entity_conf = config("other_should_protect_entity");
    String other_should_protect_entity_conf_tooltip = configTooltip("other_should_protect_entity");
    String no_protection_entity_conf = config("no_protection_entity");
    String no_protection_entity_conf_tooltip = configTooltip("no_protection_entity");
    String player_cant_hurt_entity_conf = config("player_cant_hurt_entity");
    String player_cant_hurt_entity_conf_tooltip = configTooltip("player_cant_hurt_entity");
    String can_hurt_pet_item_conf = config("can_hurt_pet_item");
    String can_hurt_pet_item_conf_tooltip = configTooltip("can_hurt_pet_item");
    String can_hurt_all_conf = config("can_hurt_all");
    String can_hurt_all_conf_tooltip = configTooltip("can_hurt_all");
    String sonic_boom_loot_chance_conf = config("sonic_boom_loot_chance");
    String sonic_boom_loot_chance_conf_tooltip = configTooltip("sonic_boom_loot_chance");
    String share_loot_chance_conf = config("share_loot_chance");
    String share_loot_chance_conf_tooltip = configTooltip("share_loot_chance");
    String paralysis_loot_chance_conf = config("paralysis_loot_chance");
    String paralysis_loot_chance_conf_tooltip = configTooltip("paralysis_loot_chance");
    String tough_loot_chance_conf = config("tough_loot_chance");
    String tough_loot_chance_conf_tooltip = configTooltip("tough_loot_chance");

    // NeoForge 配置界面（ConfigurationScreen）所需：新开关 tooltip、列表项编辑按钮、屏幕标题与分节键
    String tameable_axolotls_conf_tooltip = configTooltip("tameable_axolotls");
    String tameable_fox_conf_tooltip = configTooltip("tameable_fox");
    String tameable_frog_conf_tooltip = configTooltip("tameable_frog");
    String tameable_horse_conf_tooltip = configTooltip("tameable_horse");
    String tameable_rabbit_conf_tooltip = configTooltip("tameable_rabbit");
    String trinary_command_system_conf_tooltip = configTooltip("trinary_command_system");
    String pet_bed_respawns_conf_tooltip = configTooltip("pet_bed_respawns");
    String rabbits_scare_ravagers_conf_tooltip = configTooltip("rabbits_scare_ravagers");

    String mobcatcherBlacklist_conf_button = configButton("mobcatcherBlacklist");
    String can_hurt_pet_item_conf_button = configButton("can_hurt_pet_item");
    String can_hurt_all_conf_button = configButton("can_hurt_all");
    String no_protection_entity_conf_button = configButton("no_protection_entity");
    String other_should_protect_entity_conf_button = configButton("other_should_protect_entity");
    String player_cant_hurt_entity_conf_button = configButton("player_cant_hurt_entity");

    String configuration_title = config("title");
    String configuration_section_common = config("section.pet.home.common.toml");
    String configuration_section_common_title = config("section.pet.home.common.toml.title");

    // 以下自设置界面（GUI）与 8 个新附魔移植时新增
    String BLOCK_DRUM = block("drum");
    String BLOCK_PET_BED_WHITE = block("pet_bed_white");
    String BLOCK_PET_BED_ORANGE = block("pet_bed_orange");
    String BLOCK_PET_BED_MAGENTA = block("pet_bed_magenta");
    String BLOCK_PET_BED_LIGHT_BLUE = block("pet_bed_light_blue");
    String BLOCK_PET_BED_YELLOW = block("pet_bed_yellow");
    String BLOCK_PET_BED_LIME = block("pet_bed_lime");
    String BLOCK_PET_BED_PINK = block("pet_bed_pink");
    String BLOCK_PET_BED_GRAY = block("pet_bed_gray");
    String BLOCK_PET_BED_LIGHT_GRAY = block("pet_bed_light_gray");
    String BLOCK_PET_BED_CYAN = block("pet_bed_cyan");
    String BLOCK_PET_BED_PURPLE = block("pet_bed_purple");
    String BLOCK_PET_BED_BLUE = block("pet_bed_blue");
    String BLOCK_PET_BED_BROWN = block("pet_bed_brown");
    String BLOCK_PET_BED_GREEN = block("pet_bed_green");
    String BLOCK_PET_BED_RED = block("pet_bed_red");
    String BLOCK_PET_BED_BLACK = block("pet_bed_black");
    String BLOCK_WAYWARD_LANTERN = block("wayward_lantern");
    String ITEM_GROUP = itemGroup();
    String ITEM_COLLAR_TAG = item("collar_tag");
    String ITEM_ROTTEN_APPLE = item("rotten_apple");
    String ITEM_SINISTER_CARROT = item("sinister_carrot");
    String ITEM_DEFLECTION_SHIELD = item("deflection_shield");
    String ITEM_MAGNET = item("magnet");
    String ITEM_FEATHER_ON_A_STICK = item("feather_on_a_stick");
    String ITEM_DEED_OF_OWNERSHIP = item("deed_of_ownership");
    String ITEM_DEED_OF_OWNERSHIP_DESC = item("deed_of_ownership.desc");
    String ITEM_NET = item("net");
    String ITEM_NET_HAS_ITEM = item("net_has_item");
    String ITEM_NET_LAUNCHER = item("net_launcher");
    String ENTITY_CHAIN_LIGHTNING = entity("chain_lightning");
    String ENTITY_FEATHER = entity("feather");
    String ENTITY_FOLLOWING_JUKEBOX = entity("following_jukebox");
    String ENTITY_PSYCHIC_WALL = entity("psychic_wall");
    String ENTITY_RECALL_BALL = entity("recall_ball");
    String ENTITY_ANIMAL_TAMER = "entity.minecraft.villager.pet_home.animal_tamer";
    String MESSAGE_COMMAND_0 = message("command_0");
    String MESSAGE_COMMAND_1 = message("command_1");
    String MESSAGE_COMMAND_2 = message("command_2");
    String MESSAGE_DRUM_COMMAND_0 = message("drum_command_0");
    String MESSAGE_DRUM_COMMAND_1 = message("drum_command_1");
    String MESSAGE_DRUM_COMMAND_2 = message("drum_command_2");
    String MESSAGE_ENCHANTMENTS = message("enchantments");
    String MESSAGE_GOODBYE = message("goodbye");
    String MESSAGE_REMOVE_RESPAWN = message("remove_respawn");
    String MESSAGE_RESPAWN = message("respawn");
    String MESSAGE_SET_OWNER = message("set_owner");
    String MESSAGE_WAYWARD_LANTERN_RETURN = message("wayward_lantern_return");
    String NOTIF_FRIENDLY_FIRE_PROTECTED = "notif.friendlyfire.protected";
    String JADE_COLLAR_TAG = "config.jade.plugin_pet_home.collar_tag";
    String ENCHANTMENT_PREFIX = "enchantment.";
    String SOUND_SUBTITLE_BLAZING_PROTECTION = soundSubtitle("blazing_protection");
    String SOUND_SUBTITLE_CHAIN_LIGHTNING = soundSubtitle("chain_lightning");
    String SOUND_SUBTITLE_COLLAR_TAG = soundSubtitle("collar_tag");
    String SOUND_SUBTITLE_DRUM = soundSubtitle("drum");
    String SOUND_SUBTITLE_GIANT_BUBBLE_INFLATE = soundSubtitle("giant_bubble_inflate");
    String SOUND_SUBTITLE_GIANT_BUBBLE_POP = soundSubtitle("giant_bubble_pop");
    String SOUND_SUBTITLE_MAGNET_LOOP = soundSubtitle("magnet_loop");
    String SOUND_SUBTITLE_PET_BED_USES = soundSubtitle("pet_bed_uses");
    String SOUND_SUBTITLE_PSYCHIC_WALL = soundSubtitle("psychic_wall");
    String SOUND_SUBTITLE_PSYCHIC_WALL_DEFLECT = soundSubtitle("psychic_wall_deflect");
    String TOOLTIPS_SUBSTITUTE_COLLAR_DESC = tooltips("substitute_collar.desc");
    String TOOLTIPS_SUBSTITUTE_FEATHER_DESC = tooltips("substitute_feather.desc");
    String TOOLTIPS_SUBSTITUTE_PET_BED_DESC = tooltips("substitute_pet_bed.desc");
    String TOOLTIPS_SUBSTITUTE_ROTTEN_APPLE_DESC = tooltips("substitute_rotten_apple.desc");
    String TOOLTIPS_SUBSTITUTE_SINISTER_CARROT_DESC = tooltips("substitute_sinister_carrot.desc");
    String TOOLTIPS_WAYWARD_LANTERN_DESC = tooltips("wayward_lantern.desc");
    String TOOLTIPS_DRUM_DESC = tooltips("drum.desc");
}