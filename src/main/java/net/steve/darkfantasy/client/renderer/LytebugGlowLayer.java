package net.steve.darkfantasy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.bee.AdultBeeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Fullbright emissive overlay that gives the Lytebug its "glow." Re-renders the bee
 * model with {@link RenderType#eyes} using a separate texture ({@code lytebug_glow.png})
 * whose alpha channel masks which parts of the body glow — anywhere alpha>0 in the
 * glow texture will appear lit at light level 15, even in pitch dark.
 *
 * <p>The render pipeline mirrors vanilla {@link net.minecraft.client.renderer.entity.layers.EyesLayer}:
 * we submit the parent model a second time on the additive emissive pass after the
 * base body has drawn.
 *
 * <p>Cost: one extra model submission per lytebug per frame. Negligible.
 *
 * <p>Note: this is pure cosmetic — the world's block-light is NOT modified. If real
 * dynamic illumination is ever wanted, see the discussion in commit history; the
 * cheapest realistic path is integrating with a client-side dynamic lights mod.
 */
public class LytebugGlowLayer extends RenderLayer<BeeRenderState, AdultBeeModel> {
    private static final RenderType GLOW_RENDER_TYPE = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath("darkfantasy", "textures/entity/lytebug_glow.png"));

    public LytebugGlowLayer(RenderLayerParent<BeeRenderState, AdultBeeModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       int lightCoords, BeeRenderState state, float yRot, float xRot) {
        // order(1) draws after the base body pass (order 0). RenderType.eyes already
        // forces fullbright lighting in its shader, so passing lightCoords through is
        // harmless — the shader ignores it.
        submitNodeCollector.order(1)
                .submitModel(this.getParentModel(), state, poseStack, GLOW_RENDER_TYPE,
                        lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }
}
