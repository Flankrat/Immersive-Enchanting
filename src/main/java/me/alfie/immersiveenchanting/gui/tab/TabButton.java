package me.alfie.immersiveenchanting.gui.tab;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.ScreenEventListener;
import me.alfie.immersiveenchanting.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class TabButton implements ScreenEventListener {

    public final EnchantingTableScreen screen;

    private final int width = 20;
    private final int height = 25;
    private final int x = 200;
    private final int y = 126;

    private ItemStack icon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
    private Component label = Component.empty();

    /**
     * Creates a new tab button tied to the given screen.
     <P>
     * @param screen the parent {@link EnchantingTableScreen} this button belongs to
     */
    public TabButton(EnchantingTableScreen screen) {
        this.screen = screen;
    }

    /**
     * Renders the tab button, including its icon and hover tooltip.
     <P>
     * <p>The displayed icon and label depend on the current {@link ScreenState}:</p>
     <P>
     * <ul>
     *     <li>ENCHANTING → shows book icon (switch to books view)</li>
     *     <li>BOOKS → shows enchanting table icon (switch to enchanting view)</li>
     * </ul>
     <P>

     */
    public void render(GuiGraphicsX gx, MousePos mousePos) {
        if(screen.isState(ScreenState.ENCHANTING)) {
            label = Component.translatable("immersiveenchanting.tab.book");
            icon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
        } else if (screen.isState(ScreenState.BOOKS)) {
            label = Component.translatable("immersiveenchanting.tab.enchanting");
            icon = new ItemStack(Items.ENCHANTING_TABLE, 1);
        }

        final int x = screen.getGuiLeft() + 202;
        final int y = screen.getGuiTop() + 132;
        GuiGraphicsApi.itemStackWithTooltip(gx, icon, screen.getFont(), x, y, mousePos);
    }

    @Override
    public boolean onMouseClick(MousePos mousePos, int button) {
        if(mousePos.isOver(screen.getGuiLeft() + 202, screen.getGuiTop() + 132, width, height)) {
            FxHelper.playGenericUISound(screen.player());

            if(screen.isState(ScreenState.ENCHANTING)) {
                screen.bookTab().init();
                screen.setState(ScreenState.BOOKS);
            } else if(screen.isState(ScreenState.BOOKS)) {
                screen.setState(ScreenState.ENCHANTING);
            }

            return true;
        }

        return ScreenEventListener.super.onMouseClick(mousePos, button);
    }
}
