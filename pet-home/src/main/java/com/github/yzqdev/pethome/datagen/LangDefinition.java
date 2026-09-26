package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;

public class LangDefinition {
    public static String text(String name) {
        return "text." + PetHomeMod.MODID + "." + name;

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


    public static String item(String name) {
        return "item." + PetHomeMod.MODID + "." + name;
    }

    public static String block(String name) {
        return "block." + PetHomeMod.MODID + "." + name;
    }

    public static String entity(String name) {
        return "entity." + PetHomeMod.MODID + "." + name;
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

    public static String soundSubtitle(String name) {
        return PetHomeMod.MODID + ".sound.subtitle." + name;
    }

    public static String configuration(String name) {
        return PetHomeMod.MODID + ".configuration." + name;
    }


    public static String ITEM_PET_COMPASS = item("pet_compass");
    public static String TOOLTIP_PET_COMPASS_UNBOUND = tooltips("pet_compass.unbound");
    public static String TOOLTIP_PET_COMPASS_BOUND = tooltips("pet_compass.bound");
    public static String TOOLTIP_PET_COMPASS_DISTANCE = tooltips("pet_compass.distance");
    public static String TOOLTIP_PET_COMPASS_LAST_KNOWN = tooltips("pet_compass.last_known");
    public static String TOOLTIP_PET_COMPASS_WRONG_DIM = tooltips("pet_compass.wrong_dim");
    public static String TOOLTIP_PET_COMPASS_WAITING_LANTERN = tooltips("pet_compass.waiting_lantern");
    public static String TOOLTIP_PET_COMPASS_WAITING_RESPAWN = tooltips("pet_compass.waiting_respawn");
    public static String TOOLTIP_PET_COMPASS_UNBIND_HINT = tooltips("pet_compass.unbind_hint");

    public static String itemGroup() {
        return "itemGroup." + PetHomeMod.MODID;
    }


        public static String has_pet_bed_at_pos= LangDefinition.text("has_petbed_at_pos");
        public static String capturing_text= LangDefinition.text("capturing");
        public static String release_text= LangDefinition.text("releasing");
        public static String health_text= LangDefinition.text("health");
        public static String no_net_entity_text= LangDefinition.text("no_net_entity");
        public static String net_launcher_tip= LangDefinition.text("net_launcher_tip");
        public static String net_launcher_default_only_tamable= LangDefinition.text("net_launcher_default_only_tamable");
        public static String EFFECT_DRUNK = LangDefinition.effect("drunk");

        public static String TOOLTIPS_SUBSTITUTE_COLLAR_DESC = LangDefinition.tooltips("substitute_collar.desc");
        public static String TOOLTIPS_SUBSTITUTE_FEATHER_DESC = LangDefinition.tooltips("substitute_feather.desc");
        public static String TOOLTIPS_SUBSTITUTE_PET_BED_DESC = LangDefinition.tooltips("substitute_pet_bed.desc");
        public static String TOOLTIPS_SUBSTITUTE_ROTTEN_APPLE_DESC = LangDefinition.tooltips("substitute_rotten_apple.desc");
        public static String TOOLTIPS_SUBSTITUTE_SINISTER_CARROT_DESC = LangDefinition.tooltips("substitute_sinister_carrot.desc");
        public static String TOOLTIPS_WAYWARD_LANTERN_DESC = LangDefinition.tooltips("wayward_lantern.desc");
        public static String TOOLTIPS_DRUM_DESC = LangDefinition.tooltips("drum.desc");
        // 附魔翻译键（配置界面复用为条目标签）
        public static String ENCHANT_BUBBLING = LangDefinition.enchantment("bubbling");
        public static String ENCHANT_VAMPIRE = LangDefinition.enchantment("vampire");
        public static String ENCHANT_VOID_CLOUD = LangDefinition.enchantment("void_cloud");
        public static String ENCHANT_ORE_SCENTING = LangDefinition.enchantment("ore_scenting");
        public static String ENCHANT_MUFFLED = LangDefinition.enchantment("muffled");
        public static String ENCHANT_BLAZING_PROTECTION = LangDefinition.enchantment("blazing_protection");
        public static String ENCHANT_SHARE = LangDefinition.enchantment("share");
        public static String ENCHANT_SONIC_BOOM = LangDefinition.enchantment("sonic_boom");
        public static String ENCHANT_PARALYSIS = LangDefinition.enchantment("paralysis");
        public static String ENCHANT_TOUGH = LangDefinition.enchantment("tough");
        public static String ITEM_SINISTER_CARROT = LangDefinition.item("sinister_carrot");
        // 配置界面文案
        public static String GUI_TITLE = LangDefinition.configuration("gui.title");
        public static String GUI_SUBTITLE = LangDefinition.configuration("gui.subtitle");
        public static String GUI_ON = LangDefinition.configuration("gui.on");
        public static String GUI_OFF = LangDefinition.configuration("gui.off");
        public static String GUI_RESET = LangDefinition.configuration("gui.reset");
        public static String GUI_CAT_FRIENDLY_NAME = LangDefinition.configuration("gui.category.friendly.name");
        public static String GUI_CAT_FRIENDLY_DESC = LangDefinition.configuration("gui.category.friendly.desc");
        public static String GUI_CAT_GENERAL_NAME = LangDefinition.configuration("gui.category.general.name");
        public static String GUI_CAT_GENERAL_DESC = LangDefinition.configuration("gui.category.general.desc");
        public static String GUI_CAT_TAMEABLE_NAME = LangDefinition.configuration("gui.category.tameable.name");
        public static String GUI_CAT_TAMEABLE_DESC = LangDefinition.configuration("gui.category.tameable.desc");
        public static String GUI_CAT_LOOT_NAME = LangDefinition.configuration("gui.category.loot.name");
        public static String GUI_CAT_LOOT_DESC = LangDefinition.configuration("gui.category.loot.desc");
        public static String GUI_CAT_ENCHANTS_NAME = LangDefinition.configuration("gui.category.enchants.name");
        public static String GUI_CAT_ENCHANTS_DESC = LangDefinition.configuration("gui.category.enchants.desc");
        public static String GUI_F_PROTECT_OWNER = LangDefinition.configuration("gui.friendly.protect_owner");
        public static String GUI_F_PROTECT_OWNER_DESC = LangDefinition.configuration("gui.friendly.protect_owner.desc");
        public static String GUI_F_PROTECT_PETS = LangDefinition.configuration("gui.friendly.protect_pets");
        public static String GUI_F_PROTECT_PETS_DESC = LangDefinition.configuration("gui.friendly.protect_pets.desc");
        public static String GUI_F_PROTECT_CHILDREN = LangDefinition.configuration("gui.friendly.protect_children");
        public static String GUI_F_PROTECT_CHILDREN_DESC = LangDefinition.configuration("gui.friendly.protect_children.desc");
        public static String GUI_F_REFLECT_DAMAGE = LangDefinition.configuration("gui.friendly.reflect_damage");
        public static String GUI_F_REFLECT_DAMAGE_DESC = LangDefinition.configuration("gui.friendly.reflect_damage.desc");
        public static String GUI_F_DISPLAY_WARNING = LangDefinition.configuration("gui.friendly.display_warning");
        public static String GUI_F_DISPLAY_WARNING_DESC = LangDefinition.configuration("gui.friendly.display_warning.desc");
        public static String GUI_F_PROTECT_TEAM = LangDefinition.configuration("gui.friendly.protect_team");
        public static String GUI_F_PROTECT_TEAM_DESC = LangDefinition.configuration("gui.friendly.protect_team.desc");
        public static String GUI_F_RESPECT_TEAM = LangDefinition.configuration("gui.friendly.respect_team");
        public static String GUI_F_RESPECT_TEAM_DESC = LangDefinition.configuration("gui.friendly.respect_team.desc");
        public static String GUI_F_CAN_HURT_PET_ITEMS = LangDefinition.configuration("gui.friendly.can_hurt_pet_items");
        public static String GUI_F_CAN_HURT_PET_ITEMS_DESC = LangDefinition.configuration("gui.friendly.can_hurt_pet_items.desc");
        public static String GUI_F_CAN_HURT_ALL_ITEMS = LangDefinition.configuration("gui.friendly.can_hurt_all_items");
        public static String GUI_F_CAN_HURT_ALL_ITEMS_DESC = LangDefinition.configuration("gui.friendly.can_hurt_all_items.desc");
        public static String GUI_F_NO_PROTECTION = LangDefinition.configuration("gui.friendly.no_protection_entities");
        public static String GUI_F_NO_PROTECTION_DESC = LangDefinition.configuration("gui.friendly.no_protection_entities.desc");
        public static String GUI_F_EXTRA_PROTECTED = LangDefinition.configuration("gui.friendly.extra_protected");
        public static String GUI_F_EXTRA_PROTECTED_DESC = LangDefinition.configuration("gui.friendly.extra_protected.desc");
        public static String GUI_F_PLAYER_PROTECTED = LangDefinition.configuration("gui.friendly.player_protected");
        public static String GUI_F_PLAYER_PROTECTED_DESC = LangDefinition.configuration("gui.friendly.player_protected.desc");
        public static String GUI_G_ROTTEN_APPLE = LangDefinition.configuration("gui.general.rotten_apple");
        public static String GUI_G_ROTTEN_APPLE_DESC = LangDefinition.configuration("gui.general.rotten_apple.desc");
        public static String GUI_G_PET_BED_RESPAWN = LangDefinition.configuration("gui.general.pet_bed_respawn");
        public static String GUI_G_PET_BED_RESPAWN_DESC = LangDefinition.configuration("gui.general.pet_bed_respawn.desc");
        public static String GUI_G_RABBITS_RAVAGERS = LangDefinition.configuration("gui.general.rabbits_ravagers");
        public static String GUI_G_RABBITS_RAVAGERS_DESC = LangDefinition.configuration("gui.general.rabbits_ravagers.desc");
        public static String GUI_G_TRINARY_COMMAND = LangDefinition.configuration("gui.general.trinary_command");
        public static String GUI_G_TRINARY_COMMAND_DESC = LangDefinition.configuration("gui.general.trinary_command.desc");
        public static String GUI_G_PET_INFO_OVERLAY = LangDefinition.configuration("gui.general.pet_info_overlay");
        public static String GUI_G_PET_INFO_OVERLAY_DESC = LangDefinition.configuration("gui.general.pet_info_overlay.desc");
        public static String GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT = LangDefinition.configuration("gui.general.pet_info_overlay_require_shift");
        public static String GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT_DESC = LangDefinition.configuration("gui.general.pet_info_overlay_require_shift.desc");
        public static String GUI_G_PET_INFO_OVERLAY_IGNORE_JADE = LangDefinition.configuration("gui.general.pet_info_overlay_ignore_jade");
        public static String GUI_G_PET_INFO_OVERLAY_IGNORE_JADE_DESC = LangDefinition.configuration("gui.general.pet_info_overlay_ignore_jade.desc");
        public static String GUI_G_ANIMAL_TAMER = LangDefinition.configuration("gui.general.animal_tamer");
        public static String GUI_G_ANIMAL_TAMER_DESC = LangDefinition.configuration("gui.general.animal_tamer.desc");
        public static String GUI_G_PETSTORE_WEIGHT = LangDefinition.configuration("gui.general.petstore_weight");
        public static String GUI_G_PETSTORE_WEIGHT_DESC = LangDefinition.configuration("gui.general.petstore_weight.desc");
        public static String GUI_G_MOB_TAMABLE_ONLY = LangDefinition.configuration("gui.general.mobcatcher_tamable_only");
        public static String GUI_G_MOB_TAMABLE_ONLY_DESC = LangDefinition.configuration("gui.general.mobcatcher_tamable_only.desc");
        public static String GUI_G_PET_COMPASS = LangDefinition.configuration("gui.general.pet_compass");
        public static String GUI_G_PET_COMPASS_DESC = LangDefinition.configuration("gui.general.pet_compass.desc");
        public static String GUI_G_PET_COMPASS_TP_P = LangDefinition.configuration("gui.general.teleport_player_to_pet");
        public static String GUI_G_PET_COMPASS_TP_P_DESC = LangDefinition.configuration("gui.general.teleport_player_to_pet.desc");
        public static String GUI_G_PET_COMPASS_TP_R = LangDefinition.configuration("gui.general.teleport_pet_to_player");
        public static String GUI_G_PET_COMPASS_TP_R_DESC = LangDefinition.configuration("gui.general.teleport_pet_to_player.desc");
        public static String GUI_G_MOB_BLACKLIST = LangDefinition.configuration("gui.general.mobcatcher_blacklist");
        public static String GUI_G_MOB_BLACKLIST_DESC = LangDefinition.configuration("gui.general.mobcatcher_blacklist.desc");
        public static String GUI_T_AXOLOTL = LangDefinition.configuration("gui.tameable.axolotl");
        public static String GUI_T_AXOLOTL_DESC = LangDefinition.configuration("gui.tameable.axolotl.desc");
        public static String GUI_T_FOX = LangDefinition.configuration("gui.tameable.fox");
        public static String GUI_T_FOX_DESC = LangDefinition.configuration("gui.tameable.fox.desc");
        public static String GUI_T_FROG = LangDefinition.configuration("gui.tameable.frog");
        public static String GUI_T_FROG_DESC = LangDefinition.configuration("gui.tameable.frog.desc");
        public static String GUI_T_HORSE = LangDefinition.configuration("gui.tameable.horse");
        public static String GUI_T_HORSE_DESC = LangDefinition.configuration("gui.tameable.horse.desc");
        public static String GUI_T_RABBIT = LangDefinition.configuration("gui.tameable.rabbit");
        public static String GUI_T_RABBIT_DESC = LangDefinition.configuration("gui.tameable.rabbit.desc");
        public static String GUI_L_CURSE_LOOT_ONLY = LangDefinition.configuration("gui.loot.curse_loot_only");
        public static String GUI_L_CURSE_LOOT_ONLY_DESC = LangDefinition.configuration("gui.loot.curse_loot_only.desc");
        public static String GUI_L_SINISTER_CARROT_DESC = LangDefinition.configuration("gui.loot.sinister_carrot.desc");
        public static String GUI_L_BUBBLING_DESC = LangDefinition.configuration("gui.loot.bubbling.desc");
        public static String GUI_L_VAMPIRISM_DESC = LangDefinition.configuration("gui.loot.vampirism.desc");
        public static String GUI_L_VOID_CLOUD_DESC = LangDefinition.configuration("gui.loot.void_cloud.desc");
        public static String GUI_L_ORE_SCENTING_DESC = LangDefinition.configuration("gui.loot.ore_scenting.desc");
        public static String GUI_L_MUFFLED_DESC = LangDefinition.configuration("gui.loot.muffled.desc");
        public static String GUI_L_BLAZING_DESC = LangDefinition.configuration("gui.loot.blazing_protection.desc");
        public static String GUI_L_SHARE_DESC = LangDefinition.configuration("gui.loot.share.desc");
        public static String GUI_L_SONIC_BOOM_DESC = LangDefinition.configuration("gui.loot.sonic_boom.desc");
        public static String GUI_L_PARALYSIS_DESC = LangDefinition.configuration("gui.loot.paralysis.desc");
        public static String GUI_L_TOUGH_DESC = LangDefinition.configuration("gui.loot.tough.desc");

        // —— 物品 / 物品栏 ——
        public static String ITEM_GROUP = itemGroup();
        public static String ITEM_COLLAR_TAG = item("collar_tag");
        public static String ITEM_ROTTEN_APPLE = item("rotten_apple");
        public static String ITEM_DEFLECTION_SHIELD = item("deflection_shield");
        public static String ITEM_MAGNET = item("magnet");
        public static String ITEM_FEATHER_ON_A_STICK = item("feather_on_a_stick");
        public static String ITEM_DEED_OF_OWNERSHIP = item("deed_of_ownership");
        public static String ITEM_DEED_OF_OWNERSHIP_DESC = item("deed_of_ownership.desc");

        // —— 方块 ——
        public static String BLOCK_PET_BED_WHITE = block("pet_bed_white");
        public static String BLOCK_PET_BED_ORANGE = block("pet_bed_orange");
        public static String BLOCK_PET_BED_MAGENTA = block("pet_bed_magenta");
        public static String BLOCK_PET_BED_LIGHT_BLUE = block("pet_bed_light_blue");
        public static String BLOCK_PET_BED_YELLOW = block("pet_bed_yellow");
        public static String BLOCK_PET_BED_LIME = block("pet_bed_lime");
        public static String BLOCK_PET_BED_PINK = block("pet_bed_pink");
        public static String BLOCK_PET_BED_GRAY = block("pet_bed_gray");
        public static String BLOCK_PET_BED_LIGHT_GRAY = block("pet_bed_light_gray");
        public static String BLOCK_PET_BED_CYAN = block("pet_bed_cyan");
        public static String BLOCK_PET_BED_PURPLE = block("pet_bed_purple");
        public static String BLOCK_PET_BED_BLUE = block("pet_bed_blue");
        public static String BLOCK_PET_BED_BROWN = block("pet_bed_brown");
        public static String BLOCK_PET_BED_GREEN = block("pet_bed_green");
        public static String BLOCK_PET_BED_RED = block("pet_bed_red");
        public static String BLOCK_PET_BED_BLACK = block("pet_bed_black");
        public static String BLOCK_DRUM = block("drum");
        public static String BLOCK_WAYWARD_LANTERN = block("wayward_lantern");

        // —— 聊天 / 界面消息 ——
        public static String MESSAGE_COMMAND_0 = message("command_0");
        public static String MESSAGE_COMMAND_1 = message("command_1");
        public static String MESSAGE_COMMAND_2 = message("command_2");
        public static String MESSAGE_DRUM_COMMAND_0 = message("drum_command_0");
        public static String MESSAGE_DRUM_COMMAND_1 = message("drum_command_1");
        public static String MESSAGE_DRUM_COMMAND_2 = message("drum_command_2");
        public static String MESSAGE_RESPAWN = message("respawn");
        public static String MESSAGE_REMOVE_RESPAWN = message("remove_respawn");
        public static String MESSAGE_GOODBYE = message("goodbye");
        public static String MESSAGE_ENCHANTMENTS = message("enchantments");
        public static String MESSAGE_SET_OWNER = message("set_owner");
        public static String MESSAGE_WAYWARD_LANTERN_RETURN = message("wayward_lantern_return");

        // —— 实体 ——
        public static String ENTITY_VILLAGER_ANIMAL_TAMER = "entity.minecraft.villager." + PetHomeMod.MODID + ".animal_tamer";
        public static String ENTITY_CHAIN_LIGHTNING = entity("chain_lightning");
        public static String ENTITY_RECALL_BALL = entity("recall_ball");
        public static String ENTITY_FEATHER = entity("feather");
        public static String ENTITY_FOLLOWING_JUKEBOX = entity("following_jukebox");
        public static String ENTITY_PSYCHIC_WALL = entity("psychic_wall");

        public static String ENCHANT_HEALTH_BOOST = enchantment("health_boost");
        public static String ENCHANT_HEALTH_BOOST_DESC = enchantment("health_boost.desc");
        public static String ENCHANT_FIREPROOF = enchantment("fireproof");
        public static String ENCHANT_FIREPROOF_DESC = enchantment("fireproof.desc");
        public static String ENCHANT_IMMUNITY_FRAME = enchantment("immunity_frame");
        public static String ENCHANT_IMMUNITY_FRAME_DESC = enchantment("immunity_frame.desc");
        public static String ENCHANT_DEFLECTION = enchantment("deflection");
        public static String ENCHANT_DEFLECTION_DESC = enchantment("deflection.desc");
        public static String ENCHANT_POISON_RESISTANCE = enchantment("poison_resistance");
        public static String ENCHANT_POISON_RESISTANCE_DESC = enchantment("poison_resistance.desc");
        public static String ENCHANT_CHAIN_LIGHTNING = enchantment("chain_lightning");
        public static String ENCHANT_CHAIN_LIGHTNING_DESC = enchantment("chain_lightning.desc");
        public static String ENCHANT_SPEEDSTER = enchantment("speedster");
        public static String ENCHANT_SPEEDSTER_DESC = enchantment("speedster.desc");
        public static String ENCHANT_FROST_FANG = enchantment("frost_fang");
        public static String ENCHANT_FROST_FANG_DESC = enchantment("frost_fang.desc");
        public static String ENCHANT_MAGNETIC = enchantment("magnetic");
        public static String ENCHANT_MAGNETIC_DESC = enchantment("magnetic.desc");
        public static String ENCHANT_LINKED_INVENTORY = enchantment("linked_inventory");
        public static String ENCHANT_LINKED_INVENTORY_DESC = enchantment("linked_inventory.desc");
        public static String ENCHANT_TOTAL_RECALL = enchantment("total_recall");
        public static String ENCHANT_TOTAL_RECALL_DESC = enchantment("total_recall.desc");
        public static String ENCHANT_HEALTH_SIPHON = enchantment("health_siphon");
        public static String ENCHANT_HEALTH_SIPHON_DESC = enchantment("health_siphon.desc");
        public static String ENCHANT_BUBBLING_DESC = enchantment("bubbling.desc");
        public static String ENCHANT_AMPHIBIOUS = enchantment("amphibious");
        public static String ENCHANT_AMPHIBIOUS_DESC = enchantment("amphibious.desc");
        public static String ENCHANT_HERDING = enchantment("herding");
        public static String ENCHANT_HERDING_DESC = enchantment("herding.desc");
        public static String ENCHANT_VAMPIRE_DESC = enchantment("vampire.desc");
        public static String ENCHANT_VOID_CLOUD_DESC = enchantment("void_cloud.desc");
        public static String ENCHANT_CHARISMA = enchantment("charisma");
        public static String ENCHANT_CHARISMA_DESC = enchantment("charisma.desc");
        public static String ENCHANT_UNDEAD_CURSE = enchantment("undead_curse");
        public static String ENCHANT_UNDEAD_CURSE_DESC = enchantment("undead_curse.desc");
        public static String ENCHANT_INFAMY_CURSE = enchantment("infamy_curse");
        public static String ENCHANT_INFAMY_CURSE_DESC = enchantment("infamy_curse.desc");
        public static String ENCHANT_SHADOW_HANDS = enchantment("shadow_hands");
        public static String ENCHANT_SHADOW_HANDS_DESC = enchantment("shadow_hands.desc");
        public static String ENCHANT_DISC_JOCKEY = enchantment("disc_jockey");
        public static String ENCHANT_DISC_JOCKEY_DESC = enchantment("disc_jockey.desc");
        public static String ENCHANT_DEFUSAL = enchantment("defusal");
        public static String ENCHANT_DEFUSAL_DESC = enchantment("defusal.desc");
        public static String ENCHANT_WARPING_BITE = enchantment("warping_bite");
        public static String ENCHANT_WARPING_BITE_DESC = enchantment("warping_bite.desc");
        public static String ENCHANT_ORE_SCENTING_DESC = enchantment("ore_scenting.desc");
        public static String ENCHANT_GLUTTONOUS = enchantment("gluttonous");
        public static String ENCHANT_GLUTTONOUS_DESC = enchantment("gluttonous.desc");
        public static String ENCHANT_PSYCHIC_WALL = enchantment("psychic_wall");
        public static String ENCHANT_PSYCHIC_WALL_DESC = enchantment("psychic_wall.desc");
        public static String ENCHANT_INTIMIDATION = enchantment("intimidation");
        public static String ENCHANT_INTIMIDATION_DESC = enchantment("intimidation.desc");
        public static String ENCHANT_BLIGHT_CURSE = enchantment("blight_curse");
        public static String ENCHANT_BLIGHT_CURSE_DESC = enchantment("blight_curse.desc");
        public static String ENCHANT_TETHERED_TELEPORT = enchantment("tethered_teleport");
        public static String ENCHANT_TETHERED_TELEPORT_DESC = enchantment("tethered_teleport.desc");
        public static String ENCHANT_IMMATURITY_CURSE = enchantment("immaturity_curse");
        public static String ENCHANT_IMMATURITY_CURSE_DESC = enchantment("immaturity_curse.desc");
        public static String ENCHANT_MUFFLED_DESC = enchantment("muffled.desc");
        public static String ENCHANT_BLAZING_PROTECTION_DESC = enchantment("blazing_protection.desc");
        public static String ENCHANT_HEALING_AURA = enchantment("healing_aura");
        public static String ENCHANT_HEALING_AURA_DESC = enchantment("healing_aura.desc");
        public static String ENCHANT_REJUVENATION = enchantment("rejuvenation");
        public static String ENCHANT_REJUVENATION_DESC = enchantment("rejuvenation.desc");
        public static String ENCHANT_XP_TRANSFER = enchantment("xp_transfer");
        public static String ENCHANT_XP_TRANSFER_DESC = enchantment("xp_transfer.desc");
        public static String ENCHANT_INSIGHT = enchantment("insight");
        public static String ENCHANT_INSIGHT_DESC = enchantment("insight.desc");
        public static String ENCHANT_CHAOS = enchantment("chaos");
        public static String ENCHANT_CHAOS_DESC = enchantment("chaos.desc");
        public static String ENCHANT_SHARE_DESC = enchantment("share.desc");
        public static String ENCHANT_NIGHT_VISION = enchantment("night_vision");
        public static String ENCHANT_NIGHT_VISION_DESC = enchantment("night_vision.desc");
        public static String ENCHANT_PARALYSIS_DESC = enchantment("paralysis.desc");
        public static String ENCHANT_TOUGH_DESC = enchantment("tough.desc");
        public static String ENCHANT_VIOLENT = enchantment("violent");
        public static String ENCHANT_VIOLENT_DESC = enchantment("violent.desc");
        public static String ENCHANT_SONIC_BOOM_DESC = enchantment("sonic_boom.desc");

        // —— 音效字幕 ——
        public static String SOUND_SUBTITLE_COLLAR_TAG = soundSubtitle("collar_tag");
        public static String SOUND_SUBTITLE_MAGNET_LOOP = soundSubtitle("magnet_loop");
        public static String SOUND_SUBTITLE_CHAIN_LIGHTNING = soundSubtitle("chain_lightning");
        public static String SOUND_SUBTITLE_GIANT_BUBBLE_INFLATE = soundSubtitle("giant_bubble_inflate");
        public static String SOUND_SUBTITLE_GIANT_BUBBLE_POP = soundSubtitle("giant_bubble_pop");
        public static String SOUND_SUBTITLE_PET_BED_USES = soundSubtitle("pet_bed_uses");
        public static String SOUND_SUBTITLE_DRUM = soundSubtitle("drum");
        public static String SOUND_SUBTITLE_PSYCHIC_WALL = soundSubtitle("psychic_wall");
        public static String SOUND_SUBTITLE_PSYCHIC_WALL_DEFLECT = soundSubtitle("psychic_wall_deflect");
        public static String SOUND_SUBTITLE_BLAZING_PROTECTION = soundSubtitle("blazing_protection");

        // —— 杂项 ——
        public static String NOTIF_FRIENDLY_FIRE_PROTECTED = "notif.friendlyfire.protected";
        public static String JADE_COLLAR_TAG = "config.jade.plugin_" + PetHomeMod.MODID + ".collar_tag";

        // —— 宠物罗盘补充 ——
        public static String TOOLTIP_PET_COMPASS_DESC = tooltips("pet_compass.desc");
        public static String MESSAGE_PET_COMPASS_TITLE = message("pet_compass.title");
        public static String MESSAGE_PET_COMPASS_EMPTY = message("pet_compass.empty");
        public static String MESSAGE_PET_COMPASS_POSITION = message("pet_compass.position");
        public static String MESSAGE_PET_COMPASS_LAST_KNOWN_POS = message("pet_compass.last_known_pos");
        public static String MESSAGE_PET_COMPASS_DISTANCE = message("pet_compass.distance");
        public static String MESSAGE_PET_COMPASS_DIM_OVERWORLD = message("pet_compass.dim_overworld");
        public static String MESSAGE_PET_COMPASS_DIM_THE_NETHER = message("pet_compass.dim_the_nether");
        public static String MESSAGE_PET_COMPASS_DIM_THE_END = message("pet_compass.dim_the_end");
        public static String MESSAGE_PET_COMPASS_STATUS_LOADED = message("pet_compass.status_loaded");
        public static String MESSAGE_PET_COMPASS_STATUS_NOT_LOADED = message("pet_compass.status_not_loaded");
        public static String MESSAGE_PET_COMPASS_STATUS_WRONG_DIM = message("pet_compass.status_wrong_dim");
        public static String MESSAGE_PET_COMPASS_STATUS_DEAD = message("pet_compass.status_dead");
        public static String MESSAGE_PET_COMPASS_BUTTON_TP = message("pet_compass.button_tp");
        public static String MESSAGE_PET_COMPASS_BUTTON_RECALL = message("pet_compass.button_recall");
        public static String MESSAGE_PET_COMPASS_DISABLED = message("pet_compass.disabled");
        public static String MESSAGE_PET_COMPASS_DEAD_WAIT_RESPAWN = message("pet_compass.dead_wait_respawn");
        public static String MESSAGE_PET_COMPASS_NOT_LOADED_CONFIRM = message("pet_compass.not_loaded_confirm");
        public static String MESSAGE_PET_COMPASS_TELEPORTED = message("pet_compass.teleported");
        public static String MESSAGE_PET_COMPASS_RECALLED = message("pet_compass.recalled");
        public static String MESSAGE_PET_COMPASS_RECALL_STARTED = message("pet_compass.recall_started");
        public static String MESSAGE_PET_COMPASS_RECALL_FAILED = message("pet_compass.recall_failed");

}
