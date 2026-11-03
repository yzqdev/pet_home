package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.NbtKeys;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * disc_jockey / charisma 相关数据 ：跟随唱片机的 UUID、当前唱片 ItemStack、
 * 以及魅力附魔对主人的交易折扣。
 *
 * <p>两个功能都挂在「宠物 + citadel tag」这套存储上，读写同样只经 {@link PetDataSync#sync}。
 */
final class PetJukeboxUtils {

    private PetJukeboxUtils() {
    }

    /** charisma（魅力，自 1.20 移植）对主人的交易折扣加成：25 格内自家宠物的 10×等级之和，上限 50。 */
    static int getCharismaBonusForOwner(Player player) {
        Predicate<Entity> pet = (animal) -> PetOwnershipUtils.isTamed(animal) && PetOwnershipUtils.isPetOf(player, animal);
        List<LivingEntity> list = player
                .level()
                .getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(25, 8, 25),
                        EntitySelector.NO_SPECTATORS.and(pet));
        int charismas = 0;
        for (LivingEntity entity : list) {
            charismas += 10 * PetEnchantmentUtils.getEnchantLevel(entity, PHEnchantments.CHARISMA);
        }
        return Math.min(charismas, 50);
    }

    /** disc_jockey 唱片机跟随者的 UUID（未设置返回 null），citadel tag 存储。 */
    @Nullable
    static UUID getPetJukeboxUUID(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.read(NbtKeys.JUKEBOX_FOLLOWER_UUID, UUIDUtil.CODEC).orElse(null);
    }

    static void setPetJukeboxUUID(LivingEntity enchanted, UUID id) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.store(NbtKeys.JUKEBOX_FOLLOWER_UUID, UUIDUtil.CODEC, id);
        PetDataSync.sync(enchanted, tag);
    }

    /** disc_jockey 唱片机当前播放的唱片 ItemStack（整体序列化到 citadel tag）；未设置返回 EMPTY。 */
    static ItemStack getPetJukeboxDisc(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.read(NbtKeys.JUKEBOX_FOLLOWER_DISC, ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    /**
     * 空 stack 表示「取下唱片」：直接删键（读端本来就按缺失返回 EMPTY）。
     * {@link ItemStack#CODEC} 拒绝空 stack（{@code Item must not be minecraft:air}），若走 store
     * 会在服务端交互线程抛 IllegalStateException 被吞，导致唱片取不下来。
     */
    static void setPetJukeboxDisc(LivingEntity enchanted, ItemStack stack) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        if (stack.isEmpty()) {
            tag.remove(NbtKeys.JUKEBOX_FOLLOWER_DISC);
        } else {
            tag.store(NbtKeys.JUKEBOX_FOLLOWER_DISC, ItemStack.CODEC, stack);
        }
        PetDataSync.sync(enchanted, tag);
    }
}
