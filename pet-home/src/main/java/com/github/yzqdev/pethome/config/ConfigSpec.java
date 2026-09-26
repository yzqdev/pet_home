package com.github.yzqdev.pethome.config;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置规格：汇总所有 {@link ConfigValue}，负责 {@code config/pet_home.toml} 的加载与保存。
 *
 * <p>这是 Fabric 端自研的配置系统（替代原 Forge 版的 ForgeConfigSpec + ForgeConfigAPIPort）：
 * <ul>
 *   <li>文件位置：{@code <游戏目录>/config/pet_home.toml}（与 Forge 版文件名保持一致）</li>
 *   <li>加载：{@link #load} 读取文件并逐项写入值；解析失败/未知键记录告警并保留默认值</li>
 *   <li>保存：{@link #save} 仅在值被修改（dirty）时落盘；写入采用「临时文件 + 原子替换」，避免写坏配置</li>
 *   <li>首次启动或文件缺少新键时，自动补全为带注释的规范文件</li>
 * </ul>
 * </p>
 */
public final class ConfigSpec {

    private static final Logger LOGGER = LogManager.getLogger("pet_home");

    /** 配置文件在 config 目录下的文件名 */
    public static final String FILE_NAME = "pet_home.toml";

    /** 记录“节标题”在输出中的位置，保证 toml 段落顺序与定义顺序一致 */
    private record Section(String path) {
    }

    private final Map<String, ConfigValue<?>> valuesByPath = new LinkedHashMap<>();
    private final List<Object> layout = new ArrayList<>();
    private boolean dirty;
    private boolean loading;
    private Path file;

    void markDirty() {
        if (!loading) {
            dirty = true;
        }
    }

    void addSection(String path) {
        if (!path.isEmpty()) {
            layout.add(new Section(path));
        }
    }

    <T extends ConfigValue<?>> T add(T value) {
        value.attach(this);
        valuesByPath.put(value.getPath(), value);
        layout.add(value);
        return value;
    }

    /** 已登记的配置项（不可变视图） */
    public Collection<ConfigValue<?>> getValues() {
        return java.util.Collections.unmodifiableCollection(valuesByPath.values());
    }

    /** 按路径查询配置项 */
    public ConfigValue<?> getValue(String path) {
        return valuesByPath.get(path);
    }

    public boolean isDirty() {
        return dirty;
    }

    /** 当前使用的配置文件路径（未调用过 {@link #load} 时为默认路径） */
    public Path getFile() {
        if (file == null) {
            file = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        }
        return file;
    }

    /** 创建一个注册器（用法与 Forge 的 ForgeConfigSpec.Builder 相近） */
    public ConfigBuilder builder() {
        return new ConfigBuilder(this);
    }

    /**
     * 从默认配置文件加载；文件不存在时写出默认配置。
     */
    public void load() {
        load(getFile());
    }

    /**
     * 从指定文件加载；加载完成后若磁盘内容与规范格式不一致（首次生成 / 新增了配置项），
     * 会重写为带注释的规范文件。
     */
    public void load(Path path) {
        this.file = path;
        String existing = null;
        loading = true;
        try {
            if (Files.exists(path)) {
                existing = Files.readString(path, StandardCharsets.UTF_8);
                parse(existing);
            } else {
                LOGGER.info("[pet_home] 未找到配置文件 {}，将生成默认配置", path);
            }
        } catch (Exception e) {
            LOGGER.error("[pet_home] 读取配置文件 {} 失败，本次使用默认值", path, e);
        } finally {
            loading = false;
        }
        dirty = false;
        String canonical = toToml();
        if (!canonical.equals(existing)) {
            writeAtomically(canonical);
        }
    }

    /** 有改动时落盘 */
    public void save() {
        if (!dirty) {
            return;
        }
        if (writeAtomically(toToml())) {
            dirty = false;
        }
    }

    /** 强制落盘（忽略 dirty 标记） */
    public void saveNow() {
        if (writeAtomically(toToml())) {
            dirty = false;
        }
    }

    private void parse(String text) {
        String section = "";
        int lineNumber = 0;
        for (String rawLine : text.split("\r?\n")) {
            lineNumber++;
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                continue;
            }
            if (line.startsWith("[")) {
                int end = line.lastIndexOf(']');
                if (end > 0) {
                    section = line.substring(1, end).trim();
                }
                continue;
            }
            int equals = line.indexOf('=');
            if (equals < 0) {
                LOGGER.warn("[pet_home] 配置文件第 {} 行无法解析，已忽略：{}", lineNumber, line);
                continue;
            }
            String key = line.substring(0, equals).trim();
            String rawValue = Toml.stripInlineComment(line.substring(equals + 1)).trim();
            String fullPath = section.isEmpty() ? key : section + "." + key;
            ConfigValue<?> value = valuesByPath.get(fullPath);
            if (value == null) {
                LOGGER.warn("[pet_home] 配置文件第 {} 行存在未知配置项 '{}'，已忽略", lineNumber, fullPath);
                continue;
            }
            if (!value.read(rawValue)) {
                LOGGER.warn("[pet_home] 配置项 '{}' 的值 '{}' 非法，已回退默认值 {}", fullPath, rawValue, value.write());
            }
        }
    }

    private String toToml() {
        StringBuilder sb = new StringBuilder();
        sb.append("# pet_home 配置文件（驯养革新）\n");
        sb.append("# 本文件由模组自动维护；也可用游戏内设置界面修改（客户端命令 /pet_home_config）。\n");
        sb.append("# 布尔值 true/false，数值直接书写，列表形如 [\"a\", \"b\"]。\n");
        for (Object element : layout) {
            if (element instanceof Section section) {
                sb.append('\n').append('[').append(section.path()).append("]\n");
            } else if (element instanceof ConfigValue<?> value) {
                String comment = value.getComment();
                if (comment != null && !comment.isEmpty()) {
                    for (String commentLine : comment.split("\n")) {
                        sb.append("# ").append(commentLine).append('\n');
                    }
                }
                sb.append(simpleName(value.getPath())).append(" = ").append(value.write()).append('\n');
            }
        }
        return sb.toString();
    }

    private static String simpleName(String path) {
        int dot = path.lastIndexOf('.');
        return dot < 0 ? path : path.substring(dot + 1);
    }

    private boolean writeAtomically(String content) {
        Path target = getFile();
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temp = target.resolveSibling(target.getFileName() + ".tmp");
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailure) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("[pet_home] 写入配置文件 {} 失败", target, e);
            return false;
        }
    }
}
