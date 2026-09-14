package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModZhLangProvider extends LanguageProvider {
    public ModZhLangProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {
        add(LangDefinition.TOOLTIPS_SUBSTITUTE_FEATHER_DESC, "[可以让宠物移动到钓竿落点,比如把宠物移动到宠物床]");
        add(LangDefinition.TOOLTIPS_SUBSTITUTE_ROTTEN_APPLE_DESC, "[喂给马使其变为僵尸马]");
        add(LangDefinition.TOOLTIPS_SUBSTITUTE_SINISTER_CARROT_DESC, "[喂给兔子可将其转化为杀手兔,喂给僵尸马可将其转化为骷髅马]");
        add(LangDefinition.TOOLTIPS_WAYWARD_LANTERN_DESC, "[放置在身边,远离而被卸载的宠物会被传送回灯笼旁]");
        add(LangDefinition.TOOLTIPS_DRUM_DESC, "[右击切换指令模式,对32格内自己的宠物生效,可用红石信号触发]");
        add(LangDefinition.TOOLTIPS_SUBSTITUTE_COLLAR_DESC, "[右击宠物佩戴项圈]");
        add(LangDefinition.TOOLTIPS_SUBSTITUTE_PET_BED_DESC, "[将宠物放在宠物床上,宠物死亡会在第二天复活]");
        add(LangDefinition.ITEM_GROUP, "驯养革新 | 物品");
        add(LangDefinition.ITEM_COLLAR_TAG, "项圈标签");
        add(LangDefinition.ITEM_ROTTEN_APPLE, "烂苹果");
        add(LangDefinition.ITEM_SINISTER_CARROT, "阴恶胡萝卜");
        add(LangDefinition.ITEM_DEFLECTION_SHIELD, "偏转护盾模型");
        add(LangDefinition.ITEM_MAGNET, "磁铁模型");
        add(LangDefinition.ITEM_FEATHER_ON_A_STICK, "羽毛钓竿");
        add(LangDefinition.ITEM_DEED_OF_OWNERSHIP, "放生球");
        add(LangDefinition.ITEM_DEED_OF_OWNERSHIP_DESC, "右键宠物,可以放生它");
        add(LangDefinition.BLOCK_PET_BED_WHITE, "白色宠物床");
        add(LangDefinition.BLOCK_PET_BED_ORANGE, "橙色宠物床");
        add(LangDefinition.BLOCK_PET_BED_MAGENTA, "品红色宠物床");
        add(LangDefinition.BLOCK_PET_BED_LIGHT_BLUE, "淡蓝色宠物床");
        add(LangDefinition.BLOCK_PET_BED_YELLOW, "黄色宠物床");
        add(LangDefinition.BLOCK_PET_BED_LIME, "黄绿色宠物床");
        add(LangDefinition.BLOCK_PET_BED_PINK, "粉色宠物床");
        add(LangDefinition.BLOCK_PET_BED_GRAY, "灰色宠物床");
        add(LangDefinition.BLOCK_PET_BED_LIGHT_GRAY, "淡灰色宠物床");
        add(LangDefinition.BLOCK_PET_BED_CYAN, "青色宠物床");
        add(LangDefinition.BLOCK_PET_BED_PURPLE, "紫色宠物床");
        add(LangDefinition.BLOCK_PET_BED_BLUE, "蓝色宠物床");
        add(LangDefinition.BLOCK_PET_BED_BROWN, "棕色宠物床");
        add(LangDefinition.BLOCK_PET_BED_GREEN, "绿色宠物床");
        add(LangDefinition.BLOCK_PET_BED_RED, "红色宠物床");
        add(LangDefinition.BLOCK_PET_BED_BLACK, "黑色宠物床");
        add(LangDefinition.BLOCK_DRUM, "指挥鼓");
        add(LangDefinition.BLOCK_WAYWARD_LANTERN, "迷途灯笼");
        add(LangDefinition.MESSAGE_COMMAND_0, "%s 在游走");
        add(LangDefinition.MESSAGE_COMMAND_1, "%s 在停留 ");
        add(LangDefinition.MESSAGE_COMMAND_2, "%s 在跟随");
        add(LangDefinition.MESSAGE_DRUM_COMMAND_0, "命令 %s 游走");
        add(LangDefinition.MESSAGE_DRUM_COMMAND_1, "命令 %s 停留");
        add(LangDefinition.MESSAGE_DRUM_COMMAND_2, "命令 %s 跟随");
        add(LangDefinition.MESSAGE_RESPAWN, "%s 在它的床上重生了");
        add(LangDefinition.MESSAGE_REMOVE_RESPAWN, "移除了 %s 重生记录的床");
        add(LangDefinition.MESSAGE_GOODBYE, "%s 将不会重生，永别了……");
        add(LangDefinition.MESSAGE_ENCHANTMENTS, "魔咒：");
        add(LangDefinition.MESSAGE_SET_OWNER, "%s 现在是 %s 的所有者");
        add(LangDefinition.MESSAGE_WAYWARD_LANTERN_RETURN, "%s 发现了附近的迷途灯笼");
        add(LangDefinition.ENTITY_ANIMAL_TAMER, "驯兽师");
        add(LangDefinition.ENTITY_CHAIN_LIGHTNING, "闪电");
        add(LangDefinition.ENTITY_RECALL_BALL, "召回宝盒");
        add(LangDefinition.ENTITY_FEATHER, "羽毛");
        add(LangDefinition.ENTITY_FOLLOWING_JUKEBOX, "漂浮唱片机");
        add(LangDefinition.ENTITY_PSYCHIC_WALL, "心理墙");
        addEnchant(ModEnchantments.SonicBoom, "冲击波", "冲击波可以击退敌人,每隔一段时间触发");
        addEnchant(ModEnchantments.HEALTH_BOOST, "额外生命", "每升一级，宠物的生命值增加10点");
        addEnchant(ModEnchantments.FIREPROOF, "防火", "火和岩浆造成的伤害对宠物无效");
        addEnchant(ModEnchantments.IMMUNITY_FRAME, "免疫屏障", "每升一级会使宠物在受到伤害后增加1秒对伤害的免疫");
        addEnchant(ModEnchantments.DEFLECTION, "偏转", "使宠物受到一个无形的盾牌保护使其免受伤害");
        addEnchant(ModEnchantments.POISON_RESISTANCE, "抗毒", "使宠物对中毒效果免疫");
        addEnchant(ModEnchantments.CHAIN_LIGHTNING, "连锁闪电", "在宠物攻击敌对生物时会召唤一个闪电对怪物造成伤害，每升一级会增加敌对生物被闪电击中的次数");
        addEnchant(ModEnchantments.SPEEDSTER, "瞬速行者", "增加宠物的移动速度");
        addEnchant(ModEnchantments.FROST_FANG, "冰霜利齿", "宠物在攻击时会对敌对生物减速并对会有冰冻效果");
        addEnchant(ModEnchantments.MAGNETIC, "磁性", "使敌对生物吸引到宠物旁");
        addEnchant(ModEnchantments.LINKED_INVENTORY, "物品栏关联", "宠物可以将拾的取物品放进到主人的背包中");
        addEnchant(ModEnchantments.TOTAL_RECALL, "全面召回", "当宠物生命值低于 2 点时，宠物会进入召回球体并受到保护，直到主人释放");
        addEnchant(ModEnchantments.HEALTH_SIPHON, "生命虹吸", "对宠物造成的任何伤害都会转移给它的主人");
        addEnchant(ModEnchantments.BUBBLING, "冒泡", "会将宠物攻击的敌对生物困在向上漂浮的巨大气泡中");
        addEnchant(ModEnchantments.SHEPHERD, "群集", "野生动物会被宠物吸引并跟随，每升一级会增加宠物可以被追随的数量");
        addEnchant(ModEnchantments.AMPHIBIOUS, "两栖化", "宠物在陆地或水中不会被缺氧而死，在水中宠物不会漂浮到水面并且会增加移动速度");
        addEnchant(ModEnchantments.VAMPIRE, "类吸血鬼", "为宠物治疗它所造成的伤害，每升一级增加伤害与治疗的百分比");
        addEnchant(ModEnchantments.VOID_CLOUD, "虚空之云", "宠物受到虚空之云的保护，并不会掉入悬崖或落入虚空，而是虚空之云会将宠物托起并送回主人身旁");
        addEnchant(ModEnchantments.INFAMY_CURSE, "恶名诅咒", "宠物可以使附近的任何敌对生物对它产生敌意");
        addEnchant(ModEnchantments.SHADOW_HANDS, "暗影之手", "宠物使用黑暗魔法，用暗影之手攻击目标，每升一级会增加手的速度和手的数量");
        addEnchant(ModEnchantments.DEFUSAL, "爆炸无效", "宠物可以使爆炸对地形和其他动物的伤害无效，每升一级增加无效的范围");
        addEnchant(ModEnchantments.WARPING_BITE, "幻影移形", "宠物会随机地把目标敌对生物从身边传送走");
        addEnchant(ModEnchantments.ORE_SCENTING, "矿物之味", "宠物可以用嗅觉发现矿石并提醒主人，该效果的等级决定发现的矿石的距离、数量和使用次数");
        addEnchant(ModEnchantments.GLUTTONOUS, "贪吃", "宠物可以吃任何的食物并不受限制");
        addEnchant(ModEnchantments.PSYCHIC_WALL, "心理墙", "在战斗时宠物会召唤心理墙来以提供掩护，该效果的等级决定了墙体的大小和效果的时长。");
        addEnchant(ModEnchantments.INTIMIDATION, "恐吓", "宠物可以用可怕的外表吓跑敌对生物，该效果的等级决定了该效果的范围和使用次数");
        addEnchant(ModEnchantments.BLIGHT_CURSE, "枯萎诅咒", "宠物周围的植物将会枯萎和死亡");
        addEnchant(ModEnchantments.TETHERED_TELEPORT, "连接传送", "宠物将与主人一起传送，也可以跨越维度传送");
        addEnchant(ModEnchantments.IMMATURITY_CURSE, "变小诅咒", "宠物出现时为宠物的小时候而且宠物的攻击伤害会降低");
        addEnchant(ModEnchantments.BLAZING_PROTECTION, "火焰保护", "宠物每升一级会受到2个燃烧的棍的保护，燃烧的棍会击退并时攻击者燃烧");
        addEnchant(ModEnchantments.HEALING_AURA, "治疗之息", "宠物会在偶然间治疗主人或在主人周围的其他宠物，该效果的等级决定了治疗的效果");
        addEnchant(ModEnchantments.XP_Transfer, "经验转移", "宠物带有此附魔的项圈能收集周围的经验球,并转移到主人身上");
        addEnchant(ModEnchantments.REJUVENATION, "恢复生命", "在宠物受伤时会吸收周围的经验球来治疗自己");
        addEnchant(ModEnchantments.SHARE, "平摊", "宠物收到攻击时,会平摊伤害给周围的敌对生物");
        addEnchant(ModEnchantments.NIGHT_VISION, "夜视", "当宠物在身边时,获得夜视效果");
        addEnchant(ModEnchantments.INSIGHT, "洞察", "在暗处宠物会让周围的敌对生物高亮");
        addEnchant(ModEnchantments.CHAOS, "混乱之脑", "宠物被攻击时，使攻击者获得混乱buff，攻击位于半径10格内的其他怪物");
        addEnchant(ModEnchantments.PARALYSIS, "麻痹", "宠物被攻击时，使攻击者无法移动、跳跃或攻击几秒钟");
        addEnchant(ModEnchantments.TOUGH, "稳固", "增加宠物的防御和抗击退");
        addEnchant(ModEnchantments.VIOLENT, "暴力", "宠物攻击的生物会获得一些debuff");
        add(LangDefinition.SOUND_SUBTITLE_COLLAR_TAG, "已装备项圈标签");
        add(LangDefinition.SOUND_SUBTITLE_MAGNET_LOOP, "磁铁：呲呲");
        add(LangDefinition.SOUND_SUBTITLE_CHAIN_LIGHTNING, "闪电：电击");
        add(LangDefinition.SOUND_SUBTITLE_GIANT_BUBBLE_INFLATE, "大泡泡：膨胀");
        add(LangDefinition.SOUND_SUBTITLE_GIANT_BUBBLE_POP, "大泡泡：爆炸");
        add(LangDefinition.SOUND_SUBTITLE_PET_BED_USES, "调整过的宠物床");
        add(LangDefinition.SOUND_SUBTITLE_DRUM, "指挥鼓鼓点");
        add(LangDefinition.SOUND_SUBTITLE_PSYCHIC_WALL, "心理墙的嗡嗡声");
        add(LangDefinition.SOUND_SUBTITLE_PSYCHIC_WALL_DEFLECT, "心理墙的偏移");
        add(LangDefinition.SOUND_SUBTITLE_BLAZING_PROTECTION, "燃烧的棍消失了");
        add(LangDefinition.JADE_COLLAR_TAG, "项圈描述");
        add(LangDefinition.NOTIF_FRIENDLY_FIRE_PROTECTED, "这只%s受保护。潜行状态可绕过保护。");
        // new gen
        add(LangDefinition.animal_tamer_villager_conf, "驯兽师");
        add(LangDefinition.protectChildren_conf_tooltip, "是否保护幼年生物");
        add(LangDefinition.displayHitWarning_conf_tooltip, "是否显示保护提示");
        add(LangDefinition.rotten_apple_conf, "苹果消失的时候变成腐烂苹果");
        add(LangDefinition.rotten_apple_conf_tooltip, "苹果消失的时候变成腐烂苹果");
        add(LangDefinition.sinister_carrot_loot_chance_conf_tooltip, "阴恶胡萝卜在宝箱中的概率");
        add(LangDefinition.sinister_carrot_loot_chance_conf, "阴恶胡萝卜在宝箱中的概率");
        add(LangDefinition.petstore_village_weight_conf_tooltip, "驯兽师屋子生成的权重,修改后需要推出世界再重新进入");
        add(LangDefinition.petstore_village_weight_conf, "驯兽师屋子生成的权重");
        add(LangDefinition.protectChildren_conf, "保护幼年生物");
        add(LangDefinition.respectTeamRules_conf, "跟队伍规则一致");
        add(LangDefinition.respectTeamRules_conf_tooltip,
                "跟队伍规则一致,队伍开启友军伤害则有友军伤害,没开启友军伤害就没有友军伤害");
        add(LangDefinition.protectPetsFromOwner_conf_tooltip, "主人无法伤害宠物");
        add(LangDefinition.protectPetsFromOwner_conf, "主人无法伤害宠物");
        add(LangDefinition.displayHitWarning_conf, "显示保护提示");
        add(LangDefinition.protectTeamMembers_conf_tooltip, "同队生物之间无伤害");
        add(LangDefinition.protectTeamMembers_conf, "同队生物之间无伤害");
        add(LangDefinition.protectPetsFromPets_conf_tooltip, "宠物之间无伤害");
        add(LangDefinition.protectPetsFromPets_conf, "宠物之间无伤害");
        add(LangDefinition.reflectDamage_conf, "反弹伤害");
        add(LangDefinition.reflectDamage_conf_tooltip, "反弹伤害(主人攻击生物会受伤)");
        add(LangDefinition.ore_scenting_loot_chance_conf, "矿物之味附魔书概率");
        add(LangDefinition.ore_scenting_loot_chance_conf_tooltip, "矿物之味附魔书在(废弃矿井)宝箱中的概率");
        add(LangDefinition.bubbling_loot_chance_conf, "冒泡附魔书概率");
        add(LangDefinition.bubbling_loot_chance_conf_tooltip, "冒泡附魔书在(埋藏的宝藏)宝箱中的概率");
        add(LangDefinition.blazing_protection_loot_chance_conf_tooltip, "抗火附魔书在(下界要塞)宝箱中的概率");
        add(LangDefinition.blazing_protection_loot_chance_conf, "抗火附魔书概率");
        add(LangDefinition.vampirism_loot_chance_conf, "吸血鬼附魔书概率");
        add(LangDefinition.vampirism_loot_chance_conf_tooltip, "吸血鬼附魔书在(林地府邸)宝箱中的概率");
        add(LangDefinition.other_should_protect_entity_conf_tooltip, "无敌的生物(免疫所有伤害)");
        add(LangDefinition.other_should_protect_entity_conf, "无敌的生物");

        add(LangDefinition.no_protection_entity_conf_tooltip, "不应该保护的生物,如猪灵");
        add(LangDefinition.no_protection_entity_conf, "不应该保护的生物");
        add(LangDefinition.player_cant_hurt_entity_conf_tooltip, "玩家不能攻击的生物,如僵尸猪灵");
        add(LangDefinition.player_cant_hurt_entity_conf, "玩家不能攻击的生物");
        add(LangDefinition.can_hurt_pet_item_conf_tooltip, "可以用来攻击宠物的物品,不需要按shift进行攻击");
        add(LangDefinition.can_hurt_pet_item_conf, "可以用来攻击宠物的物品");
        add(LangDefinition.can_hurt_all_conf_tooltip,
                "可以用来绕过模组的所有保护对生物造成的物品,不需要按shift进行攻击");
        add(LangDefinition.can_hurt_all_conf, "可以用来绕过所有保护攻击的物品");


        add(LangDefinition.share_loot_chance_conf_tooltip, "平摊附魔书在(末地城)宝箱中的概率");
        add(LangDefinition.share_loot_chance_conf, "平摊附魔书概率");
        add(LangDefinition.paralysis_loot_chance_conf_tooltip,
                "麻痹附魔书在(废弃矿井宝箱,沙漠神殿宝箱,钓鱼战利品)中的概率");
        add(LangDefinition.paralysis_loot_chance_conf, "麻痹附魔书概率");
        add(LangDefinition.sonic_boom_loot_chance_conf_tooltip, "冲击波附魔书在(古城)宝箱中的概率");
        add(LangDefinition.sonic_boom_loot_chance_conf, "冲击波附魔书概率");




        add(LangDefinition.has_pet_bed_at_pos, "已绑定宠物床( %s )");
        add(PHItemRegistry.NET_LAUNCHER_ITEM.get(), "生物捕捉发射器");
        add(PHItemRegistry.NET_ITEM.get(), "生物球");
        add(PHItemRegistry.NET_HAS_ITEM.get(), "生物球");
        add(LangDefinition.capturing_text, "捕捉");
        add(LangDefinition.release_text, "释放");
        add(LangDefinition.health_text, "生命值");
        add(LangDefinition.no_net_entity_text, "没有可以释放的生物球");
        add(LangDefinition.net_launcher_tip, "[按住shift然后右击,可以切换释放和捕捉状态]");
        add(LangDefinition.net_launcher_default_only_tamable, "默认只能捕捉可驯服的生物,可以在配置文件修改");
        add(LangDefinition.mobcatcherOnlyTamableAnimal_conf, "生物球只能捕捉可驯服的生物");
        add(LangDefinition.mobcatcherOnlyTamableAnimal_conf_tooltip, "设置为否可以捕捉所有生物");
        add(LangDefinition.mobcatcherBlacklist_conf, "生物球可捕捉黑名单");
        add(LangDefinition.mobcatcherBlacklist_conf_tooltip, "生物球不能捕捉的生物");


        add(LangDefinition.tough_loot_chance_conf_tooltip, "稳固附魔书在废弃矿井宝箱中的概率");
        add(LangDefinition.tough_loot_chance_conf, "稳固附魔书概率");
        // 以下自 1.20 移植的开关翻译
        add(LangDefinition.config("tameable_axolotls"), "驯服美西螈");
        add(LangDefinition.config("tameable_fox"), "驯服狐狸");
        add(LangDefinition.config("tameable_frog"), "驯服青蛙");
        add(LangDefinition.config("tameable_horse"), "驯服马");
        add(LangDefinition.config("tameable_rabbit"), "驯服兔子");
        add(LangDefinition.config("trinary_command_system"), "三态指令系统");
        add(LangDefinition.config("pet_bed_respawns"), "宠物床重生");
        add(LangDefinition.config("rabbits_scare_ravagers"), "兔子吓掠夺兽");
        add(LangDefinition.tameable_axolotls_conf_tooltip, "是否允许完全驯服美西螈（需用热带鱼驯服）");
        add(LangDefinition.tameable_fox_conf_tooltip, "是否允许完全驯服狐狸（需用甜浆果驯服）");
        add(LangDefinition.tameable_frog_conf_tooltip, "是否允许完全驯服青蛙（需用蜘蛛眼驯服）");
        add(LangDefinition.tameable_horse_conf_tooltip, "是否允许完全驯服马");
        add(LangDefinition.tameable_rabbit_conf_tooltip, "是否允许完全驯服兔子（需用胡萝卜驯服）");
        add(LangDefinition.trinary_command_system_conf_tooltip, "是否启用三态指令系统（漫游/跟随/停留）");
        add(LangDefinition.pet_bed_respawns_conf_tooltip, "宠物死亡后次日清晨是否会在宠物床重生");
        add(LangDefinition.rabbits_scare_ravagers_conf_tooltip, "兔子是否像以前一样吓掠夺兽");
        add(LangDefinition.mobcatcherBlacklist_conf_button, "编辑...");
        add(LangDefinition.can_hurt_pet_item_conf_button, "编辑...");
        add(LangDefinition.can_hurt_all_conf_button, "编辑...");
        add(LangDefinition.no_protection_entity_conf_button, "编辑...");
        add(LangDefinition.other_should_protect_entity_conf_button, "编辑...");
        add(LangDefinition.player_cant_hurt_entity_conf_button, "编辑...");
        add(LangDefinition.configuration_title, "Pet Home 设置");
        add(LangDefinition.configuration_section_common, "Pet Home 通用配置");
        add(LangDefinition.configuration_section_common_title, "Pet Home 通用配置");
        add(LangDefinition.network_failed, "网络错误");
    }


    private void addEnchant(ResourceKey<Enchantment> enchantmentResourceKey, String name, String desc) {
        add("enchantment." + PetHomeMod.MODID + "." + enchantmentResourceKey.location()
                .getPath(), name);
        add("enchantment." + PetHomeMod.MODID + "." + enchantmentResourceKey.location()
                .getPath() + ".desc", desc);
    }
}
