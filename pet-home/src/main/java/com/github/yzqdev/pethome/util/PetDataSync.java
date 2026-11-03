package com.github.yzqdev.pethome.util;


import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.network.EntityDataTagUpdatePacket;
import com.github.yzqdev.pethome.server.entity.HighlightedBlockEntity;
import com.github.yzqdev.pethome.server.NbtKeys;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * citadel tag 写入的<b>唯一出口</b>（package-private，原 {@code TameableUtils#sync}）。
 * 
 * <p>职责类只能通过本类写 citadel tag，不得直接 {@code setData} 绕过：数据写入经 {@link
 * CitadelEntityData#setCitadelTag}（内容相等则 no-op），客户端侧另发 C2S 上行。
 * 保持包内可见而非 public——不对外暴露第二条写入路径。
 */
final class PetDataSync {

    private PetDataSync() {}
    /**
     * 把改动写回实体 citadel 附件，全模组所有 citadel tag 写入的唯一出口。
     * 服务端：交给 {@link CitadelEntityData#setCitadelTag}——内容相等时 no-op，否则 setData 换实例，
     * NeoForge 对 synced 附件的 setData 自动把新值广播给所有追踪者（本侧无手工全服广播包）。
     * 客户端：本地写入附件后再向服务端发 C2S {@code entityDataTagUpdate}（服务端有所有权校验），
     * 让服务端收下客户端侧的预测改动（如暗影手渲染状态）。
     */
    static void sync(LivingEntity entity, CompoundTag tag) {
        // S2C 同步由 NeoForge 自带：对 synced 附件 setData 会自动广播给所有追踪者（含新追踪者的初始同步），无需再手工全服广播。
        CitadelEntityData.setCitadelTag(entity, tag);
        if (entity.level().isClientSide()) {
            ClientPacketDistributor.sendToServer(new EntityDataTagUpdatePacket(entity.getId(), tag.copy()));
        }
    }
}
