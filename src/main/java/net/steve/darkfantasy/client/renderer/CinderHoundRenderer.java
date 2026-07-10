package net.steve.darkfantasy.client.renderer;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.entity.custom.CinderHoundEntity;
import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.animal.wolf.BabyWolfModel;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renders the {@link CinderHoundEntity} on the vanilla wolf rig (adult + baby layers,
 * collar layer for tamed hounds) with a fixed ember-coat texture instead of the wolf
 * variant system. State extraction mirrors vanilla {@code WolfRenderer} so tail wag,
 * head tilt, shake and sitting poses all carry over.
 */
public class CinderHoundRenderer extends AgeableMobRenderer<CinderHoundEntity, WolfRenderState, WolfModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "textures/entity/cinder_hound.png");

    public CinderHoundRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultWolfModel(context.bakeLayer(ModelLayers.WOLF)),
                new BabyWolfModel(context.bakeLayer(ModelLayers.WOLF_BABY)), 0.5F);
        this.addLayer(new WolfCollarLayer(this));
    }

    @Override
    public Identifier getTextureLocation(WolfRenderState state) {
        return TEXTURE;
    }

    @Override
    public WolfRenderState createRenderState() {
        return new WolfRenderState();
    }

    @Override
    public void extractRenderState(CinderHoundEntity entity, WolfRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAngry = entity.isAngry();
        state.isSitting = entity.isInSittingPose();
        state.tailAngle = entity.getTailAngle();
        state.headRollAngle = entity.getHeadRollAngle(partialTicks);
        state.shakeAnim = entity.getShakeAnim(partialTicks);
        state.texture = TEXTURE;
        state.wetShade = entity.getWetShade(partialTicks);
        state.collarColor = entity.isTame() ? entity.getCollarColor() : null;
        state.bodyArmorItem = entity.getBodyArmorItem().copy();
    }
}
