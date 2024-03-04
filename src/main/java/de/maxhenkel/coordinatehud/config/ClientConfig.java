package de.maxhenkel.coordinatehud.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;

public class ClientConfig {

    public final ConfigEntry<Integer> waypointScaleDistance;
    public final ConfigEntry<Boolean> showWaypointDistance;

    public ClientConfig(ConfigBuilder builder) {
        showWaypointDistance = builder.booleanEntry("show_waypoint_distance", true);
        waypointScaleDistance = builder.integerEntry("waypoint_scale_distance", 24, 8, Integer.MAX_VALUE);
    }

}
