package com.github.yzqdev.pet_home.compat.jade;


import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.datagen.LangDefinition;
import com.github.yzqdev.pet_home.util.TameableUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
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
            var hasPetbed = TameableUtils.getPetBedPos(livingEntity);
            if (hasPetbed != null) {
                iTooltip.add(Component.translatable(LangDefinition.ConstantMsg.has_pet_bed_at_pos, hasPetbed.toShortString()).withStyle(ChatFormatting.RED));
            }
            if (enchants.size() > 1) {
                for (Component enchant : enchants) {
                    iTooltip.add(enchant);
                }
            }

        }

    }


    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "collar_tag");
    }
}