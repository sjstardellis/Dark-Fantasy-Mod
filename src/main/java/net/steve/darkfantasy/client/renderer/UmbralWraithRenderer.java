package net.steve.darkfantasy.client.renderer;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.entity.custom.UmbralWraithEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renders the {@link UmbralWraithEntity} on the vanilla illager rig — the long-robed
 * silhouette reads as a hooded shade once wrapped in the near-black umbral texture.
 * (Plain {@link MobRenderer} rather than IllagerRenderer, whose type bound demands an
 * actual AbstractIllager.) The ink-smoke it sheds comes from the entity's own
 * client-side particles, not a layer.
 */
public class UmbralWraithRenderer extends MobRenderer<UmbralWraithEntity, EvokerRenderState, IllagerModel<EvokerRenderState>> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "textures/entity/umbral_wraith.png");

    public UmbralWraithRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.EVOKER)), 0.4F);
    }

    @Override
    public Identifier getTextureLocation(EvokerRenderState state) {
        return TEXTURE;
    }

    @Override
    public EvokerRenderState createRenderState() {
        return new EvokerRenderState();
    }
}
