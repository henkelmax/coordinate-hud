package de.maxhenkel.coordinatehud.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.maxhenkel.coordinatehud.CoordinateHUD;
import de.maxhenkel.coordinatehud.Waypoint;
import dev.xpple.clientarguments.arguments.CBlockPosArgument;
import dev.xpple.clientarguments.arguments.CDimensionArgument;
import dev.xpple.clientarguments.arguments.CUuidArgument;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

public class WaypointCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext ctx) {
        LiteralArgumentBuilder<FabricClientCommandSource> coords = ClientCommandManager.literal("waypoint");

        coords.executes(WaypointCommand::list);

        coords.then(ClientCommandManager.literal("create").then(ClientCommandManager.argument("location", CBlockPosArgument.blockPos()).then(ClientCommandManager.argument("name", StringArgumentType.string()).executes(context -> {
            BlockPos location = CBlockPosArgument.getBlockPos(context, "location");
            String name = StringArgumentType.getString(context, "name");
            return addWaypoint(context, name, location, context.getSource().getWorld().dimension().location());
        }))));

        coords.then(ClientCommandManager.literal("create").then(ClientCommandManager.argument("location", CBlockPosArgument.blockPos()).then(ClientCommandManager.argument("dimension", CDimensionArgument.dimension()).then(ClientCommandManager.argument("name", StringArgumentType.string()).executes(context -> {
            BlockPos location = CBlockPosArgument.getBlockPos(context, "location");
            ResourceKey<Level> dimension = CDimensionArgument.getDimension(context, "dimension");
            String name = StringArgumentType.getString(context, "name");
            return addWaypoint(context, name, location, dimension.location());
        })))));

        coords.then(ClientCommandManager.literal("remove").then(ClientCommandManager.argument("id", CUuidArgument.uuid()).executes(context -> {
            UUID id = CUuidArgument.getUuid(context, "id");
            Waypoint waypoint = CoordinateHUD.WAYPOINT_STORE.removeWaypoint(id);
            context.getSource().sendFeedback(Component.translatable("message.coordinatehud.removed_waypoint", waypoint.getName()));
            return 1;
        })));

        coords.then(ClientCommandManager.literal("list").executes(WaypointCommand::list));

        coords.then(ClientCommandManager.literal("enable").then(ClientCommandManager.argument("id", CUuidArgument.uuid()).executes(context -> {
            UUID id = CUuidArgument.getUuid(context, "id");
            Waypoint waypoint = CoordinateHUD.WAYPOINT_STORE.getWaypoint(id);
            if (waypoint == null) {
                context.getSource().sendError(Component.translatable("message.coordinatehud.waypoint_not_found"));
                return 0;
            }
            waypoint.setActive(true);
            CoordinateHUD.WAYPOINT_STORE.updateWaypoint(waypoint);
            context.getSource().sendFeedback(Component.translatable("message.coordinatehud.enabled_waypoint", waypoint.getName()));
            return 1;
        })));

        coords.then(ClientCommandManager.literal("disable").then(ClientCommandManager.argument("id", CUuidArgument.uuid()).executes(context -> {
            UUID id = CUuidArgument.getUuid(context, "id");
            Waypoint waypoint = CoordinateHUD.WAYPOINT_STORE.getWaypoint(id);
            if (waypoint == null) {
                context.getSource().sendError(Component.translatable("message.coordinatehud.waypoint_not_found"));
                return 0;
            }
            waypoint.setActive(false);
            CoordinateHUD.WAYPOINT_STORE.updateWaypoint(waypoint);
            context.getSource().sendFeedback(Component.translatable("message.coordinatehud.disabled_waypoint", waypoint.getName()));
            return 1;
        })));

        coords.then(ClientCommandManager.literal("rename").then(ClientCommandManager.argument("id", CUuidArgument.uuid()).then(ClientCommandManager.argument("name", StringArgumentType.string()).executes(context -> {
            UUID id = CUuidArgument.getUuid(context, "id");
            String newName = StringArgumentType.getString(context, "name");
            Waypoint waypoint = CoordinateHUD.WAYPOINT_STORE.getWaypoint(id);
            if (waypoint == null) {
                context.getSource().sendError(Component.translatable("message.coordinatehud.waypoint_not_found"));
                return 0;
            }
            String oldName = waypoint.getName();
            waypoint.setName(newName);
            CoordinateHUD.WAYPOINT_STORE.updateWaypoint(waypoint);
            context.getSource().sendFeedback(Component.translatable("message.coordinatehud.renamed_waypoint", oldName, waypoint.getName()));
            return 1;
        }))));

