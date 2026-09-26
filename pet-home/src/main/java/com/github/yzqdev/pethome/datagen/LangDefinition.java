package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;

public interface LangDefinition {
    public static String text(String name) {
        return "text." + PetHomeMod.MODID + "." + name;

    }


    public static String config(String name) {
        return PetHomeMod.MODID + ".configuration." + name;
    }

    public static String configTooltip(String name) {
        return PetHomeMod.MODID + ".configuration." + name + ".tooltip";
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

    public static String item(String name) {
        return "item." + PetHomeMod.MODID + "." + name;
    }

    public static String block(String name) {
        return "block." + PetHomeMod.MODID + "." + name;
    }

    public static String enchantment(String name) {
        return "enchantment." + PetHomeMod.MODID + "." + name;
    }

    public static String tooltips(String name) {
        return "tooltips." + PetHomeMod.MODID + "." + name;
    }

    public static String message(String name) {
        return "message." + PetHomeMod.MODID + "." + name;
    }

    public static String entity(String name) {
        return "entity." + PetHomeMod.MODID + "." + name;
    }

    public static String soundSubtitle(String name) {
        return PetHomeMod.MODID + ".sound.subtitle." + name;
    }

    // —— 物品 / 物品栏 ——
    String ITEM_GROUP = "itemGroup." + PetHomeMod.MODID;
    String ITEM_COLLAR_TAG = item("collar_tag");
    String ITEM_ROTTEN_APPLE = item("rotten_apple");
    String ITEM_SINISTER_CARROT = item("sinister_carrot");
    String ITEM_DEFLECTION_SHIELD = item("deflection_shield");
    String ITEM_MAGNET = item("magnet");
    String ITEM_FEATHER_ON_A_STICK = item("feather_on_a_stick");
    String ITEM_DEED_OF_OWNERSHIP = item("deed_of_ownership");
    String ITEM_DEED_OF_OWNERSHIP_DESC = item("deed_of_ownership.desc");

    // —— 方块 ——
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
    String BLOCK_DRUM = block("drum");
    String BLOCK_WAYWARD_LANTERN = block("wayward_lantern");

    // —— 物品 tooltip ——
    String TOOLTIP_SUBSTITUTE_FEATHER_DESC = tooltips("substitute_feather.desc");
    String TOOLTIP_SUBSTITUTE_ROTTEN_APPLE_DESC = tooltips("substitute_rotten_apple.desc");
    String TOOLTIP_SUBSTITUTE_SINISTER_CARROT_DESC = tooltips("substitute_sinister_carrot.desc");
    String TOOLTIP_SUBSTITUTE_COLLAR_DESC = tooltips("substitute_collar.desc");
    String TOOLTIP_SUBSTITUTE_PET_BED_DESC = tooltips("substitute_pet_bed.desc");
    String TOOLTIP_DRUM_DESC = tooltips("drum.desc");
    String TOOLTIP_WAYWARD_LANTERN_DESC = tooltips("wayward_lantern.desc");
    String TOOLTIP_PET_COMPASS_DESC = tooltips("pet_compass.desc");

    // —— 聊天 / 界面消息 ——
    String MESSAGE_COMMAND_0 = message("command_0");
    String MESSAGE_COMMAND_1 = message("command_1");
    String MESSAGE_COMMAND_2 = message("command_2");
    String MESSAGE_DRUM_COMMAND_0 = message("drum_command_0");
    String MESSAGE_DRUM_COMMAND_1 = message("drum_command_1");
    String MESSAGE_DRUM_COMMAND_2 = message("drum_command_2");
    String MESSAGE_RESPAWN = message("respawn");
    String MESSAGE_REMOVE_RESPAWN = message("remove_respawn");
    String MESSAGE_GOODBYE = message("goodbye");
    String MESSAGE_ENCHANTMENTS = message("enchantments");
    String MESSAGE_SET_OWNER = message("set_owner");
    String MESSAGE_WAYWARD_LANTERN_RETURN = message("wayward_lantern_return");

    // —— 实体 ——
    String ENTITY_VILLAGER_ANIMAL_TAMER = "entity.minecraft.villager." + PetHomeMod.MODID + ".animal_tamer";
    String ENTITY_CHAIN_LIGHTNING = entity("chain_lightning");
    String ENTITY_RECALL_BALL = entity("recall_ball");
    String ENTITY_FEATHER = entity("feather");
    String ENTITY_FOLLOWING_JUKEBOX = entity("following_jukebox");
    String ENTITY_PSYCHIC_WALL = entity("psychic_wall");

    // —— 音效字幕 ——
    String SOUND_SUBTITLE_COLLAR_TAG = soundSubtitle("collar_tag");
    String SOUND_SUBTITLE_MAGNET_LOOP = soundSubtitle("magnet_loop");
    String SOUND_SUBTITLE_CHAIN_LIGHTNING = soundSubtitle("chain_lightning");
    String SOUND_SUBTITLE_GIANT_BUBBLE_INFLATE = soundSubtitle("giant_bubble_inflate");
    String SOUND_SUBTITLE_GIANT_BUBBLE_POP = soundSubtitle("giant_bubble_pop");
    String SOUND_SUBTITLE_PET_BED_USES = soundSubtitle("pet_bed_uses");
    String SOUND_SUBTITLE_DRUM = soundSubtitle("drum");
    String SOUND_SUBTITLE_PSYCHIC_WALL = soundSubtitle("psychic_wall");
    String SOUND_SUBTITLE_PSYCHIC_WALL_DEFLECT = soundSubtitle("psychic_wall_deflect");
    String SOUND_SUBTITLE_BLAZING_PROTECTION = soundSubtitle("blazing_protection");

    // —— 杂项 ——
    String NOTIF_FRIENDLYFIRE_PROTECTED = "notif.friendlyfire.protected";
    String JADE_COLLAR_TAG = "config.jade.plugin_" + PetHomeMod.MODID + ".collar_tag";

    String ITEM_PET_COMPASS = item("pet_compass");
    String TOOLTIP_PET_COMPASS_UNBOUND = tooltips("pet_compass.unbound");
    String TOOLTIP_PET_COMPASS_BOUND = tooltips("pet_compass.bound");
    String TOOLTIP_PET_COMPASS_DISTANCE = tooltips("pet_compass.distance");
    String TOOLTIP_PET_COMPASS_LAST_KNOWN = tooltips("pet_compass.last_known");
    String TOOLTIP_PET_COMPASS_WRONG_DIM = tooltips("pet_compass.wrong_dim");
    String TOOLTIP_PET_COMPASS_WAITING_LANTERN = tooltips("pet_compass.waiting_lantern");
    String TOOLTIP_PET_COMPASS_WAITING_RESPAWN = tooltips("pet_compass.waiting_respawn");
    String TOOLTIP_PET_COMPASS_UNBIND_HINT = tooltips("pet_compass.unbind_hint");
    String MESSAGE_PET_COMPASS_TITLE = message("pet_compass.title");
    String MESSAGE_PET_COMPASS_EMPTY = message("pet_compass.empty");
    String MESSAGE_PET_COMPASS_POSITION = message("pet_compass.position");
    String MESSAGE_PET_COMPASS_LAST_KNOWN_POS = message("pet_compass.last_known_pos");
    String MESSAGE_PET_COMPASS_DISTANCE = message("pet_compass.distance");
    String MESSAGE_PET_COMPASS_DIM_OVERWORLD = message("pet_compass.dim_overworld");
    String MESSAGE_PET_COMPASS_DIM_THE_NETHER = message("pet_compass.dim_the_nether");
    String MESSAGE_PET_COMPASS_DIM_THE_END = message("pet_compass.dim_the_end");
    String MESSAGE_PET_COMPASS_STATUS_LOADED = message("pet_compass.status_loaded");
    String MESSAGE_PET_COMPASS_STATUS_NOT_LOADED = message("pet_compass.status_not_loaded");
    String MESSAGE_PET_COMPASS_STATUS_WRONG_DIM = message("pet_compass.status_wrong_dim");
    String MESSAGE_PET_COMPASS_STATUS_DEAD = message("pet_compass.status_dead");
    String MESSAGE_PET_COMPASS_BUTTON_TP = message("pet_compass.button_tp");
    String MESSAGE_PET_COMPASS_BUTTON_RECALL = message("pet_compass.button_recall");
    String MESSAGE_PET_COMPASS_DISABLED = message("pet_compass.disabled");
    String MESSAGE_PET_COMPASS_DEAD_WAIT_RESPAWN = message("pet_compass.dead_wait_respawn");
    String MESSAGE_PET_COMPASS_NOT_LOADED_CONFIRM = message("pet_compass.not_loaded_confirm");
    String MESSAGE_PET_COMPASS_TELEPORTED = message("pet_compass.teleported");
    String MESSAGE_PET_COMPASS_RECALLED = message("pet_compass.recalled");
    String MESSAGE_PET_COMPASS_RECALL_STARTED = message("pet_compass.recall_started");
    String MESSAGE_PET_COMPASS_RECALL_FAILED = message("pet_compass.recall_failed");


        // —— 屏幕 ——
        public static String GUI_TITLE = config("gui.title");
        public static String GUI_SUBTITLE = config("gui.subtitle");
        public static String GUI_RESET = config("gui.reset");
        public static String GUI_ON = config("gui.on");
        public static String GUI_OFF = config("gui.off");

        // —— 分类（Tab） ——
        public static String GUI_CAT_FRIENDLY_NAME = config("gui.category.friendly.name");
        public static String GUI_CAT_FRIENDLY_DESC = config("gui.category.friendly.desc");
        public static String GUI_CAT_GENERAL_NAME = config("gui.category.general.name");
        public static String GUI_CAT_GENERAL_DESC = config("gui.category.general.desc");
        public static String GUI_CAT_TAMEABLE_NAME = config("gui.category.tameable.name");
        public static String GUI_CAT_TAMEABLE_DESC = config("gui.category.tameable.desc");
        public static String GUI_CAT_LOOT_NAME = config("gui.category.loot.name");
        public static String GUI_CAT_LOOT_DESC = config("gui.category.loot.desc");
        public static String GUI_CAT_ENCHANTS_NAME = config("gui.category.enchants.name");
        public static String GUI_CAT_ENCHANTS_DESC = config("gui.category.enchants.desc");

        // —— 友好保护 ——
        public static String GUI_F_PROTECT_OWNER = config("gui.friendly.protect_owner");
        public static String GUI_F_PROTECT_OWNER_DESC = config("gui.friendly.protect_owner.desc");
        public static String GUI_F_PROTECT_PETS = config("gui.friendly.protect_pets");
        public static String GUI_F_PROTECT_PETS_DESC = config("gui.friendly.protect_pets.desc");
        public static String GUI_F_PROTECT_CHILDREN = config("gui.friendly.protect_children");
        public static String GUI_F_PROTECT_CHILDREN_DESC = config("gui.friendly.protect_children.desc");
        public static String GUI_F_REFLECT_DAMAGE = config("gui.friendly.reflect_damage");
        public static String GUI_F_REFLECT_DAMAGE_DESC = config("gui.friendly.reflect_damage.desc");
        public static String GUI_F_DISPLAY_WARNING = config("gui.friendly.display_warning");
        public static String GUI_F_DISPLAY_WARNING_DESC = config("gui.friendly.display_warning.desc");
        public static String GUI_F_PROTECT_TEAM = config("gui.friendly.protect_team");
        public static String GUI_F_PROTECT_TEAM_DESC = config("gui.friendly.protect_team.desc");
        public static String GUI_F_RESPECT_TEAM = config("gui.friendly.respect_team");
        public static String GUI_F_RESPECT_TEAM_DESC = config("gui.friendly.respect_team.desc");
        public static String GUI_F_CAN_HURT_PET_ITEMS = config("gui.friendly.can_hurt_pet_items");
        public static String GUI_F_CAN_HURT_PET_ITEMS_DESC = config("gui.friendly.can_hurt_pet_items.desc");
        public static String GUI_F_CAN_HURT_ALL_ITEMS = config("gui.friendly.can_hurt_all_items");
        public static String GUI_F_CAN_HURT_ALL_ITEMS_DESC = config("gui.friendly.can_hurt_all_items.desc");
        public static String GUI_F_NO_PROTECTION = config("gui.friendly.no_protection_entities");
        public static String GUI_F_NO_PROTECTION_DESC = config("gui.friendly.no_protection_entities.desc");
        public static String GUI_F_EXTRA_PROTECTED = config("gui.friendly.extra_protected");
        public static String GUI_F_EXTRA_PROTECTED_DESC = config("gui.friendly.extra_protected.desc");
        public static String GUI_F_PLAYER_PROTECTED = config("gui.friendly.player_protected");
        public static String GUI_F_PLAYER_PROTECTED_DESC = config("gui.friendly.player_protected.desc");

        // —— 通用功能 ——
        public static String GUI_G_ROTTEN_APPLE = config("gui.general.rotten_apple");
        public static String GUI_G_ROTTEN_APPLE_DESC = config("gui.general.rotten_apple.desc");
        public static String GUI_G_PET_BED_RESPAWN = config("gui.general.pet_bed_respawn");
        public static String GUI_G_PET_BED_RESPAWN_DESC = config("gui.general.pet_bed_respawn.desc");
        public static String GUI_G_RABBITS_RAVAGERS = config("gui.general.rabbits_ravagers");
        public static String GUI_G_RABBITS_RAVAGERS_DESC = config("gui.general.rabbits_ravagers.desc");
        public static String GUI_G_TRINARY_COMMAND = config("gui.general.trinary_command");
        public static String GUI_G_TRINARY_COMMAND_DESC = config("gui.general.trinary_command.desc");
        public static String GUI_G_PET_INFO_OVERLAY = config("gui.general.pet_info_overlay");
        public static String GUI_G_PET_INFO_OVERLAY_DESC = config("gui.general.pet_info_overlay.desc");
        public static String GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT = config("gui.general.pet_info_overlay_require_shift");
        public static String GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT_DESC = config("gui.general.pet_info_overlay_require_shift.desc");
        public static String GUI_G_PET_INFO_OVERLAY_IGNORE_JADE = config("gui.general.pet_info_overlay_ignore_jade");
        public static String GUI_G_PET_INFO_OVERLAY_IGNORE_JADE_DESC = config("gui.general.pet_info_overlay_ignore_jade.desc");
        public static String GUI_G_PETSTORE_WEIGHT = config("gui.general.petstore_weight");
        public static String GUI_G_PETSTORE_WEIGHT_DESC = config("gui.general.petstore_weight.desc");
        public static String GUI_G_MOB_TAMABLE_ONLY = config("gui.general.mobcatcher_tamable_only");
        public static String GUI_G_MOB_TAMABLE_ONLY_DESC = config("gui.general.mobcatcher_tamable_only.desc");
        public static String GUI_G_PET_COMPASS = config("gui.general.pet_compass");
        public static String GUI_G_PET_COMPASS_DESC = config("gui.general.pet_compass.desc");
        public static String GUI_G_PET_COMPASS_TP_P = config("gui.general.teleport_player_to_pet");
        public static String GUI_G_PET_COMPASS_TP_P_DESC = config("gui.general.teleport_player_to_pet.desc");
        public static String GUI_G_PET_COMPASS_TP_R = config("gui.general.teleport_pet_to_player");
        public static String GUI_G_PET_COMPASS_TP_R_DESC = config("gui.general.teleport_pet_to_player.desc");
        public static String GUI_G_MOB_BLACKLIST = config("gui.general.mobcatcher_blacklist");
        public static String GUI_G_MOB_BLACKLIST_DESC = config("gui.general.mobcatcher_blacklist.desc");

        // —— 驯服扩展 ——
        public static String GUI_T_AXOLOTL = config("gui.tameable.axolotl");
        public static String GUI_T_AXOLOTL_DESC = config("gui.tameable.axolotl.desc");
        public static String GUI_T_FOX = config("gui.tameable.fox");
        public static String GUI_T_FOX_DESC = config("gui.tameable.fox.desc");
        public static String GUI_T_FROG = config("gui.tameable.frog");
        public static String GUI_T_FROG_DESC = config("gui.tameable.frog.desc");
        public static String GUI_T_HORSE = config("gui.tameable.horse");
        public static String GUI_T_HORSE_DESC = config("gui.tameable.horse.desc");
        public static String GUI_T_RABBIT = config("gui.tameable.rabbit");
        public static String GUI_T_RABBIT_DESC = config("gui.tameable.rabbit.desc");

        // —— 战利品概率 ——
        public static String GUI_L_CURSE_LOOT_ONLY = config("gui.loot.curse_loot_only");
        public static String GUI_L_CURSE_LOOT_ONLY_DESC = config("gui.loot.curse_loot_only.desc");
        public static String GUI_L_SINISTER_CARROT_DESC = config("gui.loot.sinister_carrot.desc");
        public static String GUI_L_BUBBLING_DESC = config("gui.loot.bubbling.desc");
        public static String GUI_L_VAMPIRISM_DESC = config("gui.loot.vampirism.desc");
        public static String GUI_L_VOID_CLOUD_DESC = config("gui.loot.void_cloud.desc");
        public static String GUI_L_ORE_SCENTING_DESC = config("gui.loot.ore_scenting.desc");
        public static String GUI_L_BLAZING_DESC = config("gui.loot.blazing_protection.desc");
        public static String GUI_L_SHARE_DESC = config("gui.loot.share.desc");
        public static String GUI_L_SONIC_BOOM_DESC = config("gui.loot.sonic_boom.desc");
        public static String GUI_L_PARALYSIS_DESC = config("gui.loot.paralysis.desc");
        public static String GUI_L_TOUGH_DESC = config("gui.loot.tough.desc");

}
