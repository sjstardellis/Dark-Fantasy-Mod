package net.steve.darkfantasy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.steve.darkfantasy.entity.custom.ElectroDragonEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.dragon.EnderDragonModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Renders the {@link ElectroDragonEntity} using the vanilla {@link EnderDragonModel}.
 * Reuses {@link EnderDragonRenderState} so the model's existing setupAnim path (which
 * reads {@code flapTime} + {@code flightHistory}) just works — we copy those fields
 * over from our entity.
 *
 * <p>Differences from {@link net.minecraft.client.renderer.entity.EnderDragonRenderer}:
 * <ul>
 *   <li>No crystal-beam, dying-rays, or eyes-overlay rendering.</li>
 *   <li>Custom texture path (defaults to vanilla dragon texture so it works without art assets).</li>
 *   <li>No {@code isLandingOrTakingOff} / {@code isSitting} / {@code beamOffset} state —
 *       the head-Y-offset path that uses those is skipped entirely when both flags are false.</li>
 * </ul>
 */
public class ElectroDragonRenderer extends EntityRenderer<ElectroDragonEntity, EnderDragonRenderState> {
    /**
     * Custom texture. UV layout is identical to vanilla
     * {@code assets/minecraft/textures/entity/enderdragon/dragon.png}, so any retexture
     * painted over the vanilla template will line up correctly.
     */
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("darkfantasy", "textures/entity/electrodragon.png");

    private final EnderDragonModel model;

    public ElectroDragonRenderer(EntityRendererProvider.Context context) {
        super(context);
        // Vanilla ender dragon uses 0.5; halved here to match SCALE = 0.5. Note:
        // EntityRenderer (unlike LivingEntityRenderer) doesn't auto-multiply by scale,
        // so this is the final on-ground shadow radius.
        this.shadowRadius = 0.25F;
        this.model = new EnderDragonModel(context.bakeLayer(ModelLayers.ENDER_DRAGON));
    }

    @Override
    public EnderDragonRenderState createRenderState() {
        return new EnderDragonRenderState();
    }

    /**
     * Copy enough state from the entity that {@link EnderDragonModel#setupAnim} produces
     * a believable pose. {@code partialTicks} feeds the interpolated flight-history lookup
     * that bends the neck and tail.
     */
    /**
     * Per-frame scale snapshot. EntityRenderer doesn't read Attributes.SCALE the way
     * LivingEntityRenderer does, so we capture it here and apply it ourselves in submit.
     */
    private float entityScale = 1.0F;

    @Override
    public void extractRenderState(ElectroDragonEntity entity, EnderDragonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flapTime = net.minecraft.util.Mth.lerp(partialTicks, entity.oFlapTime, entity.flapTime);
        state.deathTime = 0.0F;
        state.hasRedOverlay = entity.hurtTime > 0;
        state.beamOffset = null;
        state.isLandingOrTakingOff = false;
        state.isSitting = false;
        state.distanceToEgg = 0.0;
        state.partialTicks = partialTicks;
        state.flightHistory.copyFrom(entity.flightHistory);
        this.entityScale = entity.getScale();
    }

    /**
     * Submit the dragon model. Layout copied from vanilla EnderDragonRenderer.submit
     * but trimmed: no eyes overlay, no crystal beam, no dying ray geometry. The scale
     * here is the dragon's "natural" model scale — entity-level shrinking is handled
     * by {@link ElectroDragonEntity#getScale()} which applies a multiplier in
     * {@link EntityRenderer#submit}.
     */
    @Override
    public void submit(EnderDragonRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        // Apply Attributes.SCALE manually — EntityRenderer (unlike LivingEntityRenderer)
        // doesn't do this for us. Everything below operates in model-local units, so
        // putting the scale at the top of the pose stack shrinks the model, the model's
        // built-in -1.501 ground offset, and the body-yaw/pitch translations uniformly.
        float s = this.entityScale;
        poseStack.scale(s, s, s);
        // Same orientation maths the vanilla renderer uses to align the body with its
        // historical flight path — but with an extra 180° around Y so the model's visual
        // head ends up in the entity's +lookAngle direction. (Vanilla EnderDragonModel
        // was built with the head at model-Z = -62, i.e. facing -Z; vanilla's renderer
        // rotates by -yr which keeps that "backwards" convention. We want the visual
        // front to match the entity's yRot so the AI's LookControl + our mouth-offset
        // particle/projectile origin all line up.)
        float yr = state.getHistoricalPos(7).yRot();
        float bobPitch = (float) (state.getHistoricalPos(5).y() - state.getHistoricalPos(10).y());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yr));
        poseStack.mulPose(Axis.XP.rotationDegrees(bobPitch * 10.0F));
        // Translate sign flipped from vanilla's +1.0 — the 180° Y rotation reverses the
        // local Z axis, so the forward offset needs to invert to keep the body world-
        // position unchanged.
        poseStack.translate(0.0F, 0.0F, -1.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        int overlayCoords = OverlayTexture.pack(0.0F, state.hasRedOverlay);
        submitNodeCollector.submitModel(this.model, state, poseStack, TEXTURE,
                state.lightCoords, overlayCoords, state.outlineColor, null);

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    /** Dragons aren't culled too aggressively — large bounding box, long sweeps. */
    @Override
    protected boolean affectedByCulling(ElectroDragonEntity entity) {
        return false;
    }
}
