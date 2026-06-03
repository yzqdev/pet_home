package com.github.yzqdev.pethome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * not using origin citadel as it will conflict with pet_home
 *
 * @author yzqdev
 */
public class PHConstants {
    public static String entitySyncData = "PetHomeEntityData";
    public static String entityDataTagUpdate = "PetHomeTagUpdate";
    public static final String MOD_ID = PetHomeMod.MODID;
    public static final String MOD_NAME = "pet_home";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
}
