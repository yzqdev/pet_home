# Pet Home

[English](https://github.com/yzqdev/pet_home/blob/1.21.1/README.md) | [简体中文](https://github.com/yzqdev/pet_home/blob/1.21.1/README-CN.md)

This is an unofficial high version rewrite of [Domestication Innovation](https://modrinth.com/mod/domestication-innovation)(seems not maintain any more), contains optimizations, new contents and rewrites. It's not the same with the origin mod, new features from the original mod may not be added.


Related Links: [Origin CurseForge](https://www.curseforge.com/minecraft/mc-mods/domestication-innovation) | [Origin Modrinth](https://modrinth.com/mod/domestication-innovation)
## Description

Pet home was created with one goal in mind: Making tamable mobs in Minecraft not only useful and engaging, but to promote the taming of mobs as a playstyle by giving massive improvements to how tame mobs behave and interact with the world. Not only have we expanded the amount of mobs in vanilla Minecraft that can be tamed, but we have also fixed several problems with these mobs, added new items and blocks to improve the tamed mob experience, and have introduced a new Pet Enchanting system. Most of these changes should be reflected in both mobs from vanilla and new tamed mobs introduced in other mods.

With this mod installed, no more will your pets have to sit out battles, exploration and more at home. Now they can join the fun without worry of permanently loosing them, or them being completely outclassed by other mobs and mods.

## List of features

- Add collar that can be enchanted.
- With different enchanted collar, your pets have different powers.
- Players can't hurt pets and baby animals.
- Add a new villager that sell enchants and pet items.

## Feature overview

### More tamable mobs
- **Axolotl / Fox / Frog / Horse** can now be tamed (each one can be toggled in the config).
  - **Axolotl** — feed a **Bucket of Tropical Fish** (or a **Tropical Fish**); 3 out of 4 attempts succeed.
  - **Fox** — feed **Sweet Berries** to two foxes to breed them，then the baby fox is tamed.
  - **Frog** — feed a **Spider Eye** (item tag `pet_home:tame_frogs_with`); 3 out of 4 attempts succeed.
  - **Rabbit** — feed a **Hay Block**; 1 out of 2 attempts succeed.
  - **Horse / Donkey / Mule / Llama** — tamed the vanilla way (keep mounting until it stops bucking you off); this mod turns them into full pets with collar enchants, pet beds and commands.
- Feed a tamed rabbit a **Sinister Carrot** to turn it into a Killer Bunny.
- Conversion chain: feed a **Rotten Apple** to a horse to turn it into a **Zombie Horse**, then a **Sinister Carrot** to turn it into a **Skeleton Horse**.
- Pets support the **command system** (sit / follow / wander); the **Command Drum** block broadcasts the command to all of your pets nearby (also triggerable by redstone).

### Pet collar & 40 pet enchantments
Craft a **Collar Tag**, put it on your pet and apply pet enchantment books to it. The 40 pet enchantments include:

- **Combat**: Chain Lightning, Sonic Boom, Shadow Hands, Psychic Wall, Magnetic, Vampire, Frost Fang, Warping Bite, Paralysis, Chaos, Violent, Share, Intimidation
- **Survival**: Health Boost, Tough, Fireproof, Immunity Frame, Deflection, Blazing Protection, Poison Resistance, Bubbling, Void Cloud, Total Recall, Health Siphon, Healing Aura, Rejuvenation, Amphibious
- **Utility**: Speedster, Night Vision, Linked Inventory, XP Transfer, Ore Scenting, Shepherd, Gluttonous, Insight, Defusal
- **Curses**: Blight Curse, Infamy Curse, Immaturity Curse

### Items & blocks
- **Pet Bed** (16 colors): home for your pets, remembers where they belong.
- **Wayward Lantern**: pets that are far away or unloaded will be teleported back next to the lantern.
- **Feather on a Stick**: works like a fishing rod, but reels your pets in.
- **Net / Net Launcher**: capture mobs and release them where you want.
- **Deed of Ownership**: transfers (or removes) pet ownership.
- **Command Drum**: broadcasts sit / follow / wander to nearby pets, redstone controllable.
- **Rotten Apple / Sinister Carrot**: mob conversion items, see above.

### Animal Tamer villager
A new villager profession with 5 levels, sells pet enchantment books and pet items (so mod enchantment books come from the Animal Tamer instead of the librarian).

### Friendly fire protection
Owners can't hurt their pets, pets can't hurt each other, babies are protected, team members are respected — every rule is a config option, with configurable item/entity blacklists and whitelists.

### Compatibility
- [Jade](https://modrinth.com/mod/jade): shows pet owner / command info in the tooltip.
- [Touhou Little Maid](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid) integration.

### Config
- Full in-game config screen (mod list "Config" button or `/pet_home_config` command, 1.20.1) or `config/pet_home.toml`.
- The mod is maintained for **Forge 1.20.1 / NeoForge 1.21.1 / NeoForge 26.1** with the same feature set.

## Difference with the origin mod
- remove citadel, now you dont need to add citadel as a dependency
- the new friendly fire system
  ![friendly fire](https://cdn.modrinth.com/data/cached_images/029b2f2060ef7b4134e149e60920eaafde7a5c1b_0.webp)
- [jade](https://modrinth.com/mod/jade) integration and [Touhou Little Maid](https://modrinth.com/mod/touhou-little-maid) integration
- the new config screen
  ![config screen](https://cdn.modrinth.com/data/cached_images/1333f626af50bfd15233076c8b0e9c1a29e4ea1b_0.webp)
- 7 new enchantment books
- animal tamers sell mod enchantment books not the librarian
## other items want to add

- pet compass

## some pictures
![has petbed](https://cdn.modrinth.com/data/cached_images/ec137249f938c6170a7cc6fa85d66d76339d44d4.png)
![hand](https://cdn.modrinth.com/data/cached_images/ad532a01c32a3ae172bcd7bf4776fc376e212502.png)
![sonic boom](https://cdn.modrinth.com/data/cached_images/7d7f37f8120281c14b6aeec0da54334fc015bbb9.jpeg)
