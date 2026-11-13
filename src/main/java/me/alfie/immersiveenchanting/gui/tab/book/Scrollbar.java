package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.ScreenEventListener;
import me.alfie.immersiveenchanting.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.util.Mth;

/**
 * A vertical scrollbar component used within the {@link BookTab}.
 *
 * <p>This scrollbar allows the player to navigate through a list of
 * enchantments when the total exceeds the visible limit.
 *
 * <p>Features:
 * <ul>
 *     <li>Mouse wheel scrolling</li>
 *     <li>Click-and-drag scroller interaction</li>
 *     <li>Scroll position synced to rendered enchantment list</li>
 * </ul>
 *
 * <p>The scroll position is represented by {@code scrollIndex}, which maps
 * directly to the starting index of visible enchantments.</p>
 */
public class Scrollbar implements ScreenEventListener {

    public final BookTab bookTab;

    private final int scrollerHeight = 15;

    private final int scrollbarWidth = 14;
    private final int scrollbarHeight = 114;

    protected int scrollIndex = 0;

    private int barX;
    private int barY;

    private int scrollerX;
    private int scrollerY;
    public boolean isMouseDraggingScroller;

    public Scrollbar(BookTab bookTab) {
        this.bookTab = bookTab;
    }

    public void render(GuiGraphicsX gx, MousePos mousePos) {
        barX = bookTab.screen().getGuiLeft() + 121;
        barY = bookTab.screen().getGuiTop() + 6;

        GuiGraphicsApi.blit(
                gx,
                Sprite.SCROLLBAR.id(),
                barX, barY,
                Sprite.SCROLLBAR.width(), Sprite.SCROLLBAR.height()
        );

        int scrollerOffset = getScrollwheelOffset();
        scrollerY = barY + 1 + scrollerOffset;
        scrollerX = barX + 1;


        GuiGraphicsApi.blit(
                gx,
                Sprite.SCROLLER.id(),
                scrollerX, scrollerY,
                Sprite.SCROLLER.width(), Sprite.SCROLLER.height()
        );
    }

    /**
     * Calculates the vertical offset of the scroller within the scrollbar track.
     *
     * <p>The offset is determined by mapping {@code scrollIndex} to a percentage
     * of the total scrollable range.</p>
     *
     * @return Pixel offset from the top of the scrollbar track
     */
    private int getScrollwheelOffset() {
        int trackHeight = scrollbarHeight - scrollerHeight - 2;

        int totalBoxes = bookTab.getRenderedEnchantments().size();
        int maxScrollIndex = Math.max(0, totalBoxes - bookTab.MAX_BOXES_RENDERED);

        if (maxScrollIndex == 0) return 0;

        double percent = (double) scrollIndex / maxScrollIndex;
        return (int) Math.round(percent * trackHeight);
    }

    /**
     * Resets the scroll position to the top of the list.
     */
    public void resetScrollIndex() {
        scrollIndex = 0;
    }


    @Override
    public boolean onMouseClick(MousePos mousePos, int button) {
        if(mousePos.isOver(barX, barY, Sprite.SCROLLBAR.width(), Sprite.SCROLLBAR.height())) {
            isMouseDraggingScroller = true;
            return true;
        }

        return ScreenEventListener.super.onMouseClick(mousePos, button);
    }


    @Override
    public boolean onMouseScrolled(MousePos mousePos, double scrollY) {
        scrollIndex -= (int) scrollY;

        int max = Math.max(0, bookTab.getRenderedEnchantments().size() - bookTab.MAX_BOXES_RENDERED);
        scrollIndex = Mth.clamp(scrollIndex, 0, max);

        if(scrollIndex < 0) scrollIndex = 0;

        return ScreenEventListener.super.onMouseScrolled(mousePos, scrollY);
    }

    @Override
    public boolean onMouseDrag(MousePos mousePos, int button, double dx, double dy) {

        if(isMouseDraggingScroller) {
            int minY = barY + 1;
            int maxY = barY + scrollbarHeight - scrollerHeight - 1;

            int clamped = Math.max(minY, Math.min((int) mousePos.y() - scrollerHeight / 2, maxY));

            double percent = (double) (clamped - minY) / (maxY - minY);

            int maxIndex = Math.max(0, bookTab.getRenderedEnchantments().size() - bookTab.MAX_BOXES_RENDERED);
            scrollIndex = (int) Math.round(percent * maxIndex);
            return true;
        }

        return ScreenEventListener.super.onMouseDrag(mousePos, button, dx, dy);
    }

    @Override
    public boolean onMouseRelease(MousePos mousePos, int button) {
        isMouseDraggingScroller = false;
        return ScreenEventListener.super.onMouseRelease(mousePos, button);
    }
}
