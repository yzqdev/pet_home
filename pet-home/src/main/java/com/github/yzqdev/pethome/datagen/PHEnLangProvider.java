package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/** 英文（en_us）语言文件 datagen：物品/方块/附魔/配置/事件等全部键的英文文案（中文对应 {@code PHZhLangProvider}）。 */
public class PHEnLangProvider extends FabricLanguageProvider {
  public PHEnLangProvider(
      FabricDataOutput output,
      CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries) {
    super(output, "en_us", registries);
  }

  @Override
  public void generateTranslations(
      net.minecraft.core.HolderLookup.Provider registryLookup, TranslationBuilder builder) {
    builder.add(
        LangDefinition.TOOLTIP_SUBSTITUTE_FEATHER_DESC,
        "[Allows pets to move to the fishing rod landing point, e.g. moving pets to a pet bed]");
    builder.add(
        LangDefinition.TOOLTIP_SUBSTITUTE_ROTTEN_APPLE_DESC,
        "[Feed to horses to turn them into zombie horses]");
    builder.add(
        LangDefinition.TOOLTIP_SUBSTITUTE_SINISTER_CARROT_DESC,
        "[Feed to a rabbit to turn it into a killer rabbit, feed to a zombie horse to turn it into a skeleton horse]");
    builder.add(
        LangDefinition.TOOLTIP_SUBSTITUTE_COLLAR_DESC,
        "[Right-click on pets to put a collar on them]");
    builder.add(
        LangDefinition.TOOLTIP_SUBSTITUTE_PET_BED_DESC,
        "[Place pets on pet beds, pets will respawn the next day after death]");
    builder.add(LangDefinition.ITEM_GROUP, "Pet home | Items");
    builder.add(LangDefinition.ITEM_COLLAR_TAG, "Collar Tag");
    builder.add(LangDefinition.ITEM_ROTTEN_APPLE, "Rotten Apple");
    builder.add(LangDefinition.ITEM_SINISTER_CARROT, "Sinister Carrot");
    builder.add(LangDefinition.ITEM_DEFLECTION_SHIELD, "Deflection Shield Model");
    builder.add(LangDefinition.ITEM_MAGNET, "Magnet Model");
    builder.add(LangDefinition.ITEM_FEATHER_ON_A_STICK, "Feather on a Stick");
    builder.add(LangDefinition.ITEM_DEED_OF_OWNERSHIP, "Pet Contract");
    builder.add(
        LangDefinition.ITEM_DEED_OF_OWNERSHIP_DESC, "[Right-click your pet: bind the contract; use it again on the same pet: transfer ownership]");
    builder.add(LangDefinition.BLOCK_PET_BED_WHITE, "White Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_ORANGE, "Orange Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_MAGENTA, "Magenta Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_LIGHT_BLUE, "Light Blue Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_YELLOW, "Yellow Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_LIME, "Lime Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_PINK, "Pink Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_GRAY, "Gray Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_LIGHT_GRAY, "Light Gray Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_CYAN, "Cyan Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_PURPLE, "Purple Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_BLUE, "Blue Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_BROWN, "Brown Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_GREEN, "Green Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_RED, "Red Pet Bed");
    builder.add(LangDefinition.BLOCK_PET_BED_BLACK, "Black Pet Bed");
    builder.add(LangDefinition.ITEM_DRUM, "Command Drum");
    builder.add(LangDefinition.ITEM_WAYWARD_LANTERN, "Wayward Lantern");
    builder.add(LangDefinition.MESSAGE_COMMAND_0, "%s is wandering");
    builder.add(LangDefinition.MESSAGE_COMMAND_1, "%s is staying");
    builder.add(LangDefinition.MESSAGE_COMMAND_2, "%s is following");
    builder.add(LangDefinition.MESSAGE_DRUM_COMMAND_0, "ordered %s to wander");
    builder.add(LangDefinition.MESSAGE_DRUM_COMMAND_1, "ordered %s to stay");
    builder.add(LangDefinition.MESSAGE_DRUM_COMMAND_2, "ordered %s to follow");
    builder.add(LangDefinition.MESSAGE_RESPAWN, "%s has respawned at its bed");
    builder.add(LangDefinition.MESSAGE_REMOVE_RESPAWN, "Removed respawn bed of %s");
    builder.add(LangDefinition.MESSAGE_GOODBYE, "%s will not respawn. Goodbye...");
    builder.add(LangDefinition.MESSAGE_ENCHANTMENTS, "Enchantments:");
    builder.add(LangDefinition.MESSAGE_SET_OWNER, "%s is now the owner of %s");
    builder.add(
        LangDefinition.MESSAGE_WAYWARD_LANTERN_RETURN,
        "%s has found its way to a nearby Wayward Lantern");
    builder.add(LangDefinition.ENTITY_ANIMAL_TAMER, "Animal Tamer");
    builder.add(LangDefinition.ENTITY_CHAIN_LIGHTNING, "Lightning");
    builder.add(LangDefinition.ENTITY_RECALL_BALL, "Recall Orb");
    builder.add(LangDefinition.ENTITY_FEATHER, "Feather");
    builder.add(LangDefinition.ENTITY_FOLLOWING_JUKEBOX, "Floating Jukebox");
    builder.add(LangDefinition.ENTITY_PSYCHIC_WALL, "Psychic Wall");
    addEnchant(
        builder,
        PHEnchantments.SonicBoom,
        "Sonic Boom",
        "Sonic boom knocks enemies back, triggered periodically");
    addEnchant(
        builder,
        PHEnchantments.HEALTH_BOOST,
        "Extra Health",
        "Increases pet's max health by 10 for each level");
    addEnchant(
        builder,
        PHEnchantments.FIREPROOF,
        "Fireproof",
        "Grants pet immunity to fire and lava damage");
    addEnchant(
        builder,
        PHEnchantments.IMMUNITY_FRAME,
        "Immunity Frame",
        "Each level makes pet immune for an additional second after attack");
    addEnchant(
        builder,
        PHEnchantments.DEFLECTION,
        "Deflecting",
        "Pet is protected by a ghostly shield which deflects projectile attacks");
    addEnchant(
        builder,
        PHEnchantments.POISON_RESISTANCE,
        "Poison Resistance",
        "Grants pet immunity to poison effect");
    addEnchant(
        builder,
        PHEnchantments.CHAIN_LIGHTNING,
        "Chain Lightning",
        "Summons a lightning bolt that chains from and damages mobs each time a pet attacks, level increases how many mobs can be hit by lightning");
    addEnchant(builder, PHEnchantments.SPEEDSTER, "Speedster", "Increases pet's movement speed");
    addEnchant(
        builder,
        PHEnchantments.FROST_FANG,
        "Frost Fang",
        "Pet attacks now slows mobs shortly and deals freezing damage");
    addEnchant(
        builder,
        PHEnchantments.MAGNETIC,
        "Magnetic",
        "Pet aided by ghostly magnet that pulls targeted mobs towards it");
    addEnchant(
        builder,
        PHEnchantments.LINKED_INVENTORY,
        "Linked Inventory",
        "Pet can pick up items and teleport them to owner's inventory");
    addEnchant(
        builder,
        PHEnchantments.TOTAL_RECALL,
        "Total Recall",
        "When below 2 health, pet enters a recall orb and is protected until released by its owner");
    addEnchant(
        builder,
        PHEnchantments.HEALTH_SIPHON,
        "Health Siphon",
        "Any damage done to pet is transferred to its owner");
    addEnchant(
        builder,
        PHEnchantments.BUBBLING,
        "Bubbling",
        "Pet attacks trap mob inside giant bubble that floats upwards");
    addEnchant(
        builder,
        PHEnchantments.SHEPHERD,
        "Herding",
        "Pet can be followed by wild animals, level increases maximum amount of followers");
    addEnchant(
        builder,
        PHEnchantments.AMPHIBIOUS,
        "Amphibious",
        "Pet cannot dry out or drown on land or in water, increases speed in water, pet will not float but swim quickly");
    addEnchant(
        builder,
        PHEnchantments.VAMPIRE,
        "Vampire's Familiar",
        "Pet heals amount of damage done, level increases percentage of dealt damage healed");
    addEnchant(
        builder,
        PHEnchantments.VOID_CLOUD,
        "Void Cloud",
        "Pet is protected from falling off cliffs or into void by a large cloud that lifts it up and returns it to its owner");
    addEnchant(
        builder,
        PHEnchantments.INFAMY_CURSE,
        "Curse of Infamy",
        "Pet can naturally make any nearby monster hostile to it");
    addEnchant(
        builder,
        PHEnchantments.SHADOW_HANDS,
        "Shadow Hands",
        "Pet uses dark magic to attack targets with shadowy hands, level increases speed and amount of hands");
    addEnchant(
        builder,
        PHEnchantments.DEFUSAL,
        "Defusal",
        "Pet can nullify explosion damage to terrain and other mobs, level increases range");
    addEnchant(
        builder,
        PHEnchantments.WARPING_BITE,
        "Warping Bite",
        "Pet will randomly teleport targeted mobs away from it");
    addEnchant(
        builder,
        PHEnchantments.ORE_SCENTING,
        "Ore Scenting",
        "Pet can sniff out ore blocks and alert its owner, level determines distance, amount and frequency of ore detection");
    addEnchant(
        builder,
        PHEnchantments.GLUTTONOUS,
        "Gluttonous",
        "Pet can eat any food item, regardless of dietary restrictions");
    addEnchant(
        builder,
        PHEnchantments.PSYCHIC_WALL,
        "Psychic Wall",
        "Pet summons a wall of psychic energy during combat to provide cover, level determines size of wall and length of the effect");
    addEnchant(
        builder,
        PHEnchantments.INTIMIDATION,
        "Intimidation",
        "Pet can scare away hostile mobs with its fearsome appearance, level determines range and frequency of effect");
    addEnchant(
        builder,
        PHEnchantments.BLIGHT_CURSE,
        "Curse of Blight",
        "Plants will wither and die around the pet.");
    addEnchant(
        builder,
        PHEnchantments.TETHERED_TELEPORT,
        "Tethered Teleport",
        "Pet will teleport with its owner, even across dimensions.");
    addEnchant(
        builder,
        PHEnchantments.IMMATURITY_CURSE,
        "Curse of Immaturity",
        "Pet appears and behaves as a baby version of itself, with reduced attack damage.");
    addEnchant(
        builder,
        PHEnchantments.BLAZING_PROTECTION,
        "Blazing Protection",
        "Pet will be protected from damage by 2 blazing bars per level, which knockback and set attackers on fire.");
    addEnchant(
        builder,
        PHEnchantments.HEALING_AURA,
        "Healing Aura",
        "Pet will ocassionally heal owner and other pets around it, level determines strength of healing effect.");
    addEnchant(
        builder,
        PHEnchantments.XP_Transfer,
        "XP Transfer",
        "Pets collect and transfer XP to owner");
    addEnchant(
        builder,
        PHEnchantments.REJUVENATION,
        "Rejuvenation",
        "When injured, pet may absorb experience orbs to heal itself.");
    addEnchant(
        builder,
        PHEnchantments.SHARE,
        "Share",
        "When the pet is attacked, it shares damage with nearby hostile mobs");
    addEnchant(
        builder,
        PHEnchantments.NIGHT_VISION,
        "Night Vision",
        "Grants Night Vision effect when a pet is nearby");
    addEnchant(
        builder,
        PHEnchantments.INSIGHT,
        "Insight",
        "In dark areas, pets highlight surrounding hostile mobs");
    addEnchant(
        builder,
        PHEnchantments.CHAOS,
        "Chaos Mind",
        "When attacked, pets inflict Chaos on the attacker, forcing them to attack other monsters within 10 blocks");
    addEnchant(
        builder,
        PHEnchantments.PARALYSIS,
        "Paralysis",
        "When attacked, pets paralyze the attacker for several seconds, preventing movement, jumping or attacking");
    addEnchant(
        builder, PHEnchantments.TOUGH, "Tough", "Increase pet defense and knockback resistance");
    addEnchant(
        builder,
        PHEnchantments.VIOLENT,
        "Violent",
        "Monster will get some debuffs when attacked by pet");
    addEnchant(
        builder,
        PHEnchantments.CHARISMA,
        "Charismatic",
        "Pet lowers trade prices for its owner, level increases the bargain");
    addEnchant(
        builder,
        PHEnchantments.DISC_JOCKEY,
        "Disc Jockey",
        "Pet is followed by a floating jukebox that can play music discs");
    addEnchant(
        builder,
        PHEnchantments.MUFFLED,
        "Muffled",
        "Pet will be silent and not emit vibrations when moving.");
    builder.add(LangDefinition.SOUND_SUBTITLE_COLLAR_TAG, "equipped Collar Tag");
    builder.add(LangDefinition.SOUND_SUBTITLE_MAGNET_LOOP, "Magnet buzzes");
    builder.add(LangDefinition.SOUND_SUBTITLE_CHAIN_LIGHTNING, "Lightning zaps");
    builder.add(LangDefinition.SOUND_SUBTITLE_GIANT_BUBBLE_INFLATE, "Giant Bubble inflates");
    builder.add(LangDefinition.SOUND_SUBTITLE_GIANT_BUBBLE_POP, "Giant Bubble pops");
    builder.add(LangDefinition.SOUND_SUBTITLE_PET_BED_USES, "Pet Bed adjusted");
    builder.add(LangDefinition.SOUND_SUBTITLE_DRUM, "Command Drum beats");
    builder.add(LangDefinition.SOUND_SUBTITLE_PSYCHIC_WALL, "Psychic Wall hums");
    builder.add(LangDefinition.SOUND_SUBTITLE_PSYCHIC_WALL_DEFLECT, "Psychic Wall deflects");
    builder.add(LangDefinition.SOUND_SUBTITLE_BLAZING_PROTECTION, "Blazing bar dissapears");
    builder.add(
        LangDefinition.NOTIF_FRIENDLYFIRE_PROTECTED, "The %s is protected. Crouch to bypass.");

    builder.add(LangDefinition.JADE_COLLAR_TAG, "Pet Home Info");
    builder.add(LangDefinition.effect("drunk"), "Chaos");

    builder.add(LangDefinition.has_pet_bed_at_pos, "Bound pet bed at (%s)");
    builder.add(PHItemRegistry.NET_LAUNCHER_ITEM, "Creature Capture Launcher");
    builder.add(PHItemRegistry.NET_ITEM, "Capture Ball");
    builder.add(PHItemRegistry.NET_HAS_ITEM, "Capture Ball (Occupied)");
    builder.add(LangDefinition.capturing_text, "Capturing");
    builder.add(LangDefinition.release_text, "Release");
    builder.add(LangDefinition.health_text, "Health");
    builder.add(LangDefinition.no_net_entity_text, "No creature available to release");
    builder.add(
        LangDefinition.net_launcher_tip,
        "[Hold Shift + Right Click to toggle between Capture and Release modes]");
    builder.add(
        LangDefinition.net_launcher_default_only_tamable,
        "By default, only tamable mobs can be captured (configurable in settings file).");
    builder.add(
        LangDefinition.mobcatcherOnlyTamableAnimal_conf, "Only tamable mobs can be captured");
    builder.add(
        LangDefinition.mobcatcherOnlyTamableAnimal_conf_tooltip,
        "Disable to allow capturing any mob");
    builder.add(LangDefinition.mobcatcherBlacklist_conf, "Mob Catcher prohibited list");
    builder.add(
        LangDefinition.mobcatcherBlacklist_conf_tooltip,
        "Mobs that are blocked from being captured");

    builder.add(LangDefinition.network_failed, "Network Failed, %s");
    builder.add(LangDefinition.killer_bunny, "Killer bunny");
    builder.add(LangDefinition.ITEM_WHISTLE, "Pet Whistle");
    builder.add(LangDefinition.TOOLTIP_WHISTLE_DESC, "[Right-click to recall all your pets]");
    builder.add(LangDefinition.ITEM_FREEDOM_BELL, "Freedom Bell");
    builder.add(
        LangDefinition.TOOLTIP_FREEDOM_BELL_DESC,
        "[Use on a pet to release it (press again to confirm)]");
    builder.add(LangDefinition.ITEM_DEED_OF_OWNERSHIP_BOUND, "Bound to: %s");
    builder.add(
        LangDefinition.ITEM_DEED_OF_OWNERSHIP_DESC2,
        "[To release a pet, use the Freedom Bell instead]");
    builder.add(LangDefinition.MESSAGE_DEED_BOUND, "Contract bound to %s");
    builder.add(
        LangDefinition.MESSAGE_RELEASE_CONFIRM,
        "Use the Freedom Bell on %s again to confirm release");
    builder.add(LangDefinition.MESSAGE_RELEASED, "Released %s");
    builder.add(
        LangDefinition.message("whistle_summary"), "Whistled: %s pet(s) recalled or recall queued");
    builder.add(LangDefinition.message("whistle_none"), "No pets to recall");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_DIM_LABEL, "Dim: ");
    builder.add(
        LangDefinition.MESSAGE_PET_COMPASS_STATUS_IN_BALL,
        "Status: in recall ball (open it to retrieve)");
    builder.add(
        LangDefinition.MESSAGE_PET_COMPASS_STATUS_IN_NET,
        "Status: in creature ball (use on a block to release)");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_STATUS_STALE, "Status: record is stale");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_BUTTON_FORGET, "Remove");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_BUTTON_CONFIRM_FORGET, "Confirm?");
    builder.add(
        LangDefinition.MESSAGE_PET_COMPASS_BALL_RECALLED,
        "The recall ball holding %s has been brought to you — right-click it to open");
    builder.add(
        LangDefinition.MESSAGE_PET_COMPASS_FORGOTTEN, "Removed the entry from the pet compass");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_HEALTH, "HP: %s/%s");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_CMD_0, "Wandering");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_CMD_1, "Staying");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_CMD_2, "Following");
    builder.add(
        LangDefinition.TOOLTIP_DRUM_DESC,
        "[Right-click to cycle the command for your pets within 32 blocks, can also be triggered by redstone]");
    builder.add(
        LangDefinition.TOOLTIP_WAYWARD_LANTERN_DESC,
        "[Place near you, pets that get unloaded far away will be teleported back to the lantern]");

    // —— 游戏内设置界面（client/gui/PetHomeConfigScreen）文案，键名与 1.20.1 一致 ——
    builder.add(LangDefinition.GUI_TITLE, "Pet Home Settings");
    builder.add(LangDefinition.GUI_SUBTITLE, "Changes save instantly to config/pet_home.toml");
    builder.add(LangDefinition.GUI_RESET, "Reset");
    builder.add(LangDefinition.GUI_ON, "ON");
    builder.add(LangDefinition.GUI_OFF, "OFF");
    builder.add(LangDefinition.GUI_CAT_FRIENDLY_NAME, "Friendly Fire");
    builder.add(
        LangDefinition.GUI_CAT_FRIENDLY_DESC, "Protection rules for pets, owners and teams");
    builder.add(LangDefinition.GUI_CAT_GENERAL_NAME, "General");
    builder.add(
        LangDefinition.GUI_CAT_GENERAL_DESC, "Toggles for pet beds, commands, mob catcher, etc.");
    builder.add(LangDefinition.GUI_CAT_TAMEABLE_NAME, "Extra Tameables");
    builder.add(
        LangDefinition.GUI_CAT_TAMEABLE_DESC,
        "Taming toggles for axolotl, fox, frog, horse and rabbit");
    builder.add(LangDefinition.GUI_CAT_LOOT_NAME, "Loot Chances");
    builder.add(
        LangDefinition.GUI_CAT_LOOT_DESC,
        "Chest loot chances for enchanted books and special items");
    builder.add(LangDefinition.GUI_CAT_ENCHANTS_NAME, "Enchantment Toggles");
    builder.add(LangDefinition.GUI_CAT_ENCHANTS_DESC, "Enable or disable each pet enchantment");
    builder.add(LangDefinition.GUI_F_PROTECT_OWNER, "Protect Pets from Owner");
    builder.add(
        LangDefinition.GUI_F_PROTECT_OWNER_DESC,
        "Attacks from the owner on their own pets are void");
    builder.add(LangDefinition.GUI_F_PROTECT_PETS, "No Damage Between Pets");
    builder.add(
        LangDefinition.GUI_F_PROTECT_PETS_DESC, "Pets attacking other players pets is void");
    builder.add(LangDefinition.GUI_F_PROTECT_CHILDREN, "Protect Baby Animals");
    builder.add(LangDefinition.GUI_F_PROTECT_CHILDREN_DESC, "Baby animals are protected");
    builder.add(LangDefinition.GUI_F_REFLECT_DAMAGE, "Reflect Damage");
    builder.add(
        LangDefinition.GUI_F_REFLECT_DAMAGE_DESC,
        "Damage is reflected back to the attacker when protection triggers");
    builder.add(LangDefinition.GUI_F_DISPLAY_WARNING, "Show Protection Message");
    builder.add(
        LangDefinition.GUI_F_DISPLAY_WARNING_DESC,
        "Shows a message when attacking a protected pet");
    builder.add(LangDefinition.GUI_F_PROTECT_TEAM, "Protect Team Members");
    builder.add(LangDefinition.GUI_F_PROTECT_TEAM_DESC, "Mobs on the same team protect each other");
    builder.add(LangDefinition.GUI_F_RESPECT_TEAM, "Respect Team Rules");
    builder.add(LangDefinition.GUI_F_RESPECT_TEAM_DESC, "Protection follows vanilla team rules");
    builder.add(LangDefinition.GUI_F_CAN_HURT_PET_ITEMS, "Shift-Free Pet-Hurting Items");
    builder.add(
        LangDefinition.GUI_F_CAN_HURT_PET_ITEMS_DESC,
        "Comma-separated item IDs; holding these allows hurting pets (e.g. minecraft:egg)");
    builder.add(LangDefinition.GUI_F_CAN_HURT_ALL_ITEMS, "Protection-Bypassing Items");
    builder.add(
        LangDefinition.GUI_F_CAN_HURT_ALL_ITEMS_DESC,
        "Comma-separated item IDs; holding these bypasses protection");
    builder.add(LangDefinition.GUI_F_NO_PROTECTION, "Unprotected mobs");
    builder.add(
        LangDefinition.GUI_F_NO_PROTECTION_DESC,
        "Comma-separated entity IDs exempt from protection rules");
    builder.add(LangDefinition.GUI_F_EXTRA_PROTECTED, "Extra Protected Entities");
    builder.add(
        LangDefinition.GUI_F_EXTRA_PROTECTED_DESC,
        "Comma-separated entity IDs also covered by protection");
    builder.add(LangDefinition.GUI_F_PLAYER_PROTECTED, "Player-Immune Entities");
    builder.add(
        LangDefinition.GUI_F_PLAYER_PROTECTED_DESC,
        "Comma-separated entity IDs that players cannot hurt");
    builder.add(LangDefinition.GUI_G_ROTTEN_APPLE, "Rotten Apples");
    builder.add(LangDefinition.GUI_G_ROTTEN_APPLE_DESC, "Dropped apples rot into rotten apples");
    builder.add(LangDefinition.GUI_G_PET_BED_RESPAWN, "Pet Bed Respawns");
    builder.add(
        LangDefinition.GUI_G_PET_BED_RESPAWN_DESC,
        "Pets respawn at their bound pet bed the next morning");
    builder.add(LangDefinition.GUI_G_RABBITS_RAVAGERS, "Rabbits Scare Ravagers");
    builder.add(
        LangDefinition.GUI_G_RABBITS_RAVAGERS_DESC,
        "Restores the legacy behavior: ravagers fear rabbits");
    builder.add(LangDefinition.GUI_G_TRINARY_COMMAND, "Trinary Command System");
    builder.add(
        LangDefinition.GUI_G_TRINARY_COMMAND_DESC, "Right-click pets to cycle wander/stay/follow");
    builder.add(LangDefinition.GUI_G_PET_INFO_OVERLAY, "Pet Info Panel");
    builder.add(
        LangDefinition.GUI_G_PET_INFO_OVERLAY_DESC,
        "Show pet info (command, pet bed, health, collar enchantments) while the crosshair is on your pet");
    builder.add(
        LangDefinition.GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT, "Require Sneak");
    builder.add(
        LangDefinition.GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT_DESC,
        "Only show the pet info overlay while holding Shift (default: shown whenever the crosshair is on your pet)");
    builder.add(
        LangDefinition.GUI_G_PET_INFO_OVERLAY_IGNORE_JADE, "Show Even with Jade");
    builder.add(
        LangDefinition.GUI_G_PET_INFO_OVERLAY_IGNORE_JADE_DESC,
        "Show the pet info overlay even when Jade is installed");
    builder.add(LangDefinition.GUI_G_PETSTORE_WEIGHT, "Pet Store Weight");
    builder.add(
        LangDefinition.GUI_G_PETSTORE_WEIGHT_DESC,
        "Spawn weight of pet stores in villages (0 disables)");
    builder.add(LangDefinition.GUI_G_MOB_TAMABLE_ONLY, "Mob catcher: tameable only");
    builder.add(
        LangDefinition.GUI_G_MOB_TAMABLE_ONLY_DESC,
        "If disabled, the mob catcher can capture any mob");
    builder.add(LangDefinition.GUI_G_MOB_BLACKLIST, "Mob catcher blacklist");
    builder.add(
        LangDefinition.GUI_G_MOB_BLACKLIST_DESC,
        "Comma-separated entity IDs that cannot be captured");
    builder.add(LangDefinition.GUI_T_AXOLOTL, "Tameable Axolotls");
    builder.add(LangDefinition.GUI_T_AXOLOTL_DESC, "Axolotls can be tamed with tropical fish");
    builder.add(LangDefinition.GUI_T_FOX, "Tameable Foxes");
    builder.add(LangDefinition.GUI_T_FOX_DESC, "Foxes can be tamed with sweet berries");
    builder.add(LangDefinition.GUI_T_FROG, "Tameable Frogs");
    builder.add(LangDefinition.GUI_T_FROG_DESC, "Frogs can be tamed with spider eyes");
    builder.add(LangDefinition.GUI_T_HORSE, "Tameable Horses");
    builder.add(LangDefinition.GUI_T_HORSE_DESC, "Horses can be tamed like wolves");
    builder.add(LangDefinition.GUI_T_RABBIT, "Tameable Rabbits");
    builder.add(LangDefinition.GUI_T_RABBIT_DESC, "Rabbits can be tamed with carrots");
    builder.add(LangDefinition.GUI_L_CURSE_LOOT_ONLY, "Curse enchants loot-only");
    builder.add(
        LangDefinition.GUI_L_CURSE_LOOT_ONLY_DESC, "Curse enchantments only appear in chests");
    builder.add(
        LangDefinition.GUI_L_SINISTER_CARROT_DESC,
        "Chance of sinister carrots in woodland mansion chests");
    builder.add(LangDefinition.GUI_L_BUBBLING_DESC, "Chance of Bubbling books in buried treasure");
    builder.add(
        LangDefinition.GUI_L_VAMPIRISM_DESC, "Chance of Vampire books in woodland mansions");
    builder.add(LangDefinition.GUI_L_VOID_CLOUD_DESC, "Chance of Void Cloud books in end cities");
    builder.add(
        LangDefinition.GUI_L_ORE_SCENTING_DESC, "Chance of Ore Scenting books in mineshafts");
    builder.add(
        LangDefinition.GUI_L_BLAZING_DESC,
        "Chance of Blazing Protection books in nether fortresses");
    builder.add(LangDefinition.GUI_L_SHARE_DESC, "Chance of Share books in end cities");
    builder.add(
        LangDefinition.GUI_L_SONIC_BOOM_DESC, "Chance of Sonic Boom books in woodland mansions");
    builder.add(
        LangDefinition.GUI_L_PARALYSIS_DESC,
        "Chance of Paralysis books in mineshafts, desert pyramids and bonus chests");
    builder.add(LangDefinition.GUI_L_TOUGH_DESC, "Chance of Tough books in abandoned mineshafts");

    addPetCompass(builder);
  }

  private void addEnchant(
      TranslationBuilder builder,
      ResourceKey<Enchantment> enchantmentResourceKey,
      String name,
      String desc) {
    String base = LangDefinition.enchantment(enchantmentResourceKey.location().getPath());
    builder.add(base, name);
    builder.add(base + ".desc", desc);
  }

  private void addPetCompass(FabricLanguageProvider.TranslationBuilder builder) {
    builder.add(
        LangDefinition.TOOLTIP_PET_COMPASS_DESC,
        "[Right-click to open the pet list]");
    builder.add(LangDefinition.ITEM_PET_COMPASS, "Pet Compass");
    builder.add(
        LangDefinition.TOOLTIP_PET_COMPASS_UNBOUND, "Right-click your tamed pet to bind it");
    builder.add(LangDefinition.TOOLTIP_PET_COMPASS_BOUND, "Bound to: %s");
    builder.add(LangDefinition.TOOLTIP_PET_COMPASS_DISTANCE, "Distance: %s m");
    builder.add(
        LangDefinition.TOOLTIP_PET_COMPASS_LAST_KNOWN,
        "Pet not in loaded chunks, pointing to last known position");
    builder.add(LangDefinition.TOOLTIP_PET_COMPASS_WRONG_DIM, "Pet is in another dimension: %s");
    builder.add(
        LangDefinition.TOOLTIP_PET_COMPASS_WAITING_LANTERN, "Pet is waiting at a wayward lantern");
    builder.add(
        LangDefinition.TOOLTIP_PET_COMPASS_WAITING_RESPAWN,
        "Pet has died, waiting to respawn at its pet bed");
    builder.add(
        LangDefinition.TOOLTIP_PET_COMPASS_UNBIND_HINT, "Shift + right-click in air to unbind");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_TITLE, "Pet Compass");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_EMPTY, "No trackable pets yet");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_POSITION, "Position: ");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_LAST_KNOWN_POS, "Last known: ");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_DISTANCE, "Dist: %s m");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_DIM_OVERWORLD, "Overworld");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_DIM_THE_NETHER, "The Nether");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_DIM_THE_END, "The End");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_STATUS_LOADED, "Status: loaded");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_STATUS_NOT_LOADED, "Status: not loaded");
    builder.add(
        LangDefinition.MESSAGE_PET_COMPASS_STATUS_WRONG_DIM, "Status: in another dimension");
    builder.add(
        LangDefinition.MESSAGE_PET_COMPASS_STATUS_DEAD,
        "Status: dead, waiting for pet bed respawn");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_BUTTON_TP, "Go to pet");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_BUTTON_RECALL, "Recall");
    builder.add(
        LangDefinition.MESSAGE_PET_COMPASS_DISABLED, "The pet compass is disabled in the config");
    builder.add(
        LangDefinition.MESSAGE_PET_COMPASS_DEAD_WAIT_RESPAWN,
        "This pet has died and is waiting to respawn at its pet bed");
    builder.add(
        LangDefinition.MESSAGE_PET_COMPASS_NOT_LOADED_CONFIRM,
        "Pet is not loaded, teleporting to its last known position");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_TELEPORTED, "Teleported to your pet");
    builder.add(LangDefinition.MESSAGE_PET_COMPASS_RECALLED, "%s has been recalled to you");
    builder.add(
        LangDefinition.MESSAGE_PET_COMPASS_RECALL_STARTED, "Searching for the pet, please wait...");
    builder.add(
        LangDefinition.MESSAGE_PET_COMPASS_RECALL_FAILED,
        "Could not find the pet, please try again later");
    builder.add(LangDefinition.GUI_G_PET_COMPASS, "Pet Compass");
    builder.add(LangDefinition.GUI_G_PET_COMPASS_DESC, "master switch for the pet compass system");
    builder.add(LangDefinition.GUI_G_PET_COMPASS_TP_P, "Teleport Player to Pet");
    builder.add(
        LangDefinition.GUI_G_PET_COMPASS_TP_P_DESC, "allow teleporting the player to the pet");
    builder.add(LangDefinition.GUI_G_PET_COMPASS_TP_R, "Recall Pet To Player");
    builder.add(
        LangDefinition.GUI_G_PET_COMPASS_TP_R_DESC,
        "allow teleporting (recalling) the pet to the player");
  }
}
