package net.steve.darkfantasy.client.model;

import net.steve.darkfantasy.client.renderer.EclipseKingRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The Eclipse King on the vanilla player mesh, with one addition: while channelling a spell
 * he raises both arms forward-and-up and waves them (a sine bob), tilting his head skyward —
 * a clear telegraph so his spells erupt from a gesture, not out of nowhere. Driven entirely
 * by {@link EclipseKingRenderState#casting}; idle/walk/attack fall through to vanilla.
 */
public class EclipseKingModel extends HumanoidModel<EclipseKingRenderState> {
    public EclipseKingModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(EclipseKingRenderState state) {
        super.setupAnim(state);
        if (!state.casting) {
            return;
        }
        float wave = Mth.cos(state.castTime * 0.35F) * 0.28F;
        this.rightArm.xRot = -2.1F + wave;     // arms thrust forward and up
        this.leftArm.xRot = -2.1F - wave;
        this.rightArm.zRot = 0.30F + wave * 0.6F;   // spread + wave outward
        this.leftArm.zRot = -0.30F - wave * 0.6F;
        this.rightArm.yRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        this.head.xRot = -0.35F;               // gaze lifted skyward
    }
}
