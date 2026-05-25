package net.steve.darkfantasy.client.renderer;

import net.steve.darkfantasy.entity.custom.LytebugEntity;
import net.minecraft.client.model.animal.bee.AdultBeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renders the Lytebug with the vanilla {@link AdultBeeModel} retextured via
 * {@link #BODY_TEXTURE}, plus a {@link LytebugGlowLayer} for the emissive glow.
 *
 * <p>Reuses {@link BeeRenderState} — the bee state fields the model actually reads
 * ({@code ageInTicks}, {@code isOnGround}) are populated by the parent
 * {@code MobRenderer.extractRenderState} from {@link LivingEntityRenderState}. The
 * bee-specific fields ({@code hasNectar}, {@code isAngry}, {@code hasStinger}) are
 * forced to their "neutral" values in {@link #extractRenderState} so the bee model's
 * angry / pollen pose code stays disabled.
 *
 * <p>No new model layer is registered: we bake the vanilla {@link ModelLayers#BEE}
 * directly. Texture-only re-skinning.
 */
public class LytebugRenderer extends MobRenderer<LytebugEntity, BeeRenderState, AdultBeeModel> {
    private static final Identifier BODY_TEXTURE =
            Identifier.fromNamespaceAndPath("darkfantasy", "textures/entity/lytebug.png");

    public LytebugRenderer(EntityRendererProvider.Context context) {
        // Shadow radius 0.2 — small to match the SCALE 0.6 hitbox. Vanilla bee uses 0.4.
        super(context, new AdultBeeModel(context.bakeLayer(ModelLayers.BEE)), 0.2F);
        this.addLayer(new LytebugGlowLayer(this));
    }

    @Override
    public Identifier getTextureLocation(BeeRenderState state) {
        return BODY_TEXTURE;
    }

    @Override
    public BeeRenderState createRenderState() {
        return new BeeRenderState();
    }

    @Override
    public void extractRenderState(LytebugEntity entity, BeeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        // Disable all bee-specific cosmetic features. Lytebug doesn't sting, doesn't
        // carry nectar, doesn't get angry.
        state.hasStinger = false;
        state.hasNectar = false;
        state.isAngry = false;
        state.rollAmount = 0.0F;
        // isOnGround controls the bee's wing-still pose; pull from entity directly.
        state.isOnGround = entity.onGround() && entity.getDeltaMovement().lengthSqr() < 1.0E-7;
    }
}
