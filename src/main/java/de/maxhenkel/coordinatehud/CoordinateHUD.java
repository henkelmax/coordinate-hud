package de.maxhenkel.coordinatehud;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.coordinatehud.command.WaypointCommand;
import de.maxhenkel.coordinatehud.config.ClientConfig;
import de.maxhenkel.coordinatehud.config.WaypointStore;
import de.maxhenkel.coordinatehud.events.RenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class CoordinateHUD implements ClientModInitializer {

    public static final String MODID = "coordinatehud";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ClientConfig CLIENT_CONFIG;
    public static WaypointStore WAYPOINT_STORE;

    @Override
    public void onInitializeClient() {
        Path configFolder = FabricLoader.getInstance().getConfigDir().resolve(MODID);
        CLIENT_CONFIG = ConfigBuilder.build(configFolder.resolve("coordinatehud.properties"), ClientConfig::new);

        ClientCommandRegistrationCallback.EVENT.register(WaypointCommand::register);
        WorldRenderEvents.AFTER_ENTITIES.register(RenderEvents::render);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerData serverData = handler.getServerData();
            if (serverData != null) {
                WAYPOINT_STORE = new WaypointStore(configFolder.resolve("%s_waypoints.json".formatted(cleanString(serverData.ip))));
            } else {
                String folderName = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
                WAYPOINT_STORE = new WaypointStore(configFolder.resolve("%s_waypoints.json".formatted(cleanString(folderName))));
            }
        });
    }

    private String cleanString(String str) {
        return str.replace(".", "_").replace(" ", "_").replaceAll("[^a-zA-Z0-9_-]", "");
    }

}
