package com.github.yzqdev.pethome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 全局常量：自定义网络子通道名（实体数据同步 / 标签更新 / 宠物罗盘系列）与共享 Logger。 */
public class PHConstants {
    public static String entitySyncData="PetHomeEntityData";
    public static String entityDataTagUpdate="PetHomeTagUpdate";
    // 宠物罗盘：复用 PropertiesMessage 的 propertyID 分发，不新增通道
    public static String petCompassData = "PetCompassData";
    public static String petCompassOpen = "PetCompassOpen";
    public static String petCompassAction = "PetCompassAction";
    public static final String MOD_ID = PetHomeMod.MODID;
    public static final String MOD_NAME = "pet_home";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
}
