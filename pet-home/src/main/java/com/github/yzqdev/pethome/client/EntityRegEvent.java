package com.github.yzqdev.pethome.client;

/**
 * NeoForge 侧客户端注册事件的 Fabric 等价入口：全部转发到
 * {@link ClientModEvents} 对应方法，保持两端入口类结构一致。
 */
public final class EntityRegEvent {

    private EntityRegEvent() {
    }

    /** 注册全部实体渲染器（转发 {@code ClientModEvents}）。 */
    public static void registerEntityRender() {
        ClientModEvents.registerEntityRenderers();
    }

    /** 注册渲染层（转发 {@code ClientModEvents}）。 */
    public static void registerLayer() {
        ClientModEvents.registerLayers();
    }

    /** 注册 tooltip 组件（转发 {@code ClientModEvents}）。 */
    public static void registerTooltipComponents() {
        ClientModEvents.registerTooltipComponents();
    }

    /** 注册物品 tooltip 处理（转发 {@code ClientModEvents}）。 */
    public static void registerItemTooltips() {
        ClientModEvents.registerItemTooltips();
    }

    /** NeoForge 侧的生成放置/属性事件在 Fabric 侧由原版注册表直接承担，方法体为空 */
    public static void registerSpawnPlacements() {
    }

    public static void addEntityAttributes() {
    }
}
