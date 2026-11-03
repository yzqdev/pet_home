package com.github.yzqdev.pethome.server.entity;



import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.network.EntityDataTagUpdatePacket;
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
 * citadel tag 写入的<b>唯一出口</b>（package-private，原 {@code TameableUtils#sync}）。
 * 
 * <p>职责类只能通过本类写 citadel tag，不得绕过：本侧数据在 {@code LivingEntity} 的
 * SynchedEntityData CompoundTag 槽内，写入后走三参 force set 触发 vanilla 同步，
 * 客户端侧另发 C2S 上行。保持包内可见而非 public——不对外暴露第二条写入路径。
 */
final class PetDataSync {

    private PetDataSync() {}
    /**
     * citadel tag 写回的唯一出口（服务端）：{@link CitadelEntityData#setCitadelTag} 内部 force 写 entityData，
     * 由原版机制把差异同步给追踪该实体的玩家（不再全服广播）。客户端侧额外把整个 tag 作为
     * {@code entityDataTagUpdate} 包发回服务端确认。所有 setXxx 计时器/标记方法都经由本方法。
     */
    static void sync(LivingEntity enchanted, CompoundTag tag) {
        // 服务端不再手动全服广播：setCitadelEntityData 内部 force 写 entityData，
        // vanilla 只把变化发给追踪该实体的玩家（原先 sendMSGToAll 发给全服所有玩家，双通道冗余）
        CitadelEntityData.setCitadelTag(enchanted, tag);
        if (enchanted.level().isClientSide()) {
            Networking.sendMSGToServer(new EntityDataTagUpdatePacket(enchanted.getId(), tag));
        }
    }
}
