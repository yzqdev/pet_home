package com.github.yzqdev.pet_home.util;

import com.github.yzqdev.pet_home.ModConstants;
import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.network.PropertiesMessage;
import com.github.yzqdev.pet_home.datagen.ModEnchantments;
import com.github.yzqdev.pet_home.server.entity.PHEntityRegistry;
import com.github.yzqdev.pet_home.server.entity.HighlightedBlockEntity;
import com.github.yzqdev.pet_home.server.entity.ModifedToBeTameable;
import com.github.yzqdev.pet_home.server.misc.PHParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author yzqde
 * @date time 2025/1/9 7:52
 * @modified By:
 */
public class TameableUtils {

    private static final String ENCHANTMENT_TAG = "StoredPetEnchantments";
    private static final String COLLAR_TAG = "HasPetCollar";
    private static final String IMMUNITY_TIME_TAG = "PetImmunityTimer";
    private static final String FROZEN_TIME_TAG = "PetFrozenTime";
    private static final String ATTACK_TARGET_ENTITY = "PetAttackTarget";
    private static final String SHADOW_PUNCH_TIMES = "PetShadowPunchTimes";
    private static final String SHADOW_PUNCH_COOLDOWN = "PetShadowPunchCooldown";
    private static final String PSYCHIC_WALL_COOLDOWN = "PetPsychicWallCooldown";
    private static final String INTIMIDATION_COOLDOWN = "PetIntimidationCooldown";
    private static final String SHADOW_PUNCH_STRIKING = "PetShadowPunchStriking";
    private static final String JUKEBOX_FOLLOWER_UUID = "PetJukeboxFollowerUUID";
    private static final String JUKEBOX_FOLLOWER_DISC = "PetJukeboxFollowerDisc";
    private static final String BLAZING_PROTECTION_BARS = "PetBlazingProtectionBars";
    private static final String BLAZING_PROTECTION_COOLDOWN = "PetBlazingProtectionCooldown";
    private static final String HEALING_AURA_TIME = "PetHealingAuraTime";
    private static final String Sonic_boom_TIME = "PetSonicBoomTime";
    private static final String HEALING_AURA_IMPULSE = "PetHealingAuraImpulse";
    private static final String HAS_PET_BED = "HasPetBed";
    private static final String PET_BED_X = "PetBedX";
    private static final String PET_BED_Y = "PetBedY";
    private static final String PET_BED_Z = "PetBedZ";
    private static final String PET_BED_DIMENSION = "PetBedDimension";
    private static final String FALL_DISTANCE_SYNC = "SyncedFallDistance";
    private static final String ZOMBIE_PET = "ZombiePet";
    private static final String SAFE_PET_HEALTH = "SafePetHealth";
    private static final String COLLAR_SWAP_COOLDOWN = "CollarSwapCooldown";
    private static final ResourceLocation HEALTH_BOOST_UUID = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "health_boost");// UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B166EEEEE");
    private static final ResourceLocation SPEED_BOOST_UUID = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "speed_boost");// UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31744");
    private static final ResourceLocation ARMOR_BOOST_UUID = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "armor_boost");// UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31744");
    private static final ResourceLocation RESISTANCE_BOOST_UUID = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "resistance_boost");// UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31744");

    private static final ResourceLocation SPEED_BOOST_AQUATIC_LAND_UUID = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "speed_boost_aqua");// UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31745");

    public static UUID getOwnerUUIDOf(Entity entity) {
        if (entity instanceof ModifedToBeTameable) {
            return ((ModifedToBeTameable) entity).getTameOwnerUUID();
        }

        if (entity instanceof TamableAnimal) {
            return ((TamableAnimal) entity).getOwnerUUID();
        }
        return null;
    }

    public static boolean hasCollar(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(COLLAR_TAG) && tag.getBoolean(COLLAR_TAG);
    }

    public static boolean shouldUnloadToLantern(LivingEntity tameable) {
        if (tameable instanceof IComandableMob commandableMob) {
            return commandableMob.getCommand() == 2;
        } else {
            CompoundTag tag = new CompoundTag();
            tameable.addAdditionalSaveData(tag);
            int command = -1;
            //compat with alexs mobs
            for (String s : tag.getAllKeys()) {
                if (s.endsWith("Command") && tag.contains(s, 1)) {
                    command = tag.getInt(s);
                }
            }
            if (command != -1) {
                return command == 1;
            } else if (tameable instanceof TamableAnimal animal) {
                return !animal.isOrderedToSit();
            }
        }
        return false;
    }

    public static double getSafePetHealth(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getDouble(SAFE_PET_HEALTH);
    }

    public static void setSafePetHealth(LivingEntity enchanted, double health) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putDouble(SAFE_PET_HEALTH, health);
        sync(enchanted, tag);
    }

    public static boolean isTamed(Entity entity) {
        //sometimes these are not bound on runtime
//        if (entity instanceof Axolotl) {
//            return ((ModifedToBeTameable) entity).isTame() && PetHomeConfig.tameableAxolotl;
//        }
//        if (entity instanceof Fox) {
//            return ((ModifedToBeTameable) entity).isTame() && PetHomeConfig.tameableFox;
//        }
//        if (entity instanceof Rabbit) {
//            return ((ModifedToBeTameable) entity).isTame() && PetHomeConfig.tameableRabbit;
//        }
//        if (entity instanceof Frog) {
//            return ((ModifedToBeTameable) entity).isTame() && PetHomeConfig.tameableFrog;
//        }
        return entity instanceof ModifedToBeTameable && ((ModifedToBeTameable) entity).isTame() || entity instanceof TamableAnimal && ((TamableAnimal) entity).isTame();
    }

    public static boolean isPetOf(Player player, Entity entity) {
        return entity != null && (entity.isAlliedTo(player) || hasSameOwnerAsOneWay(entity, player));
    }

    private static boolean hasSameOwnerAsOneWay(Entity tameable, Entity target) {
        if (tameable instanceof TamableAnimal tamed && tamed.getOwner() != null) {
            if (target instanceof ModifedToBeTameable axolotl && axolotl.getTameOwner() != null) {
                if (tamed.getOwner().equals(axolotl.getTameOwner())) {
                    return true;
                }
            }
            if (target instanceof TamableAnimal otherPet && otherPet.getOwner() != null) {
                if (tamed.getOwner().equals(otherPet.getOwner())) {
                    return true;
                }
            }
            return tamed.getOwner().equals(target);
        } else if (tameable instanceof ModifedToBeTameable axolotl && axolotl.getTameOwner() != null) {
            if (tameable instanceof TamableAnimal tamed && tamed.getOwner() != null) {
                if (tamed.getOwner().equals(axolotl.getTameOwner())) {
                    return true;
                }
            }
            if (target instanceof ModifedToBeTameable otherPet && otherPet.getTameOwner() != null) {
                if (axolotl.getTameOwner().equals(otherPet.getTameOwner())) {
                    return true;
                }
            }
            return axolotl.getTameOwner().equals(target);
        }
        return false;
    }

    @Nullable
    public static BlockPos getPetBedPos(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        if (tag.getBoolean(HAS_PET_BED) && tag.contains(PET_BED_X) && tag.contains(PET_BED_Y) && tag.contains(PET_BED_Z)) {
            return new BlockPos(tag.getInt(PET_BED_X), tag.getInt(PET_BED_Y), tag.getInt(PET_BED_Z));
        }
        return null;
    }

    public static void setPetBedPos(LivingEntity enchanted, BlockPos petBed) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(HAS_PET_BED, true);
        tag.putInt(PET_BED_X, petBed.getX());
        tag.putInt(PET_BED_Y, petBed.getY());
        tag.putInt(PET_BED_Z, petBed.getZ());
        sync(enchanted, tag);
    }

    public static void removePetBedPos(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(HAS_PET_BED, false);
        sync(enchanted, tag);
    }

    public static String getPetBedDimension(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(PET_BED_DIMENSION) ? "minecraft:overworld" : tag.getString(PET_BED_DIMENSION);
    }

    public static void setPetBedDimension(LivingEntity enchanted, String dimension) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putString(PET_BED_DIMENSION, dimension);
        sync(enchanted, tag);
    }

    public static Entity getOwnerOf(Entity entity) {
        if (entity instanceof ModifedToBeTameable) {
            return ((ModifedToBeTameable) entity).getTameOwner();
        }
        if (entity instanceof TamableAnimal) {
            return ((TamableAnimal) entity).getOwner();
        }
        return null;
    }

    public static int getBlazingProtectionBars(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(BLAZING_PROTECTION_BARS);
    }

    public static int getBlazingProtectionCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(BLAZING_PROTECTION_COOLDOWN);
    }

    public static void setBlazingProtectionCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(BLAZING_PROTECTION_COOLDOWN, time);
        sync(enchanted, tag);
    }

    public static void setBlazingProtectionBars(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(BLAZING_PROTECTION_BARS, time);
        sync(enchanted, tag);
    }

    private static void sync(LivingEntity entity, CompoundTag tag) {
        CitadelEntityData.setCitadelTag(entity, tag);
        if (!entity.level().isClientSide) {
            PacketDistributor.sendToAllPlayers(new PropertiesMessage(ModConstants.entityDataTagUpdate, tag, entity.getId()));
//            Citadel.sendMSGToAll(new PropertiesMessage("CitadelTagUpdate", tag, enchanted.getId()));
        } else {
            PacketDistributor.sendToServer(new PropertiesMessage(ModConstants.entityDataTagUpdate, tag, entity.getId()));
//            Citadel.sendMSGToServer(new PropertiesMessage("CitadelTagUpdate", tag, enchanted.getId()));
        }
    }

    public static void clearEnchants(LivingEntity entity) {
        setEnchantmentTag(entity, new ListTag());
    }

    private static void setEnchantmentTag(LivingEntity enchanted, ListTag enchants) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        Map<ResourceLocation, Integer> prevEnchants = getEnchants(enchanted);
        tag.put(ENCHANTMENT_TAG, enchants);
        tag.putInt(COLLAR_SWAP_COOLDOWN, 20);
        tag.putBoolean(COLLAR_TAG, true);
        sync(enchanted, tag);
        onUpdateEnchants(prevEnchants, enchanted);
    }

    private static boolean isWaterCreature(LivingEntity enchanted) {
        return enchanted.getType().getCategory() == MobCategory.WATER_CREATURE || enchanted.getType().getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE || enchanted.getType().getCategory() == MobCategory.WATER_AMBIENT;
    }

    private static void onUpdateEnchants(@Nullable Map<ResourceLocation, Integer> prevEnchants, LivingEntity enchanted) {
        int healthExtra = getEnchantLevel(enchanted, ModEnchantments.HEALTH_BOOST);
        int speedExtra = getEnchantLevel(enchanted, ModEnchantments.SPEEDSTER);
        int toughExtra = getEnchantLevel(enchanted, ModEnchantments.TOUGH);
        boolean amphib = hasEnchant(enchanted, ModEnchantments.AMPHIBIOUS) && !enchanted.isInWaterOrBubble() && isWaterCreature(enchanted);
        AttributeInstance health = enchanted.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speed = enchanted.getAttribute(Attributes.MOVEMENT_SPEED);
        var armor = enchanted.getAttribute(Attributes.ARMOR);
        var resistance = enchanted.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        var enchantReg = enchanted.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        if (hasEnchant(enchanted, ModEnchantments.IMMATURITY_CURSE) || prevEnchants != null && prevEnchants.containsKey(ModEnchantments.IMMATURITY_CURSE.registry())) {
            //change pose to update client
            enchanted.setPose(Pose.FALL_FLYING);
            AgeableMob ageable = (AgeableMob) enchanted;
            ageable.setBaby(true); // 直接设为幼年
            enchanted.refreshDimensions();

        }
        if (armor != null && resistance != null) {
            AttributeModifier armorBoost = new AttributeModifier(ARMOR_BOOST_UUID, toughExtra * 3, AttributeModifier.Operation.ADD_VALUE);
            AttributeModifier resBoost = new AttributeModifier(RESISTANCE_BOOST_UUID, toughExtra * 3, AttributeModifier.Operation.ADD_VALUE);
            if (toughExtra > 0) {
                if (armor.hasModifier(armorBoost.id())) {
                    armor.removeModifier(armorBoost);
                    armor.addPermanentModifier(armorBoost);
                } else {
                    armor.addPermanentModifier(armorBoost);
                }
                if (resistance.hasModifier(resBoost.id())) {
                    resistance.removeModifier(resBoost);
                    resistance.addPermanentModifier(resBoost);
                } else {
                    resistance.addPermanentModifier(resBoost);
                }
            } else {
                armor.removeModifier(ARMOR_BOOST_UUID);
                resistance.removeModifier(RESISTANCE_BOOST_UUID);
            }
        }
        if (health != null) {
            AttributeModifier healthBoostPetUpgrade = new AttributeModifier(HEALTH_BOOST_UUID, healthExtra * 10, AttributeModifier.Operation.ADD_VALUE);

            if (healthExtra > 0) {
                if (health.hasModifier(healthBoostPetUpgrade.id())) {
                    health.removeModifier(healthBoostPetUpgrade);
                    health.addPermanentModifier(healthBoostPetUpgrade);
                } else {
                    health.addPermanentModifier(healthBoostPetUpgrade);
                }
            } else {

                health.removeModifier(healthBoostPetUpgrade);
            }
        }
        if (speed != null) {
            AttributeModifier speedsterPetUpgrade = new AttributeModifier(SPEED_BOOST_UUID, speedExtra * 0.075F, AttributeModifier.Operation.ADD_VALUE);

            if (speedExtra > 0) {
                if (speed.hasModifier(speedsterPetUpgrade.id())) {
                    speed.removeModifier(speedsterPetUpgrade);
                    speed.addPermanentModifier(speedsterPetUpgrade);
                } else {
                    speed.addPermanentModifier(speedsterPetUpgrade);
                }
            } else {
                speed.removeModifier(speedsterPetUpgrade);
            }
            AttributeModifier speed_aqua = new AttributeModifier(SPEED_BOOST_AQUATIC_LAND_UUID, 0.13F, AttributeModifier.Operation.ADD_VALUE);

            if (amphib) {
                if (speed.hasModifier(speed_aqua.id())) {
                    speed.removeModifier(speed_aqua);
                    speed.addPermanentModifier(speed_aqua);
                } else {
                    speed.addPermanentModifier(speed_aqua);
                }
            } else {
                speed.removeModifier(speed_aqua);
            }
        }
    }

    public static boolean hasEnchant(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
        return getEnchantLevel(entity, enchantment) > 0;
    }

    public static int getEnchantLevel(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
        ListTag listtag = getEnchantmentList(entity);

        if (listtag != null) {
            for (int i = 0; i < listtag.size(); ++i) {

                CompoundTag compoundtag = listtag.getCompound(i);
                var location = ResourceLocation.parse(compoundtag.getString("id"));

                if (enchantment.location().equals(location)) {
                    return compoundtag.getInt("lvl");
                }
//                    if (res != null && res.equals(NeoForgeRegistries.ENCHANTMENTS.getKey(enchantment))) {
//                        return EnchantmentHelper.getEnchantmentLevel(compoundtag);
//                    }
            }

        }
        return 0;
    }

    @Nullable
    private static ListTag getEnchantmentList(LivingEntity entity) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(entity);
        if (tag.contains(ENCHANTMENT_TAG)) {
            return tag.getList(ENCHANTMENT_TAG, 10);
        }
        return null;
    }

    public static List<Component> getEnchantDescriptions(LivingEntity entity) {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal("   ").append(Component.translatable("message.pet_home.enchantments").withStyle(ChatFormatting.GOLD)));
        Map<ResourceLocation, Integer> map = getEnchants(entity);
        if (map != null) {
            for (Map.Entry<ResourceLocation, Integer> entry : map.entrySet()) {

                boolean isCurse = entry.getKey().getPath().contains("curse");
                list.add(Component.translatable("enchantment." + entry.getKey().getNamespace() + "." + entry.getKey().getPath()).append(Component.literal(" ")).append(Component.translatable("enchantment.level." + entry.getValue())).withStyle(isCurse ? ChatFormatting.RED : ChatFormatting.AQUA));

            }
        }
        return list;
    }

    public static Map<ResourceLocation, Integer> getEnchants(LivingEntity entity) {
        ListTag listtag = getEnchantmentList(entity);
        if (listtag == null) {
            return null;
        }
        Map<ResourceLocation, Integer> enchants = new HashMap<>();
        for (int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            ResourceLocation res = ResourceLocation.parse(compoundtag.getString("id"));

            enchants.put(res, compoundtag.getInt("lvl"));

        }
        return enchants;
    }

    public static CompoundTag storeEnchantment(@Nullable ResourceLocation pId, int pLevel) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putString("id", String.valueOf((Object) pId));
        compoundtag.putShort("lvl", (short) pLevel);
        return compoundtag;
    }

    public static void addEnchant(LivingEntity entity, ItemEnchantments itemEnchantments) {
        ListTag listTag = new ListTag();


        for (var entry : itemEnchantments.entrySet()) {
            var en = entry.getKey().getKey().location();
            var compound = storeEnchantment(en, entry.getIntValue());
            listTag.add(compound);
        }
        setEnchantmentTag(entity, listTag);

    }

    public static boolean hasSameOwnerAs(LivingEntity tameable, Entity target) {
        return hasSameOwnerAsOneWay(tameable, target) || hasSameOwnerAsOneWay(target, tameable);
    }

    public static void xpTransfer(LivingEntity living) {
        for (ExperienceOrb experienceorb : living.level().getEntitiesOfClass(ExperienceOrb.class, living.getBoundingBox().inflate(3D))) {

            Vec3 vec3 = new Vec3(living.getX() - experienceorb.getX(), living.getY() + (double) living.getEyeHeight() / 2.0D - experienceorb.getY(), living.getZ() - experienceorb.getZ());
            double d0 = vec3.lengthSqr();
            if (d0 < 2.0D) {
                Entity owner = TameableUtils.getOwnerOf(living);
                if (owner instanceof Player player) {
                    player.giveExperiencePoints(experienceorb.value);
                }
                experienceorb.discard();
//            float h = living.getHealth() + experienceorb.value;
//            living.setHealth(h);
//            if (h - living.getMaxHealth() > 0) {
//                experienceorb.value = (int) Math.floor(h - living.getMaxHealth());
//                break;
//            } else {
//                experienceorb.discard();
//            }
            }
            if (d0 < 64.0D) {
                double d1 = 1.0D - Math.sqrt(d0) / 8.0D;
                experienceorb.setDeltaMovement(experienceorb.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.5D)));
            }
        }
    }

    public static void absorbExpOrbs(LivingEntity living) {
        if (living.getHealth() < living.getMaxHealth() && !living.level().isClientSide) {
            for (ExperienceOrb experienceorb : living.level().getEntitiesOfClass(ExperienceOrb.class, living.getBoundingBox().inflate(3D))) {
                if (living.getHealth() >= living.getMaxHealth()) {
                    break;
                }
                Vec3 vec3 = new Vec3(living.getX() - experienceorb.getX(), living.getY() + (double) living.getEyeHeight() / 2.0D - experienceorb.getY(), living.getZ() - experienceorb.getZ());
                double d0 = vec3.lengthSqr();
                if (d0 < 2.0D) {
                    if (!living.isDeadOrDying()) {


                        float h = living.getHealth() + experienceorb.value;
                        living.setHealth(h);

                        if (h - living.getMaxHealth() > 0) {
                            experienceorb.value = (int) Math.floor(h - living.getMaxHealth());
                            break;
                        } else {
                            experienceorb.discard();
                        }
                    }

                }
                if (d0 < 64.0D) {
                    double d1 = 1.0D - Math.sqrt(d0) / 8.0D;
                    experienceorb.setDeltaMovement(experienceorb.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.5D)));
                }
            }
        }
    }

    public static int getHealingAuraTime(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(HEALING_AURA_TIME);
    }

    public static void setHealingAuraTime(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(HEALING_AURA_TIME, time);
        sync(enchanted, tag);
    }

    public static long getSonicboomAuraTime(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getLong(Sonic_boom_TIME);
    }

    public static void setSonicboomAuraTime(LivingEntity enchanted, long time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putLong(Sonic_boom_TIME, time);
        sync(enchanted, tag);
    }


    public static boolean getHealingAuraImpulse(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getBoolean(HEALING_AURA_IMPULSE);
    }

    public static void setHealingAuraImpulse(LivingEntity enchanted, boolean impulse) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(HEALING_AURA_IMPULSE, impulse);
        sync(enchanted, tag);
    }

    public static List<LivingEntity> getAuraHealables(LivingEntity pet) {
        Predicate<Entity> hurtAndOnTeam = (animal) -> hasSameOwnerAs((LivingEntity) animal, pet) && animal.distanceTo(pet) < 4 && ((LivingEntity) animal).getHealth() < ((LivingEntity) animal).getMaxHealth();
        return pet.level().getEntitiesOfClass(LivingEntity.class, pet.getBoundingBox().inflate(4, 4, 4), EntitySelector.NO_SPECTATORS.and(hurtAndOnTeam));
    }

    public static int getPsychicWallCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(PSYCHIC_WALL_COOLDOWN);
    }

    public static void setPsychicWallCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PSYCHIC_WALL_COOLDOWN, time);
        sync(enchanted, tag);
    }

    public static void attractAnimals(LivingEntity attractor, int max) {
        if ((attractor.tickCount + attractor.getId()) % 8 == 0) {
            Predicate<Entity> notOnTeam = (animal) -> !hasSameOwnerAs((LivingEntity) animal, attractor) && animal.distanceTo(attractor) > 3 + attractor.getBbWidth() * 1.6F;
            List<Animal> list = attractor.level().getEntitiesOfClass(Animal.class, attractor.getBoundingBox().inflate(16, 8, 16), EntitySelector.NO_SPECTATORS.and(notOnTeam));
            list.sort(Comparator.comparingDouble(attractor::distanceToSqr));
            for (int i = 0; i < Math.min(max, list.size()); i++) {
                Animal e = list.get(i);
                e.setTarget(null);
                e.setLastHurtByMob(null);
                e.getNavigation().moveTo(attractor, 1.1D);
            }

        }
    }

    public static int getPetAttackTargetID(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(ATTACK_TARGET_ENTITY) ? -1 : tag.getInt(ATTACK_TARGET_ENTITY);
    }

    @Nullable
    public static Entity getPetAttackTarget(LivingEntity enchanted) {
        int i = getPetAttackTargetID(enchanted);
        return i == -1 ? null : enchanted.level().getEntity(i);
    }

    public static void setPetAttackTarget(LivingEntity enchanted, int id) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(ATTACK_TARGET_ENTITY, id);
        sync(enchanted, tag);
    }

    public static void aggroRandomMonsters(LivingEntity attractor) {
        if ((attractor.tickCount + attractor.getId()) % 400 == 0) {
            Predicate<Entity> notOnTeamAndMonster = (animal) -> animal instanceof Enemy && !hasSameOwnerAs((LivingEntity) animal, attractor) && animal.distanceTo(attractor) > 3 + attractor.getBbWidth() * 1.6F;
            List<Mob> list = attractor.level().getEntitiesOfClass(Mob.class, attractor.getBoundingBox().inflate(20, 8, 20), EntitySelector.NO_SPECTATORS.and(notOnTeamAndMonster));
            list.sort(Comparator.comparingDouble(attractor::distanceToSqr));
            if (!list.isEmpty()) {
                list.get(0).setTarget(attractor);
            }
        }
    }

    public static int getIntimidationCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(INTIMIDATION_COOLDOWN);
    }

    public static void setIntimidationCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(INTIMIDATION_COOLDOWN, time);
        sync(enchanted, tag);
    }

    public static void scareRandomMonsters(LivingEntity scary, int level) {
        boolean interval = (scary.tickCount + scary.getId()) % Math.max(140, 600 - level * 200) == 0;
        if (interval || scary.hurtTime == 4 || getIntimidationCooldown(scary) > 0) {
            Predicate<Entity> notOnTeamAndMonster = (animal) -> animal instanceof Monster && !hasSameOwnerAs((LivingEntity) animal, scary) && animal.distanceTo(scary) > 3 + scary.getBbWidth() * 1.6F;
            List<PathfinderMob> list = scary.level().getEntitiesOfClass(PathfinderMob.class, scary.getBoundingBox().inflate(10 * level, 8 * level, 10 * level), EntitySelector.NO_SPECTATORS.and(notOnTeamAndMonster));
            list.sort(Comparator.comparingDouble(scary::distanceToSqr));
            if (!list.isEmpty()) {
                if (getIntimidationCooldown(scary) > 0 && !interval) {
                    setIntimidationCooldown(scary, getIntimidationCooldown(scary) - 1);
                } else {
                    Vec3 rots = list.get(0).getEyePosition().subtract(scary.getEyePosition()).normalize();
                    float f = Mth.sqrt((float) (rots.x * rots.x + rots.z * rots.z));
                    double yRot = Math.atan2(-rots.z, -rots.x) * (double) (180F / (float) Math.PI) + 90F;
                    double xRot = Math.atan2(-rots.y, f) * (double) (180F / (float) Math.PI);
                    scary.level().addParticle(PHParticleRegistry.INTIMIDATION.get(), scary.getX(), scary.getY(), scary.getZ(), scary.getId(), xRot, yRot);
                    setIntimidationCooldown(scary, 70 * level);
                    if (scary instanceof Mob) {
                        ((Mob) scary).playAmbientSound();
                    }
                }
                for (PathfinderMob monster : list) {
                    Vec3 vec = LandRandomPos.getPosAway(monster, 11 * level, 7, scary.position());
                    if (vec != null) {
                        monster.getNavigation().moveTo(vec.x, vec.y, vec.z, 1.5D);
                    }
                }
            }
        }
    }

    public static boolean couldBeTamed(Entity entity) {
        return entity instanceof ModifedToBeTameable || entity instanceof TamableAnimal;
    }

    public static void destroyRandomPlants(LivingEntity living) {
        if ((living.tickCount + living.getId()) % 200 == 0) {
            int range = 2;
            List<BlockPos> plants = new ArrayList<>();
            List<BlockPos> grasses = new ArrayList<>();
            BlockPos blockpos = living.blockPosition();
            int half = range / 2;
            RandomSource r = living.getRandom();
            int maxPlants = 2 + r.nextInt(2);
            for (int i = 0; i <= half && i >= -half; i = (i <= 0 ? 1 : 0) - i) {
                for (int j = 0; j <= range && j >= -range; j = (j <= 0 ? 1 : 0) - j) {
                    for (int k = 0; k <= range && k >= -range; k = (k <= 0 ? 1 : 0) - k) {
                        BlockPos offset = blockpos.offset(j, i, k);
                        BlockState state = living.level().getBlockState(offset);
                        if (!state.isAir() && r.nextInt(4) == 0) {
                            if (state.is(BlockTags.FLOWERS) || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(BlockTags.CROPS)) {
                                plants.add(offset);
                            } else if (state.is(BlockTags.DIRT) && !state.is(Blocks.DIRT) && !state.is(Blocks.COARSE_DIRT) || state.is(Blocks.FARMLAND)) {
                                grasses.add(offset);
                            }
                        }
                    }
                }
            }
            for (BlockPos plant : plants) {
                living.level().setBlockAndUpdate(plant, Blocks.AIR.defaultBlockState());
                for (int i = 0; i < 1 + r.nextInt(2); i++) {
                    living.level().addParticle(PHParticleRegistry.BLIGHT.get(), plant.getX() + r.nextFloat(), plant.getY() + r.nextFloat(), plant.getZ() + r.nextFloat(), 0, 0.08F, 0);
                }
            }
            for (BlockPos dirt : grasses) {
                living.level().setBlockAndUpdate(dirt, r.nextBoolean() ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.DIRT.defaultBlockState());
                for (int i = 0; i < 1 + r.nextInt(2); i++) {
                    living.level().addParticle(PHParticleRegistry.BLIGHT.get(), dirt.getX() + r.nextFloat(), dirt.getY() + 1, dirt.getZ() + r.nextFloat(), 0, 0.08F, 0);
                }
            }
        }
    }

    public static void detectRandomOres(LivingEntity attractor, int interval, int range, int effectLength, int maxOres) {
        int tick = (attractor.tickCount + attractor.getId()) % interval;
        if (tick <= 30) {
            attractor.xRotO = attractor.getXRot();
            attractor.setXRot((float) Math.sin(tick * 0.6F) * 30F);
            Vec3 look = attractor.getEyePosition().add(attractor.getViewVector(1.0F).scale(attractor.getBbWidth()));
            for (int i = 0; i < 3; i++) {
                double x = attractor.getRandomX(2.0F);
                double y = attractor.position().y;
                double z = attractor.getRandomZ(2.0F);
                attractor.level().addParticle(PHParticleRegistry.SNIFF.get(), x, y, z, look.x, look.y, look.z);
            }
        }
        if (tick == 30) {
            List<BlockPos> ores = new ArrayList<>();
            BlockPos blockpos = attractor.blockPosition();
            int half = range / 2;
            for (int i = 0; i <= half && i >= -half; i = (i <= 0 ? 1 : 0) - i) {
                for (int j = 0; j <= range && j >= -range; j = (j <= 0 ? 1 : 0) - j) {
                    for (int k = 0; k <= range && k >= -range; k = (k <= 0 ? 1 : 0) - k) {
                        BlockPos offset = blockpos.offset(j, i, k);
                        BlockState state = attractor.level().getBlockState(offset);
                        if (state.is(Tags.Blocks.ORES)) {
                            if (ores.size() < maxOres) {
                                ores.add(offset);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            for (BlockPos ore : ores) {
                HighlightedBlockEntity highlight = PHEntityRegistry.HIGHLIGHTED_BLOCK.get().create(attractor.level());
                highlight.setPos(Vec3.atBottomCenterOf(ore));
                highlight.setLifespan(effectLength);
                highlight.setXRot(0);
                highlight.setYRot(0);
                attractor.level().addFreshEntity(highlight);
            }
        }
    }

    public static int getImmuneTime(LivingEntity enchanted) {
        if (hasEnchant(enchanted, ModEnchantments.IMMUNITY_FRAME)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
            return tag.getInt(IMMUNITY_TIME_TAG);
        }
        return 0;
    }

    public static void setImmuneTime(LivingEntity enchanted, int time) {
        if (hasEnchant(enchanted, ModEnchantments.IMMUNITY_FRAME)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
            tag.putInt(IMMUNITY_TIME_TAG, time);
            sync(enchanted, tag);
        }
    }

    public static int getFrozenTime(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(FROZEN_TIME_TAG);
    }

    public static void setFrozenTimeTag(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(FROZEN_TIME_TAG, time);
        sync(enchanted, tag);
    }

    public static List<LivingEntity> getNearbyHealers(LivingEntity hurtOwner) {
        Predicate<Entity> healer = (animal) -> hasSameOwnerAs((LivingEntity) animal, hurtOwner) && hasEnchant((LivingEntity) animal, ModEnchantments.HEALING_AURA) && getHealingAuraTime((LivingEntity) animal) == 0;
        return hurtOwner.level().getEntitiesOfClass(LivingEntity.class, hurtOwner.getBoundingBox().inflate(16, 4, 16), EntitySelector.NO_SPECTATORS.and(healer));
    }

    public static float getFallDistance(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getFloat(FALL_DISTANCE_SYNC);
    }

    public static void setFallDistance(LivingEntity enchanted, float dist) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putFloat(FALL_DISTANCE_SYNC, dist);
        sync(enchanted, tag);

    }

    public static int getShadowPunchCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(SHADOW_PUNCH_COOLDOWN);
    }

    public static void setShadowPunchCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(SHADOW_PUNCH_COOLDOWN, time);
        sync(enchanted, tag);
    }

    public static int[] getShadowPunchTimes(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntArray(SHADOW_PUNCH_TIMES);
    }

    public static void setShadowPunchTimes(LivingEntity enchanted, int[] times) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putIntArray(SHADOW_PUNCH_TIMES, times);
        sync(enchanted, tag);
    }

    public static void setShadowPunchStriking(LivingEntity enchanted, int[] times) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putIntArray(SHADOW_PUNCH_STRIKING, times);
        sync(enchanted, tag);
    }

    public static int[] getShadowPunchStriking(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntArray(SHADOW_PUNCH_STRIKING);
    }

    public static boolean isValidTeleporter(LivingEntity owner, Mob animal) {
        if (hasEnchant(animal, ModEnchantments.TETHERED_TELEPORT)) {
            if (animal instanceof IComandableMob commandableMob) {
                return commandableMob.getCommand() == 2;
            } else if (animal instanceof TamableAnimal tame) {
                return !tame.isOrderedToSit() && animal.distanceTo(owner) < 10;
            }
        }
        return false;
    }

    public static void applyGlowingEffect(LivingEntity livingEntity, int enchantLevel) {
        var range = enchantLevel * 15;
        livingEntity.level().getEntitiesOfClass(LivingEntity.class, new AABB(livingEntity.getX() - range, livingEntity.getY() - range, livingEntity.getZ() - range,
                livingEntity.getX() + range, livingEntity.getY() + range, livingEntity.getZ() + range)).stream().filter(i -> i instanceof Enemy).forEach((entity) -> entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0)));
    }

    public static List<LivingEntity> getNearbyMobs(LivingEntity entity, double range) {
        // 计算范围边界框
        AABB area = new AABB(
                entity.getX() - range, entity.getY() - range, entity.getZ() - range,
                entity.getX() + range, entity.getY() + range, entity.getZ() + range
        );

        // 获取范围内的活体生物（排除自身）
        return entity.level().getEntitiesOfClass(LivingEntity.class, area, e ->
                e != entity && !(e instanceof Player)
        );
    }

    public static void performSonicBook(LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {
        var livings = getNearbyMobs(maid, 5.0).stream().filter(i -> i instanceof Enemy).collect(Collectors.toSet());
        if (livings.size() > 3) {
            for (var enemy : livings) {
                sonicBoomAttack(maid, enemy, serverLevel);
            }
        } else {
            sonicBoomAttack(maid, monster, serverLevel);
        }
    }

    public static void sonicBoomAttack(LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {

        Vec3 vec3 = maid.position().add(maid.getAttachments().get(EntityAttachment.WARDEN_CHEST, 0, maid.getYRot()));
        Vec3 vec32 = monster.getEyePosition().subtract(vec3);
        Vec3 vec33 = vec32.normalize();
        int i = Mth.floor(vec32.length()) + 7;

        for (int j = 1; j < i; j++) {
            Vec3 vec34 = vec3.add(vec33.scale(j));
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, vec34.x, vec34.y, vec34.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        maid.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
        if (monster.hurt(serverLevel.damageSources().sonicBoom(maid), 10.0F)) {
            double d = 0.5 * (1.0 - monster.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            double e = 2.5 * (1.0 - monster.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            monster.push(vec33.x() * e, vec33.y() * d, vec33.z() * e);
        }
    }
}
