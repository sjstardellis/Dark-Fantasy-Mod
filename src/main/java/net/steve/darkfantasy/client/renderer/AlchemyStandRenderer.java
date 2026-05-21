package net.steve.darkfantasy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.steve.darkfantasy.block.custom.AlchemyStandBlock;
import net.steve.darkfantasy.block.entity.AlchemyStandBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;


/**
 * Renders the items in the alchemy stand's three input slots + output slot, plus the lava
 * surface inside the cauldron, at the positions defined by the INPUTRENDER#/OUTPUTRENDER/
 * FLUIDRENDER marker cubes in the Blockbench source model. The whole render rig rotates
 * with the block's FACING property so items stay on the correct corners after placement.
 */
public class AlchemyStandRenderer implements BlockEntityRenderer<AlchemyStandBlockEntity, AlchemyStandRenderState> {
    // The lava still texture file (frame 0 only — we don't animate this overlay).
    private static final Identifier LAVA_STILL_TEXTURE =
            Identifier.withDefaultNamespace("textures/block/lava_still.png");
    // Source PNG is 16x512 — one frame is 1/32 of the height.
    private static final float LAVA_FRAME_V = 1.0f / 32.0f;

    // Marker positions in 1/16ths (Blockbench coords) — centers of the marker cubes.
    // Default FACING is NORTH; rotation around the block center handles other facings.
    // Slot 0 → INPUTRENDER1 (east edge), slot 1 → INPUTRENDER2 (west), slot 2 → INPUTRENDER3 (north).
    private static final float[] INPUT0_POS = {12.5f, 14.3f, 8.0f};
    private static final float[] INPUT1_POS = {3.5f,  14.3f, 8.0f};
    private static final float[] INPUT2_POS = {8.0f,  14.3f, 3.5f};
    private static final float[] OUTPUT_POS = {8.0f,  14.3f, 8.0f};


    // FLUIDRENDER cube bounds in 1/16ths.
    private static final float FLUID_X0 = 6.0f, FLUID_X1 = 10.0f;
    private static final float FLUID_Z0 = 11.0f, FLUID_Z1 = 13.0f;
    private static final float FLUID_Y_MIN = 14.5f, FLUID_Y_MAX = 15.85f;

    private static final float ITEM_SCALE = 0.28f;

    private final ItemModelResolver itemModelResolver;

    public AlchemyStandRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public AlchemyStandRenderState createRenderState() {
        return new AlchemyStandRenderState();
    }

    @Override
    public void extractRenderState(AlchemyStandBlockEntity be, AlchemyStandRenderState state,
                                   float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.facing = be.getBlockState().getValue(AlchemyStandBlock.FACING);
        state.lavaAmount = be.getLavaAmount();
        state.tankCapacity = AlchemyStandBlockEntity.TANK_CAPACITY;
        int seed = (int) be.getBlockPos().asLong();
        resolveItem(state.input0, be.getItem(AlchemyStandBlockEntity.INPUT_SLOT_0), seed);
        resolveItem(state.input1, be.getItem(AlchemyStandBlockEntity.INPUT_SLOT_1), seed + 1);
        resolveItem(state.input2, be.getItem(AlchemyStandBlockEntity.INPUT_SLOT_2), seed + 2);
        resolveItem(state.output, be.getItem(AlchemyStandBlockEntity.OUTPUT_SLOT), seed + 3);
    }

    private void resolveItem(ItemStackRenderState target, ItemStack stack, int seed) {
        target.clear();
        if (!stack.isEmpty()) {
            this.itemModelResolver.updateForTopItem(target, stack, ItemDisplayContext.FIXED, null, null, seed);
        }
    }

    @Override
    public void submit(AlchemyStandRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        // Rotate the rig around the block's vertical center to follow FACING. Matches the
        // blockstate Y rotation (north=0, east=90, south=180, west=270) — Direction.toYRot()
        // alone is 180° off because it uses south=0 as its zero point.
        poseStack.translate(0.5f, 0.0f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - state.facing.toYRot()));
        poseStack.translate(-0.5f, 0.0f, -0.5f);

        submitItem(state.input0, INPUT0_POS, poseStack, collector, state.lightCoords);
        submitItem(state.input1, INPUT1_POS, poseStack, collector, state.lightCoords);
        submitItem(state.input2, INPUT2_POS, poseStack, collector, state.lightCoords);
        submitItem(state.output, OUTPUT_POS, poseStack, collector, state.lightCoords);
        submitLavaSurface(state, poseStack, collector);

        poseStack.popPose();
    }

    private void submitItem(ItemStackRenderState item, float[] pos,
                            PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (item.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(pos[0] / 16.0f, pos[1] / 16.0f, pos[2] / 16.0f);
        // Lay the item flat (top face up), then spin 180° so its "up" points back toward the
        // player viewing the front of the block instead of away from them.
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    /** Draws the lava surface inside the cauldron — a horizontal quad whose height scales with fill. */
    private void submitLavaSurface(AlchemyStandRenderState state, PoseStack poseStack,
                                   SubmitNodeCollector collector) {
        if (state.lavaAmount <= 0) return;

        float fill = Math.min(1.0f, state.lavaAmount / (float) Math.max(1, state.tankCapacity));
        float y = (FLUID_Y_MIN + (FLUID_Y_MAX - FLUID_Y_MIN) * fill) / 16.0f;
        float x0 = FLUID_X0 / 16.0f, x1 = FLUID_X1 / 16.0f;
        float z0 = FLUID_Z0 / 16.0f, z1 = FLUID_Z1 / 16.0f;
        // First frame of the still texture (top 1/32 of the PNG).
        float v0 = 0.0f, v1 = LAVA_FRAME_V;
        float u0 = 0.0f, u1 = 1.0f;
        int fullBright = LightCoordsUtil.FULL_BRIGHT;

        collector.submitCustomGeometry(poseStack, RenderTypes.entitySolid(LAVA_STILL_TEXTURE),
                (pose, buffer) -> {
                    buffer.addVertex(pose, x0, y, z1).setColor(0xFFFFFFFF).setUv(u0, v1)
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright)
                            .setNormal(pose, 0.0f, 1.0f, 0.0f);
                    buffer.addVertex(pose, x1, y, z1).setColor(0xFFFFFFFF).setUv(u1, v1)
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright)
                            .setNormal(pose, 0.0f, 1.0f, 0.0f);
                    buffer.addVertex(pose, x1, y, z0).setColor(0xFFFFFFFF).setUv(u1, v0)
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright)
                            .setNormal(pose, 0.0f, 1.0f, 0.0f);
                    buffer.addVertex(pose, x0, y, z0).setColor(0xFFFFFFFF).setUv(u0, v0)
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright)
                            .setNormal(pose, 0.0f, 1.0f, 0.0f);
                });
    }
}
