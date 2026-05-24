package net.steve.darkfantasy.client.renderer;

import net.steve.darkfantasy.entity.custom.GoblinEntity;
import net.minecraft.client.model.animal.golem.CopperGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.WeatheringCopper;

/**
 * Renders the {@link GoblinEntity} using the vanilla {@link CopperGolemModel}.
 *
 * <p>Reusing {@link CopperGolemRenderState} directly means the model's {@code setupAnim}
 * path (which reads idle / interaction animation states) just works with empty defaults
 * — we only populate the basics here. None of the copper-golem-specific layers
 * (eye glow, hand item, antenna block, custom head) are added, so a goblin renders as
 * just the bare model with our custom texture.
 *
 * <p>The {@link Attributes#SCALE} attribute on the entity (0.7) handles model
 * downscaling automatically via {@link net.minecraft.client.renderer.entity.LivingEntityRenderer#submit}.
 */
public class GoblinRenderer extends MobRenderer<GoblinEntity, CopperGolemRenderState, CopperGolemModel> {
    /**
     * Custom texture. UV layout is identical to vanilla
     * {@code assets/minecraft/textures/entity/copper_golem/copper_golem_unaffected.png},
     * so any retexture painted over the vanilla template will line up correctly.
     */
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("darkfantasy", "textures/entity/goblin.png");

    public GoblinRenderer(EntityRendererProvider.Context context) {
        super(context, new CopperGolemModel(context.bakeLayer(ModelLayers.COPPER_GOLEM)), 0.3F);
    }

    @Override
    public Identifier getTextureLocation(CopperGolemRenderState state) {
        return TEXTURE;
    }

    @Override
    public CopperGolemRenderState createRenderState() {
        return new CopperGolemRenderState();
    }

    @Override
    public void extractRenderState(GoblinEntity entity, CopperGolemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        // CopperGolemRenderState defaults: weathering=UNAFFECTED (we ignore for our
        // own texture path), copperGolemState=IDLE (drives no interaction anim since
        // those states are empty), idle/interaction AnimationStates stay empty so
        // the model's setupAnim no-ops those branches. Walk animation is driven by
        // the LivingEntityRenderState fields (walkAnimationPos, walkAnimationSpeed)
        // which super.extractRenderState already filled.
        state.weathering = WeatheringCopper.WeatherState.UNAFFECTED;
    }
}
