package com.github.yzqdev.pethome.server.handler;

import com.github.yzqdev.pethome.server.NbtKeys;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.ServerEvents;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.IComandableMob;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.item.DeedOfOwnershipItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.server.misc.PHTagRegistry;
import com.github.yzqdev.pethome.server.misc.PHWorldData;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 从 CommonProxy.onInteractWithEntity 拆分而来：玩家与宠物实体的交互逻辑。
 */
public class EntityInteractHandler {

    /**
     * 实体右键交互总入口（实例方法，由 PetHomeMod 注册到事件总线），按顺序分发、命中即取消事件：
     * 马类/兔子实体转换（烂苹果/阴森胡萝卜）→ 契约（转让/绑定所有权）→ 自由之铃放生 →
     * 贪婪附魔喂食 → 兔子干草块驯服/治疗与三态指令（在 isPetOf 门外，野兔也可驯服）→
     * 项圈佩戴/更换（旧项圈连同附魔掉落、写入新附魔并触发 5 tick 防抖）。
     */
    @SubscribeEvent
    public void onInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity entity = event.getTarget();
        var hand = event.getHand();
        ItemStack stack = event.getItemStack();

        if (entity instanceof LivingEntity living) {
            // 3. 实体转换逻辑 (马变僵尸、兔子变邪恶、僵尸马变骷髅马)
            handleEntityConversions(event, player, hand, living, stack);
        }
        if (TameableUtils.isTamed(event.getTarget())) {
            if (event.getItemStack().is(PHItemRegistry.DEED_OF_OWNERSHIP.get())) {
                if (handleDeedOfOwnership(event, player, stack, entity)) {
                    return;
                }
            }
            // 自由之铃放生（限自家宠物，二次确认）
            if (event.getItemStack().is(PHItemRegistry.FREEDOM_BELL.get())) {
                if (handleFreedomBell(event, player, hand, entity, stack)) {
                    return;
                }
            }
        }
        if (event.getTarget() instanceof LivingEntity living && TameableUtils.isTamed(entity) && TameableUtils.hasEnchant(living, PHEnchantmentRegistry.GLUTTONOUS)) {
            tryFeedGluttonous(event, player, living, stack);
        }
        if (event.getTarget() instanceof Rabbit rabbit && PetHomeMod.CONFIG.tameableRabbit.get()) {
            if (handleRabbitHayBlock(event, player, rabbit, stack)) {
                return;
            }
            // 手持该宠物食物且原版会消费它时不拦——回血（不满血）/ 繁殖（成年可恋爱）/ 幼年加速成长；三态只接管「原版这次右键什么也不做」的情况
            boolean vanillaFoodAction = rabbit.isFood(stack) && (rabbit.getHealth() < rabbit.getMaxHealth() || rabbit.isBaby() || (rabbit.getAge() == 0 && rabbit.canFallInLove()));
            if (TameableUtils.isTamed(rabbit) && TameableUtils.isPetOf(player, rabbit) && !vanillaFoodAction) {
                ((IComandableMob) rabbit).playerSetCommand(player, rabbit);
            }
        }
        if (event.getTarget() instanceof LivingEntity living && TameableUtils.isPetOf(event.getEntity(), entity) && !living.getType().is(PHTagRegistry.REFUSES_COLLAR_TAGS)) {
            applyCollarTag(event, player, living, stack);
        }
    }

    /**
     * 汇总处理实体的转换（进化/变异）逻辑。转换只在这里做：转换完成后必须取消事件，否则原版 interactOn
     * 会继续走 Item.interactLivingEntity（历史双马根因——物品类里曾有一份重复转换逻辑，已删除）。
     * 客户端只做挥手反馈并取消事件，避免客户端预测开始进食与客户端侧实体重建。
     */
    private void handleEntityConversions(PlayerInteractEvent.EntityInteract event, Player player, InteractionHand hand, LivingEntity living, ItemStack itemstack) {
        boolean clientSide = living.level().isClientSide();
        // 马 -> 僵尸马 (烂苹果)
        if (living.getType() == EntityType.HORSE && itemstack.is(PHItemRegistry.ROTTEN_APPLE.get())) {
            if (ForgeEventFactory.canLivingConvert(living, EntityType.ZOMBIE_HORSE, (timer) -> {})) {
                if (clientSide) {
                    player.swing(hand);
                } else {
                    convertHorse(player, hand, itemstack, (Horse) living, EntityType.ZOMBIE_HORSE);
                }
                event.setCanceled(true);
                event.setCancellationResult(clientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS);
            }
        }
        // 兔子 -> 邪恶兔子 (阴森胡萝卜)
        else if (living.getType() == EntityType.RABBIT && itemstack.is(PHItemRegistry.SINISTER_CARROT.get())) {
            if (TameableUtils.isTamed(living) && TameableUtils.isPetOf(player, living)
                    && ForgeEventFactory.canLivingConvert(living, EntityType.RABBIT, (timer) -> {})) {
                if (clientSide) {
                    player.swing(hand);
                } else {
                    convertRabbitToEvil(player, hand, itemstack, living);
                }
                event.setCanceled(true);
                event.setCancellationResult(clientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS);
            }
        }
        // 僵尸马 -> 骷髅马 (阴森胡萝卜)
        else if (living.getType() == EntityType.ZOMBIE_HORSE && itemstack.is(PHItemRegistry.SINISTER_CARROT.get())) {
            if (ForgeEventFactory.canLivingConvert(living, EntityType.SKELETON_HORSE, (timer) -> {})) {
                if (clientSide) {
                    player.swing(hand);
                } else {
                    convertHorse(player, hand, itemstack, (ZombieHorse) living, EntityType.SKELETON_HORSE);
                }
                event.setCanceled(true);
                event.setCancellationResult(clientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS);
            }
        }
    }

    /**
     * 马类转换共用流程（烂苹果马→僵尸马 / 阴森胡萝卜僵尸马→骷髅马）：掉落马铠（仅普通马有）、
     * 复制 NBT（含名字/AI/幼年/缰绳）重建目标实体，触发 onLivingConvert 事件后 discard 原马，非创造扣物品。
     * 仅服务端调用。
     */
    private void convertHorse(Player player, InteractionHand hand, ItemStack stack, AbstractHorse horse, EntityType<? extends AbstractHorse> resultType) {
        horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
        horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());
        CompoundTag horseExtras = new CompoundTag();
        if (horse instanceof Horse regularHorse && !regularHorse.getArmor().isEmpty()) {
            horse.spawnAtLocation(regularHorse.getArmor().copy());
            horse.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        }
        horse.addAdditionalSaveData(horseExtras);
        spawnSneezeParticles(horse);
        AbstractHorse result = resultType.create(horse.level());
        if (result != null) {
            transferBasicData(horse, result, horseExtras);
            net.minecraftforge.event.ForgeEventFactory.onLivingConvert(horse, result);
            player.level().addFreshEntity(result);
            horse.discard();
            consumeItem(player, stack);
        }
    }
    /** 阴森胡萝卜把自家兔子变为杀手兔（EVIL 变体），仅换变体不重建实体。 */
    private void convertRabbitToEvil(Player player, InteractionHand hand, ItemStack stack, LivingEntity living) {
        Rabbit rabbit = (Rabbit) living;
        if (rabbit.getVariant() != Rabbit.Variant.EVIL) {
            rabbit.playSound(SoundEvents.RABBIT_ATTACK, 0.8F, rabbit.getVoicePitch());
            rabbit.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, rabbit.getVoicePitch());
            rabbit.setVariant(Rabbit.Variant.EVIL);
            consumeItem(player, stack);
        }
    }

    /**
     * 旧实体 → 新实体的数据搬运：位置/朝向/自定义名/幼年态/拴绳 + load 完整 NBT（extras）并设持久化。
     * extras 来自 {@code addAdditionalSaveData}（不含 UUID/位置），原样 load 安全。仅服务端调用。
     */
    private static void transferBasicData(Mob oldMob, Mob newMob, CompoundTag extras) {
        if (oldMob.isLeashed()) {
            newMob.setLeashedTo(oldMob.getLeashHolder(), true);
        }
        newMob.moveTo(oldMob.getX(), oldMob.getY(), oldMob.getZ(), oldMob.getYRot(), oldMob.getXRot());
        newMob.setNoAi(oldMob.isNoAi());
        if (oldMob instanceof AgeableMob ageable && newMob instanceof AgeableMob newAgeable) {
            newAgeable.setBaby(ageable.isBaby());
        }
        if (oldMob.hasCustomName()) {
            newMob.setCustomName(oldMob.getCustomName());
            newMob.setCustomNameVisible(oldMob.isCustomNameVisible());
        }
        newMob.readAdditionalSaveData(extras);
        newMob.setPersistenceRequired();
    }

    /** 辅助方法：生成喷嚏粒子 */
    private static void spawnSneezeParticles(Entity entity) {
        for (int i = 0; i < 6 + entity.level().random.nextInt(5); i++) {
            entity.level().addParticle(ParticleTypes.SNEEZE, entity.getRandomX(1.0F), entity.getRandomY(), entity.getRandomZ(1.0F), 0F, 0F, 0F);
        }
    }

    /** 辅助方法：消耗物品 */
    private static void consumeItem(Player player, ItemStack stack) {
        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }

    /**
     * 契约（deed_of_ownership）两段逻辑：未绑定 + 打自家宠物 → 绑定该宠物 UUID；
     * 已绑定 + 打同名宠物 → 转移所有权给持有人（原主人收到提示，5 tick 冷却，消费契约）。
     *
     * @return true 表示已处理并取消事件（调用方应 return）
     */
    private boolean handleDeedOfOwnership(PlayerInteractEvent.EntityInteract event, Player player, ItemStack stack, Entity entity) {
        CompoundTag tag = stack.getTag();
        boolean unbound = !DeedOfOwnershipItem.isBound(event.getItemStack());
        Entity currentOwner = TameableUtils.getOwnerOf(entity);
        if (TameableUtils.isTamed(entity) && currentOwner != null && currentOwner.equals(player) && unbound) {
            CompoundTag newTag = new CompoundTag();
            newTag.putBoolean(NbtKeys.HAS_BOUND_ENTITY, true);
            newTag.putUUID(NbtKeys.BOUND_ENTITY, entity.getUUID());
            newTag.putString(NbtKeys.BOUND_ENTITY_NAME, entity.getName().getString());
            stack.setTag(newTag);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }
        if (TameableUtils.isTamed(entity) && tag != null && tag.getBoolean(NbtKeys.HAS_BOUND_ENTITY) && tag.getUUID(NbtKeys.BOUND_ENTITY) != null) {
            UUID fromItem = tag.getUUID(NbtKeys.BOUND_ENTITY);
            if (entity.getUUID().equals(fromItem)) {
                player.getCooldowns().addCooldown(stack.getItem(), 5);
                TameableUtils.setOwnerUUIDOf(entity, player.getUUID());
                player.displayClientMessage(Component.translatable("message.pet_home.set_owner", player.getName(), entity.getName()), true);
                if (currentOwner instanceof Player && !currentOwner.equals(player)) {
                    ((Player) currentOwner).displayClientMessage(Component.translatable("message.pet_home.set_owner", player.getName(), entity.getName()), true);
                }
                stack.setTag(new CompoundTag());
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return true;
            }
        }
        return false;
    }

    /** 放生二次确认的待确认条目：宠物 UUID + 过期时间（Util.getMillis）；超时未确认自动失效 */
    private record PendingRelease(UUID petId, long expireAt) {}

    /** 自由之铃放生确认的存活窗口（毫秒） */
    private static final long RELEASE_CONFIRM_WINDOW_MS = 10_000L;

    /** 每个玩家的待确认放生（玩家 UUID → 待确认宠物），10s 内对同一宠物再次使用自由之铃才真正放生 */
    private static final Map<UUID, PendingRelease> PENDING_RELEASES = new HashMap<>();

    /**
     * 自由之铃（freedom_bell）放生逻辑：
     *
     * <ol>
     *   <li>对自家宠物首次使用 → 仅发送二次确认提示（10s 内有效）；
     *   <li>10s 内对同一宠物再次使用 → 真正放生（{@link #releasePet}，消耗铃铛）。
     * </ol>
     *
     * 服务端校验归属，防伪造交互；非自家宠物或未驯服实体不拦截，走原版逻辑。
     *
     * <p>仅服务端执行确认状态机：{@code EntityInteract} 客户端也会触发（{@code Player.interactOn} 双端
     * 同一代码路径，客户端还会先本地执行再发包），而 {@link #PENDING_RELEASES} 是静态 Map，单 JVM （runClient/单人）下客户端先
     * put、服务端随后命中同一窗口，会「一次右键即放生」。客户端侧只取消事件以阻断 原版交互预测（驯服宠物右键切坐姿/兔子切三指令、副手二次发包），不写确认状态。
     *
     * @return true 表示已处理并取消事件（调用方应 return）
     */
    private boolean handleFreedomBell(PlayerInteractEvent.EntityInteract event, Player player, InteractionHand hand, Entity target, ItemStack itemstack) {
        if (!TameableUtils.isPetOf(player, target)) {
            return false;
        }
        if (player.level().isClientSide()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }
        UUID petId = target.getUUID();
        PendingRelease pending = PENDING_RELEASES.get(player.getUUID());
        if (pending != null && pending.petId().equals(petId) && Util.getMillis() < pending.expireAt()) {
            PENDING_RELEASES.remove(player.getUUID());
            PetHomeMod.LOGGER.info("FreedomBell release player={} pet={}", player.getName().getString(), petId);
            releasePet(player, hand, target, itemstack);
        } else {
            PENDING_RELEASES.put(player.getUUID(), new PendingRelease(petId, Util.getMillis() + RELEASE_CONFIRM_WINDOW_MS));
            PetHomeMod.LOGGER.info("FreedomBell pending player={} pet={}", player.getName().getString(), petId);
            player.displayClientMessage(Component.translatable(LangDefinition.MESSAGE_RELEASE_CONFIRM, target.getName()), true);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        return true;
    }

    /** 放生：解除驯服（清附魔/宠物床/主人，起立），并同步删除罗盘档案（避免残留「失效」条目）。 自由之铃的二次确认已在调用方完成。仅服务端调用。 */
    private void releasePet(Player player, InteractionHand hand, Entity target, ItemStack itemstack) {
        LivingEntity pet = (LivingEntity) target;
        TameableUtils.clearEnchants(pet);
        TameableUtils.removePetBedPos(pet);
        if (pet instanceof TamableAnimal tamableAnimal) {
            tamableAnimal.setTame(false);
            tamableAnimal.setOwnerUUID(null);
            tamableAnimal.setOrderedToSit(false);
            tamableAnimal.setInSittingPose(false);
        }
        if (pet instanceof ModifiedToBeTameable modified) {
            modified.setTame(false);
            modified.setTameOwnerUUID(null);
        }
        PHWorldData data = PHWorldData.get(pet.level());
        if (data != null) {
            data.removePetRecord(pet.getUUID());
        }
        player.displayClientMessage(Component.translatable(LangDefinition.MESSAGE_RELEASED, pet.getName()), true);
        if (!player.isCreative()) {
            itemstack.shrink(1);
        }
        player.swing(hand);
    }

    /** gluttonous（贪婪）附魔：手持可食用物喂受伤宠物，按食物营养值 ×1.5 回血并消耗物品。 */
    private void tryFeedGluttonous(PlayerInteractEvent.EntityInteract event, Player player, LivingEntity living, ItemStack stack) {
        if (stack.getItem().isEdible() && living.getHealth() < living.getMaxHealth() && stack.getItem().getFoodProperties() != null) {
            living.heal((float) Math.floor(stack.getItem().getFoodProperties().getNutrition() * 1.5F));
            if (!event.getEntity().isCreative()) {
                stack.shrink(1);
            }
            living.playSound(living.getRandom().nextBoolean() ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT, 1F, living.getVoicePitch());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    /**
     * 兔子干草块交互：受伤兔子治疗 3 点；野兔 50% 概率驯服（驯服后置坐下指令）、失败出烟粒子。
     *
     * @return true 表示已处理并取消事件（调用方应 return）
     */
    private boolean handleRabbitHayBlock(PlayerInteractEvent.EntityInteract event, Player player, Rabbit rabbit, ItemStack stack) {
        if (stack.getItem() == Items.HAY_BLOCK) {
            if (TameableUtils.isTamed(rabbit) && rabbit.getHealth() < rabbit.getMaxHealth()) {
                rabbit.heal(3);
                if (!event.getEntity().isCreative()) {
                    stack.shrink(1);
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return true;
            }
            if (!TameableUtils.isTamed(rabbit) && !rabbit.level().isClientSide()) {
                if (!event.getEntity().isCreative()) {
                    stack.shrink(1);
                }
                rabbit.playSound(SoundEvents.FOX_EAT);
                if (rabbit.getRandom().nextBoolean()) {
                    for (int i = 0; i < 3; ++i) {
                        double d0 = rabbit.getRandom().nextGaussian() * 0.02D;
                        double d1 = rabbit.getRandom().nextGaussian() * 0.02D;
                        double d2 = rabbit.getRandom().nextGaussian() * 0.02D;
                        ((ServerLevel) rabbit.level()).sendParticles(ParticleTypes.HEART, rabbit.getRandomX(1.0D), rabbit.getRandomY() + 0.5D, rabbit.getRandomZ(1.0D), 3, d0, d1, d2, 0.02F);
                    }
                    ((ModifiedToBeTameable) rabbit).setTame(true);
                    ((ModifiedToBeTameable) rabbit).setTameOwnerUUID(player.getUUID());
                    ((IComandableMob) rabbit).setCommand(1);
                } else {
                    for (int i = 0; i < 3; ++i) {
                        double d0 = rabbit.getRandom().nextGaussian() * 0.02D;
                        double d1 = rabbit.getRandom().nextGaussian() * 0.02D;
                        double d2 = rabbit.getRandom().nextGaussian() * 0.02D;
                        ((ServerLevel) rabbit.level()).sendParticles(ParticleTypes.SMOKE, rabbit.getRandomX(1.0D), rabbit.getRandomY() + 0.5D, rabbit.getRandomZ(1.0D), 3, d0, d1, d2, 0.02F);
                    }
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return true;
            }
        }
        return false;
    }

    /**
     * 项圈佩戴/更换（双端拦截，实际逻辑仅服务端）：同名同附魔的项圈直接 FAIL 拒绝；
     * 项圈名作为宠物名应用；已戴项圈时旧项圈连同宠物现有附魔掉落；随后按新项圈附魔
     * addEnchant（空附魔则 clearEnchants）并标 5 tick 防抖，最后取消事件返回 SUCCESS。
     */
    private void applyCollarTag(PlayerInteractEvent.EntityInteract event, Player player, LivingEntity living, ItemStack stack) {
        if (event.getItemStack().is(PHItemRegistry.COLLAR_TAG.get()) && PetHomeMod.CONFIG.collarTag.get()) {
            if (!event.getEntity().level().isClientSide() && living.isAlive()) {
                Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.deserializeEnchantments(stack.getEnchantmentTags());
                Map<ResourceLocation, Integer> entityEnchantments = TameableUtils.getEnchants(living);
                if (stack.hasCustomHoverName() && living.hasCustomName() && stack.getHoverName().equals(living.getCustomName())) {
                    boolean hasSameEnchants = itemEnchantments.isEmpty();
                    if (entityEnchantments != null) {
                        hasSameEnchants = true;
                        for (Map.Entry<Enchantment, Integer> itemEntry : itemEnchantments.entrySet()) {
                            ResourceLocation name = ForgeRegistries.ENCHANTMENTS.getKey(itemEntry.getKey());
                            if (entityEnchantments.get(name) == null || !entityEnchantments.get(name).equals(itemEntry.getValue())) {
                                hasSameEnchants = false;
                            }
                        }
                    }
                    if (hasSameEnchants) {
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.FAIL);
                        return;
                    }
                }
                if (stack.hasCustomHoverName()) {
                    living.setCustomName(stack.getHoverName());
                }
                if (!event.getEntity().isCreative()) {
                    stack.shrink(1);
                }
                ServerEvents.blockCollarTick(living);
                if (TameableUtils.hasCollar(living)) {
                    ItemStack collarFrom = new ItemStack(PHItemRegistry.COLLAR_TAG.get());
                    if (entityEnchantments != null) {
                        collarFrom.getOrCreateTag();
                        if (!collarFrom.getTag().contains(NbtKeys.ENCHANTMENTS, 9)) {
                            collarFrom.getTag().put(NbtKeys.ENCHANTMENTS, new ListTag());
                        }

                        ListTag listtag = collarFrom.getTag().getList(NbtKeys.ENCHANTMENTS, 10);
                        for (Map.Entry<ResourceLocation, Integer> entry : entityEnchantments.entrySet()) {
                            listtag.add(EnchantmentHelper.storeEnchantment(entry.getKey(), entry.getValue()));
                        }
                    } else {
                        collarFrom.setTag(null);
                    }
                    living.spawnAtLocation(collarFrom);
                }
                living.playSound(PHSoundRegistry.COLLAR_TAG.get(), 1, 1);
                if (itemEnchantments.isEmpty()) {
                    TameableUtils.clearEnchants(living);
                } else {
                    ListTag listTag = new ListTag();
                    for (Map.Entry<Enchantment, Integer> entry : itemEnchantments.entrySet()) {
                        TameableUtils.addEnchant(living, new EnchantmentInstance(entry.getKey(), entry.getValue()), listTag);
                    }
                }
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
