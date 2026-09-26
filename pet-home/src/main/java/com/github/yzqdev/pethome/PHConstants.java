package com.github.yzqdev.pethome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * not using origin citadel as it will conflict with pet_home
 *
 * @author yzqdev
 */
public class PHConstants {
    public static String entitySyncData = "pet_home_entity_data";
    public static String entityDataTagUpdate = "PetHomeTagUpdate";
    // 宠物罗盘：复用 PropertiesMessage 的 propertyID 分发（避免新增 payload）
    public static String petCompassData = "PetCompassData";
    public static String petCompassOpen = "PetCompassOpen";
    public static String petCompassAction = "PetCompassAction";
    public static final String MOD_ID = PetHomeMod.MODID;
    public static final String MOD_NAME = "pet_home";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
}
