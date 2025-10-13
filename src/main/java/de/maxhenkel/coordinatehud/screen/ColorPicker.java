package de.maxhenkel.coordinatehud.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.function.Consumer;

public class ColorPicker extends AbstractWidget {

    protected Consumer<Integer> onColorChange;

    public ColorPicker(int x, int y, int width, int height, Consumer<Integer> onColorChange) {
        super(x, y, width, height, Component.empty());
        this.onColorChange = onColorChange;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFFFFFFFF);

        int startX = getX() + 1;
        int width = getWidth() - 2;

        for (int i = 0; i < width; i++) {
            int color = getColor((float) i / (float) width);
            guiGraphics.fill(startX + i, getY() + 1, startX + i + 1, getY() + getHeight() - 1, color);
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double d, double e) {
        return updateColor(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        boolean success = updateColor(event);
        if (success) {
            playDownSound(Minecraft.getInstance().getSoundManager());
        }
        return success;
    }

    private boolean updateColor(MouseButtonEvent event) {
        if (!isValidClickButton(event.buttonInfo())) {
            return false;
        }

        if (!isMouseOver(event.x(), event.y())) {
            return false;
        }

        double value = Math.max(Math.min((event.x() - getX() + 1) / (getWidth() - 2), 1D), 0D);

        onColorChange.accept(getColor((float) value));
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {

    }

    public static int getColor(float value) {
        Color hsbColor = Color.getHSBColor(value, 1F, 1F);
        return hsbColor.getAlpha() << 24 | hsbColor.getRed() << 16 | hsbColor.getGreen() << 8 | hsbColor.getBlue();
    }
}
