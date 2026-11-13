package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import com.mojang.blaze3d.platform.InputConstants;
import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.ScreenEventListener;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;

/**
 * Handles rendering and interaction logic for a tooltip associated with a {@link Node}.
 *
 * <p>This tooltip is composed of two main parts:
 * <ul>
 *     <li>{@link TooltipTitle} - Displays the node's title</li>
 *     <li>{@link TooltipDescription} - Displays additional descriptive text</li>
 * </ul>
 *
 * <p>The tooltip dynamically calculates its size based on content and aligns both
 * title and description to a shared width for visual consistency.
 *
 * <p>Also responsible for detecting mouse hover over the node and notifying the screen
 * to display this tooltip as the active one.
 */
public class NodeTooltip implements ScreenEventListener {

    private final Node node;
    private final EnchantingTableScreen screen;

    private final TooltipTitle title;
    private final TooltipDescription description;

    private Vector2f screenPos;

    private int hoverWidth;
    private int hoverHeight;

    private boolean lockingAllowed = true;

    /**
     * Creates a tooltip for a specific node on the enchanting screen.
     *
     * @param screen the parent screen
     * @param node the node this tooltip represents
     */
    public NodeTooltip(EnchantingTableScreen screen, Node node) {
        this.node = node;
        this.screen = screen;
        screen().enchantmentCostRenderer().setCostToRender(node().branchId(), node().getPosition() + 1);

        this.title = new TooltipTitle(this);
        this.description = new TooltipDescription(this);
    }

    /**
     * Renders the tooltip components at the node's screen position.
     *
     * <p>This method:
     * <ul>
     *     <li>Converts the node's canvas position to screen coordinates</li>
     *     <li>Positions both title and description components</li>
     *     <li>Calculates a shared width based on the widest component</li>
     *     <li>Applies appropriate heights for each component</li>
     *     <li>Renders description first, then title (for layering)</li>
     *     <li>Detects mouse hover and schedules this tooltip for display</li>
     * </ul>
     *
     */
    public void render(GuiGraphicsX gx, MousePos mousePos) {
        screenPos = screen.canvas().canvasToScreen(node.canvasX(), node.canvasY());

        title.setPos((int) screenPos.x(), (int) screenPos.y());
        description.setPos((int) screenPos.x(), (int) screenPos.y());

        int titleWidth = Minecraft.getInstance().font.width(title.getTitleText()) + Node.WIDTH + 2;
        int descWidth = description.getDescriptionLayout().getRenderedWidth();
        int sharedWidth = Math.max(titleWidth, descWidth);
        title.setWidth(sharedWidth);
        description.setWidth(sharedWidth+1);

        int titleHeight = Node.HEIGHT;
        int descHeight = description.getDescriptionLayout().getRenderedHeight() + 8;
        title.setHeight(titleHeight);
        description.setHeight(descHeight);

        description.render(gx, mousePos);

        int titleUvOffset = node().isState(NodeState.OBTAINED) ? 0 : 26;
        title.blitNineSliceSprite(gx, titleUvOffset);

        boolean locked = screen().tooltipManager().isTooltipLockedFor(node());
        hoverWidth = locked ? sharedWidth : Node.WIDTH;
        hoverHeight = locked ? titleHeight+descHeight-13 : Node.HEIGHT;

        if(mousePos.isOver((int) screenPos.x(), (int) screenPos.y(), hoverWidth, hoverHeight)) {
            screen().tooltipManager().requestTooltip(node(), 0);

            if(screen().tooltipManager().isHoldingTooltip()) screen().tooltipManager().updateHold();
        } else {
            screen().tooltipManager().unlockTooltip();

            screen().tooltipManager().resetHold();
        }
    }


    @Override
    public boolean onMouseClick(MousePos mousePos, int button) {
        if(mousePos.isOver((int) screenPos.x(), (int) screenPos.y(), Node.WIDTH, Node.HEIGHT)) {
            if(button == InputConstants.MOUSE_BUTTON_LEFT) {
                node().click();
                screen().tooltipManager().unlockTooltip();
                return true;
            }
        }


        if(mousePos.isOver((int) screenPos.x(), (int) screenPos.y(), hoverWidth, hoverHeight)) {
            if(button == InputConstants.MOUSE_BUTTON_RIGHT && isLockingAllowed()) {
                if(screen().tooltipManager().isTooltipLockedFor(node())) {
                    screen().tooltipManager().unlockTooltip();
                } else {
                    screen().tooltipManager().lockTooltip(node());
                    FxHelper.playTooltipLock(screen().player());
                }
            }
            return true;
        }

        return ScreenEventListener.super.onMouseClick(mousePos, button);
    }

    @Override
    public boolean onMouseRelease(MousePos mousePos, int button) {
        screen().tooltipManager().resetHold();

        return ScreenEventListener.super.onMouseRelease(mousePos, button);
    }

    /**
     * @return the node associated with this tooltip
     */
    public Node node() {
        return node;
    }

    /**
     * @return the parent enchanting table screen
     */
    public EnchantingTableScreen screen() {
        return screen;
    }

    public void setLockingAllowed(boolean allowed) {
        lockingAllowed = allowed;
    }

    public boolean isLockingAllowed() {
        return lockingAllowed;
    }
}
