package de.maxhenkel.coordinatehud.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.coordinatehud.screen.DimensionFilter;
import de.maxhenkel.coordinatehud.screen.SortOrder;

public class ClientConfig {

    public final ConfigEntry<Integer> waypointScaleDistance;
    public final ConfigEntry<Boolean> showWaypointDistance;
    public final ConfigEntry<SortOrder> waypointSortOrder;
    public final ConfigEntry<DimensionFilter> dimensionFilter;

    public ClientConfig(ConfigBuilder builder) {
        showWaypointDistance = builder.booleanEntry("show_waypoint_distance", true);
        waypointScaleDistance = builder.integerEntry("waypoint_scale_distance", 24, 8, Integer.MAX_VALUE);
        waypointSortOrder = builder.enumEntry("waypoint_sort_order", SortOrder.CREATION_ASC);
        dimensionFilter = builder.enumEntry("dimension_filter", DimensionFilter.CURRENT_DIMENSION);
        //TODO Add hide HUD option
    }

}
