package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.item.DeedOfOwnershipItem;
import com.github.yzqdev.pethome.server.item.NetItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.item.Type;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.server.misc.PHWorldData;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 实体右键交互总入口（{@code EntityInteract}，HIGHEST 优先级）。
 * 分发顺序与取消语义：捕网抓捕 → 自家宠物的贪婪喂食/项圈/契约 →
 * （isPetOf 门外的）兔子干草块驯服与三态指令 → 实体转换（烂苹果马/阴森胡萝卜兔/僵尸马）。
 * 各分支处理成功后取消事件并设 SUCCESS/CONSUME，避免原版继续走进食等预测。
 */
@EventBusSubscriber(modid = PetHomeMod.MODID)
public class PlayerInteractEntityHandler {
    /** 交互分发主入口；服务端分支各自 setCanceled，客户端只做挥手/预测反馈 */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        Level level = event.getLevel();
        Entity target = event.getTarget();
        ItemStack itemstack = event.getItemStack();

        // 1. 抓捕逻辑 (Net Item)
        if (handleNetCapture(event, player, hand, level, target, itemstack)) {
            return;
        }

        // 2. 宠物相关逻辑 (仅限 LivingEntity 且属于该玩家)
        if (target instanceof LivingEntity living && TameableUtils.isPetOf(player, target)) {
            // 贪婪附魔喂食
            if (handleGluttonousFeeding(event, living, itemstack)) return;

            // 项圈逻辑
            if (handleCollarTag(event, living, itemstack)) return;
        }

        // 契约逻辑（绑定/转让，转让需非主人也能对绑定宠物使用）：放在 isPetOf 门槛之外，方法内自行判定
        if (handleDeedOfOwnership(event, player, hand, target, itemstack)) return;

        // 自由之铃放生（限自家宠物，二次确认）
        if (handleFreedomBell(event, player, hand, target, itemstack)) return;

        // 兔子驯服/指令 (自 1.20 移植)——必须在 isPetOf 门之外：
        // 干草块驯服针对野兔（没有主人，isPetOf 恒 false），嵌进门内会导致驯服永远无法触发
        if (target instanceof Rabbit rabbit && PetHomeConfig.tameableRabbit) {
            if (handleRabbitHayBlock(event, player, rabbit, itemstack)) return;
            // 手持该宠物食物且原版会消费它时不拦——回血（不满血）/ 繁殖（成年可恋爱）/ 幼年加速成长；三态只接管「原版这次右键什么也不做」的情况
            boolean vanillaFoodAction =
                    rabbit.isFood(itemstack)
                        && (rabbit.getHealth() < rabbit.getMaxHealth()
                            || rabbit.isBaby()
                            || (rabbit.getAge() == 0 && rabbit.canFallInLove()));
            if (TameableUtils.isTamed(rabbit)
                && TameableUtils.isPetOf(player, rabbit)
                && !vanillaFoodAction) {
                ((IComandableMob) rabbit).playerSetCommand(player, rabbit);
            }
        }

