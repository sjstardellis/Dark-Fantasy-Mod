package net.steve.darkfantasy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.steve.darkfantasy.block.entity.TwilightPortalBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.state.EndPortalRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Reuses vanilla's end-portal pipeline for the twilight portal too — same starfield
 * shader, just dispatched against {@link TwilightPortalBlockEntity}. Visual variation
 * between Skylands and Twilight comes from the surrounding frame / ambient particles,
 * not the portal interior itself.
 */
public class TwilightPortalRenderer extends AbstractEndPortalRenderer<TwilightPortalBlockEntity, EndPortalRenderState> {
    @Override
    public EndPortalRenderState createRenderState() {
        return new EndPortalRenderState();
    }

    @Override
    public void submit(EndPortalRenderState state, PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        submitCube(state.facesToShow, RenderTypes.endPortal(), poseStack, submitNodeCollector);
    }
}
