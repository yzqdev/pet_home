package com.github.yzqdev.pethome.util;

import net.minecraft.network.syncher.EntityDataAccessor;

import java.util.Optional;
import java.util.UUID;

/**
 * 26.1：SynchedEntityData 对同步数据的两条硬约束——
 * ① defineId 的调用者类必须是实体类本身、实体类上不得有 @MixinMerged 的 EntityDataAccessor 字段
 *   （CommonHooks.verifyEntityDataAccessorRegistration，IDE 内违规直接抛异常）；
 * ② defineId 必须在实体 <clinit> 内完成：实体构造时 Builder 容量 = 已注册访问器数量，
 *   晚注册会导致 builder.define 数组越界（AIOOBE）。
 * 因此由 mixin 注入到实体 <clinit> 的回调调用 defineId（合并后调用者类=实体类），
 * 返回的访问器统一存放在这个外部类里（owner 用原版 STRING 序列化器存 UUID 字符串，
 * 避免 <clinit> 期依赖 DeferredRegister 注册的自定义序列化器）。
 */
public class PHEntityDataAccessors {
    public static EntityDataAccessor<String> axolotlOwner;
    public static EntityDataAccessor<Integer> axolotlCommand;
    public static EntityDataAccessor<Boolean> axolotlTamed;
    public static EntityDataAccessor<String> frogOwner;
    public static EntityDataAccessor<Integer> frogCommand;
    public static EntityDataAccessor<Boolean> frogTamed;
    public static EntityDataAccessor<String> rabbitOwner;
    public static EntityDataAccessor<Integer> rabbitCommand;
    public static EntityDataAccessor<Boolean> rabbitTamed;
    public static EntityDataAccessor<Integer> wolfCommand;
    public static EntityDataAccessor<Integer> catCommand;
    public static EntityDataAccessor<Integer> parrotCommand;
    public static EntityDataAccessor<Integer> foxCommand;
}
