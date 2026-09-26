package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;


public class PHAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, PetHomeMod.MODID);

    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, PetHomeMod.MODID);

    public static final Supplier<EntityDataSerializer<CompoundTag>> COMPOUND_TAG =
            ENTITY_DATA_SERIALIZERS.register("compound_tag", () -> EntityDataSerializer.forValueType(ByteBufCodecs.TRUSTED_COMPOUND_TAG));

    public static final Supplier<EntityDataSerializer<Optional<UUID>>> OPTIONAL_UUID =
            ENTITY_DATA_SERIALIZERS.register("optional_uuid", () -> EntityDataSerializer.forValueType(
                    UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional)));


    public static final Supplier<AttachmentType<CompoundTag>> PETHOME_DATA =
            ATTACHMENT_TYPES.register(PHConstants.entitySyncData, () -> AttachmentType.builder(() -> new CompoundTag())
                    .serialize(new IAttachmentSerializer<CompoundTag>() {
                        @Override
                        public CompoundTag read(IAttachmentHolder holder, ValueInput input) {

                            CompoundTag tag = input.read("PetHomeData", CompoundTag.CODEC).orElseGet(CompoundTag::new);

                            if (holder instanceof LivingEntity) {
                                bridgeHotTimer(holder, tag, TameableUtils.HEALING_AURA_TIME, HEALING_AURA_TIME);
                                bridgeHotTimer(holder, tag, TameableUtils.FROZEN_TIME_TAG, FROZEN_TIME);
                            }
                            return tag;
                        }

                        @Override
                        public boolean write(CompoundTag attachment, ValueOutput output) {
                            output.store("PetHomeData", CompoundTag.CODEC, attachment);
                            return !attachment.isEmpty();
                        }
                    })
                    .sync(ByteBufCodecs.TRUSTED_COMPOUND_TAG)
                    .build());

    /**
     * 附魔等级解析缓存：非同步、非持久化附件（无 serialize/sync），纯运行期对象，
     * setData 不会产生任何网络包；随实体回收。
     */
    public static final Supplier<AttachmentType<PetEnchantCache>> ENCHANT_CACHE =
            ATTACHMENT_TYPES.register("pet_enchant_cache", () -> AttachmentType.builder(PetEnchantCache::new).build());

    /**
     * 高频计时器独立 int 附件：每 tick 的增减只广播一个小整数，
     * 不再为计数器改动重发整个 citadel CompoundTag（与 1.20/1.21 的 int 数据槽同语义）。
     */
    public static final Supplier<AttachmentType<Integer>> HEALING_AURA_TIME =
            ATTACHMENT_TYPES.register("pet_healing_aura_time", () -> AttachmentType.builder(() -> 0)
                    .serialize(new IAttachmentSerializer<Integer>() {
                        @Override
                        public Integer read(IAttachmentHolder holder, ValueInput input) {
                            return input.getIntOr("value", 0);
                        }

                        @Override
                        public boolean write(Integer attachment, ValueOutput output) {
                            output.putInt("value", attachment);
                            return attachment != 0;
                        }
                    })
                    .sync(ByteBufCodecs.VAR_INT)
                    .build());

    public static final Supplier<AttachmentType<Integer>> FROZEN_TIME =
            ATTACHMENT_TYPES.register("pet_frozen_time", () -> AttachmentType.builder(() -> 0)
                    .serialize(new IAttachmentSerializer<Integer>() {
                        @Override
                        public Integer read(IAttachmentHolder holder, ValueInput input) {
                            return input.getIntOr("value", 0);
                        }

                        @Override
                        public boolean write(Integer attachment, ValueOutput output) {
                            output.putInt("value", attachment);
                            return attachment != 0;
                        }
                    })
                    .sync(ByteBufCodecs.VAR_INT)
                    .build());

    private static void bridgeHotTimer(IAttachmentHolder holder, CompoundTag tag, String legacyKey, Supplier<AttachmentType<Integer>> target) {
        if (tag.contains(legacyKey)) {
            holder.setData(target.get(), tag.getIntOr(legacyKey, 0));
            tag.remove(legacyKey);
        }
    }
}
