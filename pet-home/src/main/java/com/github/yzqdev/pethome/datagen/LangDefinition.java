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
    String killer_bunny = "entity.minecraft.killer_bunny";

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
    // —— 宠物罗盘 ——
    String petCompassEnable_conf = config("petCompassEnable");
    String petCompassEnable_conf_tooltip = configTooltip("petCompassEnable");
    String teleportPlayerToPet_conf = config("teleportPlayerToPet");
    String teleportPlayerToPet_conf_tooltip = configTooltip("teleportPlayerToPet");
    String teleportPetToPlayer_conf = config("teleportPetToPlayer");
    String teleportPetToPlayer_conf_tooltip = configTooltip("teleportPetToPlayer");
    // —— 宠物信息悬浮面板 ——
    String petInfoOverlay_conf = config("petInfoOverlay");
    String petInfoOverlay_conf_tooltip = configTooltip("petInfoOverlay");
    String petInfoOverlayRequireShift_conf = config("petInfoOverlayRequireShift");
    String petInfoOverlayRequireShift_conf_tooltip = configTooltip("petInfoOverlayRequireShift");
    String petInfoOverlayIgnoreJade_conf = config("petInfoOverlayIgnoreJade");
    String petInfoOverlayIgnoreJade_conf_tooltip = configTooltip("petInfoOverlayIgnoreJade");
    // —— 调试日志 ——
    String debugMode_conf = config("debugMode");
    String debugMode_conf_tooltip = configTooltip("debugMode");
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
    String muffled_loot_chance_conf = config("muffled_loot_chance");
    String muffled_loot_chance_conf_tooltip = configTooltip("muffled_loot_chance");

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
  // —— 自研配置界面（client/gui，Tab 单屏风格，与 1.20 侧界面同构）——
  String GUI_TITLE = config("gui.title");
  String GUI_RESET = config("gui.reset");
  String GUI_ON = config("gui.on");
  String GUI_OFF = config("gui.off");
  String GUI_LIST_ADD = config("gui.list.add");
  String GUI_LIST_ELEMENT_INDEX = config("gui.list.element_index");
  // 分类（friendly_fire / general / tameable / loot 与 toml 分组对应）
  String GUI_CAT_FRIENDLY_NAME = config("gui.category.friendly_fire");
  String GUI_CAT_GENERAL_NAME = config("gui.category.general");
  String GUI_CAT_TAMEABLE_NAME = config("gui.category.tameable");
  String GUI_CAT_LOOT_NAME = config("gui.category.loot");
  // 条目说明键后缀为 .tooltip（extractTooltip 按标签键 + ".tooltip" 运行时拼接），常量名 _DESC 仅沿用历史
  String GUI_F_PROTECT_OWNER = config("gui.friendly.protect_owner");
  String GUI_F_PROTECT_OWNER_DESC = configTooltip("gui.friendly.protect_owner");
  String GUI_F_PROTECT_PETS = config("gui.friendly.protect_pets");
  String GUI_F_PROTECT_PETS_DESC = configTooltip("gui.friendly.protect_pets");
  String GUI_F_PROTECT_CHILDREN = config("gui.friendly.protect_children");
  String GUI_F_PROTECT_CHILDREN_DESC = configTooltip("gui.friendly.protect_children");
  String GUI_F_REFLECT_DAMAGE = config("gui.friendly.reflect_damage");
  String GUI_F_REFLECT_DAMAGE_DESC = configTooltip("gui.friendly.reflect_damage");
  String GUI_F_DISPLAY_WARNING = config("gui.friendly.display_warning");
  String GUI_F_DISPLAY_WARNING_DESC = configTooltip("gui.friendly.display_warning");
  String GUI_F_PROTECT_TEAM = config("gui.friendly.protect_team");
  String GUI_F_PROTECT_TEAM_DESC = configTooltip("gui.friendly.protect_team");
  String GUI_F_RESPECT_TEAM = config("gui.friendly.respect_team");
  String GUI_F_RESPECT_TEAM_DESC = configTooltip("gui.friendly.respect_team");
  String GUI_F_CAN_HURT_PET_ITEMS = config("gui.friendly.can_hurt_pet_items");
  String GUI_F_CAN_HURT_PET_ITEMS_DESC = configTooltip("gui.friendly.can_hurt_pet_items");
  String GUI_F_CAN_HURT_ALL_ITEMS = config("gui.friendly.can_hurt_all_items");
  String GUI_F_CAN_HURT_ALL_ITEMS_DESC = configTooltip("gui.friendly.can_hurt_all_items");
  String GUI_F_NO_PROTECTION = config("gui.friendly.no_protection");
  String GUI_F_NO_PROTECTION_DESC = configTooltip("gui.friendly.no_protection");
  String GUI_F_EXTRA_PROTECTED = config("gui.friendly.extra_protected");
  String GUI_F_EXTRA_PROTECTED_DESC = configTooltip("gui.friendly.extra_protected");
  String GUI_F_PLAYER_PROTECTED = config("gui.friendly.player_protected");
  String GUI_F_PLAYER_PROTECTED_DESC = configTooltip("gui.friendly.player_protected");
  // —— 通用分类条目 ——
  String GUI_G_ROTTEN_APPLE = config("gui.general.rotten_apple");
  String GUI_G_ROTTEN_APPLE_DESC = configTooltip("gui.general.rotten_apple");
  String GUI_G_PET_BED_RESPAWN = config("gui.general.pet_bed_respawn");
  String GUI_G_PET_BED_RESPAWN_DESC = configTooltip("gui.general.pet_bed_respawn");
  String GUI_G_RABBITS_RAVAGERS = config("gui.general.rabbits_ravagers");
  String GUI_G_RABBITS_RAVAGERS_DESC = configTooltip("gui.general.rabbits_ravagers");
  String GUI_G_TRINARY_COMMAND = config("gui.general.trinary_command");
  String GUI_G_TRINARY_COMMAND_DESC = configTooltip("gui.general.trinary_command");
  String GUI_G_PETSTORE_WEIGHT = config("gui.general.petstore_weight");
  String GUI_G_PETSTORE_WEIGHT_DESC = configTooltip("gui.general.petstore_weight");
  String GUI_G_MOB_TAMABLE_ONLY = config("gui.general.mob_tamable_only");
  String GUI_G_MOB_TAMABLE_ONLY_DESC = configTooltip("gui.general.mob_tamable_only");
  String GUI_G_MOB_BLACKLIST = config("gui.general.mob_blacklist");
  String GUI_G_MOB_BLACKLIST_DESC = configTooltip("gui.general.mob_blacklist");
  String GUI_G_PET_COMPASS_ENABLE = config("gui.general.pet_compass_enable");
  String GUI_G_PET_COMPASS_ENABLE_DESC = configTooltip("gui.general.pet_compass_enable");
  String GUI_G_TELEPORT_PLAYER_TO_PET = config("gui.general.teleport_player_to_pet");
  String GUI_G_TELEPORT_PLAYER_TO_PET_DESC = configTooltip("gui.general.teleport_player_to_pet");
  String GUI_G_RECALL_PET_TO_PLAYER = config("gui.general.recall_pet_to_player");
  String GUI_G_RECALL_PET_TO_PLAYER_DESC = configTooltip("gui.general.recall_pet_to_player");
  String GUI_G_PET_INFO_OVERLAY = config("gui.general.pet_info_overlay");
  String GUI_G_PET_INFO_OVERLAY_DESC = configTooltip("gui.general.pet_info_overlay");
  String GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT =
      config("gui.general.pet_info_overlay_require_shift");
  String GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT_DESC =
      configTooltip("gui.general.pet_info_overlay_require_shift");
  String GUI_G_PET_INFO_OVERLAY_IGNORE_JADE = config("gui.general.pet_info_overlay_ignore_jade");
  String GUI_G_PET_INFO_OVERLAY_IGNORE_JADE_DESC =
      configTooltip("gui.general.pet_info_overlay_ignore_jade");
  String GUI_G_DEBUG_MODE = config("gui.general.debug_mode");
  String GUI_G_DEBUG_MODE_DESC = configTooltip("gui.general.debug_mode");
  // —— 可驯化分类条目 ——
  String GUI_T_AXOLOTL = config("gui.tameable.axolotl");
  String GUI_T_AXOLOTL_DESC = configTooltip("gui.tameable.axolotl");
  String GUI_T_FOX = config("gui.tameable.fox");
  String GUI_T_FOX_DESC = configTooltip("gui.tameable.fox");
  String GUI_T_FROG = config("gui.tameable.frog");
  String GUI_T_FROG_DESC = configTooltip("gui.tameable.frog");
  String GUI_T_HORSE = config("gui.tameable.horse");
  String GUI_T_HORSE_DESC = configTooltip("gui.tameable.horse");
  String GUI_T_RABBIT = config("gui.tameable.rabbit");
  String GUI_T_RABBIT_DESC = configTooltip("gui.tameable.rabbit");

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
    String ITEM_DEED_OF_OWNERSHIP_DESC2 = item("deed_of_ownership.desc2");
    String ITEM_DEED_OF_OWNERSHIP_BOUND = item("deed_of_ownership.bound");
    String ITEM_WHISTLE = item("whistle");
    String TOOLTIP_WHISTLE_DESC = tooltips("whistle.desc");
    String ITEM_FREEDOM_BELL = item("freedom_bell");
    String TOOLTIP_FREEDOM_BELL_DESC = tooltips("freedom_bell.desc");
    String ITEM_NET = item("net");
    String ITEM_NET_HAS_ITEM = item("net_has_item");
    String ITEM_NET_LAUNCHER = item("net_launcher");
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
    String MESSAGE_GOODBYE = message("goodbye");
    String MESSAGE_REMOVE_RESPAWN = message("remove_respawn");
    String MESSAGE_RESPAWN = message("respawn");
    String MESSAGE_SET_OWNER = message("set_owner");
    String MESSAGE_DEED_BOUND = message("deed_bound");
    String MESSAGE_RELEASE_CONFIRM = message("release_confirm");
    String MESSAGE_RELEASED = message("released");
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
    String SOUND_SUBTITLE_PET_BED_USES = soundSubtitle("pet_bed_use");
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