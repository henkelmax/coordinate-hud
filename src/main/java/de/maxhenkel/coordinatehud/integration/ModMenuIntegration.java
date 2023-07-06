package de.maxhenkel.coordinatehud.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.maxhenkel.coordinatehud.CoordinateHUD;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (isClothConfigLoaded()) {
            return ClothConfigIntegration::createConfigScreen;
        }
        return parent -> null;
    }

    private static boolean isClothConfigLoaded() {
        if (FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            try {
                Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
                CoordinateHUD.LOGGER.warn("Using Cloth Config GUI");
                return true;
            } catch (Exception e) {
                CoordinateHUD.LOGGER.warn("Failed to load Cloth Config", e);
            }
        }
        return false;
    }

}