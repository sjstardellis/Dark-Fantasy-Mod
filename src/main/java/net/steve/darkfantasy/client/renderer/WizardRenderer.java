package net.steve.darkfantasy.client.renderer;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.entity.custom.WizardEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renders the {@link WizardEntity} on the vanilla illager model rig (so we get the
 * arms-up casting pose, crossed-idle pose, and walk animation for free). The model
 * layer is reused from the Evoker — same bone structure — but textured with our own
 * wizard skin.
 *
 * <p>The state object is the existing {@link EvokerRenderState} so we can flag the
 * casting pose without inventing a new state class.
 */
public class WizardRenderer extends IllagerRenderer<WizardEntity, EvokerRenderState> {
    private static final Identifier WIZARD_TEXTURE =
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "textures/entity/wizard.png");

    public WizardRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.EVOKER)), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(EvokerRenderState state) {
        return WIZARD_TEXTURE;
    }

    @Override
    public EvokerRenderState createRenderState() {
        return new EvokerRenderState();
    }

    @Override
    public void extractRenderState(WizardEntity entity, EvokerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isCastingSpell = entity.isCastingSpell();
    }
}
