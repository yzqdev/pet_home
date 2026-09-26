package com.github.yzqdev.pethome.client;

public final class EntityRegEvent {

    private EntityRegEvent() {
    }

    public static void registerEntityRender() {
        ClientModEvents.registerEntityRenderers();
    }

    public static void registerLayer() {
        ClientModEvents.registerLayers();
    }

    public static void registerTooltipComponents() {
        ClientModEvents.registerTooltipComponents();
    }

    public static void registerItemTooltips() {
        ClientModEvents.registerItemTooltips();
    }

    /** NeoForge 侧的生成放置/属性事件在 Fabric 侧由原版注册表直接承担，方法体为空 */
    public static void registerSpawnPlacements() {
    }

    public static void addEntityAttributes() {
    }
}
