package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.NbtKeys;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 宠物契约（deed_of_ownership），双行为（本侧为 26.3 契约双行为化的语义蓝本）：
 *
 * <ul>
 *   <li>右键自家宠物 → 绑定该宠物；绑定态契约对同一宠物使用 → 转让所有权给持有人（转让在 {@code
 *       EntityInteractHandler#handleDeedOfOwnership}，NBT：{@code HAS_BOUND_ENTITY/BOUND_ENTITY/BOUND_ENTITY_NAME}）。
 *   <li>放生功能已拆分为独立物品自由之铃（{@link FreedomBellItem}），本契约不再承担放生。
 * </ul>
 */
public class DeedOfOwnershipItem extends Item {

    public DeedOfOwnershipItem() {
        super(new Item.Properties().stacksTo(1));
    }

    /** 绑定的宠物 UUID；未绑定返回 null（沿用本侧 NBT 存储与既有键名，保持存档兼容） */
    @Nullable
    public static UUID getBoundPetId(ItemStack stack) {
        if (stack.getTag() == null || !stack.getTag().hasUUID(NbtKeys.BOUND_ENTITY)) {
            return null;
        }
        return stack.getTag().getUUID(NbtKeys.BOUND_ENTITY);
    }

    public static boolean isBound(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean(NbtKeys.HAS_BOUND_ENTITY);
    }

    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || isBound(stack);
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flags) {
        list.add(Component.translatable(LangDefinition.ITEM_DEED_OF_OWNERSHIP_DESC).withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable(LangDefinition.ITEM_DEED_OF_OWNERSHIP_DESC2).withStyle(ChatFormatting.GRAY));
        if (isBound(stack) && stack.getTag() != null) {
            list.add(Component.translatable(LangDefinition.ITEM_DEED_OF_OWNERSHIP_BOUND, stack.getTag().getString(NbtKeys.BOUND_ENTITY_NAME)).withStyle(ChatFormatting.AQUA));
        }
    }
}
