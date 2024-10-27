package de.maxhenkel.coordinatehud;

import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.UUID;

public class Waypoint {

    private final UUID id;
    private String name;
    private ResourceLocation dimension;
    private BlockPos pos;
    private boolean active;

    public Waypoint(UUID id, String name, ResourceLocation dimension, BlockPos pos, boolean active) {
        this.id = id;
        setName(name);
        setDimension(dimension);
        setPos(pos);
        setActive(active);
    }

    public Waypoint(String name, ResourceLocation dimension, BlockPos pos, boolean active) {
        this(UUID.randomUUID(), name, dimension, pos, active);
    }

    public Waypoint(String name, ResourceLocation dimension, BlockPos pos) {
        this(UUID.randomUUID(), name, dimension, pos, true);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean isActive() {
        return active;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        this.name = name;
    }

    public void setDimension(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Waypoint waypoint = (Waypoint) o;

        return Objects.equals(id, waypoint.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static class Serializer implements JsonSerializer<Waypoint> {
        @Override
        public JsonElement serialize(Waypoint src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("id", src.getId().toString());
            object.addProperty("name", src.getName());
            object.addProperty("dimension", src.getDimension().toString());
            object.addProperty("x", src.getPos().getX());
            object.addProperty("y", src.getPos().getY());
            object.addProperty("z", src.getPos().getZ());
            object.addProperty("active", src.isActive());
            return object;
        }
    }

    public static class Deserializer implements JsonDeserializer<Waypoint> {
        @Override
        public Waypoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            UUID id = UUID.fromString(object.get("id").getAsString());
            String name = object.get("name").getAsString();
            ResourceLocation dimension = ResourceLocation.tryParse(object.get("dimension").getAsString());
            int x = object.get("x").getAsInt();
            int y = object.get("y").getAsInt();
            int z = object.get("z").getAsInt();
            boolean active = object.get("active").getAsBoolean();
            return new Waypoint(id, name, dimension, new BlockPos(x, y, z), active);
        }
    }

}
