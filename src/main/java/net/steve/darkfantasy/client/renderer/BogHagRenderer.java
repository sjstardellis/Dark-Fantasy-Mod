package net.steve.darkfantasy.client.renderer;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.entity.custom.BogHagEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renders the {@link BogHagEntity} on the vanilla witch rig with a marsh-rotten skin.
 * Mirrors the vanilla witch state extraction (nose wiggle via entityId, held-potion
 * pose) so the hag reads exactly like a witch that has spent too long in the bog.
 */
public class BogHagRenderer extends MobRenderer<BogHagEntity, WitchRenderState, WitchModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "textures/entity/bog_hag.png");

    public BogHagRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchModel(context.bakeLayer(ModelLayers.WITCH)), 0.5F);
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
    public void extractRenderState(BogHagEntity entity, WitchRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.entityId = entity.getId();
        state.isHoldingItem = !entity.getMainHandItem().isEmpty();
        state.isHoldingPotion = entity.isDrinkingPotion();
    }
}
