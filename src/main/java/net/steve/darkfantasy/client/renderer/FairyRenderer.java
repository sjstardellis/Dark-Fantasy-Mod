package net.steve.darkfantasy.client.renderer;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.entity.custom.FairyEntity;
import net.minecraft.client.model.animal.allay.AllayModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/**
 * Renders the {@link FairyEntity} using vanilla's {@link AllayModel} with a re-skinned
 * texture. Pixie-sized scale, full block light so the fairy reads as a small glowing mote
 * even in dim caves.
 */
public class FairyRenderer extends MobRenderer<FairyEntity, AllayRenderState, AllayModel> {
    private static final Identifier FAIRY_TEXTURE =
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "textures/entity/fairy.png");

    public FairyRenderer(EntityRendererProvider.Context context) {
        super(context, new AllayModel(context.bakeLayer(ModelLayers.ALLAY)), 0.4F);
    }

    @Override
    public Identifier getTextureLocation(AllayRenderState state) {
        return FAIRY_TEXTURE;
    }

    @Override
    public AllayRenderState createRenderState() {
        return new AllayRenderState();
    }

    @Override
    protected int getBlockLightLevel(FairyEntity entity, BlockPos blockPos) {
        return 15;
    }
}
