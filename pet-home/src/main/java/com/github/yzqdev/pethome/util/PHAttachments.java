package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;


/**
 * Fabric Data Attachment 侧的宠物数据附件与自定义实体数据序列化器注册：
 * {@link #PETHOME_DATA} 是挂在每个 {@code LivingEntity} 上的 {@code CompoundTag}
 * （由 {@link CitadelEntityData} 统一读写，随附件自动同步到客户端）；
 * 另注册与 NeoForge 契约一致的两个 {@link EntityDataSerializer}
 * （{@code pet_home:compound_tag} / {@code pet_home:optional_uuid}）。
 */
public final class PHAttachments {

    private PHAttachments() {
    }

    /** 同步用 codec：{@code ByteBufCodecs} 里的是 {@code StreamCodec<ByteBuf, CompoundTag>}，
     *  附件同步要求 {@code StreamCodec<? super RegistryFriendlyByteBuf, CompoundTag>}，用 {@code cast()} 转换 */
    private static final StreamCodec<RegistryFriendlyByteBuf, CompoundTag> PET_DATA_SYNC_CODEC =
            ByteBufCodecs.TRUSTED_COMPOUND_TAG.cast();

    /** 宠物数据（附魔列表、宠物床坐标、项圈状态、各种冷却……） */
    public static final AttachmentType<CompoundTag> PETHOME_DATA = AttachmentRegistry.<CompoundTag>builder()
            .initializer(CompoundTag::new)
            .persistent(CompoundTag.CODEC)
            .syncWith(PET_DATA_SYNC_CODEC, AttachmentSyncPredicate.all())
            .buildAndRegister(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, PHConstants.entitySyncData));

    /**
     * 附魔等级解析缓存：非同步、非持久化附件（无 persistent/syncWith），纯运行期对象，
     * setAttached 不会产生任何网络包；随实体回收（对齐 NF 26.3 基准的 {@code pet_enchant_cache}）。
     * Fabric 侧按 {@link #PETHOME_DATA} 惯例直接持有 {@link AttachmentType} 实例
     * （{@code getAttached/setAttached} 不收 Supplier，与 NF 的 Supplier 形态不同）。
     */
    public static final AttachmentType<PetEnchantCache> ENCHANT_CACHE = AttachmentRegistry.<PetEnchantCache>builder()
            .initializer(PetEnchantCache::new)
            .buildAndRegister(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_enchant_cache"));

    /**
     * 高频计时器独立 int 附件（对齐 NF 26.3 基准）：治愈光环相位每 tick 的增减只广播一个小整数，
     * 不再为计数器改动重发整个 citadel CompoundTag。旧存档 citadel tag 里残留的
     * {@code PetHealingAuraTime} 键不再读取（瞬态相位，读档后从待机重新开始）。
     */
    public static final AttachmentType<Integer> HEALING_AURA_TIME = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .persistent(Codec.INT)
            .syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.all())
            .buildAndRegister(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_healing_aura_time"));

    /** frost_fang 冻结计时独立 int 附件（对齐 NF 26.3 基准 {@code pet_frozen_time}），语义同 {@link #HEALING_AURA_TIME}。 */
    public static final AttachmentType<Integer> FROZEN_TIME = AttachmentRegistry.<Integer>builder()
            .initializer(() -> 0)
            .persistent(Codec.INT)
            .syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.all())
            .buildAndRegister(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_frozen_time"));

    /**
     * 实体同步数据序列化器：对应 NeoForge 侧注册在
     * {@code NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS} 的两个序列化器
     * （{@code RecallBallEntity} 与 {@code PsychicWallEntity} 通过
     * {@link #COMPOUND_TAG} / {@link #OPTIONAL_UUID} 使用）。
     *
     * <p><b>26.1 必须注册</b>：{@code SynchedEntityData.Builder#define} 会调用
     * {@code EntityDataSerializers.getSerializedId(serializer)} 校验序列化器已登记，
     * 未注册会抛 {@code IllegalArgumentException: Unregistered serializer ... for N!}。
     * （1.21 侧没有这个校验，本类此前只提供实例常量。）</p>
     */
    private static final EntityDataSerializer<CompoundTag> COMPOUND_TAG_INSTANCE =
            EntityDataSerializer.forValueType(ByteBufCodecs.TRUSTED_COMPOUND_TAG);

    private static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID_INSTANCE =
            EntityDataSerializer.forValueType(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional));

    /**
     * 类加载时立刻登记两个自定义序列化器。
     *
     * <p><b>必须走 Fabric 的注册入口</b>：直接调用原版
     * {@code EntityDataSerializers.registerSerializer} 会被 fabric-object-builder-api-v1
     * 拦截并抛 {@code IllegalStateException: ... use FabricEntityDataRegistry.register instead}
     * ——原版那条路径不会分配网络同步 id，会导致双端 id 不一致。
     * {@link FabricEntityDataRegistry#register} 会同时完成「登记 + 分配同步 id」。</p>
     *
     * <p>注册名与 NeoForge 侧保持一致（{@code pet_home:compound_tag} / {@code pet_home:optional_uuid}）。
     * 放在静态块里而不是 {@link #init()}：实体类（{@code PsychicWallEntity} /
     * {@code RecallBallEntity}）的静态字段会调用 {@link #COMPOUND_TAG} / {@link #OPTIONAL_UUID}
     * 构造 {@code EntityDataAccessor}，从而先行触发本类初始化——这样能保证任何
     * {@code defineSynchedData} 执行之前序列化器**已经**登记完毕，不依赖
     * {@code PetHomeMod.onInitialize()} 的调用时序。</p>
     */
    static {
        FabricEntityDataRegistry.register(
                Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "compound_tag"), COMPOUND_TAG_INSTANCE);
        FabricEntityDataRegistry.register(
                Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "optional_uuid"), OPTIONAL_UUID_INSTANCE);
    }

    /** 保持 NeoForge 侧 {@code Supplier} 形态，调用点的 {@code .get()} 无需改动 */
    public static final Supplier<EntityDataSerializer<CompoundTag>> COMPOUND_TAG = () -> COMPOUND_TAG_INSTANCE;

    public static final Supplier<EntityDataSerializer<Optional<UUID>>> OPTIONAL_UUID = () -> OPTIONAL_UUID_INSTANCE;

    /** 触发类加载，确保附件在玩家进入世界前完成注册 */
    public static void init() {
        PetHomeMod.LOGGER.debug("[pet_home] attachments registered: {}", PETHOME_DATA);
    }
}
