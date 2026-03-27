package de.maxhenkel.coordinatehud.events;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.coordinatehud.CoordinateHUD;
import de.maxhenkel.coordinatehud.screen.WaypointScreen;
import de.maxhenkel.coordinatehud.screen.WaypointsScreen;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyEvents {

    public static KeyMapping.Category CATEGORY_COORDINATE_HUD;
    public static KeyMapping HIDE_HUD;
    public static KeyMapping WAYPOINTS;
    public static KeyMapping CREATE_WAYPOINT;

    public static void init() {
        CATEGORY_COORDINATE_HUD = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(CoordinateHUD.MODID, "coordinatehud"));
        HIDE_HUD = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.coordinatehud.hide_hud", InputConstants.UNKNOWN.getValue(), CATEGORY_COORDINATE_HUD));
        WAYPOINTS = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.coordinatehud.waypoints", GLFW.GLFW_KEY_M, CATEGORY_COORDINATE_HUD));
        CREATE_WAYPOINT = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.coordinatehud.create_waypoint", InputConstants.UNKNOWN.getValue(), CATEGORY_COORDINATE_HUD));
    }

    public static void onTick(Minecraft mc) {
        if (mc.player == null) {
            return;
        }

        if (HIDE_HUD.consumeClick()) {
            CoordinateHUD.CLIENT_CONFIG.hideHud.set(!CoordinateHUD.CLIENT_CONFIG.hideHud.get()).save();
        }
        if (WAYPOINTS.consumeClick()) {
            mc.setScreen(new WaypointsScreen(null));
        }
        if (CREATE_WAYPOINT.consumeClick()) {
            mc.setScreen(new WaypointScreen(null, null));
        }
    }
}
