package com.github.yzqdev.pethome.client.compass;

import com.github.yzqdev.pethome.network.PetCompassActionPacket;
import com.github.yzqdev.pethome.network.PetCompassForgetRequest;
import com.github.yzqdev.pethome.network.PetCompassOpenPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

/**
 * 罗盘的网络收发：S2C {@code pet_compass_data} 解析为 {@link CompassEntry} 列表；C2S 动作/删除/打开请求发包。
 * 解析只做数据转换，开屏/刷新决策（复用在场 GUI 还是新建）由 Screen 的入口方法处理。
 */
public final class CompassPacket {

    private CompassPacket() {
    }

    /** 解析结果：条目列表 + 服务端权威下发的按钮显隐配置 */
    public record Payload(List<CompassEntry> entries, boolean cfgToPet, boolean cfgToPlayer) {
    }

    /**
     * S2C pet_compass_data 解析；tag 为 null 返回 null（调用方放弃处理）。 Health 缺失（离线宠物）写 -1，
     * 由 {@link CompassTexts} 决定不显示详情行。
     */
    @Nullable
    public static Payload parse(@Nullable CompoundTag tag, String playerDim) {
        if (tag == null) {
            return null;
        }
        boolean cfgToPet = tag.getBooleanOr("CfgToPet", false);
        boolean cfgToPlayer = tag.getBooleanOr("CfgToPlayer", false);
        int count = tag.getIntOr("Count", 0);
        List<CompassEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            CompoundTag entry = tag.getCompoundOrEmpty("Pet" + i);
            entries.add(new CompassEntry(
                    parseUuid(entry.getStringOr("PetId", "")),
                    entry.getStringOr("Name", "?"),
                    entry.getStringOr("Type", "?"),
                    entry.getStringOr("Dim", "?"),
                    entry.getIntOr("X", 0),
                    entry.getIntOr("Y", 0),
                    entry.getIntOr("Z", 0),
                    entry.getIntOr("Dist", 0),
                    entry.getBooleanOr("Loaded", false),
                    entry.getBooleanOr("Alive", false),
                    entry.getBooleanOr("InBall", false),
                    entry.getStringOr("BallKind", "recall"),
                    entry.getBooleanOr("Stale", false),
                    entry.getStringOr("Dim", "").equals(playerDim),
                    entry.getLongOr("LastTime", 0L),
                    entry.contains("Health") ? entry.getFloatOr("Health", 0f) : -1f,
                    entry.getFloatOr("MaxHealth", 0f),
                    entry.getIntOr("Cmd", -1),
                    entry.getStringOr("Enchants", "")));
        }
        return new Payload(entries, cfgToPet, cfgToPlayer);
    }

    /** 非法 UUID 兜底随机（仅作列表键，不会与服务端档案匹配，操作时被服务端校验拒绝） */
    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return new UUID(RandomSource.create().nextLong(), RandomSource.create().nextLong());
        }
    }

    /** C2S 打开罗盘请求（PetCompassItem 右键触发） */
    public static void sendOpen() {
        ClientPlayNetworking.send(PetCompassOpenPacket.INSTANCE);
    }

    /** C2S 传送/召回动作（{@code action}: to_pet / to_player）；服务端有所有权校验，客户端隐藏按钮不是权限边界 */
    public static void sendAction(UUID petId, String action) {
        CompoundTag tag = new CompoundTag();
        tag.putString("PetId", petId.toString());
        tag.putString("Action", action);
        ClientPlayNetworking.send(new PetCompassActionPacket(tag));
    }

    /** C2S 删除档案（C2S petCompassForget）；服务端会再校验一次所有权 */
    public static void sendForget(UUID petId) {
        CompoundTag tag = new CompoundTag();
        tag.putString("PetId", petId.toString());
        ClientPlayNetworking.send(new PetCompassForgetRequest(tag));
    }
}
