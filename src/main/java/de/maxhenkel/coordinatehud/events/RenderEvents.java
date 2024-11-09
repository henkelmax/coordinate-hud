package de.maxhenkel.coordinatehud.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.maxhenkel.coordinatehud.CoordinateHUD;
import de.maxhenkel.coordinatehud.Waypoint;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RenderEvents {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final ResourceLocation COLOR_LOCATION = ResourceLocation.fromNamespaceAndPath(CoordinateHUD.MODID, "textures/icons/waypoint_color.png");
    private static final ResourceLocation OVERLAY_LOCATION = ResourceLocation.fromNamespaceAndPath(CoordinateHUD.MODID, "textures/icons/waypoint_overlay.png");

    public static void render(WorldRenderContext context) {
        if (mc.options.hideGui) {
            return;
        }
        if (CoordinateHUD.CLIENT_CONFIG.hideHud.get()) {
            return;
        }

        PoseStack stack = context.matrixStack();

        Vec3 position = mc.gameRenderer.getMainCamera().getPosition();
        Camera mainCamera = mc.gameRenderer.getMainCamera();

        List<Waypoint> activeWaypoints = CoordinateHUD.WAYPOINT_STORE.getActiveWaypoints();

        stack.pushPose();
        stack.translate(-position.x, -position.y, -position.z);

        ResourceLocation currentDimension = null;
        if (mc.level != null) {
            currentDimension = mc.level.dimension().location();
        }

        for (Waypoint waypoint : activeWaypoints) {
            if (!waypoint.getDimension().location().equals(currentDimension)) {
                continue;
            }
            stack.pushPose();
            renderWaypoint(context, mc, stack, mainCamera, waypoint);
            stack.popPose();
        }
        stack.popPose();
    }

    public static void renderWaypoint(WorldRenderContext context, Minecraft minecraft, PoseStack stack, Camera mainCamera, Waypoint waypoint) {
        Font font = minecraft.font;
        Vec3 waypointPos = getFakePos(mainCamera, waypoint.getPos().getCenter());
        stack.translate(waypointPos.x, waypointPos.y, waypointPos.z);
        stack.pushPose();

        stack.mulPose(Axis.YP.rotationDegrees(180F - mainCamera.getYRot()));
        stack.mulPose(Axis.XP.rotationDegrees(-mainCamera.getXRot()));

        VertexConsumer color = context.consumers().getBuffer(RenderType.textSeeThrough(COLOR_LOCATION));
        vertex(color, stack, -0.5F, -0.5F, 0F, 0F, 1F, waypoint.getColor(), 0.5F);
        vertex(color, stack, 0.5F, -0.5F, 0F, 1F, 1F, waypoint.getColor(), 0.5F);
        vertex(color, stack, 0.5F, 0.5F, 0F, 1F, 0F, waypoint.getColor(), 0.5F);
        vertex(color, stack, -0.5F, 0.5F, 0F, 0F, 0F, waypoint.getColor(), 0.5F);

        VertexConsumer overlay = context.consumers().getBuffer(RenderType.textSeeThrough(OVERLAY_LOCATION));
        vertex(overlay, stack, -0.5F, -0.5F, 0F, 0F, 1F, 0xFFFFFF, 0.5F);
        vertex(overlay, stack, 0.5F, -0.5F, 0F, 1F, 1F, 0xFFFFFF, 0.5F);
        vertex(overlay, stack, 0.5F, 0.5F, 0F, 1F, 0F, 0xFFFFFF, 0.5F);
        vertex(overlay, stack, -0.5F, 0.5F, 0F, 0F, 0F, 0xFFFFFF, 0.5F);

        stack.pushPose();
        stack.translate(0D, 1D, 0D);
        stack.mulPose(Axis.XP.rotationDegrees(180F));

        int textWidth = font.width(waypoint.getName());

        float offsetX = (float) (-textWidth / 2);
        float offsetY = (float) (-font.lineHeight / 2);

        float textScale = 0.05F;

        stack.scale(textScale, textScale, textScale);

        if (isLookingAtWaypoint(mainCamera, waypointPos)) {
            MutableComponent name;
            if (CoordinateHUD.CLIENT_CONFIG.showWaypointDistance.get()) {
                name = Component.translatable("message.coordinatehud.waypoint_name_distance", waypoint.getName(), (int) mainCamera.getPosition().distanceTo(waypoint.getPos().getCenter()));
            } else {
                name = Component.translatable("message.coordinatehud.waypoint_name", waypoint.getName());
            }
            name.withStyle(ChatFormatting.WHITE);

            font.drawInBatch(name, offsetX, offsetY, 0, false, stack.last().pose(), context.consumers(), Font.DisplayMode.SEE_THROUGH, 255, LightTexture.FULL_BRIGHT);
            font.drawInBatch(name, offsetX, offsetY, 0, false, stack.last().pose(), context.consumers(), Font.DisplayMode.NORMAL, 255, LightTexture.FULL_BRIGHT);
        }

        stack.popPose();
        stack.popPose();
    }

    private static Vec3 getFakePos(Camera camera, Vec3 pos) {
        Integer waypointDistance = CoordinateHUD.CLIENT_CONFIG.waypointScaleDistance.get();
        Vec3 cameraPos = camera.getPosition();

        if (cameraPos.distanceTo(pos) < waypointDistance) {
            return pos;
        }

        Vec3 dir = pos.subtract(cameraPos).normalize();
        return cameraPos.add(dir.scale(waypointDistance));
    }

    private static boolean isLookingAtWaypoint(Camera camera, Vec3 waypointPos) {
        Integer waypointDistance = CoordinateHUD.CLIENT_CONFIG.waypointScaleDistance.get();
        Vec3 cameraPos = camera.getPosition();
        Vec3 dir = new Vec3(camera.getLookVector().x, camera.getLookVector().y, camera.getLookVector().z);
        Vec3 waypointDir = waypointPos.subtract(cameraPos).normalize();
        double threshold = 0.999D;
        double distance = cameraPos.distanceTo(waypointPos);
        if (distance < waypointDistance) {
            threshold = Mth.lerp(1 - distance / waypointDistance, 0.999D, 0.99D);
        }
        return dir.dot(waypointDir) > threshold;
    }

    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int color, int alpha) {
        vertex(builder, matrixStack, x, y, z, u, v, (color & 0xFFFFFF) | alpha << 24);
    }

    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int color, float alpha) {
        vertex(builder, matrixStack, x, y, z, u, v, color, (int) (alpha * 255F));
    }

    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int color) {
        PoseStack.Pose entry = matrixStack.last();
        builder.addVertex(entry.pose(), x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0F, 0F, -1F);
    }


}
