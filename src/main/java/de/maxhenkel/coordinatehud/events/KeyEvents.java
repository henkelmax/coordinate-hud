package de.maxhenkel.coordinatehud.events;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.coordinatehud.CoordinateHUD;
import de.maxhenkel.coordinatehud.screen.WaypointScreen;
import de.maxhenkel.coordinatehud.screen.WaypointsScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class KeyEvents {

    public static KeyMapping HIDE_HUD;
    public static KeyMapping WAYPOINTS;
    public static KeyMapping CREATE_WAYPOINT;

    public static void init() {
        HIDE_HUD = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.coordinatehud.hide_hud", InputConstants.UNKNOWN.getValue(), "key.categories.coordinatehud"));
        WAYPOINTS = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.coordinatehud.waypoints", GLFW.GLFW_KEY_M, "key.categories.coordinatehud"));
        CREATE_WAYPOINT = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.coordinatehud.create_waypoint", InputConstants.UNKNOWN.getValue(), "key.categories.coordinatehud"));
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
