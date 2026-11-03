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


/**
 * citadel 的替代数据层：挂在 LivingEntity 上的 {@link AttachmentType} 注册中心。
 * 分层存储——大块状态（附魔/项圈/宠物床坐标/指令）走 {@link #PETHOME_DATA}（synced CompoundTag）；
 * 高频计时器走 {@link #HEALING_AURA_TIME}/{@link #FROZEN_TIME}（synced int）；解析缓存走 {@link #ENCHANT_CACHE}（纯运行期）。
 */
public class PHAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, PetHomeMod.MODID);

    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, PetHomeMod.MODID);

    /** 自定义 EntityDataSerializer：CompoundTag 槽位（RecallBallEntity 的内容 NBT 等用）。 */
    public static final Supplier<EntityDataSerializer<CompoundTag>> COMPOUND_TAG =
            ENTITY_DATA_SERIALIZERS.register("compound_tag", () -> EntityDataSerializer.forValueType(ByteBufCodecs.TRUSTED_COMPOUND_TAG));

    /** 自定义 EntityDataSerializer：{@code Optional<UUID>} 槽位（召回球/念力墙的 owner 槽用）。 */
    public static final Supplier<EntityDataSerializer<Optional<UUID>>> OPTIONAL_UUID =
            ENTITY_DATA_SERIALIZERS.register("optional_uuid", () -> EntityDataSerializer.forValueType(
                    UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional)));


    /**
     * 主数据附件（注册名同 {@code PHConstants.entitySyncData}）：宠物附魔/项圈/宠物床坐标/指令状态等全存这里。
     * serializer 即存档桥接——读档时把旧版内嵌在 {@code PetHomeData} 里高频计时器
     * （{@link TameableUtils#HEALING_AURA_TIME} / {@code PetFrozenTime} 键）迁移到独立 int 附件并从 tag 中移除，
     * 键名与 1.20/1.21 侧保持一致；写盘时空 tag 不落盘。
     */
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

    /** 存档桥接：读档时若 {@code PetHomeData} 里还带着旧版高频计时器键，迁移到对应 int 附件并从主 tag 删除（一次性迁移动作）。 */
    private static void bridgeHotTimer(IAttachmentHolder holder, CompoundTag tag, String legacyKey, Supplier<AttachmentType<Integer>> target) {
        if (tag.contains(legacyKey)) {
            holder.setData(target.get(), tag.getIntOr(legacyKey, 0));
            tag.remove(legacyKey);
        }
    }
}
