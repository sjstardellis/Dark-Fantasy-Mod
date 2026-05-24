package net.steve.darkfantasy.client.screen;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.menu.BrewingKegMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Screen for the brewing keg.
 *
 * <p>The background PNG provides the static frame (slots, labels, empty arrow,
 * tank well). This class layers the dynamic bits on top:
 * <ol>
 *   <li>{@link #renderWaterTank} fills the tank well from the bottom up with the
 *       vanilla water-still texture, tiled 16 px at a time.</li>
 *   <li>{@link #renderProgressArrow} fills the horizontal arrow between the
 *       inputs and the tank as a batch cooks.</li>
 * </ol>
 *
 * <p>Coordinates here mirror the alchemy stand layout — the goal is for the two
 * machines to feel related at a glance. If you make a custom GUI texture, keep the
 * tank well at (12, 18, 14, 50) and the arrow at (84, 39, 24, 17) and these
 * constants stay correct.
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
    private static final int TANK_X = 12;
    private static final int TANK_Y = 18;
    private static final int TANK_W = 14;
    private static final int TANK_H = 50;

    // Progress arrow — placed between the input row (y=17–33) and the tank.
    // Background PNG includes the empty arrow outline; this overlays the filled
    // portion from the right-edge "extras" zone of the texture.
    private static final int ARROW_X = 74;
    private static final int ARROW_Y = 39;
    private static final int ARROW_W = 24;
    private static final int ARROW_H = 17;
    private static final int ARROW_SRC_U = 176;
    private static final int ARROW_SRC_V = 0;

    // Countdown timer (MM:SS) — drawn centered below the arrow, only while brewing.
    // y=58 sits just under the arrow (which ends at 39+17=56) without crowding the
    // player inventory that starts at y=84.
    private static final int TIMER_CENTER_X = ARROW_X + ARROW_W / 2;
    private static final int TIMER_Y = 58;
    private static final int TIMER_COLOR = 0x555555; // warm amber so it reads on dark woodgrain

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
        renderProgressArrow(graphics, xo, yo);
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
            List<Component> lines = List.of(
                    Component.translatable("tooltip.darkfantasy.brewing_keg.tank"),
                    Component.translatable("tooltip.darkfantasy.brewing_keg.tank.amount", beer, capacity)
            );
            graphics.setTooltipForNextFrame(
                    this.font,
                    lines.stream().map(Component::getVisualOrderText).toList(),
                    mouseX, mouseY);
        }
    }

    /**
     * Overlay the filled portion of the progress arrow on top of the static
     * empty-arrow outline drawn from the background. Grows top → bottom over the
     * course of one batch.
     */
    private void renderProgressArrow(GuiGraphicsExtractor graphics, int guiX, int guiY) {
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        if (progress <= 0 || maxProgress <= 0) return;

        int filledHeight = (int) ((long) progress * ARROW_H / maxProgress);
        if (filledHeight <= 0) return;
        filledHeight = Math.min(filledHeight, ARROW_H);

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_LOCATION,
                guiX + ARROW_X, guiY + ARROW_Y,
                (float) ARROW_SRC_U, (float) ARROW_SRC_V,
                ARROW_W, filledHeight,
                ARROW_W, filledHeight,
                256, 256);
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
    }
}
