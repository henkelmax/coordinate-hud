package de.maxhenkel.coordinatehud.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;

public class ClientConfig {

    public final ConfigEntry<Integer> waypointDistance;

    public ClientConfig(ConfigBuilder builder) {
        waypointDistance = builder.integerEntry("waypoint_distance", 24, 8, Integer.MAX_VALUE);
    }

}
