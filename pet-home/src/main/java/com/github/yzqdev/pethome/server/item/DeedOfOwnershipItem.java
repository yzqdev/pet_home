package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.PHDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * 宠物契约（deed_of_ownership），双行为（对齐 26.3 侧语义）：
 *
 * <ul>
 *   <li>右键自家宠物 → 绑定该宠物；绑定态契约对同一宠物使用 → 转让所有权给持有人（转让在 {@code
 *       PlayerInteractEntityHandler#handleDeedOfOwnership}）。
 *   <li>放生功能已拆分为独立物品自由之铃（{@link FreedomBellItem}），本契约不再承担放生。
 * </ul>
 *
 * 绑定态存 {@link PHDataComponents#BOUND_PET}（NBT：PetUUID / PetName，26.1 CompoundTag 无 putUUID/getUUID，UUID 以字符串存）。
 */
public class DeedOfOwnershipItem extends Item {

    public static final String BOUND_UUID_KEY = "PetUUID";
    public static final String BOUND_NAME_KEY = "PetName";

    public DeedOfOwnershipItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    /** 绑定的宠物 UUID；未绑定或格式非法返回 null（26.1 CompoundTag 无 putUUID/getUUID，UUID 以字符串存） */
    public static UUID getBoundPetId(ItemStack stack) {
        CompoundTag tag = stack.get(PHDataComponents.BOUND_PET);
        if (tag == null) {
            return null;
        }
        try {
            return UUID.fromString(tag.getStringOr(BOUND_UUID_KEY, ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 绑定宠物到契约（覆盖旧绑定） */
    public static void bind(ItemStack stack, UUID petId, String petName) {
        CompoundTag tag = new CompoundTag();
        tag.putString(BOUND_UUID_KEY, petId.toString());
        tag.putString(BOUND_NAME_KEY, petName);
        stack.set(PHDataComponents.BOUND_PET, tag);
    }

    public static boolean isBound(ItemStack stack) {
        return getBoundPetId(stack) != null;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || isBound(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltip, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable(LangDefinition.ITEM_DEED_OF_OWNERSHIP_DESC).withStyle(ChatFormatting.GREEN));
        builder.accept(Component.translatable(LangDefinition.ITEM_DEED_OF_OWNERSHIP_DESC2).withStyle(ChatFormatting.GRAY));
        UUID bound = getBoundPetId(stack);
        if (bound != null) {
            CompoundTag tag = stack.get(PHDataComponents.BOUND_PET);
            builder.accept(Component.translatable(LangDefinition.ITEM_DEED_OF_OWNERSHIP_BOUND, tag != null ? tag.getStringOr(BOUND_NAME_KEY, "") : "").withStyle(ChatFormatting.AQUA));
        }
    }
}
