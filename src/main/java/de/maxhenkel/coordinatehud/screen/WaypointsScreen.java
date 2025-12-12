package de.maxhenkel.coordinatehud.screen;

import de.maxhenkel.coordinatehud.CoordinateHUD;
import de.maxhenkel.coordinatehud.Waypoint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.List;

public class WaypointsScreen extends Screen implements UpdatableScreen {

    private static final Component TITLE = Component.translatable("gui.coordinatehud.waypoints.title");
    private static final Component ORDER = Component.translatable("message.coordinatehud.order");
    private static final Component DIMENSION_FILTER = Component.translatable("message.coordinatehud.dimension_filter");
    private static final Component BACK = Component.translatable("message.coordinatehud.back");
    private static final Component CREATE = Component.translatable("message.coordinatehud.new_waypoint");
    private static final Component EDIT = Component.translatable("message.coordinatehud.edit");
    private static final Component DELETE = Component.translatable("message.coordinatehud.delete");
    private static final Identifier EDIT_ICON = Identifier.fromNamespaceAndPath(CoordinateHUD.MODID, "icon/edit");
    private static final Identifier DELETE_ICON = Identifier.fromNamespaceAndPath(CoordinateHUD.MODID, "icon/delete");
    private static final int HEADER_SIZE = 30;
    private static final int FOOTER_SIZE = 50;
    private static final int CELL_HEIGHT = 40;
    private static final int PADDING = 5;
    private static final int SPACING = 2;
    private static final int COLOR_SIZE = 20;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    @Nullable
    protected final Screen parent;
    protected CycleButton<SortOrder> sortOrder;
    protected CycleButton<DimensionFilter> dimensionFilter;
    protected WaypointList waypointList;

