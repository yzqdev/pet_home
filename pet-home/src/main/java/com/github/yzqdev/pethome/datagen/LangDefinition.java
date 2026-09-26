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

    public static String conf(String name) {
        return PetHomeMod.MODID + ".configuration." + name;
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

    String mobcatcherOnlyTamableAnimal_conf = conf("mobcatcherOnlyTamableAnimal");
    String mobcatcherOnlyTamableAnimal_conf_tooltip = conf("mobcatcherOnlyTamableAnimal.tooltip");
    String mobcatcherBlacklist_conf = conf("mobcatcherBlacklist");
    String mobcatcherBlacklist_conf_tooltip = conf("mobcatcherBlacklist.tooltip");
    String sonic_boom_loot_chance_conf = conf("sonic_boom_loot_chance");
    String sonic_boom_loot_chance_conf_tooltip = conf("sonic_boom_loot_chance.tooltip");
    String share_loot_chance_conf = conf("share_loot_chance");
    String share_loot_chance_conf_tooltip = conf("share_loot_chance.tooltip");
    String paralysis_loot_chance_conf = conf("paralysis_loot_chance");
    String paralysis_loot_chance_conf_tooltip = conf("paralysis_loot_chance.tooltip");
    String tough_loot_chance_conf = conf("tough_loot_chance");
    String tough_loot_chance_conf_tooltip = conf("tough_loot_chance.tooltip");

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
    String ITEM_PET_COMPASS = item("pet_compass");
    String TOOLTIP_PET_COMPASS_DESC = tooltips("pet_compass.desc");
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
    String MESSAGE_ENCHANT_ERROR = message("enchant_error");
    String MESSAGE_GOODBYE = message("goodbye");
    String MESSAGE_REMOVE_RESPAWN = message("remove_respawn");
    String MESSAGE_RESPAWN = message("respawn");
    String MESSAGE_SET_OWNER = message("set_owner");
    String MESSAGE_WAYWARD_LANTERN_RETURN = message("wayward_lantern_return");
    String MESSAGE_PET_COMPASS_TITLE = message("pet_compass.title");
    String MESSAGE_PET_COMPASS_EMPTY = message("pet_compass.empty");
    String MESSAGE_PET_COMPASS_POSITION = message("pet_compass.position");
    String MESSAGE_PET_COMPASS_LAST_KNOWN_POS = message("pet_compass.last_known_pos");
    String MESSAGE_PET_COMPASS_DISTANCE = message("pet_compass.distance");
    String MESSAGE_PET_COMPASS_STATUS_LOADED = message("pet_compass.status_loaded");
    String MESSAGE_PET_COMPASS_STATUS_NOT_LOADED = message("pet_compass.status_not_loaded");
    String MESSAGE_PET_COMPASS_STATUS_WRONG_DIM = message("pet_compass.status_wrong_dim");
    String MESSAGE_PET_COMPASS_STATUS_DEAD = message("pet_compass.status_dead");
    String MESSAGE_PET_COMPASS_BUTTON_TP = message("pet_compass.button_tp");
    String MESSAGE_PET_COMPASS_BUTTON_RECALL = message("pet_compass.button_recall");
    String MESSAGE_PET_COMPASS_DISABLED = message("pet_compass.disabled");
    String MESSAGE_PET_COMPASS_NOT_LOADED_CONFIRM = message("pet_compass.not_loaded_confirm");
    String MESSAGE_PET_COMPASS_DEAD_WAIT_RESPAWN = message("pet_compass.dead_wait_respawn");
    String MESSAGE_PET_COMPASS_RECALL_STARTED = message("pet_compass.recall_started");
    String MESSAGE_PET_COMPASS_RECALL_FAILED = message("pet_compass.recall_failed");
    String MESSAGE_PET_COMPASS_TELEPORTED = message("pet_compass.teleported");
    String MESSAGE_PET_COMPASS_RECALLED = message("pet_compass.recalled");
    String MESSAGE_PET_COMPASS_DIM_OVERWORLD = message("pet_compass.dim_overworld");
    String MESSAGE_PET_COMPASS_DIM_THE_NETHER = message("pet_compass.dim_the_nether");
    String MESSAGE_PET_COMPASS_DIM_THE_END = message("pet_compass.dim_the_end");
    String NOTIF_FRIENDLY_FIRE_PROTECTED = "notif.friendlyfire.protected";
    String JADE_COLLAR_TAG = "config.jade.plugin_pet_home.collar_tag";
    String EFFECT_DRUNK = effect("drunk");
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

    String ENCHANT_AMPHIBIOUS = enchantment("amphibious");
    String ENCHANT_AMPHIBIOUS_DESC = enchantment("amphibious.desc");
    String ENCHANT_BLAZING_PROTECTION = enchantment("blazing_protection");
    String ENCHANT_BLAZING_PROTECTION_DESC = enchantment("blazing_protection.desc");
    String ENCHANT_BLIGHT_CURSE = enchantment("blight_curse");
    String ENCHANT_BLIGHT_CURSE_DESC = enchantment("blight_curse.desc");
    String ENCHANT_BUBBLING = enchantment("bubbling");
    String ENCHANT_BUBBLING_DESC = enchantment("bubbling.desc");
    String ENCHANT_CHAIN_LIGHTNING = enchantment("chain_lightning");
    String ENCHANT_CHAIN_LIGHTNING_DESC = enchantment("chain_lightning.desc");
    String ENCHANT_CHAOS = enchantment("chaos");
    String ENCHANT_CHAOS_DESC = enchantment("chaos.desc");
    String ENCHANT_CHARISMA = enchantment("charisma");
    String ENCHANT_CHARISMA_DESC = enchantment("charisma.desc");
    String ENCHANT_DEFLECTION = enchantment("deflection");
    String ENCHANT_DEFLECTION_DESC = enchantment("deflection.desc");
    String ENCHANT_DEFUSAL = enchantment("defusal");
    String ENCHANT_DEFUSAL_DESC = enchantment("defusal.desc");
    String ENCHANT_DISC_JOCKEY = enchantment("disc_jockey");
    String ENCHANT_DISC_JOCKEY_DESC = enchantment("disc_jockey.desc");
    String ENCHANT_FIREPROOF = enchantment("fireproof");
    String ENCHANT_FIREPROOF_DESC = enchantment("fireproof.desc");
    String ENCHANT_FROST_FANG = enchantment("frost_fang");
    String ENCHANT_FROST_FANG_DESC = enchantment("frost_fang.desc");
    String ENCHANT_GLUTTONOUS = enchantment("gluttonous");
    String ENCHANT_GLUTTONOUS_DESC = enchantment("gluttonous.desc");
    String ENCHANT_HEALING_AURA = enchantment("healing_aura");
    String ENCHANT_HEALING_AURA_DESC = enchantment("healing_aura.desc");
    String ENCHANT_HEALTH_BOOST = enchantment("health_boost");
    String ENCHANT_HEALTH_BOOST_DESC = enchantment("health_boost.desc");
    String ENCHANT_HEALTH_SIPHON = enchantment("health_siphon");
    String ENCHANT_HEALTH_SIPHON_DESC = enchantment("health_siphon.desc");
    String ENCHANT_HERDING = enchantment("herding");
    String ENCHANT_HERDING_DESC = enchantment("herding.desc");
    String ENCHANT_IMMATURITY_CURSE = enchantment("immaturity_curse");
    String ENCHANT_IMMATURITY_CURSE_DESC = enchantment("immaturity_curse.desc");
    String ENCHANT_IMMUNITY_FRAME = enchantment("immunity_frame");
    String ENCHANT_IMMUNITY_FRAME_DESC = enchantment("immunity_frame.desc");
    String ENCHANT_INFAMY_CURSE = enchantment("infamy_curse");
    String ENCHANT_INFAMY_CURSE_DESC = enchantment("infamy_curse.desc");
    String ENCHANT_INSIGHT = enchantment("insight");
    String ENCHANT_INSIGHT_DESC = enchantment("insight.desc");
    String ENCHANT_INTIMIDATION = enchantment("intimidation");
    String ENCHANT_INTIMIDATION_DESC = enchantment("intimidation.desc");
    String ENCHANT_LINKED_INVENTORY = enchantment("linked_inventory");
    String ENCHANT_LINKED_INVENTORY_DESC = enchantment("linked_inventory.desc");
    String ENCHANT_MAGNETIC = enchantment("magnetic");
    String ENCHANT_MAGNETIC_DESC = enchantment("magnetic.desc");
    String ENCHANT_MUFFLED = enchantment("muffled");
    String ENCHANT_MUFFLED_DESC = enchantment("muffled.desc");
    String ENCHANT_NIGHT_VISION = enchantment("night_vision");
    String ENCHANT_NIGHT_VISION_DESC = enchantment("night_vision.desc");
    String ENCHANT_ORE_SCENTING = enchantment("ore_scenting");
    String ENCHANT_ORE_SCENTING_DESC = enchantment("ore_scenting.desc");
    String ENCHANT_PARALYSIS = enchantment("paralysis");
    String ENCHANT_PARALYSIS_DESC = enchantment("paralysis.desc");
    String ENCHANT_POISON_RESISTANCE = enchantment("poison_resistance");
    String ENCHANT_POISON_RESISTANCE_DESC = enchantment("poison_resistance.desc");
    String ENCHANT_PSYCHIC_WALL = enchantment("psychic_wall");
    String ENCHANT_PSYCHIC_WALL_DESC = enchantment("psychic_wall.desc");
    String ENCHANT_REJUVENATION = enchantment("rejuvenation");
    String ENCHANT_REJUVENATION_DESC = enchantment("rejuvenation.desc");
    String ENCHANT_SHADOW_HANDS = enchantment("shadow_hands");
    String ENCHANT_SHADOW_HANDS_DESC = enchantment("shadow_hands.desc");
    String ENCHANT_SHARE = enchantment("share");
    String ENCHANT_SHARE_DESC = enchantment("share.desc");
    String ENCHANT_SONIC_BOOM = enchantment("sonic_boom");
    String ENCHANT_SONIC_BOOM_DESC = enchantment("sonic_boom.desc");
    String ENCHANT_SPEEDSTER = enchantment("speedster");
    String ENCHANT_SPEEDSTER_DESC = enchantment("speedster.desc");
    String ENCHANT_TETHERED_TELEPORT = enchantment("tethered_teleport");
    String ENCHANT_TETHERED_TELEPORT_DESC = enchantment("tethered_teleport.desc");
    String ENCHANT_TOTAL_RECALL = enchantment("total_recall");
    String ENCHANT_TOTAL_RECALL_DESC = enchantment("total_recall.desc");
    String ENCHANT_TOUGH = enchantment("tough");
    String ENCHANT_TOUGH_DESC = enchantment("tough.desc");
    String ENCHANT_UNDEAD_CURSE = enchantment("undead_curse");
    String ENCHANT_UNDEAD_CURSE_DESC = enchantment("undead_curse.desc");
    String ENCHANT_VAMPIRE = enchantment("vampire");
    String ENCHANT_VAMPIRE_DESC = enchantment("vampire.desc");
    String ENCHANT_VIOLENT = enchantment("violent");
    String ENCHANT_VIOLENT_DESC = enchantment("violent.desc");
    String ENCHANT_VOID_CLOUD = enchantment("void_cloud");
    String ENCHANT_VOID_CLOUD_DESC = enchantment("void_cloud.desc");
    String ENCHANT_WARPING_BITE = enchantment("warping_bite");
    String ENCHANT_WARPING_BITE_DESC = enchantment("warping_bite.desc");
    String ENCHANT_XP_TRANSFER = enchantment("xp_transfer");
    String ENCHANT_XP_TRANSFER_DESC = enchantment("xp_transfer.desc");

    // ===== 1.20.1 专属设置界面（PetHomeConfigScreen）=====
    String GUI_TITLE = conf("gui.title");
    String GUI_SUBTITLE = conf("gui.subtitle");
    String GUI_ON = conf("gui.on");
    String GUI_OFF = conf("gui.off");
    String GUI_RESET = conf("gui.reset");
    String GUI_CAT_FRIENDLY_NAME = conf("gui.category.friendly.name");
    String GUI_CAT_FRIENDLY_DESC = conf("gui.category.friendly.desc");
    String GUI_CAT_GENERAL_NAME = conf("gui.category.general.name");
    String GUI_CAT_GENERAL_DESC = conf("gui.category.general.desc");
    String GUI_CAT_TAMEABLE_NAME = conf("gui.category.tameable.name");
    String GUI_CAT_TAMEABLE_DESC = conf("gui.category.tameable.desc");
    String GUI_CAT_LOOT_NAME = conf("gui.category.loot.name");
    String GUI_CAT_LOOT_DESC = conf("gui.category.loot.desc");
    String GUI_CAT_ENCHANTS_NAME = conf("gui.category.enchants.name");
    String GUI_CAT_ENCHANTS_DESC = conf("gui.category.enchants.desc");
    String GUI_F_PROTECT_OWNER = conf("gui.friendly.protect_owner");
    String GUI_F_PROTECT_OWNER_DESC = conf("gui.friendly.protect_owner.desc");
    String GUI_F_PROTECT_PETS = conf("gui.friendly.protect_pets");
    String GUI_F_PROTECT_PETS_DESC = conf("gui.friendly.protect_pets.desc");
    String GUI_F_PROTECT_CHILDREN = conf("gui.friendly.protect_children");
    String GUI_F_PROTECT_CHILDREN_DESC = conf("gui.friendly.protect_children.desc");
    String GUI_F_REFLECT_DAMAGE = conf("gui.friendly.reflect_damage");
    String GUI_F_REFLECT_DAMAGE_DESC = conf("gui.friendly.reflect_damage.desc");
    String GUI_F_DISPLAY_WARNING = conf("gui.friendly.display_warning");
    String GUI_F_DISPLAY_WARNING_DESC = conf("gui.friendly.display_warning.desc");
    String GUI_F_PROTECT_TEAM = conf("gui.friendly.protect_team");
    String GUI_F_PROTECT_TEAM_DESC = conf("gui.friendly.protect_team.desc");
    String GUI_F_RESPECT_TEAM = conf("gui.friendly.respect_team");
    String GUI_F_RESPECT_TEAM_DESC = conf("gui.friendly.respect_team.desc");
    String GUI_F_CAN_HURT_PET_ITEMS = conf("gui.friendly.can_hurt_pet_items");
    String GUI_F_CAN_HURT_PET_ITEMS_DESC = conf("gui.friendly.can_hurt_pet_items.desc");
    String GUI_F_CAN_HURT_ALL_ITEMS = conf("gui.friendly.can_hurt_all_items");
    String GUI_F_CAN_HURT_ALL_ITEMS_DESC = conf("gui.friendly.can_hurt_all_items.desc");
    String GUI_F_NO_PROTECTION = conf("gui.friendly.no_protection_entities");
    String GUI_F_NO_PROTECTION_DESC = conf("gui.friendly.no_protection_entities.desc");
    String GUI_F_EXTRA_PROTECTED = conf("gui.friendly.extra_protected");
    String GUI_F_EXTRA_PROTECTED_DESC = conf("gui.friendly.extra_protected.desc");
    String GUI_F_PLAYER_PROTECTED = conf("gui.friendly.player_protected");
    String GUI_F_PLAYER_PROTECTED_DESC = conf("gui.friendly.player_protected.desc");
    String GUI_G_ROTTEN_APPLE = conf("gui.general.rotten_apple");
    String GUI_G_ROTTEN_APPLE_DESC = conf("gui.general.rotten_apple.desc");
    String GUI_G_PET_BED_RESPAWN = conf("gui.general.pet_bed_respawn");
    String GUI_G_PET_BED_RESPAWN_DESC = conf("gui.general.pet_bed_respawn.desc");
    String GUI_G_RABBITS_RAVAGERS = conf("gui.general.rabbits_ravagers");
    String GUI_G_RABBITS_RAVAGERS_DESC = conf("gui.general.rabbits_ravagers.desc");
    String GUI_G_TRINARY_COMMAND = conf("gui.general.trinary_command");
    String GUI_G_TRINARY_COMMAND_DESC = conf("gui.general.trinary_command.desc");
    String GUI_G_ANIMAL_TAMER = conf("gui.general.animal_tamer");
    String GUI_G_ANIMAL_TAMER_DESC = conf("gui.general.animal_tamer.desc");
    String GUI_G_PETSTORE_WEIGHT = conf("gui.general.petstore_weight");
    String GUI_G_PETSTORE_WEIGHT_DESC = conf("gui.general.petstore_weight.desc");
    String GUI_G_MOB_TAMABLE_ONLY = conf("gui.general.mobcatcher_tamable_only");
    String GUI_G_MOB_TAMABLE_ONLY_DESC = conf("gui.general.mobcatcher_tamable_only.desc");
    String GUI_G_MOB_BLACKLIST = conf("gui.general.mobcatcher_blacklist");
    String GUI_G_MOB_BLACKLIST_DESC = conf("gui.general.mobcatcher_blacklist.desc");
    // 宠物罗盘（对应 1.21/26.1 的三个配置项：总开关 + 两个独立传送）
    String GUI_G_PET_COMPASS_ENABLE = conf("gui.general.pet_compass_enable");
    String GUI_G_PET_COMPASS_ENABLE_DESC = conf("gui.general.pet_compass_enable.desc");
    String GUI_G_TELEPORT_PLAYER_TO_PET = conf("gui.general.teleport_player_to_pet");
    String GUI_G_TELEPORT_PLAYER_TO_PET_DESC = conf("gui.general.teleport_player_to_pet.desc");
    String GUI_G_RECALL_PET_TO_PLAYER = conf("gui.general.recall_pet_to_player");
    String GUI_G_RECALL_PET_TO_PLAYER_DESC = conf("gui.general.recall_pet_to_player.desc");
    // 宠物信息悬浮面板（对应 1.21/26.1 的 petInfoOverlay / petInfoOverlayRequireShift / petInfoOverlayIgnoreJade）
    String GUI_G_PET_INFO_OVERLAY = conf("gui.general.pet_info_overlay");
    String GUI_G_PET_INFO_OVERLAY_DESC = conf("gui.general.pet_info_overlay.desc");
    String GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT = conf("gui.general.pet_info_overlay_require_shift");
    String GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT_DESC = conf("gui.general.pet_info_overlay_require_shift.desc");
    String GUI_G_PET_INFO_OVERLAY_IGNORE_JADE = conf("gui.general.pet_info_overlay_ignore_jade");
    String GUI_G_PET_INFO_OVERLAY_IGNORE_JADE_DESC = conf("gui.general.pet_info_overlay_ignore_jade.desc");
    String GUI_T_AXOLOTL = conf("gui.tameable.axolotl");
    String GUI_T_AXOLOTL_DESC = conf("gui.tameable.axolotl.desc");
    String GUI_T_FOX = conf("gui.tameable.fox");
    String GUI_T_FOX_DESC = conf("gui.tameable.fox.desc");
    String GUI_T_FROG = conf("gui.tameable.frog");
    String GUI_T_FROG_DESC = conf("gui.tameable.frog.desc");
    String GUI_T_HORSE = conf("gui.tameable.horse");
    String GUI_T_HORSE_DESC = conf("gui.tameable.horse.desc");
    String GUI_T_RABBIT = conf("gui.tameable.rabbit");
    String GUI_T_RABBIT_DESC = conf("gui.tameable.rabbit.desc");
    String GUI_L_CURSE_LOOT_ONLY = conf("gui.loot.curse_loot_only");
    String GUI_L_CURSE_LOOT_ONLY_DESC = conf("gui.loot.curse_loot_only.desc");
    String GUI_L_SINISTER_CARROT_DESC = conf("gui.loot.sinister_carrot.desc");
    String GUI_L_BUBBLING_DESC = conf("gui.loot.bubbling.desc");
    String GUI_L_VAMPIRISM_DESC = conf("gui.loot.vampirism.desc");
    String GUI_L_VOID_CLOUD_DESC = conf("gui.loot.void_cloud.desc");
    String GUI_L_ORE_SCENTING_DESC = conf("gui.loot.ore_scenting.desc");
    String GUI_L_MUFFLED_DESC = conf("gui.loot.muffled.desc");
    String GUI_L_BLAZING_DESC = conf("gui.loot.blazing_protection.desc");
    String GUI_L_SHARE_DESC = conf("gui.loot.share.desc");
    String GUI_L_SONIC_BOOM_DESC = conf("gui.loot.sonic_boom.desc");
    String GUI_L_PARALYSIS_DESC = conf("gui.loot.paralysis.desc");
    String GUI_L_TOUGH_DESC = conf("gui.loot.tough.desc");
}
