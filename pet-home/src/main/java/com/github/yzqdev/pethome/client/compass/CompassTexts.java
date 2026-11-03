package com.github.yzqdev.pethome.client.compass;

import com.github.yzqdev.pethome.datagen.LangDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * 罗盘 GUI 的文本与文案规则：语言键拼装、维度名映射、行内状态文案（按失效/收存容器/加载/维度选择）、实时详情行（血量/指令/附魔）。 全部为无状态静态方法；颜色规则集中在此，渲染器只管画。
 */
public final class CompassTexts {

    private CompassTexts() {}

    /** {@code pet_compass.<key>} 语言键组件 */
    public static MutableComponent msg(String key, Object... args) {
        return Component.translatable(LangDefinition.message("pet_compass." + key), args);
    }

    /**
     * 维度显示（参考 skirk HUD 同款规则）：翻译键 {@code dimension.<ns>.<path>} 存在时显示「名字 id」（空格分隔，如「主世界
     * minecraft:overworld」）， 不存在（模组自定义维度无翻译）时只显示 id，避免残留多余空格。
     */
    public static MutableComponent dimName(String dim) {
        int sep = dim.indexOf(':');
        String ns = sep >= 0 ? dim.substring(0, sep) : dim;
        String path = sep >= 0 ? dim.substring(sep + 1) : dim;
        MutableComponent id = Component.literal(dim);
        String key = "dimension." + ns + "." + path;
        if (!Language.getInstance().has(key)) {
            return id;
        }
        return Component.translatable(key).append(" ").append(id);
    }

    /** 行内状态文案：失效（红）优先 → 收存容器中（宝盒/生物球，浅紫）→ 已加载（同维绿 / 跨维黄）→ 未加载（黄）。 判定依据是包内字段，不做任何查询。 */
    public static Component statusOf(CompassEntry entry) {
        if (entry.stale()) {
            return msg("status_stale").withStyle(ChatFormatting.RED);
        }
        if (entry.inBall()) {
            return ("net".equals(entry.ballKind()) ? msg("status_in_net") : msg("status_in_ball"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE);
        }
        if (entry.loaded()) {
            return (entry.sameDim()
                    ? msg("status_loaded").withStyle(ChatFormatting.GREEN)
                    : msg("status_wrong_dim").withStyle(ChatFormatting.YELLOW))
                    .copy();
        }
        return msg("status_not_loaded").withStyle(ChatFormatting.YELLOW);
    }

    /** 实时详情行是否可见（仅在线宠物有包内数据，且三段拼出来非空才占行）；行高计算与渲染共用。 */
    public static boolean hasDetailLine(CompassEntry entry) {
        return entry.health() >= 0 && buildDetailLine(entry) != null;
    }

    /**
     * 实时详情行：血量（整数比 + 颜色随比例）＋ 指令态（0 游走 / 1 停留 / 2 跟随，-1 非三态宠物不显示）＋ 附魔摘要（「附魔名X 等级」逗号分隔， 语言键与
     * Jade/信息面板共用 {@code enchantment.<ns>.<path>}）。 三段全空返回 {@code null}（不占行）。
     */
    public static Component buildDetailLine(CompassEntry entry) {
        MutableComponent line = Component.empty();
        // 血量：16.0/20.0，低于 1/3 红色、低于 2/3 黄色、其余绿色
        String hpText =
                msg("health", trimFloat(entry.health()), trimFloat(entry.maxHealth())).getString();
        float ratio = entry.maxHealth() > 0 ? entry.health() / entry.maxHealth() : 0f;
        ChatFormatting hpColor =
                ratio <= 0.34f
                        ? ChatFormatting.RED
                        : ratio <= 0.67f ? ChatFormatting.YELLOW : ChatFormatting.GREEN;
        line.append(Component.literal(hpText).withStyle(hpColor));
        if (entry.cmd() >= 0 && entry.cmd() <= 2) {
            line.append(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY));
            line.append(msg("cmd_" + entry.cmd()).withStyle(ChatFormatting.AQUA));
        }
        if (!entry.enchants().isEmpty()) {
            MutableComponent ench = Component.empty();
            for (String part : entry.enchants().split(";")) {
                String[] seg = part.split(":");
                if (seg.length != 2) {
                    continue;
                }
                if (!ench.getString().isEmpty()) {
                    ench = ench.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
                }
                boolean isCurse = seg[0].contains("curse");
                ench =
                        ench.append(
                                Component.translatable("enchantment.pet_home." + seg[0])
                                        .append(Component.translatable("enchantment.level." + seg[1]))
                                        .withStyle(isCurse ? ChatFormatting.RED : ChatFormatting.DARK_AQUA));
            }
            if (!ench.getString().isEmpty()) {
                line.append(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY));
                line.append(ench);
            }
        }
        return line.getString().isEmpty() ? null : line;
    }

    /** 血量整数化显示：小数部分为 0 去掉小数点，否则保留 1 位。 */
    private static String trimFloat(float value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.format("%.1f", value);
    }
}
