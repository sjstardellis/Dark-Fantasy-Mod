package net.steve.darkfantasy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Animates a worn armour set whose layer texture has been split into per-frame files
 * ({@code <material>_0.png … _<frames-1>.png}, each a single 64×32 frame, sitting next
 * to the base {@code <material>.png}).
 *
 * <p>Worn-armour layer textures are bound directly (see {@code EquipmentLayerRenderer}'s
 * {@code armorCutoutNoCull}), not stitched into a texture atlas, so the usual {@code .mcmeta}
 * frame animation is ignored for them. Instead we pick the current frame every render via
 * NeoForge's {@link #getArmorTexture} hook (called from {@code ClientHooks.getArmorTexture}).
 *
 * <p>The sequence ping-pongs — {@code 0,1,…,n-1,n-2,…,1} then back to 0 — so it eases back
 * instead of snapping from the last frame to the first. One instance is registered per
 * material in {@code DarkFantasyClient} (dawnmetal, eclipsium, …).
 */
public final class AnimatedArmorExtensions implements IClientItemExtensions {
    private final String suffix;          // "<material>.png"
    private final int frames;
    private final int ticksPerFrame;

    /**
     * @param material      texture base name, e.g. {@code "eclipsium"}
     * @param frames        number of split frame files ({@code _0 … _frames-1})
     * @param ticksPerFrame ticks each frame is held (6 ≈ a ~3s ping-pong round trip at 6 frames)
     */
    public AnimatedArmorExtensions(String material, int frames, int ticksPerFrame) {
        this.suffix = material + ".png";
        this.frames = frames;
        this.ticksPerFrame = ticksPerFrame;
    }

    @Override
    public Identifier getArmorTexture(ItemStack stack, EquipmentClientInfo.LayerType type,
                                      EquipmentClientInfo.Layer layer, Identifier base) {
        String path = base.getPath();
        if (!path.endsWith(suffix)) {
            return base;   // not this material's base layer (e.g. a dyeable overlay) — leave it
        }
        var level = Minecraft.getInstance().level;
        long time = level != null ? level.getGameTime() : 0L;
        int period = 2 * (frames - 1);                       // ping-pong period
        int step = (int) ((time / ticksPerFrame) % period);
        int frame = step < frames ? step : period - step;    // triangle: up then back down
        String framed = path.substring(0, path.length() - ".png".length()) + "_" + frame + ".png";
        return Identifier.fromNamespaceAndPath(base.getNamespace(), framed);
    }
}
