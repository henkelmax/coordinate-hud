package de.maxhenkel.coordinatehud.screen;

import de.maxhenkel.coordinatehud.Waypoint;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Predicate;

public enum DimensionFilter {

    CURRENT_DIMENSION(Component.translatable("message.coordinatehud.dimension_filter.current"), Waypoint::isCurrentDimension),
    ALL_DIMENSIONS(Component.translatable("message.coordinatehud.dimension_filter.all"), waypoint -> true);

    private final Component name;
    private final Predicate<Waypoint> predicate;

    DimensionFilter(Component name, Predicate<Waypoint> predicate) {
        this.name = name;
        this.predicate = predicate;
    }

    public Component getName() {
        return name;
    }

    public Predicate<Waypoint> getPredicate() {
        return predicate;
    }

    public List<Waypoint> filter(List<Waypoint> waypoints) {
        return waypoints.stream().filter(predicate).toList();
    }

}