    public WaypointsScreen(@Nullable Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        dimensionFilter = addRenderableWidget(
                CycleButton.builder(DimensionFilter::getName, CoordinateHUD.CLIENT_CONFIG.dimensionFilter.get())
                        .withValues(DimensionFilter.values())
                        .create(PADDING, PADDING, 100, 20, DIMENSION_FILTER, (cycleButton, value) -> {
                            CoordinateHUD.CLIENT_CONFIG.dimensionFilter.set(value).save();
                            waypointList.updateEntries();
                        })
        );

        sortOrder = addRenderableWidget(
                CycleButton.builder(SortOrder::getName, CoordinateHUD.CLIENT_CONFIG.waypointSortOrder.get())
                        .withValues(SortOrder.values())
                        .create(width - 100 - PADDING, PADDING, 100, 20, ORDER, (cycleButton, value) -> {
                            CoordinateHUD.CLIENT_CONFIG.waypointSortOrder.set(value).save();
                            waypointList.updateEntries();
                        })
        );

        if (waypointList != null) {
            waypointList.updateSizeAndPosition(width, height - HEADER_SIZE - FOOTER_SIZE, HEADER_SIZE);
        } else {
            waypointList = new WaypointList(width, height - HEADER_SIZE - FOOTER_SIZE, HEADER_SIZE, CELL_HEIGHT);
        }
        addRenderableWidget(waypointList);

        addRenderableWidget(Button.builder(CREATE, button -> {
            minecraft.setScreen(new WaypointScreen(this, null));
        }).bounds(width / 2 - 100 - 5, height - FOOTER_SIZE / 2 - 10, 100, 20).build());

        addRenderableWidget(Button.builder(BACK, button -> {
            back();
        }).bounds(width / 2 + 5, height - FOOTER_SIZE / 2 - 10, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float delta) {
        super.render(guiGraphics, x, y, delta);
        guiGraphics.drawString(font, TITLE, width / 2 - font.width(TITLE) / 2, HEADER_SIZE / 2 - font.lineHeight / 2, 0xFFFFFF, true);
    }

    @Override
    public void onClose() {
        super.onClose();
        back();
    }

    private void back() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        return super.mouseClicked(event, bl) || waypointList.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return super.mouseReleased(event) || waypointList.mouseReleased(event);
    }

    public void updateWaypoint(Waypoint waypoint) {
        CoordinateHUD.WAYPOINT_STORE.addWaypoint(waypoint);
    }

    @Override
    public void update() {
        waypointList.updateEntries();
    }

    public class WaypointList extends ListBase<WaypointList.Entry> {

        public WaypointList(int width, int height, int y, int itemHeight) {
            super(width, height, y, itemHeight);
            updateEntries();
        }

        public void updateEntries() {
            clearEntries();
            setSelected(null);

            List<Waypoint> waypointsList = sortOrder.getValue().sort(CoordinateHUD.WAYPOINT_STORE.getWaypoints());
            for (Waypoint waypoint : waypointsList) {
                if (dimensionFilter.getValue().getPredicate().test(waypoint)) {
                    addEntry(new Entry(waypoint));
                }
            }

            refreshScrollAmount();
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 100;
        }

        private class Entry extends ListEntryBase<Entry> {

            private final Waypoint waypoint;
            private final Checkbox visible;
            private final SpriteIconButton edit;
            private final SpriteIconButton delete;

            public Entry(Waypoint waypoint) {
                this.waypoint = waypoint;
                visible = Checkbox.builder(Component.empty(), minecraft.font)
                        .pos(0, 0)
                        .onValueChange((checkbox, selected) -> {
                            waypoint.setActive(selected);
                            updateWaypoint(waypoint);
                        })
                        .selected(waypoint.isActive())
                        .build();
                children.add(visible);

                edit = SpriteIconButton.builder(EDIT, button -> {
                            minecraft.setScreen(new WaypointScreen(WaypointsScreen.this, waypoint));
                        }, true)
                        .width(20)
                        .sprite(EDIT_ICON, 16, 16)
                        .build();
                children.add(edit);
                delete = SpriteIconButton.builder(DELETE, button -> {
                            minecraft.setScreen(new DeleteWaypointScreen(WaypointsScreen.this, waypoint));
                        }, true)
                        .width(20)
                        .sprite(DELETE_ICON, 16, 16)
                        .build();
                children.add(delete);
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float delta) {
                guiGraphics.fill(getContentX() + PADDING, getContentY() + getContentHeight() / 2 - COLOR_SIZE / 2, getContentX() + PADDING + COLOR_SIZE, getContentY() + getContentHeight() / 2 + COLOR_SIZE / 2, 0xFFFFFFFF);
                guiGraphics.fill(getContentX() + PADDING + 1, getContentY() + getContentHeight() / 2 - COLOR_SIZE / 2 + 1, getContentX() + PADDING + COLOR_SIZE - 1, getContentY() + getContentHeight() / 2 + COLOR_SIZE / 2 - 1, waypoint.getColor());

                int posY = getContentY() + 4;
                int colorEnd = getContentX() + PADDING + COLOR_SIZE;
                int visibleStart = getContentX() + getContentWidth() - 17 - 20 * 2 - SPACING * 2 - PADDING;
                int textSpace = colorEnd - visibleStart;

                guiGraphics.drawString(font, waypoint.getName(), visibleStart + textSpace / 2 - WaypointsScreen.this.font.width(waypoint.getName()) / 2, posY, 0xFFFFFFFF, true);
                posY += font.lineHeight + 1;

                Component coords = Component.translatable("message.coordinatehud.coordinates", waypoint.getPos().getX(), waypoint.getPos().getY(), waypoint.getPos().getZ());
                guiGraphics.drawString(font, coords, visibleStart + textSpace / 2 - WaypointsScreen.this.font.width(coords) / 2, posY, 0xFFFFFFFF, true);
                posY += font.lineHeight + 1;

                Component details;
                Identifier dimensionLocation = waypoint.getDimension().identifier();
                if (minecraft.level == null || dimensionLocation.equals(minecraft.level.dimension().identifier())) {
                    int distanceInBlocks = (int) minecraft.gameRenderer.getMainCamera().position().distanceTo(waypoint.getPos().getCenter());
                    details = Component.translatable("message.coordinatehud.distance", NUMBER_FORMAT.format(distanceInBlocks));
                } else {
                    details = waypoint.translateDimension();
                }
                guiGraphics.drawString(font, details, visibleStart + textSpace / 2 - WaypointsScreen.this.font.width(details) / 2, posY, 0xFFFFFFFF, true);

                visible.setPosition(visibleStart, getContentY() + getContentHeight() / 2 - visible.getHeight() / 2);
                visible.render(guiGraphics, mouseX, mouseY, delta);

                edit.setPosition(getContentX() + getContentWidth() - 20 * 2 - SPACING - PADDING, getContentY() + getContentHeight() / 2 - 10);
                edit.render(guiGraphics, mouseX, mouseY, delta);

                delete.setPosition(getContentX() + getContentWidth() - 20 - PADDING, getContentY() + getContentHeight() / 2 - 10);
                delete.render(guiGraphics, mouseX, mouseY, delta);
            }
        }

    }

}
