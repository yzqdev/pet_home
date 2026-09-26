package com.github.yzqdev.pethome.server.misc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * 项圈 tick 抑制管理。
 *
 * <p>项圈刚被取下 / 装备后的短时间内不再对宠物跑附魔 tick（自 1.20 移植）。
 * NeoForge 侧这些方法挂在 {@code ServerEvent} 上（由 {@code LevelTickEvent.Post} 驱动），
 * Fabric 侧抽成独立类，避免核心逻辑依赖尚未完成迁移的事件处理器。</p>
 */
public final class CollarTickManager {

    private static final Map<Level, CollarTickTracker> TRACKER_MAP = new HashMap<>();

    private CollarTickManager() {
    }

    public static boolean canTickCollar(Entity entity) {
        if (entity.level().isClientSide()) {
            return true;
        }
        CollarTickTracker tracker = TRACKER_MAP.get(entity.level());
        return tracker == null || !tracker.isEntityBlocked(entity);
    }

    public static void blockCollarTick(Entity entity) {
        if (!entity.level().isClientSide()) {
            TRACKER_MAP.computeIfAbsent(entity.level(), k -> new CollarTickTracker())
                    .addBlockedEntityTick(entity.getUUID(), 5);
        }
    }

    /** 每级维度 tick 调用一次，递减抑制计时 */
    public static void tick(Level level) {
        if (level.isClientSide()) {
            return;
        }
        CollarTickTracker tracker = TRACKER_MAP.get(level);
        if (tracker != null) {
            tracker.tick();
        }
    }
}
