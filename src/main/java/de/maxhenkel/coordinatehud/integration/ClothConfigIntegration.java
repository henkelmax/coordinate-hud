package de.maxhenkel.coordinatehud.integration;

import de.maxhenkel.configbuilder.ConfigBuilderImpl;
import de.maxhenkel.configbuilder.ConfigEntry;
import de.maxhenkel.coordinatehud.CoordinateHUD;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfigIntegration {
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder
                .create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("cloth_config.coordinatehud.settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("cloth_config.coordinatehud.category.general"));

        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.coordinatehud.waypoint_distance"),
                Component.translatable("cloth_config.coordinatehud.waypoint_distance.description"),
                CoordinateHUD.CLIENT_CONFIG.waypointDistance
        ));

        return builder.build();
    }

    private static <T> AbstractConfigListEntry<T> fromConfigEntry(ConfigEntryBuilder entryBuilder, Component name, Component description, ConfigEntry<T> entry) {
        if (entry instanceof ConfigBuilderImpl.DoubleConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startDoubleField(name, e.get())
                    .setTooltip(description)
                    .setMin(e.getMin())
                    .setMax(e.getMax())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        } else if (entry instanceof ConfigBuilderImpl.IntegerConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startIntField(name, e.get())
                    .setTooltip(description)
                    .setMin(e.getMin())
                    .setMax(e.getMax())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        } else if (entry instanceof ConfigBuilderImpl.BooleanConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startBooleanToggle(name, e.get())
                    .setTooltip(description)
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        } else if (entry instanceof ConfigBuilderImpl.StringConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startStrField(name, e.get())
                    .setTooltip(description)
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        }

        return null;
    }
}