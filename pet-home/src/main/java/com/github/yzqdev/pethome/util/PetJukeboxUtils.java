package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.NbtKeys;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** 跟随唱片机（disc_jockey）数据与魅力（charisma）交易折扣（原 {@code TameableUtils} 八区）。 */
final class PetJukeboxUtils {

  private PetJukeboxUtils() {}

  /** charisma（魅力，自 1.20 移植）对主人的交易折扣加成：25 格内自家宠物的 10×等级之和，上限 50。 */
  static int getCharismaBonusForOwner(Player player) {
    Predicate<Entity> pet =
        (animal) -> PetOwnershipUtils.isTamed(animal) && PetOwnershipUtils.isPetOf(player, animal);
    List<LivingEntity> list =
        player
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
    return tag.hasUUID(NbtKeys.JUKEBOX_FOLLOWER_UUID)
        ? tag.getUUID(NbtKeys.JUKEBOX_FOLLOWER_UUID)
        : null;
  }

  static void setPetJukeboxUUID(LivingEntity enchanted, UUID id) {
    CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
    tag.putUUID(NbtKeys.JUKEBOX_FOLLOWER_UUID, id);
    PetDataSync.sync(enchanted, tag);
  }

  /** disc_jockey 唱片机当前播放的唱片 ItemStack（整体序列化到 citadel tag）；未设置返回 EMPTY。 */
  static ItemStack getPetJukeboxDisc(LivingEntity enchanted) {
    CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
    return tag.contains(NbtKeys.JUKEBOX_FOLLOWER_DISC)
        ? ItemStack.parseOptional(
            enchanted.level().registryAccess(), tag.getCompound(NbtKeys.JUKEBOX_FOLLOWER_DISC))
        : ItemStack.EMPTY;
  }

  static void setPetJukeboxDisc(LivingEntity enchanted, ItemStack stack) {
    CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
    tag.put(NbtKeys.JUKEBOX_FOLLOWER_DISC, stack.save(enchanted.level().registryAccess()));
    PetDataSync.sync(enchanted, tag);
  }
}
