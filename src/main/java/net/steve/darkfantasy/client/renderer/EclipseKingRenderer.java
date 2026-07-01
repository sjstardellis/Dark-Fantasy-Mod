package net.steve.darkfantasy.client.renderer;

import net.steve.darkfantasy.client.model.EclipseKingModel;
import net.steve.darkfantasy.entity.custom.EclipseKingEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;

/**
 * Renders the {@link EclipseKingEntity} on the vanilla player rig via {@link EclipseKingModel}
 * (which adds the spell-casting arm wave). A {@link HumanoidArmorLayer} draws his crown, and
 * the item-in-hand layer added by {@link HumanoidMobRenderer} shows his greatsword. Texture is
 * user-supplied at {@code assets/darkfantasy/textures/entity/eclipse_king.png}.
 */
public class EclipseKingRenderer
        extends HumanoidMobRenderer<EclipseKingEntity, EclipseKingRenderState, EclipseKingModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("darkfantasy", "textures/entity/eclipse_king.png");

    public EclipseKingRenderer(EntityRendererProvider.Context context) {
        super(context, new EclipseKingModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        ArmorModelSet<HumanoidModel<EclipseKingRenderState>> armor =
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new);
        this.addLayer(new HumanoidArmorLayer<>(this, armor, context.getEquipmentRenderer()));
    }

    @Override
    public Identifier getTextureLocation(EclipseKingRenderState state) {
        return TEXTURE;
    }

    @Override
    public EclipseKingRenderState createRenderState() {
        return new EclipseKingRenderState();
    }

    @Override
    public void extractRenderState(EclipseKingEntity entity, EclipseKingRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.casting = entity.isCasting();
        state.castTime = entity.tickCount + partialTicks;
    }
}
