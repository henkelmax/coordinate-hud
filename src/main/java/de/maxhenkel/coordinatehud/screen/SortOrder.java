package de.maxhenkel.coordinatehud.screen;

import de.maxhenkel.coordinatehud.Waypoint;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;

public enum SortOrder {

    CREATION_ASC(Component.translatable("message.coordinatehud.order.creation_asc"), Comparator.comparingLong(Waypoint::getCreationTime)),
    CREATION_DESC(Component.translatable("message.coordinatehud.order.creation_desc"), (w1, w2) -> Long.compare(w2.getCreationTime(), w1.getCreationTime())),
    NAME_ASC(Component.translatable("message.coordinatehud.order.name_asc"), (w1, w2) -> w1.getName().compareToIgnoreCase(w2.getName())),
    NAME_DESC(Component.translatable("message.coordinatehud.order.name_desc"), (w1, w2) -> w2.getName().compareToIgnoreCase(w1.getName())),
    DISTANCE_ASC(Component.translatable("message.coordinatehud.order.distance_asc"), Comparator.comparingDouble(Waypoint::distanceToCamera)),
    DISTANCE_DESC(Component.translatable("message.coordinatehud.order.distance_desc"), (w1, w2) -> Double.compare(w2.distanceToCamera(), w1.distanceToCamera()));

    private final Component name;
    private final Comparator<Waypoint> comparator;

    SortOrder(Component name, Comparator<Waypoint> comparator) {
        this.name = name;
        this.comparator = comparator;
    }

    public Component getName() {
        return name;
    }

    public Comparator<Waypoint> getComparator() {
        return comparator;
    }

    /**
     * This method may sort the list in place or return a new list.
     *
     * @param waypoints the list to sort
     * @return the sorted list
     */
    public List<Waypoint> sort(List<Waypoint> waypoints) {
        waypoints.sort(comparator);
        return waypoints;
    }

}
