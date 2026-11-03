package com.github.yzqdev.pethome.server.entity;



import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.server.NbtKeys;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 附魔行为执行：经验转移/机敏/牧羊/恶名/威吓/枯萎/嗅矿/音波/洞察等 runtime 行为
 * （原 {@code TameableUtils} 六区）。
 */
final class PetEnchantmentBehaviorUtils {

    private PetEnchantmentBehaviorUtils() {}
    /** attract（吸引）附魔：每 8 tick 把 16 格内最多 max 只非同主动物吸引过来（清仇恨并寻路走向施放者）。 */
    static void attractAnimals(LivingEntity attractor, int max) {
        if ((attractor.tickCount + attractor.getId()) % 8 == 0) {
            Predicate<Entity> notOnTeam = (animal) -> !PetOwnershipUtils.hasSameOwnerAs((LivingEntity) animal, attractor) && animal.distanceTo(attractor) > 3 + attractor.getBbWidth() * 1.6F;
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

    /** aggro（挑衅）附魔：每 400 tick 让 20 格内最近的一只怪物把施放者设为攻击目标（拉仇恨保护主人）。 */
    static void aggroRandomMonsters(LivingEntity attractor) {
        if ((attractor.tickCount + attractor.getId()) % 400 == 0) {
            Predicate<Entity> notOnTeamAndMonster = (animal) -> animal instanceof Monster && !PetOwnershipUtils.hasSameOwnerAs((LivingEntity) animal, attractor) && animal.distanceTo(attractor) > 3 + attractor.getBbWidth() * 1.6F;
            List<Mob> list = attractor.level().getEntitiesOfClass(Mob.class, attractor.getBoundingBox().inflate(20, 8, 20), EntitySelector.NO_SPECTATORS.and(notOnTeamAndMonster));
            list.sort(Comparator.comparingDouble(attractor::distanceToSqr));
            if (!list.isEmpty()) {
                list.get(0).setTarget(attractor);
            }
        }
    }

    /** intimidation（威吓）附魔主逻辑：按等级间隔（或受击瞬间）威吓 10*level 格内怪物逃离，同时播放威吓粒子与吼声；冷却期只递减冷却不重复触发。 */
    static void scareRandomMonsters(LivingEntity scary, int level) {
        boolean interval = (scary.tickCount + scary.getId()) % Math.max(140, 600 - level * 200) == 0;
        if (interval || scary.hurtTime == 4 || PetStateUtils.getIntimidationCooldown(scary) > 0) {
            Predicate<Entity> notOnTeamAndMonster = (animal) -> animal instanceof Monster && !PetOwnershipUtils.hasSameOwnerAs((LivingEntity) animal, scary) && animal.distanceTo(scary) > 3 + scary.getBbWidth() * 1.6F;
            List<PathfinderMob> list = scary.level().getEntitiesOfClass(PathfinderMob.class, scary.getBoundingBox().inflate(10 * level, 8 * level, 10 * level), EntitySelector.NO_SPECTATORS.and(notOnTeamAndMonster));
            list.sort(Comparator.comparingDouble(scary::distanceToSqr));
            if (!list.isEmpty()) {
                if (PetStateUtils.getIntimidationCooldown(scary) > 0 && !interval) {
                    PetStateUtils.setIntimidationCooldown(scary, PetStateUtils.getIntimidationCooldown(scary) - 1);
                } else {
                    Vec3 rots = list.get(0).getEyePosition().subtract(scary.getEyePosition()).normalize();
                    float f = Mth.sqrt((float) (rots.x * rots.x + rots.z * rots.z));
                    double yRot = Math.atan2(-rots.z, -rots.x) * (double) (180F / (float) Math.PI) + 90F;
                    double xRot = Math.atan2(-rots.y, f) * (double) (180F / (float) Math.PI);
                    scary.level().addParticle(PHParticleRegistry.INTIMIDATION.get(), scary.getX(), scary.getY(), scary.getZ(), scary.getId(), xRot, yRot);
                    PetStateUtils.setIntimidationCooldown(scary, 70 * level);
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

    /** ore_scenting（矿石嗅探）附魔：每 interval tick 播放嗅探动作/粒子，第 30 tick 扫描 range 范围内矿石方块，在各矿点上生成存在 effectLength tick 的发光高亮实体（{@link HighlightedBlockEntity}）。 */
    static void detectRandomOres(LivingEntity attractor, int interval, int range, int effectLength, int maxOres) {
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

    /** blight（枯萎）附魔：每 200 tick 随机破坏身边花草/作物，并把草方块/耕地退化为泥土或砂土，配枯萎粒子。 */
    static void destroyRandomPlants(LivingEntity living) {
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

    /** Insight（洞察）：黑暗中让周围敌对生物发光，范围随附魔等级提升 */
    static void applyGlowingEffect(LivingEntity livingEntity, int enchantLevel) {
        var range = enchantLevel * 15;
        livingEntity.level().getEntitiesOfClass(LivingEntity.class, new AABB(livingEntity.getX() - range, livingEntity.getY() - range,
                        livingEntity.getZ() - range, livingEntity.getX() + range, livingEntity.getY() + range, livingEntity.getZ() + range))
                .stream().filter(i -> i instanceof Enemy).forEach((entity) -> entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0)));
    }

    /** Sonic Boom（音波轰击）：目标周围敌人密集时对群体释放，否则对主目标释放 */
    static void performSonicBook(LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {
        var livings = PetEntityQueryUtils.getNearbyMobs(maid, 5.0).stream().filter(i -> i instanceof Enemy).collect(Collectors.toSet());
        if (livings.size() > 3) {
            for (var enemy : livings) {
                sonicBoomAttack(maid, enemy, serverLevel);
            }
        } else {
            sonicBoomAttack(maid, monster, serverLevel);
        }
    }

    /** 监守者音波攻击的复刻：从施放者头上 1.3 格向目标射出 SONIC_BOOM 粒子轨迹，命中造成 10 点 sonic_boom 伤害并按击退抗性缩放的击退。 */
    static void sonicBoomAttack(LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {
        // 1.20.1 没有 EntityAttachment：监守者本体代码用 position + (0, 1.3, 0) 作为声波起点
        Vec3 vec3 = maid.position().add(0.0, 1.3F, 0.0);
        Vec3 vec32 = monster.getEyePosition().subtract(vec3);
        Vec3 vec33 = vec32.normalize();
        int i = Mth.floor(vec32.length()) + 7;

        for (int j = 1; j < i; j++) {
            Vec3 vec34 = vec3.add(vec33.scale(j));
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM, vec34.x, vec34.y, vec34.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        maid.playSound(net.minecraft.sounds.SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
        if (monster.hurt(serverLevel.damageSources().sonicBoom(maid), 10.0F)) {
            double d = 0.5 * (1.0 - monster.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            double e = 2.5 * (1.0 - monster.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            monster.push(vec33.x() * e, vec33.y() * d, vec33.z() * e);
        }
    }

    /** xp_transfer（经验转移）附魔：3 格内经验球被吸向宠物，2 格内被吸收并直接把经验值发给主人（宠物本体不加血），64 平方格内受引力影响。 */
    static void xpTransfer(LivingEntity living) {
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

    /** rejuvenation（回复）附魔：未满血时吸收 3 格内经验球回复自身（超出上限的余额留回经验球），仅服务端生效。 */
    static void absorbExpOrbs(LivingEntity living) {
        if (living.getHealth() < living.getMaxHealth() && !living.level().isClientSide()) {
            for (ExperienceOrb experienceorb : living.level().getEntitiesOfClass(ExperienceOrb.class, living.getBoundingBox().inflate(3D))) {
                if (living.getHealth() >= living.getMaxHealth()) {
                    break;
                }
                Vec3 vec3 = new Vec3(living.getX() - experienceorb.getX(), living.getY() + (double) living.getEyeHeight() / 2.0D - experienceorb.getY(), living.getZ() - experienceorb.getZ());
                double d0 = vec3.lengthSqr();
                if (d0 < 2.0D) {
                    float h = living.getHealth() + experienceorb.value;
                    living.setHealth(h);
                    if (h - living.getMaxHealth() > 0) {
                        experienceorb.value = (int) Math.floor(h - living.getMaxHealth());
                        break;
                    } else {
                        experienceorb.discard();
                    }
                }
                if (d0 < 64.0D) {
                    double d1 = 1.0D - Math.sqrt(d0) / 8.0D;
                    experienceorb.setDeltaMovement(experienceorb.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.5D)));
                }
            }
        }
    }
}
