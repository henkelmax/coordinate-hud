package de.maxhenkel.coordinatehud.screen;

import de.maxhenkel.coordinatehud.CoordinateHUD;
import de.maxhenkel.coordinatehud.DimensionUtils;
import de.maxhenkel.coordinatehud.Waypoint;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class WaypointScreen extends Screen {

    private static final Component TITLE = Component.translatable("gui.coordinatehud.waypoint.title");
    private static final Component EDIT_WAYPOINT = Component.translatable("gui.coordinatehud.edit_waypoint.title");
    private static final Component CREATE_WAYPOINT = Component.translatable("gui.coordinatehud.create_waypoint.title");
    private static final Component WAYPOINT_NAME = Component.translatable("message.coordinatehud.edit_waypoint.waypoint_name").withStyle(ChatFormatting.GRAY);
    private static final Component COORDINATES = Component.translatable("message.coordinatehud.edit_waypoint.coordinates").withStyle(ChatFormatting.GRAY);
    private static final Component VISIBLE = Component.translatable("message.coordinatehud.edit_waypoint.visible").withStyle(ChatFormatting.GRAY);
    private static final Component DIMENSION = Component.translatable("message.coordinatehud.edit_waypoint.dimension");
    private static final Component COLOR = Component.translatable("message.coordinatehud.edit_waypoint.color").withStyle(ChatFormatting.GRAY);
    private static final Component SAVE = Component.translatable("message.coordinatehud.edit_waypoint.save");
    private static final Component CANCEL = Component.translatable("message.coordinatehud.edit_waypoint.cancel");

    private static final String COORDINATE_REGEX = "-?[0-9]{0,8}";

    @Nullable
    protected Screen parent;

    protected EditBox waypointName;
    protected EditBox coordinateX;
    protected EditBox coordinateY;
    protected EditBox coordinateZ;
    protected Checkbox visible;
    protected ResourceKey<Level> dimension;
    protected WaypointIconDisplay waypointColor;
    protected Button saveButton;

    protected boolean newWaypoint;
    protected Waypoint waypoint;

    public WaypointScreen(@Nullable Screen parent, @Nullable Waypoint waypoint) {
        super(TITLE);
        this.parent = parent;
        this.newWaypoint = waypoint == null;
        if (newWaypoint) {
            waypoint = Waypoint.create();
        }
        this.waypoint = waypoint;
    }

    @Override
    protected void init() {
        super.init();
        LinearLayout contentLayout = LinearLayout.vertical().spacing(5);

        contentLayout.addChild(new StringWidget(WAYPOINT_NAME, font));
        waypointName = contentLayout.addChild(new EditBox(font, 200, 20, WAYPOINT_NAME));
        waypointName.setValue(waypoint.getName());
        waypointName.setMaxLength(Waypoint.MAX_WAYPOINT_NAME_LENGTH);

        contentLayout.addChild(new StringWidget(COORDINATES, font));
        LinearLayout coordsLayout = LinearLayout.horizontal().spacing(4);
        coordinateX = coordsLayout.addChild(new EditBox(font, 64, 20, COORDINATES));
        coordinateY = coordsLayout.addChild(new EditBox(font, 64, 20, COORDINATES));
        coordinateZ = coordsLayout.addChild(new EditBox(font, 64, 20, COORDINATES));
        coordinateX.setMaxLength(9);
        coordinateX.setFilter(s -> s.isEmpty() || s.matches(COORDINATE_REGEX));
        coordinateX.setValue(String.valueOf(waypoint.getPos().getX()));
        coordinateY.setMaxLength(9);
        coordinateY.setFilter(s -> s.isEmpty() || s.matches(COORDINATE_REGEX));
        coordinateY.setValue(String.valueOf(waypoint.getPos().getY()));
        coordinateZ.setMaxLength(9);
        coordinateZ.setFilter(s -> s.isEmpty() || s.matches(COORDINATE_REGEX));
        coordinateZ.setValue(String.valueOf(waypoint.getPos().getZ()));
        contentLayout.addChild(coordsLayout);

        dimension = waypoint.getDimension();
        CycleButton<ResourceKey<Level>> dimensionButton = CycleButton.builder(DimensionUtils::translateDimension)
                .withValues(DimensionUtils.getAllDimensions())
                .withInitialValue(dimension)
                .create(0, 0, 200, 20, DIMENSION, (cycleButton, dim) -> dimension = dim);
        contentLayout.addChild(dimensionButton);

        contentLayout.addChild(new StringWidget(COLOR, font));
        LinearLayout colorLayout = LinearLayout.horizontal().spacing(4);
        waypointColor = colorLayout.addChild(new WaypointIconDisplay(0, 0, 20, 20, waypoint.getColor()));
        colorLayout.addChild(new ColorPicker(0, 0, 176, 20, color -> {
            waypointColor.setColor(color);
        }));
        contentLayout.addChild(colorLayout);

        visible = contentLayout.addChild(Checkbox.builder(VISIBLE, font).selected(waypoint.isActive()).build());

        LinearLayout linearlayout = LinearLayout.horizontal().spacing(4);
        saveButton = linearlayout.addChild(Button.builder(SAVE, b -> {
            updateWaypoint();
            onClose();
        }).width(98).build());
        linearlayout.addChild(Button.builder(CANCEL, b -> onClose()).width(98).build());
        contentLayout.addChild(linearlayout);

        contentLayout.visitWidgets(this::addRenderableWidget);
        contentLayout.arrangeElements();
        FrameLayout.centerInRectangle(contentLayout, getRectangle());
    }

    @Override
    public void tick() {
        super.tick();
        if (saveButton != null) {
            saveButton.active = !waypointName.getValue().isBlank();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, newWaypoint ? CREATE_WAYPOINT : EDIT_WAYPOINT, width / 2, 15, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        super.onClose();
        if (parent instanceof UpdatableScreen updatableScreen) {
            updatableScreen.update();
        }
        minecraft.setScreen(parent);
    }

    private void updateWaypoint() {
        String newName = waypointName.getValue();
        if (!waypoint.getName().equals(newName)) {
            waypoint.setName(newName.trim());
        }
        waypoint.setPos(new BlockPos(parseCoordinate(coordinateX), parseCoordinate(coordinateY), parseCoordinate(coordinateZ)));
        waypoint.setDimension(dimension);
        waypoint.setColor(waypointColor.getColor());
        waypoint.setActive(visible.selected());
        CoordinateHUD.WAYPOINT_STORE.addWaypoint(waypoint);
    }

    private int parseCoordinate(EditBox editBox) {
        if (editBox.getValue().isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(editBox.getValue());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
