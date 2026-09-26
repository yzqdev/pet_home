package com.github.yzqdev.pethome.compat.jade;


import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.addon.vanilla.WaxedProvider;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PetHomeComponentProvider implements IEntityComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {

        if (entityAccessor.getEntity() instanceof LivingEntity livingEntity) {
            var enchants = TameableUtils.getEnchantDescriptions(livingEntity);

            if (livingEntity instanceof IComandableMob cmd && TameableUtils.isTamed(livingEntity)&& PetHomeConfig.trinaryCommandSystem) {
                iTooltip.add(Component.translatable("message.pet_home.command_" + cmd.getCommand(),livingEntity.getDisplayName() ));
            }
            var hasPetbed = TameableUtils.getPetBedPos(livingEntity);
            if (hasPetbed != null) {
                iTooltip.add(Component.translatable(LangDefinition.has_pet_bed_at_pos, hasPetbed.toShortString()).withStyle(ChatFormatting.RED));
            }
            if (enchants.size() > 1) {
                for (Component enchant : enchants) {
                    iTooltip.add(enchant);
                }
            }

        }

    }


    @Override
    public Identifier getUid() {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "collar_tag");
    }
}