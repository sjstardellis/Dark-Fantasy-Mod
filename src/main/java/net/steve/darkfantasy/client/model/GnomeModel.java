package net.steve.darkfantasy.client.model;

import net.steve.darkfantasy.DarkFantasy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Blockbench-authored gnome model. Geometry was exported from Blockbench in legacy
 * 1.17 format and ported to the current 1.21+ API: modern {@link EntityModel} generic
 * (parameterized on {@link WitchRenderState}), modern {@link #setupAnim} signature
 * (single state arg, no manual rotation params), and {@link Identifier} for the layer
 * location.
 *
 * <p>Reuses {@link WitchRenderState} so {@link net.steve.darkfantasy.client.renderer.GnomeRenderer}
 * doesn't need to define a custom state — all the fields this model reads
 * ({@code xRot}, {@code yRot}, {@code walkAnimationPos}, {@code walkAnimationSpeed},
 * {@code ageInTicks}) come from {@code LivingEntityRenderState} which {@code WitchRenderState}
 * inherits.
 *
 * <h2>Head-attachment fix</h2>
 * The Blockbench export had {@code beard}, {@code headwear}, and {@code headwear2}
 * as <em>siblings</em> of {@code head} — when the head rotated to track a player
 * via {@code LookAtPlayerGoal}, the hat and beard stayed put (or swung wildly
 * around their own off-center pivots). The fix applied here re-parents them
 * under {@code head} in {@link #createBodyLayer} so they inherit head rotation
 * automatically. PartPose offsets are unchanged because {@code head} itself is at
 * the origin, so world positions stay the same.
 */
public class GnomeModel extends EntityModel<WitchRenderState> implements HeadedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "gnome"), "main");

    private final ModelPart head;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public GnomeModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        // beard, headwear, headwear2 are now nested under head (see createBodyLayer)
        // so they inherit head rotations natively — we keep field references only
        // for parts whose pose we mutate; these don't need mutating any more.
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    /**
     * Geometry copied verbatim from the Blockbench export (only the texture-offset
     * numbers and box dimensions matter for visuals — those are unchanged).
     * Texture is 64×128.
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Head is now the parent of beard/headwear/headwear2 so they swing along when
        // the gnome looks around. PartPose offsets were authored relative to root (0,0,0)
        // in Blockbench; since head itself sits at (0,0,0), nesting them under head
        // doesn't move them — their pivots stay in the exact same world positions.
        PartDefinition head = partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("beard",
                CubeListBuilder.create()
                        .texOffs(46, 0).addBox(-3.5F, 0.0F, -5.0F, 7.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(50, 1).addBox(-2.0F, 5.0F, -4.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition headwear = head.addOrReplaceChild("headwear",
                CubeListBuilder.create()
                        .texOffs(0, 64).addBox(0.0F, 0.0F, 0.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, -10.05F, -5.0F));

        PartDefinition hat2 = headwear.addOrReplaceChild("hat2",
                CubeListBuilder.create()
                        .texOffs(0, 76).addBox(0.0F, 0.0F, 0.0F, 7.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.0524F, 0.0F, 0.0262F));

        PartDefinition hat3 = hat2.addOrReplaceChild("hat3",
                CubeListBuilder.create()
                        .texOffs(0, 87).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.1047F, 0.0F, 0.0524F));

        hat3.addOrReplaceChild("hat4",
                CubeListBuilder.create()
                        .texOffs(0, 95).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(1.75F, -2.0F, 2.0F, -0.2094F, 0.0F, 0.1047F));

        head.addOrReplaceChild("headwear2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("bodywear",
                CubeListBuilder.create()
                        .texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 20.0F, 6.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition arms = partdefinition.addOrReplaceChild("arms",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 2.95F, -1.05F, -0.7505F, 0.0F, 0.0F));

        PartDefinition mirrored = arms.addOrReplaceChild("mirrored",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 21.05F, 1.05F));

        // Both arms share a single 16×16 UV island at (44, 18) → (59, 33). The
        // mirrored side uses .mirror() so the painted left arm appears correctly
        // on the right side of the model (vanilla witch's symmetric-arms pattern).
        // If you want them to look truly identical (not mirrored), drop the
        // .mirror() / .mirror(false) calls on the second addBox.
        mirrored.addOrReplaceChild("mirrored2",
                CubeListBuilder.create()
                        .texOffs(44, 18).addBox(-8.0F, -18.05F, 12.95F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(44, 18).mirror().addBox(4.0F, -18.05F, 12.95F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7418F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    @Override
    public void setupAnim(WitchRenderState state) {
        super.setupAnim(state);

        // Head look-at: convert degrees → radians (vanilla convention). Because
        // beard/headwear/headwear2 are now nested under head in createBodyLayer,
        // they pick up these rotations automatically — no manual copy needed.
        this.head.yRot = state.yRot * (float) (Math.PI / 180.0);
        this.head.xRot = state.xRot * (float) (Math.PI / 180.0);

        // Leg walk cycle — same coefficients as vanilla WitchModel.
        this.rightLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F)
                * 1.4F * state.walkAnimationSpeed * 0.5F;
        this.leftLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + (float) Math.PI)
                * 1.4F * state.walkAnimationSpeed * 0.5F;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }
}
