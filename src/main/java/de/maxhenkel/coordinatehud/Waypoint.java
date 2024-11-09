package de.maxhenkel.coordinatehud;

import com.google.gson.*;
import de.maxhenkel.coordinatehud.screen.ColorPicker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class Waypoint {

    public static final int MAX_WAYPOINT_NAME_LENGTH = 32;

    private final UUID id;
    private String name;
    private long creationTime;
    private ResourceKey<Level> dimension;
    private BlockPos pos;
    private int color;
    private boolean active;

    private Waypoint(UUID id, String name, long creationTime, ResourceKey<Level> dimension, BlockPos pos, int color, boolean active) {
        this.id = id;
        this.name = name;
        this.creationTime = creationTime;
        this.dimension = dimension;
        this.pos = pos;
        this.color = color;
        this.active = active;
    }

    public static Waypoint create() {
        Minecraft mc = Minecraft.getInstance();
        return new Waypoint(UUID.randomUUID(), "", System.currentTimeMillis(), mc.level == null ? ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld")) : mc.level.dimension(), mc.gameRenderer.getMainCamera().getBlockPosition(), randomColor(), true);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getColor() {
        return color;
    }

    public boolean isActive() {
        return active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double distanceToCamera() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(pos.getCenter());
    }

    public MutableComponent translateDimension() {
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

    public static int randomColor() {
        return ColorPicker.getColor(new Random().nextFloat());
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
            object.addProperty("creationTime", src.getCreationTime());
            object.addProperty("dimension", src.getDimension().location().toString());
            object.addProperty("x", src.getPos().getX());
            object.addProperty("y", src.getPos().getY());
            object.addProperty("z", src.getPos().getZ());
            object.addProperty("color", src.getColor());
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
            JsonElement creationTimeElement = object.get("creationTime");
            long creationTime;
            if (creationTimeElement != null) {
                creationTime = creationTimeElement.getAsLong();
            } else {
                creationTime = System.currentTimeMillis();
            }
            JsonElement dimensionElement = object.get("dimension");
            ResourceKey<Level> dimension = null;
            if (dimensionElement != null) {
                ResourceLocation dimensionLocation = ResourceLocation.tryParse(dimensionElement.getAsString());
                if (dimensionLocation != null) {
                    dimension = ResourceKey.create(Registries.DIMENSION, dimensionLocation);
                }
            }
            if (dimension == null) {
                dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld"));
            }

            int x = object.get("x").getAsInt();
            int y = object.get("y").getAsInt();
            int z = object.get("z").getAsInt();
            JsonElement colorElement = object.get("color");
            int color;
            if (colorElement != null) {
                color = colorElement.getAsInt();
            } else {
                color = randomColor();
            }
            boolean active = object.get("active").getAsBoolean();
            return new Waypoint(id, name, creationTime, dimension, new BlockPos(x, y, z), color, active);
        }
    }

}
