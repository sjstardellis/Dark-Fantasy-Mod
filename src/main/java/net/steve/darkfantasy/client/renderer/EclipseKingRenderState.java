package net.steve.darkfantasy.client.renderer;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Render state for the Eclipse King. Adds the synced casting flag (drives the raised, waving
 * arm pose in {@link net.steve.darkfantasy.client.model.EclipseKingModel}) plus a continuous
 * time value to animate the wave.
 */
public class EclipseKingRenderState extends HumanoidRenderState {
    public boolean casting;
    public float castTime;
}