        dispatcher.register(coords);
    }

    private static int list(CommandContext<FabricClientCommandSource> context) {
        List<Waypoint> waypoints = CoordinateHUD.WAYPOINT_STORE.getWaypoints();
        if (waypoints.isEmpty()) {
            context.getSource().sendFeedback(Component.translatable("message.coordinatehud.no_waypoints_found"));
            return 1;
        }
        waypoints.sort(Comparator.comparing(Waypoint::getName));
        for (Waypoint waypoint : waypoints) {
            Component waypointComponent = Component.literal("")
                    .append(Component.literal(waypoint.getName()).withStyle(waypoint.isActive() ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .append(" ")
                    .append(fromLocation(context.getSource().getWorld(), waypoint))
                    .append(" ")
                    .append(toggle(waypoint))
                    .append(" ")
                    .append(rename(waypoint))
                    .append(" ")
                    .append(remove(waypoint));
            context.getSource().sendFeedback(waypointComponent);
        }
        return 1;
    }

    private static int addWaypoint(CommandContext<FabricClientCommandSource> context, String name, BlockPos location, ResourceLocation dimension) {
        Waypoint waypoint = new Waypoint(name, dimension, location);
        CoordinateHUD.WAYPOINT_STORE.addWaypoint(waypoint);
        context.getSource().sendFeedback(Component.translatable("message.coordinatehud.added_waypoint", waypoint.getName()));
        return 1;
    }

    public static Component fromLocation(ClientLevel level, Waypoint waypoint) {
        MutableComponent c = Component.translatable("chat.coordinates", waypoint.getPos().getX(), waypoint.getPos().getY(), waypoint.getPos().getZ());
        if (!waypoint.getDimension().equals(level.dimension().location())) {
            c.append(" in ").append(Component.literal(shortName(waypoint.getDimension())));
        }
        return ComponentUtils.wrapInSquareBrackets(c).withStyle(ChatFormatting.BLUE);
    }

    private static final Component ENABLE = Component.translatable("message.coordinatehud.enable");
    private static final Component DISABLE = Component.translatable("message.coordinatehud.disable");
    private static final Component RENAME = Component.translatable("message.coordinatehud.rename");
    private static final Component REMOVE = Component.translatable("message.coordinatehud.remove");

    public static Component toggle(Waypoint waypoint) {
        return ComponentUtils.wrapInSquareBrackets(
                        Component.empty().append(waypoint.isActive() ? DISABLE : ENABLE)
                                .withStyle((style) -> {
                                    return style
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/waypoint %s %s".formatted(waypoint.isActive() ? "disable" : "enable", waypoint.getId())))
                                            .withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, waypoint.isActive() ? DISABLE : ENABLE));
                                })
                )
                .withStyle(ChatFormatting.YELLOW);
    }

    public static Component rename(Waypoint waypoint) {
        return ComponentUtils.wrapInSquareBrackets(
                        Component.empty().append(RENAME)
                                .withStyle((style) -> {
                                    return style
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/waypoint rename %s ".formatted(waypoint.getId())))
                                            .withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, RENAME));
                                })
                )
                .withStyle(ChatFormatting.GOLD);
    }

    public static Component remove(Waypoint waypoint) {
        return ComponentUtils.wrapInSquareBrackets(
                        Component.empty().append(REMOVE)
                                .withStyle((style) -> {
                                    return style
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/waypoint remove %s".formatted(waypoint.getId())))
                                            .withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, REMOVE));
                                })
                )
                .withStyle(ChatFormatting.RED);
    }

    public static String shortName(ResourceLocation resourceLocation) {
        if (resourceLocation.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            return resourceLocation.getPath();
        }
        return resourceLocation.toString();
    }

}
