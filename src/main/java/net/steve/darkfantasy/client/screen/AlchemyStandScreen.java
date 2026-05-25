package net.steve.darkfantasy.client.screen;

import net.steve.darkfantasy.DarkFantasy;
import net.steve.darkfantasy.menu.AlchemyStandMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class AlchemyStandScreen extends AbstractContainerScreen<AlchemyStandMenu> {
    private static final Identifier BACKGROUND_LOCATION =
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "textures/gui/alchemy_stand.png");

    /** Mod elixir still texture. 16 px wide, 512 px tall = 32 animation frames stacked vertically. */
    private static final Identifier ELIXIR_STILL =
            Identifier.fromNamespaceAndPath(DarkFantasy.MOD_ID, "textures/block/elixir_still.png");
    private static final int FLUID_FRAME_SIZE = 16;
    private static final int FLUID_TEX_WIDTH = 16;
    private static final int FLUID_TEX_HEIGHT = 512;

    // Tank rectangle within the GUI background. Tall vertical bar on the left side.
    private static final int TANK_X = 12;
    private static final int TANK_Y = 18;
    private static final int TANK_W = 14;
    private static final int TANK_H = 50;

    // Progress arrow — down-pointing, between the input row (y=17–33) and the output (y=60).
    // The empty arrow outline is part of the GUI background; the filled overlay lives
    // in the "extras" zone at (176, 0) and is drawn dynamically (top -> bottom).
    private static final int ARROW_X = 84;       // dest x within the GUI
    private static final int ARROW_Y = 39;       // dest y within the GUI
    private static final int ARROW_W = 24;       // width of the arrow
    private static final int ARROW_H = 17;       // full height of the arrow
    private static final int ARROW_SRC_U = 176;  // u in the GUI texture (extras column)
    private static final int ARROW_SRC_V = 0;    // v in the GUI texture

    public AlchemyStandScreen(AlchemyStandMenu menu, Inventory inventory, Component title) {
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

        renderElixirTank(graphics, xo, yo);
        renderProgressArrow(graphics, xo, yo);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        // Hovering over the tank area shows "Elixir: <amount> / <capacity> mB".
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        int tankLeft = xo + TANK_X;
        int tankTop = yo + TANK_Y;
        if (mouseX >= tankLeft && mouseX < tankLeft + TANK_W
                && mouseY >= tankTop && mouseY < tankTop + TANK_H) {
            int elixir = this.menu.getElixirAmount();
            int capacity = this.menu.getTankCapacity();
            List<Component> lines = List.of(
                    Component.translatable("tooltip.darkfantasy.alchemy_stand.tank"),
                    Component.translatable("tooltip.darkfantasy.alchemy_stand.tank.amount", elixir, capacity)
            );
            graphics.setTooltipForNextFrame(
                    this.font,
                    lines.stream().map(Component::getVisualOrderText).toList(),
                    mouseX, mouseY);
        }
    }

    /**
     * Draws the filled portion of the progress arrow. The empty arrow outline is part of
     * the static background PNG; this overlays the filled portion on top, growing
     * top -> bottom as the cook progresses (flowing from inputs down to output).
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
     * Draws the elixir texture as a vertical bar inside the tank area,
     * filling from the bottom up proportional to the current elixir amount.
     */
    private void renderElixirTank(GuiGraphicsExtractor graphics, int guiX, int guiY) {
        int elixir = this.menu.getElixirAmount();
        if (elixir <= 0) return;

        int capacity = this.menu.getTankCapacity();
        int filledHeight = Math.max(1, (int) ((long) elixir * TANK_H / capacity));
        filledHeight = Math.min(filledHeight, TANK_H);

        int x = guiX + TANK_X;
        int barBottom = guiY + TANK_Y + TANK_H;

        // Tile vertically: 16 px frames stacked from the bottom up.
        int fullTiles = filledHeight / FLUID_FRAME_SIZE;
        int remainder = filledHeight % FLUID_FRAME_SIZE;

        for (int i = 0; i < fullTiles; i++) {
            int tileY = barBottom - (i + 1) * FLUID_FRAME_SIZE;
            graphics.blit(RenderPipelines.GUI_TEXTURED, ELIXIR_STILL,
                    x, tileY, 0.0F, 0.0F,
                    TANK_W, FLUID_FRAME_SIZE,
                    FLUID_FRAME_SIZE, FLUID_FRAME_SIZE,
                    FLUID_TEX_WIDTH, FLUID_TEX_HEIGHT);
        }

        if (remainder > 0) {
            int tileY = barBottom - fullTiles * FLUID_FRAME_SIZE - remainder;
            // Source: take just the bottom 'remainder' rows of frame 0 so the partial fill
            // looks like the top of the fluid surface meeting the bar's fill line.
            float v = FLUID_FRAME_SIZE - remainder;
            graphics.blit(RenderPipelines.GUI_TEXTURED, ELIXIR_STILL,
                    x, tileY, 0.0F, v,
                    TANK_W, remainder,
                    FLUID_FRAME_SIZE, remainder,
                    FLUID_TEX_WIDTH, FLUID_TEX_HEIGHT);
        }
    }
}