        // 3. 实体转换逻辑 (马变僵尸、兔子变邪恶、僵尸马变骷髅马)
        if (target instanceof LivingEntity living) {
            handleEntityConversions(event, player, hand, living, itemstack);
        }
    }

    /**
     * 处理使用捕网抓捕实体的逻辑
     */
    private static boolean handleNetCapture(PlayerInteractEvent.EntityInteract event, Player player, InteractionHand hand, Level level, Entity target, ItemStack itemstack) {
        if (!level.isClientSide() && itemstack.is(PHItemRegistry.NET_ITEM)) {
            if (!target.isAlive() || NetItem.containsEntity(itemstack)) {
                return true;
            }
            var netItem = (NetItem) itemstack.getItem();
            if (netItem.getType() == Type.EMPTY) {
                if (!NetItem.canCatchMob(target)) {
                    event.setCanceled(true);
                    return true;
                }
                ItemStack newStack = new ItemStack(PHItemRegistry.NET_HAS_ITEM.get());
                CompoundTag nbt = NetItem.getNBTfromEntity(target);
                ItemStack newerStack = newStack.split(1);
                newerStack.set(PHDataComponents.ENTITY_HOLDER, nbt);

                player.swing(hand);
                player.setItemInHand(hand, newStack);
                if (!player.addItem(newerStack)) {
                    ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), newerStack);
                    player.level().addFreshEntity(itemEntity);
                    // 罗盘档案标记「球中」：不接钩子档案仍指向已 discard 的实体 UUID，罗盘会误判「记录已失效」
                    if (target instanceof LivingEntity capturedPet) {
                        PetCompassTracker.onPetCapturedByNet(capturedPet, itemEntity);
                    }
                } else if (target instanceof LivingEntity capturedPet) {
                    PetCompassTracker.onPetCapturedByNet(capturedPet, player);
                }
                target.discard();
                player.getCooldowns().addCooldown(itemstack.getItem(), 5);
                return true;
            }
        }
        return false;
    }

    /**
     * 处理“贪婪”附魔的自动喂食愈合逻辑
     */
    private static boolean handleGluttonousFeeding(PlayerInteractEvent.EntityInteract event, LivingEntity living, ItemStack itemstack) {
        if (TameableUtils.hasEnchant(living, PHEnchantments.GLUTTONOUS)) {
            var foodProperty = itemstack.getItem().getFoodProperties(itemstack, living);
            if (foodProperty != null && living.getHealth() < living.getMaxHealth()) {
                living.heal((float) Math.floor(foodProperty.nutrition() * 1.5F));
                if (!event.getEntity().isCreative()) {
                    itemstack.shrink(1);
                }
                living.playSound(living.getRandom().nextBoolean() ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT, 1F, living.getVoicePitch());
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return true;
            }
        }
        return false;
    }

    /**
     * 处理项圈标签的应用与替换逻辑
     */
    /**
     * 项圈戴上：带自定义名则改名；已戴项圈时旧附魔落回新项圈物品掉落，
     * 再清空宠物附魔并写入新项圈上的附魔；触发 5 tick 防抖后取消事件（双端）。
     */
    private static boolean handleCollarTag(PlayerInteractEvent.EntityInteract event, LivingEntity living, ItemStack itemstack) {
        if (itemstack.is(PHItemRegistry.COLLAR_TAG.get())) {
            if (!event.getEntity().level().isClientSide && living.isAlive()) {
                var itemEnchantments = itemstack.getTagEnchantments();
                Map<ResourceLocation, Integer> entityEnchantments = TameableUtils.getEnchants(living);

                if (itemstack.has(DataComponents.CUSTOM_NAME)) {
                    living.setCustomName(itemstack.getHoverName());
                }
                if (!event.getEntity().isCreative()) {
                    itemstack.shrink(1);
                }

                if (TameableUtils.hasCollar(living)) {
                    ItemStack collarFrom = new ItemStack(PHItemRegistry.COLLAR_TAG.get());
                    if (entityEnchantments != null) {
                        var reg = living.level().registryAccess().registry(Registries.ENCHANTMENT);
                        reg.ifPresent(r -> {
                            for (Map.Entry<ResourceLocation, Integer> entry : entityEnchantments.entrySet()) {
                                var oneEnchant = r.get(entry.getKey());
                                if (oneEnchant != null) {
                                    collarFrom.enchant(r.wrapAsHolder(oneEnchant), entry.getValue());
                                }
                            }
                        });
                    }
                    living.spawnAtLocation(collarFrom);
                }

                ServerEvent.blockCollarTick(living);
                living.playSound(PHSoundRegistry.COLLAR_TAG.get(), 1, 1);
                TameableUtils.clearEnchants(living);
                if (!itemEnchantments.isEmpty()) {
                    TameableUtils.addEnchant(living, itemEnchantments);
                }
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
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
     * 宠物契约（deed_of_ownership）两段逻辑（对齐 26.3 语义；放生已拆分为自由之铃，见 {@link #handleFreedomBell}）：
     *
     * <ol>
     *   <li>未绑定 + 对自家宠物使用 → 绑定该宠物（存 {@code BOUND_PET} 组件）；
     *   <li>已绑定 + 对同一宠物使用 → 转让所有权给当前持有人（原主人在线则提示，5 tick 冷却，消耗契约）。
     * </ol>
     *
     * 转让服务端校验归属，防伪造交互。
     *
     * @return true 表示已处理并取消事件（调用方应 return）
     */
    private static boolean handleDeedOfOwnership(PlayerInteractEvent.EntityInteract event, Player player, InteractionHand hand, Entity target, ItemStack itemstack) {
        if (!TameableUtils.isTamed(target) || !itemstack.is(PHItemRegistry.DEED_OF_OWNERSHIP.get())) {
            return false;
        }

        // 1. 未绑定 + 自家宠物 → 绑定
        UUID boundPetId = DeedOfOwnershipItem.getBoundPetId(itemstack);
        if (boundPetId == null) {
            if (!TameableUtils.isPetOf(player, target)) {
                return false;
            }
            DeedOfOwnershipItem.bind(itemstack, target.getUUID(), target.getName().getString());
            player.displayClientMessage(Component.translatable(LangDefinition.MESSAGE_DEED_BOUND, target.getName()), true);
            player.swing(hand);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }

        // 2. 已绑定 + 同一宠物 → 转让所有权给当前持有人
        if (!boundPetId.equals(target.getUUID())) {
            return false;
        }
        player.getCooldowns().addCooldown(itemstack.getItem(), 5);
        Entity previousOwner = TameableUtils.getOwnerOf(target);
        setOwnerOf(target, player.getUUID());
        player.displayClientMessage(Component.translatable(LangDefinition.MESSAGE_SET_OWNER, player.getName(), target.getName()), true);
        if (previousOwner instanceof Player previousOwnerPlayer
                && !previousOwnerPlayer.getUUID().equals(player.getUUID())) {
            previousOwnerPlayer.displayClientMessage(Component.translatable(LangDefinition.MESSAGE_SET_OWNER, player.getName(), target.getName()), true);
        }
        itemstack.remove(PHDataComponents.BOUND_PET);
        if (!player.isCreative()) {
            itemstack.shrink(1);
        }
        player.swing(hand);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        return true;
    }

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
    private static boolean handleFreedomBell(PlayerInteractEvent.EntityInteract event, Player player, InteractionHand hand, Entity target, ItemStack itemstack) {
        if (!TameableUtils.isTamed(target) || !itemstack.is(PHItemRegistry.FREEDOM_BELL.get())) {
            return false;
        }
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
    private static void releasePet(Player player, InteractionHand hand, Entity target, ItemStack itemstack) {
        LivingEntity pet = (LivingEntity) target;
        TameableUtils.clearEnchants(pet);
        TameableUtils.removePetBedPos(pet);
        if (pet instanceof TamableAnimal tamableAnimal) {
            tamableAnimal.setTame(false, false);
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

    /** 设置宠物主人 UUID：本模组改造实体与原版 TamableAnimal 两类归属载体兼容。 */
    private static void setOwnerOf(Entity pet, UUID ownerUuid) {
        if (pet instanceof ModifiedToBeTameable modified) {
            modified.setTame(true);
            modified.setTameOwnerUUID(ownerUuid);
        } else if (pet instanceof TamableAnimal tamableAnimal) {
            tamableAnimal.setOwnerUUID(ownerUuid);
        }
    }

    /**
     * 汇总处理实体的转换（进化/变异）逻辑
     */
    /**
     * 汇总处理实体的转换（进化/变异）逻辑。
     * 转换只在这里做：转换完成后必须取消事件，否则原版 interactOn 会继续走
     * Item.interactLivingEntity（历史双马根因——物品类里曾有一份重复转换逻辑，已删除）。
     * 客户端只做挥手反馈并取消事件，避免客户端预测开始进食。
     */
    private static void handleEntityConversions(PlayerInteractEvent.EntityInteract event, Player player, InteractionHand hand, LivingEntity living, ItemStack itemstack) {
        boolean clientSide = living.level().isClientSide();
        // 马 -> 僵尸马 (烂苹果)
        if (living.getType() == EntityType.HORSE && itemstack.is(PHItemRegistry.ROTTEN_APPLE)) {
            if (EventHooks.canLivingConvert(living, EntityType.ZOMBIE_HORSE, (timer) -> {})) {
                if (clientSide) {
                    player.swing(hand);
                } else {
                    convertHorse(player, hand, (Horse) living, itemstack, EntityType.ZOMBIE_HORSE);
                }
                event.setCanceled(true);
                event.setCancellationResult(clientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS);
            }
        }
        // 兔子 -> 邪恶兔子 (阴森胡萝卜)
        else if (living.getType() == EntityType.RABBIT && itemstack.is(PHItemRegistry.SINISTER_CARROT)) {
            if (TameableUtils.isTamed(living) && TameableUtils.isPetOf(player, living) &&
                    EventHooks.canLivingConvert(living, EntityType.RABBIT, (timer) -> {})) {
                if (clientSide) {
                    player.swing(hand);
                } else {
                    convertRabbitToEvil(player, hand, (Rabbit) living, itemstack);
                }
                event.setCanceled(true);
                event.setCancellationResult(clientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS);
            }
        }
        // 僵尸马 -> 骷髅马 (阴森胡萝卜)
        else if (living.getType() == EntityType.ZOMBIE_HORSE && itemstack.is(PHItemRegistry.SINISTER_CARROT)) {
            if (EventHooks.canLivingConvert(living, EntityType.SKELETON_HORSE, (timer) -> {})) {
                if (clientSide) {
                    player.swing(hand);
                } else {
                    convertHorse(player, hand, (ZombieHorse) living, itemstack, EntityType.SKELETON_HORSE);
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
    private static void convertHorse(Player player, InteractionHand hand, AbstractHorse horse, ItemStack itemstack, EntityType<? extends AbstractHorse> resultType) {
        horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
        horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());

        CompoundTag horseExtras = new CompoundTag();
        if (!horse.getBodyArmorItem().isEmpty()) {
            horse.spawnAtLocation(horse.getBodyArmorItem().copy());
            horse.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        }
        horse.addAdditionalSaveData(horseExtras);
        spawnSneezeParticles(horse);

        AbstractHorse result = resultType.create(horse.level());

        if (result != null) {
            transferBasicData(horse, result, horseExtras);
            EventHooks.onLivingConvert(horse, result);
            player.level().addFreshEntity(result);
            horse.discard();
            consumeItem(player, itemstack);
        }
    }

    private static void convertRabbitToEvil(Player player, InteractionHand hand, Rabbit rabbit, ItemStack itemstack) {
        if (rabbit.getVariant() != Rabbit.Variant.EVIL) {
            player.swing(hand);
            rabbit.playSound(SoundEvents.RABBIT_ATTACK, 0.8F, rabbit.getVoicePitch());
            rabbit.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, rabbit.getVoicePitch());
            rabbit.setVariant(Rabbit.Variant.EVIL);
            consumeItem(player, itemstack);
        }
    }

    /**
     * 实体转换时的数据继承：拴绳/位置/AI/幼年/自定义名外，
     * 还把旧实体<b>附加存档 NBT（不含 UUID，来自 addAdditionalSaveData）</b>readAdditionalSaveData 进新实体并设持久化。
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

    // 辅助方法：生成粒子
    private static void spawnSneezeParticles(Entity entity) {
        for (int i = 0; i < 6 + entity.level().getRandom().nextInt(5); i++) {
            entity.level().addParticle(ParticleTypes.SNEEZE, entity.getRandomX(1.0F), entity.getRandomY(), entity.getRandomZ(1.0F), 0F, 0F, 0F);
        }
    }

    // 辅助方法：消耗物品
    private static void consumeItem(Player player, ItemStack stack) {
        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }
    /**
     * 兔子驯服/治疗：干草块驯服兔子（自 1.20 移植）
     */
    /**
     * 干草块驯服兔子：已驯服个体治疗 3 点；野兔 50% 概率驯服成功（随机烟雾=失败），
     * 成功后写入主人并置三态指令为停留(1)。必须位于 isPetOf 门外（野兔无主人）。
     */
    private static boolean handleRabbitHayBlock(PlayerInteractEvent.EntityInteract event, Player player, Rabbit rabbit, ItemStack stack) {
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
}
