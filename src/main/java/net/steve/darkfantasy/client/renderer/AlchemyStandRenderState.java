package net.steve.darkfantasy.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class AlchemyStandRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public ItemStackRenderState input0 = new ItemStackRenderState();
    public ItemStackRenderState input1 = new ItemStackRenderState();
    public ItemStackRenderState input2 = new ItemStackRenderState();
    public ItemStackRenderState output = new ItemStackRenderState();
    public int lavaAmount;
    public int tankCapacity = 1;
}
