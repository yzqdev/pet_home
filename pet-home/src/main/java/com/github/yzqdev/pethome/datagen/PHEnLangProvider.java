package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class PHEnLangProvider extends LanguageProvider {
    public PHEnLangProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {
        add(LangDefinition.TOOLTIPS_SUBSTITUTE_FEATHER_DESC, "[Allows pets to move to the fishing rod landing point, e.g. moving pets to a pet bed]");
        add(LangDefinition.TOOLTIPS_SUBSTITUTE_ROTTEN_APPLE_DESC, "[Feed to horses to turn them into zombie horses]");
        add(LangDefinition.TOOLTIPS_SUBSTITUTE_SINISTER_CARROT_DESC, "[Feed to a rabbit to turn it into a killer rabbit, feed to a zombie horse to turn it into a skeleton horse]");
        add(LangDefinition.TOOLTIPS_WAYWARD_LANTERN_DESC, "[Place near you, pets that get unloaded far away will be teleported back to the lantern]");
        add(LangDefinition.TOOLTIPS_DRUM_DESC, "[Right-click to cycle the command for your pets within 32 blocks, can also be triggered by redstone]");
        add(LangDefinition.TOOLTIPS_SUBSTITUTE_COLLAR_DESC, "[Right-click on pets to put a collar on them]");
        add(LangDefinition.TOOLTIPS_SUBSTITUTE_PET_BED_DESC, "[Place pets on pet beds, pets will respawn the next day after death]");
        add(LangDefinition.ITEM_GROUP, "Pet home | Items");
        add(LangDefinition.ITEM_COLLAR_TAG, "Collar Tag");
        add(LangDefinition.ITEM_ROTTEN_APPLE, "Rotten Apple");
        add(LangDefinition.ITEM_SINISTER_CARROT, "Sinister Carrot");
        add(LangDefinition.ITEM_DEFLECTION_SHIELD, "Deflection Shield Model");
        add(LangDefinition.ITEM_MAGNET, "Magnet Model");
        add(LangDefinition.ITEM_FEATHER_ON_A_STICK, "Feather on a Stick");
        add(LangDefinition.ITEM_DEED_OF_OWNERSHIP, "Pet Contract");
        add(LangDefinition.ITEM_DEED_OF_OWNERSHIP_DESC, "[Right-click your pet: bind the contract; use it again on the same pet: transfer ownership]");
        add(LangDefinition.ITEM_DEED_OF_OWNERSHIP_DESC2, "[To release a pet, use the Freedom Bell instead]");
        add(LangDefinition.ITEM_DEED_OF_OWNERSHIP_BOUND, "Bound to: %s");
        add(LangDefinition.ITEM_PET_COMPASS, "Pet Compass");
        add(LangDefinition.TOOLTIP_PET_COMPASS_DESC, "[Right-click to open the pet list]");
        add(LangDefinition.ITEM_WHISTLE, "Pet Whistle");
        add(LangDefinition.TOOLTIP_WHISTLE_DESC, "[Right-click to recall all your pets]");
        add(LangDefinition.ITEM_FREEDOM_BELL, "Freedom Bell");
        add(LangDefinition.TOOLTIP_FREEDOM_BELL_DESC, "[Use on a pet to release it (press again to confirm)]");
        add(LangDefinition.message("whistle_summary"), "Whistled: %s pet(s) recalled or recall queued");
        add(LangDefinition.message("whistle_none"), "No pets to recall");
        add(LangDefinition.message("pet_compass.title"), "Pet Compass");
        add(LangDefinition.message("pet_compass.empty"), "No trackable pets yet");
        add(LangDefinition.message("pet_compass.position"), "Position: ");
        add(LangDefinition.message("pet_compass.last_known_pos"), "Last known: ");
        add(LangDefinition.message("pet_compass.distance"), "Dist: %s m");
        add(LangDefinition.message("pet_compass.status_loaded"), "Status: loaded");
        add(LangDefinition.message("pet_compass.status_not_loaded"), "Status: not loaded");
        add(LangDefinition.message("pet_compass.status_in_ball"), "Status: in recall ball (open it to retrieve)");
        add(LangDefinition.message("pet_compass.status_in_net"), "Status: in creature ball (use on a block to release)");
        add(LangDefinition.message("pet_compass.status_wrong_dim"), "Status: in another dimension");
        add(LangDefinition.message("pet_compass.status_dead"), "Status: dead, waiting for pet bed respawn");
        add(LangDefinition.message("pet_compass.button_tp"), "Go to pet");
        add(LangDefinition.message("pet_compass.button_recall"), "Recall");
        add(LangDefinition.message("pet_compass.button_forget"), "Remove");
        add(LangDefinition.message("pet_compass.button_confirm_forget"), "Confirm?");
        add(LangDefinition.message("pet_compass.status_stale"), "Status: record is stale");
        add(LangDefinition.message("pet_compass.forgotten"), "Removed the entry from the pet compass");
        add(LangDefinition.message("pet_compass.disabled"), "The pet compass is disabled in the config");
    add(LangDefinition.message("pet_compass.dim_label"), "Dim: ");
    add(LangDefinition.message("pet_compass.health"), "HP: %s/%s");
    add(LangDefinition.message("pet_compass.cmd_0"), "Wandering");
    add(LangDefinition.message("pet_compass.cmd_1"), "Staying");
    add(LangDefinition.message("pet_compass.cmd_2"), "Following");
        add(LangDefinition.message("pet_compass.not_loaded_confirm"), "Pet is not loaded, teleporting to its last known position");
        add(LangDefinition.message("pet_compass.dead_wait_respawn"), "This pet has died and is waiting to respawn at its pet bed");
        add(LangDefinition.message("pet_compass.recall_started"), "Searching for the pet, please wait...");
        add(LangDefinition.message("pet_compass.recall_failed"), "Could not find the pet, please try again later");
        add(LangDefinition.message("pet_compass.teleported"), "Teleported to your pet");
        add(LangDefinition.message("pet_compass.recalled"), "%s has been recalled to you");
        add(LangDefinition.message("pet_compass.ball_recalled"), "The recall ball holding %s has been brought to you — right-click it to open");
        add(LangDefinition.BLOCK_PET_BED_WHITE, "White Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_ORANGE, "Orange Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_MAGENTA, "Magenta Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_LIGHT_BLUE, "Light Blue Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_YELLOW, "Yellow Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_LIME, "Lime Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_PINK, "Pink Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_GRAY, "Gray Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_LIGHT_GRAY, "Light Gray Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_CYAN, "Cyan Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_PURPLE, "Purple Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_BLUE, "Blue Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_BROWN, "Brown Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_GREEN, "Green Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_RED, "Red Pet Bed");
        add(LangDefinition.BLOCK_PET_BED_BLACK, "Black Pet Bed");
        add(LangDefinition.BLOCK_DRUM, "Command Drum");
        add(LangDefinition.BLOCK_WAYWARD_LANTERN, "Wayward Lantern");
        add(LangDefinition.MESSAGE_COMMAND_0, "%s is wandering");
        add(LangDefinition.MESSAGE_COMMAND_1, "%s is staying");
        add(LangDefinition.MESSAGE_COMMAND_2, "%s is following");
        add(LangDefinition.MESSAGE_DRUM_COMMAND_0, "ordered %s to wander");
        add(LangDefinition.MESSAGE_DRUM_COMMAND_1, "ordered %s to stay");
        add(LangDefinition.MESSAGE_DRUM_COMMAND_2, "ordered %s to follow");
        add(LangDefinition.MESSAGE_RESPAWN, "%s has respawned at its bed");
        add(LangDefinition.MESSAGE_REMOVE_RESPAWN, "Removed respawn bed of %s");
        add(LangDefinition.MESSAGE_GOODBYE, "%s will not respawn. Goodbye...");
        add(LangDefinition.MESSAGE_ENCHANTMENTS, "Enchantments:");
        add(LangDefinition.MESSAGE_SET_OWNER, "%s is now the owner of %s");
        add(LangDefinition.MESSAGE_DEED_BOUND, "Contract bound to %s");
        add(LangDefinition.MESSAGE_RELEASE_CONFIRM, "Use the Freedom Bell on %s again to confirm release");
        add(LangDefinition.MESSAGE_RELEASED, "Released %s");
        add(LangDefinition.MESSAGE_WAYWARD_LANTERN_RETURN, "%s has found its way to a nearby Wayward Lantern");
        add(LangDefinition.ENTITY_ANIMAL_TAMER, "Animal Tamer");
        add(LangDefinition.ENTITY_CHAIN_LIGHTNING, "Lightning");
        add(LangDefinition.ENTITY_RECALL_BALL, "Recall Orb");
        add(LangDefinition.ENTITY_FEATHER, "Feather");
        add(LangDefinition.ENTITY_FOLLOWING_JUKEBOX, "Floating Jukebox");
        add(LangDefinition.ENTITY_PSYCHIC_WALL, "Psychic Wall");
        addEnchant(PHEnchantments.SonicBoom, "Sonic Boom", "Sonic boom knocks enemies back, triggered periodically");
        addEnchant(PHEnchantments.HEALTH_BOOST, "Extra Health", "Increases pet's max health by 10 for each level");
        addEnchant(PHEnchantments.FIREPROOF, "Fireproof", "Grants pet immunity to fire and lava damage");
        addEnchant(PHEnchantments.IMMUNITY_FRAME, "Immunity Frame", "Each level makes pet immune for an additional second after attack");
        addEnchant(PHEnchantments.DEFLECTION, "Deflecting", "Pet is protected by a ghostly shield which deflects projectile attacks");
        addEnchant(PHEnchantments.POISON_RESISTANCE, "Poison Resistance", "Grants pet immunity to poison effect");
        addEnchant(PHEnchantments.CHAIN_LIGHTNING, "Chain Lightning", "Summons a lightning bolt that chains from and damages mobs each time a pet attacks, level increases how many mobs can be hit by lightning");
        addEnchant(PHEnchantments.SPEEDSTER, "Speedster", "Increases pet's movement speed");
        addEnchant(PHEnchantments.FROST_FANG, "Frost Fang", "Pet attacks now slows mobs shortly and deals freezing damage");
        addEnchant(PHEnchantments.MAGNETIC, "Magnetic", "Pet aided by ghostly magnet that pulls targeted mobs towards it");
        addEnchant(PHEnchantments.LINKED_INVENTORY, "Linked Inventory", "Pet can pick up items and teleport them to owner's inventory");
        addEnchant(PHEnchantments.TOTAL_RECALL, "Total Recall", "When below 2 health, pet enters a recall orb and is protected until released by its owner");
        addEnchant(PHEnchantments.HEALTH_SIPHON, "Health Siphon", "Any damage done to pet is transferred to its owner");
        addEnchant(PHEnchantments.BUBBLING, "Bubbling", "Pet attacks trap mob inside giant bubble that floats upwards");
        addEnchant(PHEnchantments.SHEPHERD, "Herding", "Pet can be followed by wild animals, level increases maximum amount of followers");
        addEnchant(PHEnchantments.AMPHIBIOUS, "Amphibious", "Pet cannot dry out or drown on land or in water, increases speed in water, pet will not float but swim quickly");
        addEnchant(PHEnchantments.VAMPIRE, "Vampire's Familiar", "Pet heals amount of damage done, level increases percentage of dealt damage healed");
        addEnchant(PHEnchantments.VOID_CLOUD, "Void Cloud", "Pet is protected from falling off cliffs or into void by a large cloud that lifts it up and returns it to its owner");
        addEnchant(PHEnchantments.INFAMY_CURSE, "Curse of Infamy", "Pet can naturally make any nearby monster hostile to it");
        addEnchant(PHEnchantments.SHADOW_HANDS, "Shadow Hands", "Pet uses dark magic to attack targets with shadowy hands, level increases speed and amount of hands");
        addEnchant(PHEnchantments.DEFUSAL, "Defusal", "Pet can nullify explosion damage to terrain and other mobs, level increases range");
        addEnchant(PHEnchantments.WARPING_BITE, "Warping Bite", "Pet will randomly teleport targeted mobs away from it");
        addEnchant(PHEnchantments.ORE_SCENTING, "Ore Scenting", "Pet can sniff out ore blocks and alert its owner, level determines distance, amount and frequency of ore detection");
        addEnchant(PHEnchantments.GLUTTONOUS, "Gluttonous", "Pet can eat any food item, regardless of dietary restrictions");
        addEnchant(PHEnchantments.PSYCHIC_WALL, "Psychic Wall", "Pet summons a wall of psychic energy during combat to provide cover, level determines size of wall and length of the effect");
        addEnchant(PHEnchantments.INTIMIDATION, "Intimidation", "Pet can scare away hostile mobs with its fearsome appearance, level determines range and frequency of effect");
        addEnchant(PHEnchantments.BLIGHT_CURSE, "Curse of Blight", "Plants will wither and die around the pet.");
        addEnchant(PHEnchantments.TETHERED_TELEPORT, "Tethered Teleport", "Pet will teleport with its owner, even across dimensions.");
        addEnchant(PHEnchantments.IMMATURITY_CURSE, "Curse of Immaturity", "Pet appears and behaves as a baby version of itself, with reduced attack damage.");
        addEnchant(PHEnchantments.BLAZING_PROTECTION, "Blazing Protection", "Pet will be protected from damage by 2 blazing bars per level, which knockback and set attackers on fire.");
        addEnchant(PHEnchantments.HEALING_AURA, "Healing Aura", "Pet will ocassionally heal owner and other pets around it, level determines strength of healing effect.");
        addEnchant(PHEnchantments.XP_Transfer, "XP Transfer", "Pets collect and transfer XP to owner");
        addEnchant(PHEnchantments.REJUVENATION, "Rejuvenation", "When injured, pet may absorb experience orbs to heal itself.");
        addEnchant(PHEnchantments.SHARE, "Share", "When the pet is attacked, it shares damage with nearby hostile mobs");
        addEnchant(PHEnchantments.NIGHT_VISION, "Night Vision", "Grants Night Vision effect when a pet is nearby");
        addEnchant(PHEnchantments.INSIGHT, "Insight", "In dark areas, pets highlight surrounding hostile mobs");
        addEnchant(PHEnchantments.CHAOS, "Chaos Mind", "When attacked, pets inflict Chaos on the attacker, forcing them to attack other monsters within 10 blocks");
        addEnchant(PHEnchantments.PARALYSIS, "Paralysis", "When attacked, pets paralyze the attacker for several seconds, preventing movement, jumping or attacking");
        addEnchant(PHEnchantments.TOUGH, "Tough", "Increase pet defense and knockback resistance");
        addEnchant(PHEnchantments.VIOLENT, "Violent", "Monster will get some debuffs when attacked by pet");
        // 3 enchantments backported from 1.20.1
        addEnchant(PHEnchantments.CHARISMA, "Charismatic", "Pet lowers trade prices for its owner, level increases the bargain");
        addEnchant(PHEnchantments.DISC_JOCKEY, "Disc Jockey", "Pet is followed by a floating jukebox that can play music discs");
        addEnchant(PHEnchantments.MUFFLED, "Muffled", "Pet will be silent and not emit vibrations when moving.");
        add(LangDefinition.SOUND_SUBTITLE_COLLAR_TAG, "equipped Collar Tag");
        add(LangDefinition.SOUND_SUBTITLE_MAGNET_LOOP, "Magnet buzzes");
        add(LangDefinition.SOUND_SUBTITLE_CHAIN_LIGHTNING, "Lightning zaps");
        add(LangDefinition.SOUND_SUBTITLE_GIANT_BUBBLE_INFLATE, "Giant Bubble inflates");
        add(LangDefinition.SOUND_SUBTITLE_GIANT_BUBBLE_POP, "Giant Bubble pops");
        add(LangDefinition.SOUND_SUBTITLE_PET_BED_USES, "Pet Bed adjusted");
        add(LangDefinition.SOUND_SUBTITLE_DRUM, "Command Drum beats");
        add(LangDefinition.SOUND_SUBTITLE_PSYCHIC_WALL, "Psychic Wall hums");
        add(LangDefinition.SOUND_SUBTITLE_PSYCHIC_WALL_DEFLECT, "Psychic Wall deflects");
        add(LangDefinition.SOUND_SUBTITLE_BLAZING_PROTECTION, "Blazing bar dissapears");
        add(LangDefinition.NOTIF_FRIENDLY_FIRE_PROTECTED, "The %s is protected. Crouch to bypass.");

        add(LangDefinition.JADE_COLLAR_TAG, "Pet Home Info");
        add(LangDefinition.animal_tamer_villager_conf, "Animal Tamer");
        add(LangDefinition.protectChildren_conf_tooltip, "Whether to protect juvenile creatures");
        add(LangDefinition.displayHitWarning_conf_tooltip, "Whether to display protection warnings");
        add(LangDefinition.rotten_apple_conf, "Apples become rotten apples when disappearing");
        add(LangDefinition.rotten_apple_conf_tooltip, "Apples become rotten apples when disappearing");
        add(LangDefinition.sinister_carrot_loot_chance_conf_tooltip, "Probability of Sinister Carrot in woodland mansion chests");
        add(LangDefinition.sinister_carrot_loot_chance_conf, "Probability of Sinister Carrot in chests");
        add(LangDefinition.petstore_village_weight_conf_tooltip, "Spawn weight of Animal Tamer buildings in villages,need to logout world and then login to take effect");
        add(LangDefinition.petstore_village_weight_conf, "Spawn weight of Animal Tamer buildings in villages");
        add(LangDefinition.protectChildren_conf, "Protect juvenile creatures");
        add(LangDefinition.respectTeamRules_conf, "Respect team rules");
        add(LangDefinition.respectTeamRules_conf_tooltip, "Match team rules: friendly fire on/off based on team settings");
        add(LangDefinition.protectPetsFromOwner_conf_tooltip, "Owners cannot harm their pets");
        add(LangDefinition.protectPetsFromOwner_conf, "Owners cannot harm their pets");
        add(LangDefinition.displayHitWarning_conf, "Display protection warnings");
        add(LangDefinition.protectTeamMembers_conf_tooltip, "No damage between creatures in the same team");
        add(LangDefinition.protectTeamMembers_conf, "No damage between teammates");
        add(LangDefinition.protectPetsFromPets_conf_tooltip, "No damage between pets");
        add(LangDefinition.protectPetsFromPets_conf, "No damage between pets");
        add(LangDefinition.reflectDamage_conf, "Reflect damage");
        add(LangDefinition.reflectDamage_conf_tooltip, "Reflect damage (owner takes damage when attacking creatures)");
        add(LangDefinition.petCompassEnable_conf, "Pet Compass");
        add(LangDefinition.petCompassEnable_conf_tooltip, "Whether to enable the pet compass system");
        add(LangDefinition.teleportPlayerToPet_conf, "Teleport Player to Pet");
        add(LangDefinition.teleportPlayerToPet_conf_tooltip, "Whether to allow teleporting the player to the pet");
        add(LangDefinition.teleportPetToPlayer_conf, "Recall Pet");
        add(LangDefinition.teleportPetToPlayer_conf_tooltip, "Whether to allow recalling (teleporting) the pet to the player");
        add(LangDefinition.petInfoOverlay_conf, "Pet Info Panel");
        add(LangDefinition.petInfoOverlay_conf_tooltip, "Show the pet info panel at the top of the screen while looking at your own pet");
        add(LangDefinition.petInfoOverlayRequireShift_conf, "Require Sneak");
        add(LangDefinition.petInfoOverlayRequireShift_conf_tooltip, "Require holding the sneak key while looking at the pet to show the panel (off = just look at the pet)");
        add(LangDefinition.petInfoOverlayIgnoreJade_conf, "Show Even with Jade");
        add(LangDefinition.debugMode_conf, "Debug Logs");

    // —— 自研配置界面（client/gui）——
    add(LangDefinition.GUI_TITLE, "Pet Home Settings");
    add(LangDefinition.GUI_RESET, "Reset");
    add(LangDefinition.GUI_ON, "ON");
    add(LangDefinition.GUI_OFF, "OFF");
    add(LangDefinition.GUI_LIST_ADD, "+");
    add(LangDefinition.GUI_LIST_ELEMENT_INDEX, "%s:");
    add(LangDefinition.GUI_CAT_FRIENDLY_NAME, "Friendly Fire");
    add(LangDefinition.GUI_CAT_GENERAL_NAME, "General");
    add(LangDefinition.GUI_CAT_TAMEABLE_NAME, "Tameable Mobs");
    add(LangDefinition.GUI_CAT_LOOT_NAME, "Loot");
    add(LangDefinition.GUI_F_PROTECT_OWNER, "Protect Pets from Owner");
    add(LangDefinition.GUI_F_PROTECT_OWNER_DESC, "When on, the owner cannot hurt their own pets");
    add(LangDefinition.GUI_F_PROTECT_PETS, "No Damage Between Pets");
    add(LangDefinition.GUI_F_PROTECT_PETS_DESC, "When on, pets cannot hurt other pets");
    add(LangDefinition.GUI_F_PROTECT_CHILDREN, "Protect Baby Animals");
    add(LangDefinition.GUI_F_PROTECT_CHILDREN_DESC, "When on, baby animals cannot be hurt");
    add(LangDefinition.GUI_F_REFLECT_DAMAGE, "Reflect Damage");
    add(LangDefinition.GUI_F_REFLECT_DAMAGE_DESC, "When on, players attacking protected creatures take the damage back");
    add(LangDefinition.GUI_F_DISPLAY_WARNING, "Show Protection Message");
    add(
        LangDefinition.GUI_F_DISPLAY_WARNING_DESC,
        "When on, attacking a protected creature shows a chat message");
    add(LangDefinition.GUI_F_PROTECT_TEAM, "Protect Team Members");
    add(LangDefinition.GUI_F_PROTECT_TEAM_DESC, "When on, pets and players on the same team cannot hurt each other");
    add(LangDefinition.GUI_F_RESPECT_TEAM, "Respect Team Rules");
    add(
        LangDefinition.GUI_F_RESPECT_TEAM_DESC,
        "When on, protection follows the team's friendly fire setting: teams without friendly fire are protected");
    add(LangDefinition.GUI_F_CAN_HURT_PET_ITEMS, "Shift-Free Pet-Hurting Items");
    add(
        LangDefinition.GUI_F_CAN_HURT_PET_ITEMS_DESC,
        "Items in this list can hurt pets without holding Shift (sneak)");
    add(LangDefinition.GUI_F_CAN_HURT_ALL_ITEMS, "Protection-Bypassing Items");
    add(
        LangDefinition.GUI_F_CAN_HURT_ALL_ITEMS_DESC,
        "Items in this list bypass all mod protections and can hurt protected creatures");
    add(LangDefinition.GUI_F_NO_PROTECTION, "Unprotected Entities");
    add(LangDefinition.GUI_F_NO_PROTECTION_DESC, "Entities in this list receive no protection (e.g. piglins)");
    add(LangDefinition.GUI_F_EXTRA_PROTECTED, "Extra Protected Entities");
    add(
        LangDefinition.GUI_F_EXTRA_PROTECTED_DESC,
        "Non-pet entities in this list are protected like pets");
    add(LangDefinition.GUI_F_PLAYER_PROTECTED, "Player-Immune Entities");
    add(LangDefinition.GUI_F_PLAYER_PROTECTED_DESC, "Entities in this list cannot be hurt by players");
    add(LangDefinition.GUI_G_ROTTEN_APPLE, "Rotten Apples");
    add(
        LangDefinition.GUI_G_ROTTEN_APPLE_DESC,
        "When on, apples turn into rotten apples when they despawn");
    add(LangDefinition.GUI_G_PET_BED_RESPAWN, "Pet Bed Respawns");
    add(
        LangDefinition.GUI_G_PET_BED_RESPAWN_DESC,
        "When on, pets respawn in their bound pet bed the next morning after dying");
    add(LangDefinition.GUI_G_RABBITS_RAVAGERS, "Rabbits Scare Ravagers");
    add(
        LangDefinition.GUI_G_RABBITS_RAVAGERS_DESC,
        "When on, rabbits scare ravagers like they used to");
    add(LangDefinition.GUI_G_TRINARY_COMMAND, "Trinary Command System");
    add(
        LangDefinition.GUI_G_TRINARY_COMMAND_DESC,
        "When on, pets can be set to wander, stay or follow");
    add(LangDefinition.GUI_G_PETSTORE_WEIGHT, "Pet Store Weight");
    add(
        LangDefinition.GUI_G_PETSTORE_WEIGHT_DESC,
        "Spawn weight of pet stores in villages, set to 0 to disable; requires re-entering the world");
    add(LangDefinition.GUI_G_MOB_TAMABLE_ONLY, "Catch Tamable Only");
    add(LangDefinition.GUI_G_MOB_TAMABLE_ONLY_DESC, "When on, catchers can only catch tameable animals");
    add(LangDefinition.GUI_G_MOB_BLACKLIST, "Catcher Blacklist");
    add(
        LangDefinition.GUI_G_MOB_BLACKLIST_DESC,
        "Entities in this list cannot be caught");
    add(LangDefinition.GUI_G_PET_COMPASS_ENABLE, "Pet Compass");
    add(LangDefinition.GUI_G_PET_COMPASS_ENABLE_DESC, "Master switch for the pet compass system");
    add(LangDefinition.GUI_G_TELEPORT_PLAYER_TO_PET, "Teleport Player to Pet");
    add(
        LangDefinition.GUI_G_TELEPORT_PLAYER_TO_PET_DESC,
        "When on, the pet compass can teleport the player to the pet");
    add(LangDefinition.GUI_G_RECALL_PET_TO_PLAYER, "Recall Pet to Player");
    add(
        LangDefinition.GUI_G_RECALL_PET_TO_PLAYER_DESC,
        "When on, the pet compass can teleport (recall) the pet to the player");
    add(LangDefinition.GUI_G_PET_INFO_OVERLAY, "Pet Info Panel");
    add(
        LangDefinition.GUI_G_PET_INFO_OVERLAY_DESC,
        "When on, looking at your own pet shows an info panel at the top of the screen");
    add(LangDefinition.GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT, "Require Sneak");
    add(
        LangDefinition.GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT_DESC,
        "When on, the info panel only shows while sneaking and looking at the pet");
    add(LangDefinition.GUI_G_PET_INFO_OVERLAY_IGNORE_JADE, "Show Even with Jade");
    add(
        LangDefinition.GUI_G_PET_INFO_OVERLAY_IGNORE_JADE_DESC,
        "The panel hides by default when Jade is installed (its tooltip already shows pet info); enable to force it visible");
    add(LangDefinition.GUI_G_DEBUG_MODE, "Debug Logs");
    add(
        LangDefinition.GUI_G_DEBUG_MODE_DESC,
        "When on, prints debug logs for troubleshooting; keep off for normal play");
    add(LangDefinition.GUI_T_AXOLOTL, "Tameable Axolotls");
    add(
        LangDefinition.GUI_T_AXOLOTL_DESC,
        "When on, axolotls can be fully tamed with tropical fish");
    add(LangDefinition.GUI_T_FOX, "Tameable Foxes");
    add(
        LangDefinition.GUI_T_FOX_DESC,
        "When on, foxes can be fully tamed with sweet berries");
    add(LangDefinition.GUI_T_FROG, "Tameable Frogs");
    add(
        LangDefinition.GUI_T_FROG_DESC,
        "When on, frogs can be fully tamed with spider eyes");
    add(LangDefinition.GUI_T_HORSE, "Tameable Horses");
    add(
        LangDefinition.GUI_T_HORSE_DESC,
        "When on, horses, donkeys, llamas, etc. can be enchanted, use pet beds and other pet features");
    add(LangDefinition.GUI_T_RABBIT, "Tameable Rabbits");
    add(
        LangDefinition.GUI_T_RABBIT_DESC,
        "When on, rabbits can be fully tamed with carrots");
        add(LangDefinition.debugMode_conf_tooltip, "Enable debug logs for troubleshooting (key systems print diagnostic info when on)");
        add(LangDefinition.petInfoOverlayIgnoreJade_conf_tooltip, "Show the panel even when Jade is installed (with Jade present the panel is hidden by default since its tooltip already shows pet info)");
        add(LangDefinition.ore_scenting_loot_chance_conf, "Probability of Ore Scenting enchantment book");
        add(LangDefinition.ore_scenting_loot_chance_conf_tooltip, "Probability of Ore Scenting enchantment book in minecart chests (abandoned mineshafts)");
        add(LangDefinition.bubbling_loot_chance_conf, "Probability of Bubbling enchantment book");
        add(LangDefinition.bubbling_loot_chance_conf_tooltip, "Probability of Bubbling enchantment book in buried treasure chests");
        add(LangDefinition.blazing_protection_loot_chance_conf_tooltip, "Probability of Blazing Protection enchantment book in nether fortress chests");
        add(LangDefinition.blazing_protection_loot_chance_conf, "Probability of Blazing Protection enchantment book");
        add(LangDefinition.vampirism_loot_chance_conf, "Probability of Vampirism enchantment book");
        add(LangDefinition.vampirism_loot_chance_conf_tooltip, "Probability of Vampirism enchantment book in woodland mansion chests");
        add(LangDefinition.other_should_protect_entity_conf_tooltip, "Entities immune to all attacks");
        add(LangDefinition.other_should_protect_entity_conf, "Entities immune to all attacks");

        add(LangDefinition.no_protection_entity_conf_tooltip, "Creatures not to protect, e.g. Piglins");
        add(LangDefinition.no_protection_entity_conf, "Unprotected creatures");
        add(LangDefinition.player_cant_hurt_entity_conf_tooltip, "Creatures players cannot attack, e.g. Zombified Piglins");
        add(LangDefinition.player_cant_hurt_entity_conf, "Player-attack immune creatures");
        add(LangDefinition.can_hurt_pet_item_conf_tooltip, "Items that can harm pets without needing to press Shift");
        add(LangDefinition.can_hurt_pet_item_conf, "Pet-harming items");
        add(LangDefinition.can_hurt_all_conf_tooltip, "Items that bypass all mod protections to damage creatures without needing to press Shift");
        add(LangDefinition.can_hurt_all_conf, "All-protection bypass items");

        add(LangDefinition.effect("drunk"), "Chaos");
        add(LangDefinition.sonic_boom_loot_chance_conf_tooltip, "Probability of Sonic Boom enchantment book appearing in Ancient City chests");
        add(LangDefinition.sonic_boom_loot_chance_conf, "Sonic Boom Enchantment Chance");
        add(LangDefinition.share_loot_chance_conf_tooltip, "Probability of Share enchantment book appearing in End City chests");
        add(LangDefinition.share_loot_chance_conf, "Share Enchantment Chance");
        add(LangDefinition.paralysis_loot_chance_conf_tooltip, "Probability of Paralysis enchantment book in abandoned mineshaft and desert pyramid chests, and bonus chests");
        add(LangDefinition.paralysis_loot_chance_conf, "Paralysis Enchantment Chance");

        add(LangDefinition.has_pet_bed_at_pos, "Bound pet bed at (%s)");
        add(PHItemRegistry.NET_LAUNCHER_ITEM.get(), "Creature Capture Launcher");
        add(PHItemRegistry.NET_ITEM.get(), "Capture Ball");
        add(PHItemRegistry.NET_HAS_ITEM.get(), "Capture Ball (Occupied)");
        add(LangDefinition.capturing_text, "Capturing");
        add(LangDefinition.release_text, "Release");
        add(LangDefinition.health_text, "Health");
        add(LangDefinition.no_net_entity_text, "No creature available to release");
        add(LangDefinition.net_launcher_tip, "[Hold Shift + Right Click to toggle between Capture and Release modes]");
        add(LangDefinition.net_launcher_default_only_tamable, "By default, only tamable mobs can be captured (configurable in settings file).");
        add(LangDefinition.mobcatcherOnlyTamableAnimal_conf, "Only tamable mobs can be captured");
        add(LangDefinition.mobcatcherOnlyTamableAnimal_conf_tooltip, "Disable to allow capturing any mob");
        add(LangDefinition.mobcatcherBlacklist_conf, "Mob Catcher prohibited list");
        add(LangDefinition.mobcatcherBlacklist_conf_tooltip, "Mobs that are blocked from being captured");

        add(LangDefinition.tough_loot_chance_conf_tooltip, "Probability of Tough enchantment book appearing in Abandoned Mineshaft chests");
        add(LangDefinition.tough_loot_chance_conf, "Tough Enchantment Chance");
        add(LangDefinition.muffled_loot_chance_conf_tooltip, "Probability of Muffled enchantment book appearing in Ancient City chests");
        add(LangDefinition.muffled_loot_chance_conf, "Muffled Enchantment Chance");
        // 自 1.20 移植的开关翻译
        add(LangDefinition.config("tameable_axolotls"), "Tameable Axolotls");
        add(LangDefinition.config("tameable_fox"), "Tameable Foxes");
        add(LangDefinition.config("tameable_frog"), "Tameable Frogs");
        add(LangDefinition.config("tameable_horse"), "Tameable Horses");
        add(LangDefinition.config("tameable_rabbit"), "Tameable Rabbits");
        add(LangDefinition.config("trinary_command_system"), "Trinary Command System");
        add(LangDefinition.config("pet_bed_respawns"), "Pet Bed Respawns");
        add(LangDefinition.config("rabbits_scare_ravagers"), "Rabbits Scare Ravagers");
        add(LangDefinition.tameable_axolotls_conf_tooltip, "Whether axolotls are fully tameable (tame with a tropical fish)");
        add(LangDefinition.tameable_fox_conf_tooltip, "Whether foxes are fully tameable (tame with sweet berries)");
        add(LangDefinition.tameable_frog_conf_tooltip, "Whether frogs are fully tameable (tame with a spider eye)");
        add(LangDefinition.tameable_horse_conf_tooltip, "Whether horses are fully tameable");
        add(LangDefinition.tameable_rabbit_conf_tooltip, "Whether rabbits are fully tameable (tame with a carrot)");
        add(LangDefinition.trinary_command_system_conf_tooltip, "Whether the trinary command system (wander/follow/stay) is enabled");
        add(LangDefinition.pet_bed_respawns_conf_tooltip, "Whether pets can respawn in their pet bed the morning after they die");
        add(LangDefinition.rabbits_scare_ravagers_conf_tooltip, "Whether rabbits scare ravagers like they used to");
        add(LangDefinition.mobcatcherBlacklist_conf_button, "Edit...");
        add(LangDefinition.can_hurt_pet_item_conf_button, "Edit...");
        add(LangDefinition.can_hurt_all_conf_button, "Edit...");
        add(LangDefinition.no_protection_entity_conf_button, "Edit...");
        add(LangDefinition.other_should_protect_entity_conf_button, "Edit...");
        add(LangDefinition.player_cant_hurt_entity_conf_button, "Edit...");
        add(LangDefinition.configuration_title, "Pet Home Settings");
        add(LangDefinition.configuration_section_common, "Pet Home Common Config");
        add(LangDefinition.configuration_section_common_title, "Pet Home Common Config");
        add(LangDefinition.network_failed,"Network Failed, %s");
        add(LangDefinition.killer_bunny, "Killer bunny");
    }
    private void addEnchant(ResourceKey<Enchantment> enchantmentResourceKey, String name, String desc) {
        add("enchantment." + PetHomeMod.MODID + "." + enchantmentResourceKey.location()
                .getPath(), name);
        add("enchantment." + PetHomeMod.MODID + "." + enchantmentResourceKey.location()
                .getPath() + ".desc", desc);
    }
}
