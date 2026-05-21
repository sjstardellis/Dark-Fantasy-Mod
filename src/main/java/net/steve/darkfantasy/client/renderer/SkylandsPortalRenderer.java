package net.steve.darkfantasy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.steve.darkfantasy.block.entity.SkylandsPortalBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.state.EndPortalRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Reuses vanilla's end-portal rendering pipeline to give the Skylands portal that
 * starfield parallax effect. The vertical portal faces are controlled by
 * {@link SkylandsPortalBlockEntity#shouldRenderFace}.
 */
public class SkylandsPortalRenderer extends AbstractEndPortalRenderer<SkylandsPortalBlockEntity, EndPortalRenderState> {
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
