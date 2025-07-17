package com.github.yzqdev.pet_home;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModConfigScreen extends  ConfigurationScreen.ConfigurationSectionScreen{
    public ModConfigScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
        super(parent, type, modConfig, title);
    }

    @Override
    protected   <T extends Enum<T>> Element createEnumValue(String key, ModConfigSpec.ValueSpec spec, Supplier<T> source, Consumer<T> target) {
        final Class<T> clazz = (Class<T>) spec.getClazz();
        assert clazz != null;

        final List<T> list = Arrays.stream(clazz.getEnumConstants()).filter(spec::test).toList();

        return new Element(getTranslationComponent(key), getTooltipComponent(key, null),
                new OptionInstance<>(getTranslationKey(key), getTooltip(key, null), (caption, displayvalue) ->   Component.translatable(PetHomeMod.MODID+".configuration." + key + "." + displayvalue.name().toLowerCase()),
                        new Custom<>(list), source.get(), newValue -> {
                    // regarding change detection: new value always is different (cycle button)
                    undoManager.add(v -> {
                        target.accept(v);
                        onChanged(key);
                    }, newValue, v -> {
                        target.accept(v);
                        onChanged(key);
                    }, source.get());
                }));
    }
}
