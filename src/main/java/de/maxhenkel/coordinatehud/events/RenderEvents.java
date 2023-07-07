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

    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(CoordinateHUD.MODID, "textures/icons/waypoint.png");

    public static void render(WorldRenderContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        PoseStack stack = context.matrixStack();

        Vec3 position = minecraft.gameRenderer.getMainCamera().getPosition();
        Camera mainCamera = minecraft.gameRenderer.getMainCamera();

        List<Waypoint> activeWaypoints = CoordinateHUD.WAYPOINT_STORE.getActiveWaypoints();

        stack.pushPose();
        stack.translate(-position.x, -position.y, -position.z);

        ResourceLocation currentDimension = null;
        if (minecraft.level != null) {
            currentDimension = minecraft.level.dimension().location();
        }

        for (Waypoint waypoint : activeWaypoints) {
            if (!waypoint.getDimension().equals(currentDimension)) {
                continue;
            }
            stack.pushPose();
            renderWaypoint(context, minecraft, stack, mainCamera, waypoint);
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

        int alpha = 128;
        VertexConsumer builderSeeThrough = context.consumers().getBuffer(RenderType.textSeeThrough(TEXTURE_LOCATION));
        vertex(builderSeeThrough, stack, -0.5F, -0.5F, 0F, 0F, 1F, alpha);
        vertex(builderSeeThrough, stack, 0.5F, -0.5F, 0F, 1F, 1F, alpha);
        vertex(builderSeeThrough, stack, 0.5F, 0.5F, 0F, 1F, 0F, alpha);
        vertex(builderSeeThrough, stack, -0.5F, 0.5F, 0F, 0F, 0F, alpha);

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

    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v) {
        vertex(builder, matrixStack, x, y, z, u, v, 255);
    }

    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int alpha) {
        PoseStack.Pose entry = matrixStack.last();
        builder.vertex(entry.pose(), x, y, z)
                .color(255, 255, 255, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(entry.normal(), 0F, 0F, -1F)
                .endVertex();
    }


}
