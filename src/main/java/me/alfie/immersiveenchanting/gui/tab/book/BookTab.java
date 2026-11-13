package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.*;

public class BookTab {
    private final EnchantingTableScreen screen;

    private final List<Holder<Enchantment>> renderedEnchantments = new ArrayList<>();

    private final List<FilterCheckbox> filterCheckboxes = new ArrayList<>();
    private final Scrollbar scrollbar;
    private final Searchbar searchbar;


    public final int MAX_BOXES_RENDERED = 6;

    /**
     * Creates a new book tab instance for the enchanting screen.
     <P>
     * <p>Initializes filter checkboxes, scrollbar, and search bar components.</p>
     <P>
     * @param screen the parent {@link EnchantingTableScreen}
     */
    public BookTab(EnchantingTableScreen screen) {
        this.screen = screen;

        filterCheckboxes.add(new FilterCheckbox(this, BookFilters.UNLOCKED));
        filterCheckboxes.add(new FilterCheckbox(this, BookFilters.LOCKED));
        scrollbar = new Scrollbar(this);
        searchbar = new Searchbar(this);
    }

    /**
     * Initializes the book tab state.
     <P>
     * <p>Resets all filters, scroll position, and search input.</p>
     */
    public void init() {
        resetFilters();
        scrollbar().resetScrollIndex();
        searchbar.clearSearch();
    }

    /**
     * Gets the parent enchanting table screen.
     <P>
     * @return the {@link EnchantingTableScreen} associated with this tab
     */
    public EnchantingTableScreen screen() {
        return screen;
    }

    /**
     * Retrieves all currently enabled filters.
     <P>
     * @return a list of active {@link BookFilters}
     */
    public List<BookFilters> getEnabledFilters() {
        List<BookFilters> bookFilters = new ArrayList<>();
        for(FilterCheckbox checkbox : filterCheckboxes) {
            if(checkbox.isEnabled()) bookFilters.add(checkbox.filterType);
        }
        return bookFilters;
    }

    /**
     * Applies active filters and search criteria to determine which enchantments are rendered.
     <P>
     * <p>This method:</p>
     <P>
     * <ul>
     *     <li>Clears the current rendered enchantment list</li>
     *     <li>Adds unlocked enchantments if enabled</li>
     *     <li>Adds locked enchantments if enabled</li>
     *     <li>Applies search filtering</li>
     *     <li>Sorts the results alphabetically</li>
     * </ul>
     */
    private void applyFilters() {
        renderedEnchantments.clear();
        List<BookFilters> enabledFilters = getEnabledFilters();

        if(enabledFilters.contains(BookFilters.UNLOCKED)) {
            renderedEnchantments.addAll(screen.getMenu().getAvailableEnchantments());
        }

        if (enabledFilters.contains(BookFilters.LOCKED)) {
            Set<Holder<Enchantment>> unlocked =
                    new HashSet<>(screen.getMenu().getAvailableEnchantments());

            renderedEnchantments.addAll(
                    CostRegistry.client().getAllEnabledEnchantmentHolders().stream()
                            .filter(e -> !unlocked.contains(e))
                            .toList()
            );
        }

        searchbar.checkSearch();
        renderedEnchantments.sort(Comparator.comparing(e -> e.value().getDescriptionId()));
    }

    /**
     * Resets all filters to their default enabled state.
     <P>
     * <p>All filter checkboxes are set to enabled.</p>
     */
    private void resetFilters() {
        for(FilterCheckbox filterCheckbox : filterCheckboxes) {
            filterCheckbox.setEnabled(true);
        }
    }


    public void render(GuiGraphicsX gx, MousePos mousePos) {
        renderFilterCheckboxes(gx, mousePos);
        renderEnchantmentBoxes(gx);
        scrollbar.render(gx, mousePos);
        searchbar.render(gx);
    }


    private void renderEnchantmentBoxes(GuiGraphicsX gx) {
        int x = screen.getGuiLeft() + 13;
        int y = screen.getGuiTop() + 6;

        for (int i = 0; i < MAX_BOXES_RENDERED; i++) {
            applyFilters();

            if (scrollbar.scrollIndex+i < renderedEnchantments.size()) {
                Holder<Enchantment> enchantmentHolder = renderedEnchantments.get(scrollbar.scrollIndex + i);
                EnchantmentBox box = new EnchantmentBox(this, enchantmentHolder);
                box.render(gx, x, y);
            } else {
                EnchantmentBox box = new EnchantmentBox(this, null);
                box.render(gx, x, y);
            }

            y += Sprite.ENCHANTMENT_BOX_LOCKED.height();
        }
    }


    private void renderFilterCheckboxes(GuiGraphicsX gx, MousePos mousePos) {
        int count = 0;
        final int spacing = 18;

        for(FilterCheckbox filterCheckbox : filterCheckboxes()) {
            filterCheckbox.setX(138);
            filterCheckbox.setY(12 + (count*spacing));
            filterCheckbox.render(gx, mousePos);
            count++;
        }

        final int xPos = screen.getGuiLeft() + 138;
        final int yPos = screen.getGuiTop() + (count * spacing) + 16;

        GuiGraphicsApi.text(
                gx, screen().getFont(),
                Component.translatable("immersiveenchanting.label.total_enchantments", renderedEnchantments.size()),
                xPos, yPos, true);
    }

    /**
     * Gets the list of filter checkbox components.
     <P>
     * @return the list of {@link FilterCheckbox} instances
     */
    public List<FilterCheckbox> filterCheckboxes() {
        return filterCheckboxes;
    }

    /**
     * Gets the scrollbar component for this tab.
     <P>
     * @return the {@link Scrollbar}
     */
    public Scrollbar scrollbar() {
        return scrollbar;
    }

    /**
     * Gets the search bar component.
     <P>
     * @return the {@link Searchbar}
     */
    public Searchbar searchbar() {
        return searchbar;
    }

    /**
     * Gets the list of enchantments currently being rendered.
     <P>
     * @return the filtered and sorted list of {@link Enchantment} holders
     */
    public List<Holder<Enchantment>> getRenderedEnchantments() {
        return renderedEnchantments;
    }

}
