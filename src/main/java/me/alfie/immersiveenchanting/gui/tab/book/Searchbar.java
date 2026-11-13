package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

/**
 * A text input component used in the {@link BookTab} to filter enchantments.
 *
 * <p>This search bar allows players to dynamically filter the visible
 * enchantments list by typing part of an enchantment's name.</p>
 *
 * <p>Features:
 * <ul>
 *     <li>Real-time filtering of enchantments</li>
 *     <li>Blinking caret indicator</li>
 *     <li>Automatic scrollbar reset on input changes</li>
 * </ul>
 *
 * <p>The filtering logic is applied via {@link #checkSearch()}, which removes
 * non-matching enchantments from the rendered list.</p>
 */
public class Searchbar {

    BookTab bookTab;

    private final StringBuilder searchString = new StringBuilder();
    private int caretPosition = 0;
    private long lastCaretTime = 0;
    private boolean caretVisible;

    public Searchbar(BookTab bookTab) {
        this.bookTab = bookTab;
    }


    public void render(GuiGraphicsX gx) {
        int xPos = bookTab.screen().getGuiLeft() + 138;
        int yPos = bookTab.screen().getGuiTop() + 100;

        GuiGraphicsApi.blit(
                gx,
                Sprite.SEARCH.id(),
                xPos, yPos,
                Sprite.SEARCH.width(), Sprite.SEARCH.height()
        );


        final int padding = 4;
        GuiGraphicsApi.text(gx, bookTab.screen().getFont(),
                Component.literal(searchString.toString()),
                xPos + padding, yPos + padding + 2, true);

        renderCaret(gx, xPos + padding, yPos + padding + 2);
    }

    private void renderCaret(GuiGraphicsX gx, int x, int y) {
        long currentTime = System.currentTimeMillis();
        final int caretSpeed = 500;
        if(currentTime - lastCaretTime > caretSpeed) {
            caretVisible = !caretVisible;
            lastCaretTime = currentTime;
        }

        if(caretVisible) {
            int textWidth = Minecraft.getInstance().font.width(searchString.toString());
            GuiGraphicsApi.text(gx, bookTab.screen().getFont(), Component.literal("_"),
                    x + textWidth, y, true);
        }
    }

    /**
     * Appends a character to the search string.
     *
     * <p>Also advances the caret position and resets the scrollbar
     * to ensure filtered results are visible from the top.</p>
     *
     * @param codePoint Character to append
     */
    public void addCharToSearch(char codePoint) {
        searchString.append(codePoint);
        caretPosition++;
        bookTab.scrollbar().resetScrollIndex();
    }

    /**
     * Removes the last character from the search string.
     *
     * <p>Safely handles empty strings and ensures the caret position
     * does not become negative. Also resets the scrollbar.</p>
     */
    public void removeCharFromSearch() {
        if(!searchString.isEmpty()) {
            searchString.deleteCharAt(caretPosition-1);
            caretPosition--;
            if(caretPosition < 0) caretPosition = 0;
            bookTab.scrollbar().resetScrollIndex();
        }
    }

    /**
     * Filters the {@link BookTab}'s rendered enchantments based on the current search string.
     *
     * <p>Any enchantment whose name does not contain the search string
     * (case-insensitive) is removed from the rendered list.</p>
     *
     * <p>This method mutates the list returned by
     * {@link BookTab#getRenderedEnchantments()}.</p>
     */
    protected void checkSearch() {
        List<Holder<Enchantment>> toRemove = new ArrayList<>();

        for (Holder<Enchantment> enchantmentHolder : bookTab.getRenderedEnchantments()) {
            String enchantmentName = enchantmentHolder.value().getDescriptionId();

            if (!enchantmentName.toLowerCase().contains(searchString.toString().toLowerCase())) {
                toRemove.add(enchantmentHolder);
            }
        }

        bookTab.getRenderedEnchantments().removeAll(toRemove);
    }

    /**
     * Clears the current search string.
     *
     * <p>Resets the input to an empty state but does not automatically
     * reapply filters or reset scroll position.</p>
     */
    public void clearSearch() {
        searchString.setLength(0);
    }
}
