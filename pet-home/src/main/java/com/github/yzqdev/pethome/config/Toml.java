package com.github.yzqdev.pethome.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 极简 TOML 字面量辅助工具：只覆盖本模组配置文件用到的语法子集
 * （布尔、数字、字符串、字符串数组），不引入任何第三方 TOML 库。
 */
final class Toml {

    private Toml() {
    }

    /** 去掉行内注释（# 之后的内容），字符串内的 # 不受影响 */
    static String stripInlineComment(String line) {
        boolean inSingle = false;
        boolean inDouble = false;
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\' && inDouble) {
                escaped = true;
                continue;
            }
            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
            } else if (c == '"' && !inSingle) {
                inDouble = !inDouble;
            } else if (c == '#' && !inSingle && !inDouble) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    /** 去掉字符串两端的引号并反转义 */
    static String unquote(String raw) {
        String text = raw.trim();
        if (text.length() >= 2) {
            char first = text.charAt(0);
            char last = text.charAt(text.length() - 1);
            if (first == '"' && last == '"') {
                return unescape(text.substring(1, text.length() - 1));
            }
            if (first == '\'' && last == '\'') {
                return text.substring(1, text.length() - 1);
            }
        }
        return text;
    }

    private static String unescape(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        boolean escaped = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                switch (c) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    default -> sb.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                sb.append(c);
            }
        }
        if (escaped) {
            sb.append('\\');
        }
        return sb.toString();
    }

    static String quote(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * 解析字符串数组字面量。
     *
     * @return 解析结果；格式非法时返回 null
     */
    static List<String> parseStringArray(String raw) {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        if (!text.startsWith("[")) {
            // 容忍用户写成单个字符串
            return List.of(unquote(text));
        }
        if (!text.endsWith("]")) {
            return null;
        }
        List<String> result = new ArrayList<>();
        String body = text.substring(1, text.length() - 1);
        StringBuilder current = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;
        boolean escaped = false;
        boolean hasToken = false;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (escaped) {
                current.append(c);
                escaped = false;
                hasToken = true;
                continue;
            }
            if (c == '\\' && inDouble) {
                current.append(c);
                escaped = true;
                continue;
            }
            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
                current.append(c);
                hasToken = true;
                continue;
            }
            if (c == '"' && !inSingle) {
                inDouble = !inDouble;
                current.append(c);
                hasToken = true;
                continue;
            }
            if (c == ',' && !inSingle && !inDouble) {
                if (hasToken) {
                    result.add(unquote(current.toString()));
                    current.setLength(0);
                    hasToken = false;
                }
                continue;
            }
            current.append(c);
            if (!Character.isWhitespace(c)) {
                hasToken = true;
            }
        }
        if (inSingle || inDouble) {
            return null;
        }
        if (hasToken) {
            result.add(unquote(current.toString()));
        }
        return result;
    }

    static String writeStringArray(List<String> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(quote(values.get(i)));
        }
        return sb.append(']').toString();
    }
}
