package de.maxhenkel.coordinatehud;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

public class DimensionUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    public static final ResourceKey<Level> OVERWORLD = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld"));
    public static final ResourceKey<Level> THE_NETHER = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("the_nether"));
    public static final ResourceKey<Level> THE_END = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("the_end"));

    public static Collection<ResourceKey<Level>> getAllDimensions() {
        Set<ResourceKey<Level>> dimensions = new HashSet<>(CoordinateHUD.WAYPOINT_STORE.getWaypoints().stream().map(Waypoint::getDimension).toList());
        dimensions.add(OVERWORLD);
        dimensions.add(THE_NETHER);
        dimensions.add(THE_END);
        return dimensions;
    }

    public static MutableComponent translateDimension(ResourceKey<Level> dimension) {
        String[] split = dimension.location().getPath().split("_");
        StringBuilder builder = new StringBuilder();
        for (String s : split) {
            if (s.isEmpty()) {
                continue;
            }
            builder.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
        }
        return Component.literal(builder.toString());
    }

    public static ResourceKey<Level> getCurrentDimension() {
        return mc.level == null ? OVERWORLD : mc.level.dimension();
    }

}
