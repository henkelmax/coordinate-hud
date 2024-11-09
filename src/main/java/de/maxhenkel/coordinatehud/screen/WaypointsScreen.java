package de.maxhenkel.coordinatehud.screen;

import de.maxhenkel.coordinatehud.CoordinateHUD;
import de.maxhenkel.coordinatehud.Waypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.List;

public class WaypointsScreen extends Screen implements UpdatableScreen {

    private static final Component TITLE = Component.translatable("gui.coordinatehud.waypoints.title");
    private static final Component ORDER = Component.translatable("message.coordinatehud.order");
    private static final Component BACK = Component.translatable("message.coordinatehud.back");
    private static final Component CREATE = Component.translatable("message.coordinatehud.new_waypoint");
    private static final Component EDIT = Component.translatable("message.coordinatehud.edit");
    private static final Component DELETE = Component.translatable("message.coordinatehud.delete");
    private static final ResourceLocation EDIT_ICON = ResourceLocation.fromNamespaceAndPath(CoordinateHUD.MODID, "icon/edit");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(CoordinateHUD.MODID, "icon/delete");
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
    protected WaypointList waypointList;

    public WaypointsScreen(@Nullable Screen parent) {
        super(TITLE);
        this.minecraft = Minecraft.getInstance();
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        sortOrder = addRenderableWidget(
                CycleButton.builder(SortOrder::getName)
                        .withValues(SortOrder.values())
                        .withInitialValue(CoordinateHUD.CLIENT_CONFIG.waypointSortOrder.get())
                        .create(width - 100 - PADDING, PADDING, 100, 20, ORDER, (cycleButton, value) -> {
                            CoordinateHUD.CLIENT_CONFIG.waypointSortOrder.set(value);
                            CoordinateHUD.CLIENT_CONFIG.waypointSortOrder.save();
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button) || waypointList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button) || waypointList.mouseReleased(mouseX, mouseY, button);
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
                addEntry(new Entry(waypoint));
            }

            clampScrollAmount();
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 100;
        }

        @Override
        protected int getScrollbarPosition() {
            return WaypointsScreen.this.width - 6;
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
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
                guiGraphics.fill(left + PADDING, top + height / 2 - COLOR_SIZE / 2, left + PADDING + COLOR_SIZE, top + height / 2 + COLOR_SIZE / 2, 0xFFFFFFFF);
                guiGraphics.fill(left + PADDING + 1, top + height / 2 - COLOR_SIZE / 2 + 1, left + PADDING + COLOR_SIZE - 1, top + height / 2 + COLOR_SIZE / 2 - 1, waypoint.getColor());

                int posY = top + 4;
                int colorEnd = left + PADDING + COLOR_SIZE;
                int visibleStart = left + width - 17 - 20 * 2 - SPACING * 2 - PADDING;
                int textSpace = colorEnd - visibleStart;

                guiGraphics.drawString(font, waypoint.getName(), visibleStart + textSpace / 2 - WaypointsScreen.this.font.width(waypoint.getName()) / 2, posY, 0xFFFFFFFF, true);
                posY += font.lineHeight + 1;

                Component coords = Component.translatable("message.coordinatehud.coordinates", waypoint.getPos().getX(), waypoint.getPos().getY(), waypoint.getPos().getZ());
                guiGraphics.drawString(font, coords, visibleStart + textSpace / 2 - WaypointsScreen.this.font.width(coords) / 2, posY, 0xFFFFFFFF, true);
                posY += font.lineHeight + 1;

                Component details;
                ResourceLocation dimensionLocation = waypoint.getDimension().location();
                if (minecraft.level == null || dimensionLocation.equals(minecraft.level.dimension().location())) {
                    int distanceInBlocks = (int) minecraft.gameRenderer.getMainCamera().getPosition().distanceTo(waypoint.getPos().getCenter());
                    details = Component.translatable("message.coordinatehud.distance", NUMBER_FORMAT.format(distanceInBlocks));
                } else {
                    details = waypoint.translateDimension();
                }
                guiGraphics.drawString(font, details, visibleStart + textSpace / 2 - WaypointsScreen.this.font.width(details) / 2, posY, 0xFFFFFFFF, true);

                visible.setPosition(visibleStart, top + height / 2 - visible.getHeight() / 2);
                visible.render(guiGraphics, mouseX, mouseY, delta);

                edit.setPosition(left + width - 20 * 2 - SPACING - PADDING, top + height / 2 - 10);
                edit.render(guiGraphics, mouseX, mouseY, delta);

                delete.setPosition(left + width - 20 - PADDING, top + height / 2 - 10);
                delete.render(guiGraphics, mouseX, mouseY, delta);
            }
        }

    }

}
