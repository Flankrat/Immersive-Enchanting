package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.ScreenEventListener;
import me.alfie.immersiveenchanting.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.util.FxHelper;

/**
 * A clickable checkbox UI element used to enable or disable a {@link BookFilters} option
 * within the {@link BookTab}.
 *
 * <p>This component:
 * <ul>
 *     <li>Visually represents filter state (enabled/disabled)</li>
 *     <li>Handles mouse interaction</li>
 *     <li>Notifies the parent {@link BookTab} to reset scrolling when toggled</li>
 * </ul>
 */
public class FilterCheckbox implements ScreenEventListener {

    private boolean enabled = true;
    protected final BookFilters filterType;
    private final BookTab bookTab;

    private int x;
    private int y;

    public FilterCheckbox(BookTab bookTab, BookFilters filter) {
        filterType = filter;
        this.bookTab = bookTab;
    }

    /**
     * @return {@code true} if this filter is currently enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled state of this filter.
     *
     * @param enabled The new state
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Toggles the enabled state of this filter.
     *
     * <p>Also resets the scrollbar position in the parent {@link BookTab}
     * to ensure consistent rendering after filtering changes.</p>
     */
    public void toggleEnabled() {
        this.enabled = !this.enabled;
        bookTab.scrollbar().resetScrollIndex();
    }


    public void render(GuiGraphicsX gx, MousePos mousePos) {
        Sprite sprite = isEnabled() ? Sprite.CHECKBOX_ON : Sprite.CHECKBOX_OFF;

        int x = bookTab.screen().getGuiLeft() + this.x;
        int y = bookTab.screen().getGuiTop() + this.y;

        GuiGraphicsApi.blit(gx,
                sprite.id(),
                x, y,
                sprite.width(), sprite.height());


        GuiGraphicsApi.text(gx, bookTab.screen().getFont(), filterType.getLabel(), x+20, y, true);
    }


    private boolean isMouseOver(MousePos mousePos) {
        return mousePos.isOver(bookTab.screen().getGuiLeft() + x, bookTab.screen().getGuiTop() + y,
                Sprite.CHECKBOX_ON.width(), Sprite.CHECKBOX_ON.height());
    }


    @Override
    public boolean onMouseClick(MousePos mousePos, int button) {
        if(this.isMouseOver(mousePos)) {
            FxHelper.playGenericUISound(bookTab.screen().player());
            toggleEnabled();
            return true;
        }

        return ScreenEventListener.super.onMouseClick(mousePos, button);
    }

    /**
     * @return X position relative to the GUI
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the X position relative to the GUI.
     *
     * @param x New X position
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return Y position relative to the GUI
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the Y position relative to the GUI.
     *
     * @param y New Y position
     */
    public void setY(int y) {
        this.y = y;
    }
}
