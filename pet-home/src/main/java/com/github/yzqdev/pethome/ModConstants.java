package com.github.yzqdev.pethome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModConstants {
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
