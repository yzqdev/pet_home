package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.server.entity.FeatherEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * 羽毛钓竿（160 耐久）：右键抛出 {@link FeatherEntity}（落地后把最近的宠物拉到身边），
 * 再次右键按钓鱼钩规则收回并按损耗掉耐久。
 */
public class FeatherOnAStickItem extends Item {

    public FeatherOnAStickItem() {
        super(new Item.Properties().durability(160));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltips.pet_home.substitute_feather.desc").withStyle(ChatFormatting.GREEN));
    }

    /** 右键：已有抛出的羽毛则收回（按 retrieve 结果掉耐久），否则抛出新的 FeatherEntity（服务端生成实体）。 */
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (player.fishing != null && player.fishing.isAlive()) {
            if (!level.isClientSide()) {
                int i = player.fishing.retrieve(itemstack);
                itemstack.hurtAndBreak(i, player, (stac) -> {
                    stac.broadcastBreakEvent(hand);
                });
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            FeatherEntity featherEntity = new FeatherEntity(player, level);
            featherEntity.setOwner(player);
            if (!level.isClientSide()) {
                level.addFreshEntity(featherEntity);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            player.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
