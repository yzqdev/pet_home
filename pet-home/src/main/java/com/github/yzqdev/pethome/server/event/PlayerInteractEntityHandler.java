package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.misc.CollarTickManager;
import com.github.yzqdev.pethome.server.misc.PHWorldData;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.mixin.RabbitAccessor;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.item.DeedOfOwnershipItem;
import com.github.yzqdev.pethome.server.item.NetItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.item.Type;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 实体交互总入口（{@code UseEntityCallback} 接线）。判定顺序：
 * 捕网抓捕 → （自家宠物）贪婪喂食 → 附魔书应用 → 项圈标签 → 所有权契约 →
 * （兔子，在 isPetOf 门之外）干草块驯服/指令轮换 → 实体转换。
 *
 * <p>服务端执行实际逻辑；客户端只做挥手反馈并取消事件，避免客户端预测开始进食。
 * 转换逻辑（烂苹果/阴森胡萝卜）统一在本类，物品类只是 tooltip 壳。</p>
 */
public class PlayerInteractEntityHandler {


    /** 注册 {@code UseEntityCallback}：构造 {@link EntityInteractContext} 后进入 {@link #onInteractWithEntity}，返回其 result（PASS/取消/成功）。 */
    public static void init() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            ItemStack itemStack = player.getItemInHand(hand);
            EntityInteractContext context = new EntityInteractContext(player, hand, level, entity, itemStack);
            onInteractWithEntity(context);
            return context.result();
        });
    }

    private static boolean canLivingConvert(LivingEntity living, EntityType<?> type, Consumer<LivingEntity> callback) {
        return living instanceof Mob;
    }


    private static void onLivingConvert(LivingEntity from, LivingEntity to) {
    }

    private static ItemEnchantments getStackEnchantments(ItemStack stack) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        return stored.isEmpty() ? stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY) : stored;
    }

    /**
     * 交互分发主流程（见类注释的判定顺序）。各分支命中即 return（通过 {@code event.result()}
     * 回传 {@code PASS}/取消/成功）；兔子驯服分支必须放在 {@code isPetOf} 门之外，
     * 因为干草块驯服针对的是没有主人的野兔。
     */
    public static void onInteractWithEntity(EntityInteractContext event) {
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

            // 附魔书应用（DI 原版行为：手持宠物附魔书右键宠物即可附加附魔）
            if (handleEnchantedBook(event, living, itemstack)) return;

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
     * 空捕网右键生物：经 {@code NetItem.canCatchMob} 校验（黑名单/仅可驯服）后，把实体完整 NBT
     * 存入 {@code ENTITY_HOLDER} 组件、生成 {@code net_has_item} 物品，目标 discard，
     * 并给捕网 5 tick 冷却。仅服务端；命中后返回 true（消费本次交互）。
     */
    private static boolean handleNetCapture(EntityInteractContext event, Player player, InteractionHand hand, Level level, Entity target, ItemStack itemstack) {
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
                ItemStack newStack = new ItemStack(PHItemRegistry.NET_HAS_ITEM);
                CompoundTag nbt = NetItem.getNBTfromEntity(target);
                ItemStack newerStack = newStack.split(1);
                newerStack.set(PHDataComponents.ENTITY_HOLDER, nbt);

                player.swing(hand);
                player.setItemInHand(hand, newStack);
                if (!player.addItem(newerStack)) {
                    ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), newerStack);
                   
                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.addFreshEntity(itemEntity);
                    }
                    // 罗盘档案标记「球中」：不接钩子档案仍指向已 discard 的实体 UUID，罗盘会误判「记录已失效」
                    if (target instanceof LivingEntity capturedPet) {
                        PetCompassTracker.onPetCapturedByNet(capturedPet, itemEntity);
                    }
                } else if (target instanceof LivingEntity capturedPet) {
                    PetCompassTracker.onPetCapturedByNet(capturedPet, player);
                }
                target.discard();
                // 26.1: ItemCooldowns#addCooldown 改为接收 ItemStack
                player.getCooldowns().addCooldown(itemstack, 5);
                return true;
            }
        }
        return false;
    }

    /**
     * 附魔书右键宠物：提取书中全部 pet_home 附魔（书里没有则放行走原版交互），
     * 经 {@code TameableUtils.addEnchant} 整体写入宠物（覆盖原附魔）、播升级音效并消耗书本。
     */
    private static boolean handleEnchantedBook(EntityInteractContext event, LivingEntity living, ItemStack itemstack) {
        if (!itemstack.is(Items.ENCHANTED_BOOK)) {
            return false;
        }
        ItemEnchantments stored = getStackEnchantments(itemstack);
        ItemEnchantments.Mutable petEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        int petEnchantCount = 0;
        for (Holder<Enchantment> holder : stored.keySet()) {
            boolean isPetEnchant = holder.unwrapKey()
                    .map(ResourceKey::identifier)
                    .map(id -> id.getNamespace().equals(PetHomeMod.MODID))
                    .orElse(false);
            if (isPetEnchant) {
                petEnchants.set(holder, stored.getLevel(holder));
                petEnchantCount++;
            }
        }
        if (petEnchantCount == 0) {
            return false;
        }
        if (!event.getEntity().level().isClientSide() && living.isAlive()) {
            TameableUtils.addEnchant(living, petEnchants.toImmutable());
            living.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
            if (!event.getEntity().isCreative()) {
                itemstack.shrink(1);
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        return true;
    }

    /**
     * 处理“贪婪”附魔的自动喂食愈合逻辑
     */
    private static boolean handleGluttonousFeeding(EntityInteractContext event, LivingEntity living, ItemStack itemstack) {
        if (TameableUtils.hasEnchant(living, PHEnchantments.GLUTTONOUS)) {
            // 26.1: 食物属性改由 DataComponents.FOOD 组件提供
            var foodProperty = itemstack.get(DataComponents.FOOD);
            if (foodProperty != null && living.getHealth() < living.getMaxHealth()) {
                living.heal((float) Math.floor(foodProperty.nutrition() * 1.5F));
                if (!event.getEntity().isCreative()) {
                    itemstack.shrink(1);
                }
                living.playSound(living.getRandom().nextBoolean() ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT.value(), 1F, living.getVoicePitch());
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return true;
            }
        }
        return false;
    }

    /**
     * 项圈标签应用/替换：换名（标签有 CUSTOM_NAME 时）→ 旧项圈（已挂附魔）连附魔重铸成
     * 项圈物品掉落 → 清空宠物附魔后写入新项圈的附魔（{@code clearEnchants} +
     * {@code addEnchant}）；{@code CollarTickManager.blockCollarTick} 抑制刚换装的 20 tick。
     */
    private static boolean handleCollarTag(EntityInteractContext event, LivingEntity living, ItemStack itemstack) {
        if (itemstack.is(PHItemRegistry.COLLAR_TAG)) {
            if (!event.getEntity().level().isClientSide() && living.isAlive()) {
                var itemEnchantments = getStackEnchantments(itemstack);
                Map<Identifier, Integer> entityEnchantments = TameableUtils.getEnchants(living);

                if (itemstack.has(DataComponents.CUSTOM_NAME)) {
                    living.setCustomName(itemstack.getHoverName());
                }
                if (!event.getEntity().isCreative()) {
                    itemstack.shrink(1);
                }

                if (TameableUtils.hasCollar(living)) {
                    ItemStack collarFrom = new ItemStack(PHItemRegistry.COLLAR_TAG);
                    if (entityEnchantments != null) {
                        // 26.1: RegistryAccess#registry 改名 lookup，Registry#get 改名 getValue
                        var reg = living.level().registryAccess().lookup(Registries.ENCHANTMENT);
                        reg.ifPresent(r -> {
                            for (Map.Entry<Identifier, Integer> entry : entityEnchantments.entrySet()) {
                                var oneEnchant = r.getValue(entry.getKey());
                                if (oneEnchant != null) {
                                    collarFrom.enchant(r.wrapAsHolder(oneEnchant), entry.getValue());
                                }
                            }
                        });
                    }
                    // 26.1: spawnAtLocation 需要 ServerLevel
                    if (living.level() instanceof ServerLevel serverLevel) {
                        living.spawnAtLocation(serverLevel, collarFrom);
                    }
                }

               CollarTickManager.blockCollarTick(living);
                living.playSound(PHSoundRegistry.COLLAR_TAG, 1, 1);
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
     * 宠物契约（deed_of_ownership）两段逻辑（对齐 26.3 语义；放生已拆分为自由之铃）：
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
    private static boolean handleDeedOfOwnership(EntityInteractContext event, Player player, InteractionHand hand, Entity target, ItemStack itemstack) {
        if (!TameableUtils.isTamed(target) || !itemstack.is(PHItemRegistry.DEED_OF_OWNERSHIP)) {
            return false;
        }

        // 1. 未绑定 + 自家宠物 → 绑定
        UUID boundPetId = DeedOfOwnershipItem.getBoundPetId(itemstack);
        if (boundPetId == null) {
            if (!TameableUtils.isPetOf(player, target)) {
                return false;
            }
            DeedOfOwnershipItem.bind(itemstack, target.getUUID(), target.getName().getString());
            player.sendOverlayMessage(Component.translatable(LangDefinition.MESSAGE_DEED_BOUND, target.getName()));
            player.swing(hand);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }

        // 2. 已绑定 + 同一宠物 → 转让所有权给当前持有人
        if (!boundPetId.equals(target.getUUID())) {
            return false;
        }
        player.getCooldowns().addCooldown(itemstack, 5);
        Entity previousOwner = TameableUtils.getOwnerOf(target);
        setOwnerOf(target, player.getUUID());
        player.sendOverlayMessage(Component.translatable(LangDefinition.MESSAGE_SET_OWNER, player.getName(), target.getName()));
        if (previousOwner instanceof Player previousOwnerPlayer
                && !previousOwnerPlayer.getUUID().equals(player.getUUID())) {
            previousOwnerPlayer.sendOverlayMessage(Component.translatable(LangDefinition.MESSAGE_SET_OWNER, player.getName(), target.getName()));
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
     * <p>仅服务端执行确认状态机：交互事件客户端也会触发，而 {@link #PENDING_RELEASES} 是静态 Map，
     * 单 JVM（runClient/单人）下客户端先 put、服务端随后命中同一窗口，会「一次右键即放生」。
     * 客户端侧只取消事件以阻断原版交互预测，不写确认状态。
     *
     * @return true 表示已处理并取消事件（调用方应 return）
     */
    private static boolean handleFreedomBell(EntityInteractContext event, Player player, InteractionHand hand, Entity target, ItemStack itemstack) {
        if (!TameableUtils.isTamed(target) || !itemstack.is(PHItemRegistry.FREEDOM_BELL)) {
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
            player.sendOverlayMessage(Component.translatable(LangDefinition.MESSAGE_RELEASE_CONFIRM, target.getName()));
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
            // 26.1: TamableAnimal 不再有 setOwnerUUID
            tamableAnimal.setOwnerReference(null);
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
        player.sendOverlayMessage(Component.translatable(LangDefinition.MESSAGE_RELEASED, pet.getName()));
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
            tamableAnimal.setOwnerReference(EntityReference.of(ownerUuid));
        }
    }

    /**
     * 汇总处理实体的转换（进化/变异）逻辑。
     * 转换只在这里做：转换完成后必须取消事件，否则原版 interactOn 会继续走
     * Item.interactLivingEntity（历史双马根因——物品类里曾有一份重复转换逻辑，已删除）。
     * 客户端只做挥手反馈并取消事件，避免客户端预测开始进食。
     *
     * <p>物品判定写 {@code PHItemRegistry.X}（注册后的实例字段），
     * {@code ItemStack#is(Item)} 是实例相等判定。</p>
     */
    private static void handleEntityConversions(EntityInteractContext event, Player player, InteractionHand hand, LivingEntity living, ItemStack itemstack) {
        boolean clientSide = living.level().isClientSide();
        // 马 -> 僵尸马 (烂苹果)
        if (living.getType() == EntityType.HORSE && itemstack.is(PHItemRegistry.ROTTEN_APPLE)) {
            if (canLivingConvert(living, EntityType.ZOMBIE_HORSE, (timer) -> {})) {
                if (clientSide) {
                    player.swing(hand);
                } else {
                    convertHorseToZombie(player, hand, (Horse) living, itemstack);
                }
                event.setCanceled(true);
                event.setCancellationResult(clientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS);
            }
        }
        // 兔子 -> 邪恶兔子 (阴森胡萝卜)
        else if (living.getType() == EntityType.RABBIT && itemstack.is(PHItemRegistry.SINISTER_CARROT)) {
            if (TameableUtils.isTamed(living) && TameableUtils.isPetOf(player, living) &&
                    canLivingConvert(living, EntityType.RABBIT, (timer) -> {})) {
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
            if (canLivingConvert(living, EntityType.SKELETON_HORSE, (timer) -> {})) {
                if (clientSide) {
                    player.swing(hand);
                } else {
                    convertZombieToSkeletonHorse(player, hand, (ZombieHorse) living, itemstack);
                }
                event.setCanceled(true);
                event.setCancellationResult(clientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS);
            }
        }
    }

    /** 马 → 僵尸马（烂苹果）：掉落马铠、保存全量 NBT、播马死亡+感染音效，转换后 discard 原马并消耗苹果。 */
    private static void convertHorseToZombie(Player player, InteractionHand hand, Horse horse, ItemStack itemstack) {
        player.swing(hand);
        horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
        horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());

        CompoundTag horseExtras;
        if (!horse.getBodyArmorItem().isEmpty()) {
            // 26.1: spawnAtLocation 需要 ServerLevel
            if (horse.level() instanceof ServerLevel serverLevel) {
                horse.spawnAtLocation(serverLevel, horse.getBodyArmorItem().copy());
            }
            horse.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        }
        // 26.1: addAdditionalSaveData 改为写入 ValueOutput
        TagValueOutput horseOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, horse.registryAccess());
        horse.saveWithoutId(horseOutput);
        horseExtras = horseOutput.buildResult();
        spawnSneezeParticles(horse);

        ZombieHorse zombie = EntityType.ZOMBIE_HORSE.create(horse.level(), EntitySpawnReason.CONVERSION);

        if (zombie != null) {
            transferBasicData(horse, zombie, horseExtras);
            onLivingConvert(horse, zombie);
           
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.addFreshEntity(zombie);
            }
            horse.discard();
            consumeItem(player, itemstack);
        }
    }

    /** 自家兔子 → 邪恶兔（阴森胡萝卜）：同一实体上直接换 variant（经 {@code RabbitAccessor.invokeSetVariant}，26.1 已私有），不重建实体。 */
    private static void convertRabbitToEvil(Player player, InteractionHand hand, Rabbit rabbit, ItemStack itemstack) {
        if (rabbit.getVariant() != Rabbit.Variant.EVIL) {
            player.swing(hand);
            rabbit.playSound(SoundEvents.RABBIT_ATTACK, 0.8F, rabbit.getVoicePitch());
            rabbit.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, rabbit.getVoicePitch());
            ((RabbitAccessor) rabbit).invokeSetVariant(Rabbit.Variant.EVIL);
            consumeItem(player, itemstack);
        }
    }

    /** 僵尸马 → 骷髅马（阴森胡萝卜）：流程同 {@link #convertHorseToZombie}，全量 NBT 经 {@link #transferBasicData} 迁移（剔除 UUID）。 */
    private static void convertZombieToSkeletonHorse(Player player, InteractionHand hand, ZombieHorse horse, ItemStack itemstack) {
        player.swing(hand);
        horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
        horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());

        // 26.1: addAdditionalSaveData 改为写入 ValueOutput
        TagValueOutput horseOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, horse.registryAccess());
        horse.saveWithoutId(horseOutput);
        CompoundTag horseExtras = horseOutput.buildResult();
        spawnSneezeParticles(horse);

        SkeletonHorse skeleton = EntityType.SKELETON_HORSE.create(horse.level(), EntitySpawnReason.CONVERSION);
        if (skeleton != null) {
            transferBasicData(horse, skeleton, horseExtras);
            onLivingConvert(horse, skeleton);
           
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.addFreshEntity(skeleton);
            }
            horse.discard();
            consumeItem(player, itemstack);
        }
    }

    /**
     * NBT 迁移：位置/朝向/名字/幼年/拴绳等基础数据 + {@code saveWithoutId} 全量 NBT 载入新实体。
     * <b>UUID 键必须剔除</b>：26.1 {@code saveWithoutId} 会写入旧实体 UUID，原样 load 会让新实体
     * 沿用旧 UUID，与仍在世界里的旧实体冲突而被 "UUID of added entity already exists" 丢弃，
     * 旧实体随后又被 discard——表现为"喂完马消失"。
     */
    private static void transferBasicData(Mob oldMob, Mob newMob, CompoundTag extras) {
        if (oldMob.isLeashed()) {
            newMob.setLeashedTo(oldMob.getLeashHolder(), true);
        }
        newMob.setPos(oldMob.getX(), oldMob.getY(), oldMob.getZ());
        newMob.setYRot(oldMob.getYRot());
        newMob.setXRot(oldMob.getXRot());
        newMob.setNoAi(oldMob.isNoAi());
        if (oldMob instanceof AgeableMob ageable && newMob instanceof AgeableMob newAgeable) {
            newAgeable.setBaby(ageable.isBaby());
        }
        if (oldMob.hasCustomName()) {
            newMob.setCustomName(oldMob.getCustomName());
            newMob.setCustomNameVisible(oldMob.isCustomNameVisible());
        }
        // 26.1: Entity.saveWithoutId 会把旧实体的 UUID 一并写入，若原样 load，
        // 新实体会沿用旧实体的 UUID；而旧实体此时仍在世界里，PersistentEntitySectionManager
        // 会报 "UUID of added entity already exists" 并丢弃新实体，随后旧实体又被 discard，
        // 表现出来就是"喂完东西马消失了"。这里剔除 UUID，让新实体保留自己新建的 UUID。
        extras.remove("UUID");
        // 26.1: readAdditionalSaveData 改为读取 ValueInput
        newMob.load(TagValueInput.create(ProblemReporter.DISCARDING, newMob.registryAccess(), extras));
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
     * 干草块对兔子：已驯服→治疗 3 点；未驯服→消耗干草块，50% 概率驯服成功（爱心粒子 + 设主人 + 指令置 1 停留），
     * 失败冒烟。驯服判定仅服务端跑（随机数与落库都只在服务端）。
     */
    private static boolean handleRabbitHayBlock(EntityInteractContext event, Player player, Rabbit rabbit, ItemStack stack) {
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
