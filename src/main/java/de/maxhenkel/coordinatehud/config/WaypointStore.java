package de.maxhenkel.coordinatehud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.maxhenkel.coordinatehud.CoordinateHUD;
import de.maxhenkel.coordinatehud.Waypoint;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WaypointStore {

    private final Path file;
    private final Gson gson;
    private Map<UUID, Waypoint> waypoints;
    private List<Waypoint> activeWaypointCache;

    public WaypointStore(Path file) {
        this.file = file;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Waypoint.class, new Waypoint.Serializer())
                .registerTypeAdapter(Waypoint.class, new Waypoint.Deserializer())
                .create();
        this.waypoints = new HashMap<>();
        load();
    }

    public void load() {
        if (Files.notExists(file)) {
            return;
        }
        List<Waypoint> waypointList = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(file)) {
            Type waypointListType = new TypeToken<ArrayList<Waypoint>>() {
            }.getType();
            waypointList = gson.fromJson(reader, waypointListType);
        } catch (Exception e) {
            CoordinateHUD.LOGGER.error("Failed to load waypoints", e);
        }
        waypoints = waypointList.stream().collect(HashMap::new, (map, waypoint) -> map.put(waypoint.getId(), waypoint), HashMap::putAll);
    }

    public void save() {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                gson.toJson(waypoints.values(), writer);
            }
        } catch (Exception e) {
            CoordinateHUD.LOGGER.error("Failed to save waypoints", e);
        }
        activeWaypointCache = null;
    }

    public List<Waypoint> getActiveWaypoints() {
        if (activeWaypointCache == null) {
            activeWaypointCache = new ArrayList<>();
            waypoints.values().stream().filter(Waypoint::isActive).forEach(activeWaypointCache::add);
        }
        return activeWaypointCache;
    }

    public List<Waypoint> getWaypoints() {
        return new ArrayList<>(waypoints.values());
    }

    @Nullable
    public Waypoint getWaypoint(UUID waypointId) {
        return waypoints.get(waypointId);
    }

    public void addWaypoint(Waypoint waypoint) {
        if (waypoint.getName().isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        waypoints.put(waypoint.getId(), waypoint);
        save();
    }

    public Waypoint removeWaypoint(UUID waypointId) {
        Waypoint waypoint = waypoints.remove(waypointId);
        save();
        return waypoint;
    }

    public Waypoint removeWaypoint(Waypoint waypoint) {
        return removeWaypoint(waypoint.getId());
    }
}
