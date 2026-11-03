package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/** 标签 Key 注册（标签内容在 datagen / JSON 中填充）：实体标签控制各功能对物种的适配范围。 */
public class PHTagRegistry {
    /** 宠物商店结构鱼缸可展示的水生生物。 */
    public static final TagKey<EntityType<?>> PETSTORE_FISHTANK = registerEntity("petstore_fishtank");
    /** 宠物商店 4 个笼位可展示的生物（按笼位大小分组）。 */
    public static final TagKey<EntityType<?>> PETSTORE_CAGE_0 = registerEntity("petstore_cage_0");
    public static final TagKey<EntityType<?>> PETSTORE_CAGE_1 = registerEntity("petstore_cage_1");
    public static final TagKey<EntityType<?>> PETSTORE_CAGE_2 = registerEntity("petstore_cage_2");
    public static final TagKey<EntityType<?>> PETSTORE_CAGE_3 = registerEntity("petstore_cage_3");
    /** 拒绝挂项圈标签的物种（交互时跳过附魔写入）。 */
    public static final TagKey<EntityType<?>> REFUSES_COLLAR_TAGS = registerEntity("refuses_collar_tags");
    /** 拒绝绑定宠物床的物种。 */
    public static final TagKey<EntityType<?>> REFUSES_PET_BEDS = registerEntity("refuses_pet_beds");
    /** 可用于驯服青蛙的物品。 */
    public static final TagKey<Item> TAME_FROGS_WITH = registerItem("tame_frogs_with");
    /** 矿脉嗅探附魔可探测的矿石方块。 */
    public static final TagKey<net.minecraft.world.level.block.Block> ORES = TagKey.create(Registries.BLOCK, new ResourceLocation(PetHomeMod.MODID, "ores"));

    private static TagKey<EntityType<?>> registerEntity(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(PetHomeMod.MODID, name));
    }

    private static TagKey<Item> registerItem(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(PetHomeMod.MODID, name));
    }
}