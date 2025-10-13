package de.maxhenkel.coordinatehud.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.coordinatehud.events.RenderEvents;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "submitBlockEntities", at = @At(value = "RETURN"))
    private void submitBlockEntities(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeStorage submitNodeStorage, CallbackInfo ci) {
        RenderEvents.render(poseStack, levelRenderState, submitNodeStorage);
    }

}
