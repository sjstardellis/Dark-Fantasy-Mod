package net.steve.darkfantasy.client.renderer;

import net.steve.darkfantasy.client.model.GnomeModel;
import net.steve.darkfantasy.entity.custom.GnomeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renders the {@link GnomeEntity} using the custom {@link GnomeModel} (Blockbench-
 * authored: gnome-with-beard variant of the witch). Reuses {@link WitchRenderState}
 * since the model only reads fields that exist on the witch state (head rotation,
 * walk animation, ageInTicks).
 *
 * <p>Model scaling is handled by {@link Attributes#SCALE} on the entity via
 * {@link net.minecraft.client.renderer.entity.LivingEntityRenderer#submit}.
 */
public class GnomeRenderer extends MobRenderer<GnomeEntity, WitchRenderState, GnomeModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("darkfantasy", "textures/entity/gnome.png");

    public GnomeRenderer(EntityRendererProvider.Context context) {
        // Tiny shadow — the entity is small (SCALE 0.5 in GnomeEntity), and the
        // default 0.5 shadow looks oversized against a half-scale model.
        super(context, new GnomeModel(context.bakeLayer(GnomeModel.LAYER_LOCATION)), 0.15F);
    }

    @Override
    public Identifier getTextureLocation(WitchRenderState state) {
        return TEXTURE;
    }

    @Override
    public WitchRenderState createRenderState() {
        return new WitchRenderState();
    }

    @Override
    public void extractRenderState(GnomeEntity entity, WitchRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        // entityId would drive the witch nose-wiggle but our custom model has no
        // nose part, so it's read-but-ignored. Held-item flags stay false — gnomes
        // don't visually render the splash potion they're about to throw.
        state.entityId = entity.getId();
        state.isHoldingItem = false;
        state.isHoldingPotion = false;
    }
}
