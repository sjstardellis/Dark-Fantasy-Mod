package net.steve.darkfantasy.client.screen;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.item.ModItems;
import net.steve.darkfantasy.menu.BrewingKegMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Screen for the brewing keg.
 *
 * <p>The background PNG provides the static frame. This class layers the dynamic bits:
 * <ol>
 *   <li>{@link #renderBrewingOverlay} draws the bubbles + brew-progress arrow, copied
 *       verbatim from the vanilla brewing stand at its exact coordinates.</li>
 *   <li>{@link #renderWaterTank} fills the tank well from the bottom up, tinted to the
 *       current brew's colour.</li>
 *   <li>{@link #renderCountdownTimer} shows the time left on the current batch.</li>
 * </ol>
 * The three input slots sit on the brewing stand's bottle arc — (56,51)/(79,51)/(102,51).
 */
public class BrewingKegScreen extends AbstractContainerScreen<BrewingKegMenu> {
    private static final Identifier BACKGROUND_LOCATION =
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "textures/gui/brewing_keg.png");

    /** Vanilla water-still texture: 16 px wide × 512 px tall (32 frames). */
    private static final Identifier WATER_STILL =
            Identifier.withDefaultNamespace("textures/block/water_still.png");
    private static final int WATER_FRAME_SIZE = 16;
    private static final int WATER_TEX_WIDTH = 16;
    private static final int WATER_TEX_HEIGHT = 512;

    // Tank rectangle within the GUI background. Tall vertical bar on the left side —
    // same coords as the alchemy stand so the two GUIs share a visual language.
    private static final int TANK_X = 150;
    private static final int TANK_Y = 18;
    private static final int TANK_W = 14;
    private static final int TANK_H = 50;

    // Bubbles + brew-progress arrow — copied 1:1 from the vanilla brewing stand
    // (BrewingStandScreen): same sprites, sprite sizes, and exact coordinates.
    //   arrow   → sprite 9×28 at (97,16), fills top → bottom
    //   bubbles → sprite 12×29 at (63,14), rises from the bottom
    private static final Identifier BREW_PROGRESS_SPRITE =
            Identifier.withDefaultNamespace("container/brewing_stand/brew_progress");
    private static final Identifier BUBBLES_SPRITE =
            Identifier.withDefaultNamespace("container/brewing_stand/bubbles");
    private static final int[] BUBBLELENGTHS = {29, 24, 20, 16, 11, 6, 0};

    // Countdown timer (MM:SS) — drawn only while brewing.
    private static final int TIMER_CENTER_X = 87;
    private static final int TIMER_Y = 25;
    private static final int TIMER_COLOR = 0xFF444444;

    public BrewingKegScreen(BrewingKegMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_LOCATION,
                xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        renderWaterTank(graphics, xo, yo);
        renderBrewingOverlay(graphics, xo, yo);
        renderCountdownTimer(graphics, xo, yo);
    }

    /**
     * Countdown timer below the progress arrow. Shown only while a batch is actively
     * cooking (progress > 0) so the GUI isn't cluttered with "5:00" when the keg is
     * idle. Remaining ticks ÷ 20 = remaining seconds; formatted as M:SS.
     */
    private void renderCountdownTimer(GuiGraphicsExtractor graphics, int guiX, int guiY) {
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        if (progress <= 0 || maxProgress <= 0) return;

        int remainingTicks = Math.max(0, maxProgress - progress);
        int remainingSeconds = (remainingTicks + 19) / 20; // round up so we never show 0:00 mid-cook
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String text = String.format("%d:%02d", minutes, seconds);

        int textWidth = this.font.width(text);
        int x = guiX + TIMER_CENTER_X - textWidth / 2;
        int y = guiY + TIMER_Y;
        graphics.text(this.font, text, x, y, TIMER_COLOR, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        // Hovering over the tank shows "Beer: <amount> / <capacity> mB".
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        int tankLeft = xo + TANK_X;
        int tankTop = yo + TANK_Y;
        if (mouseX >= tankLeft && mouseX < tankLeft + TANK_W
                && mouseY >= tankTop && mouseY < tankTop + TANK_H) {
            int beer = this.menu.getBeerAmount();
            int capacity = this.menu.getTankCapacity();
            ItemStack brew = this.menu.currentBrew();
            Component title = brew.isEmpty()
                    ? Component.translatable("tooltip.darkfantasy.brewing_keg.tank")
                    : brew.getHoverName();
            List<Component> lines = List.of(
                    title,
                    Component.translatable("tooltip.darkfantasy.brewing_keg.tank.amount", beer, capacity)
            );
            graphics.setTooltipForNextFrame(
                    this.font,
                    lines.stream().map(Component::getVisualOrderText).toList(),
                    mouseX, mouseY);
        }
    }

    /**
     * Bubbles + brew-progress arrow, copied from the vanilla brewing stand. Shown only
     * while a batch is actively cooking (progress &gt; 0). The keg's progress counts UP
     * to maxProgress, so the arrow fills as {@code progress/maxProgress} (vanilla runs a
     * tick timer down instead); the bubbles cycle through {@link #BUBBLELENGTHS}.
     */
    private void renderBrewingOverlay(GuiGraphicsExtractor graphics, int guiX, int guiY) {
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        if (progress <= 0 || maxProgress <= 0) return;

        // Brew-progress arrow — sprite 9×28 at (97,16), fills top → bottom.
        int length = Math.min(28, (int) (28.0F * progress / maxProgress));
        if (length > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BREW_PROGRESS_SPRITE,
                    9, 28, 0, 0, guiX + 105, guiY + 16, 9, length);
        }

        // Bubbles — sprite 12×29 at (63,14), rising from the bottom.
        length = BUBBLELENGTHS[progress / 2 % 7];
        if (length > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUBBLES_SPRITE,
                    12, 29, 0, 29 - length, guiX + 57, guiY + 14 + 29 - length, 12, length);
        }
    }

    /**
     * Fill the tank rect from the bottom up with tiled water-still frames. We always
     * sample frame 0 (the top 16 rows) — the animation comes from the texture's
     * existing GIF-style scrolling done at the texture-atlas level, not from us
     * picking different rows here. The partial tile at the top samples from the
     * upper portion of frame 0 so the "surface" reads as the water's top edge.
     */
    private void renderWaterTank(GuiGraphicsExtractor graphics, int guiX, int guiY) {
        int beer = this.menu.getBeerAmount();
        if (beer <= 0) return;

        int capacity = this.menu.getTankCapacity();
        int filledHeight = Math.max(1, (int) ((long) beer * TANK_H / capacity));
        filledHeight = Math.min(filledHeight, TANK_H);

        int x = guiX + TANK_X;
        int barBottom = guiY + TANK_Y + TANK_H;

        int fullTiles = filledHeight / WATER_FRAME_SIZE;
        int remainder = filledHeight % WATER_FRAME_SIZE;

        for (int i = 0; i < fullTiles; i++) {
            int tileY = barBottom - (i + 1) * WATER_FRAME_SIZE;
            graphics.blit(RenderPipelines.GUI_TEXTURED, WATER_STILL,
                    x, tileY, 0.0F, 0.0F,
                    TANK_W, WATER_FRAME_SIZE,
                    WATER_FRAME_SIZE, WATER_FRAME_SIZE,
                    WATER_TEX_WIDTH, WATER_TEX_HEIGHT);
        }

        if (remainder > 0) {
            int tileY = barBottom - fullTiles * WATER_FRAME_SIZE - remainder;
            float v = WATER_FRAME_SIZE - remainder;
            graphics.blit(RenderPipelines.GUI_TEXTURED, WATER_STILL,
                    x, tileY, 0.0F, v,
                    TANK_W, remainder,
                    WATER_FRAME_SIZE, remainder,
                    WATER_TEX_WIDTH, WATER_TEX_HEIGHT);
        }

        // Tint the fill toward the current brew's colour (beer amber, dark stout, cyan
        // glowbrew, …). A translucent overlay leaves the animated water shimmer visible.
        int fillTop = barBottom - filledHeight;
        graphics.fill(x, fillTop, x + TANK_W, barBottom, 0xC0000000 | brewTint());
    }

    /** Tint colour (0xRRGGBB) for the tank fill, from whichever brew the keg holds. */
    private int brewTint() {
        Item brew = this.menu.currentBrew().getItem();
        if (brew == ModItems.DARK_ALE.get()) return 0x5A3410;
        if (brew == ModItems.HONEY_MEAD.get()) return 0xE8B923;
        if (brew == ModItems.GLOWBREW.get()) return 0x2FD9C4;
        if (brew == ModItems.MUSHROOM_STOUT.get()) return 0x7A2E1E;
        if (brew == ModItems.WITHER_STOUT.get()) return 0x2B2B2B;
        if (brew == ModItems.BATTLE_BREW.get()) return 0xC81E28;
        return 0xC8811E; // beer amber (default)
    }
}
