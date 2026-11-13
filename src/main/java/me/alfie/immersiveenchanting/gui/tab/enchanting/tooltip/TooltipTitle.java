package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.ItemIcon;
import me.alfie.immersiveenchanting.api.node.NodeIcon;
import me.alfie.immersiveenchanting.api.node.SpriteIcon;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.gui.core.NineSliceSprite;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.awt.*;

/**
 * A visual component representing the title section of a node tooltip.
 *
 * <p>Displays the node's background sprite, icon (if present), and the node's
 * title text. The title text is styled based on the node's state: white by default,
 * or using an alternate font for locked nodes.</p>
 */
public class TooltipTitle extends TooltipComponent {

    private Component titleText;
    private final NodeTooltip tooltip;

    /**
     * Constructs a TooltipTitle for the given NodeTooltip.
     *
     * <p>Selects the appropriate background sprite depending on whether the
     * node is obtained or not, and sets the initial title text with proper styling.</p>
     *
     * @param tooltip The NodeTooltip this title belongs to
     */
    public TooltipTitle(NodeTooltip tooltip) {
        super(NineSliceSprite.TOOLTIP_WIDGETS.id());
        this.tooltip = tooltip;

        Component titleText = tooltip.node().getTitle();
        setTitleText(titleText);
    }

    /**
     * Sets the title text for this tooltip title, applying the appropriate style.
     *
     * <p>All titles are styled white by default. If the node is locked, an alternate
     * font style is applied to indicate that the node is inaccessible.</p>
     *
     * @param component The original title text component
     */
    private void setTitleText(Component component) {
        component = component.copy().withStyle(ChatFormatting.WHITE);

        if(tooltip.node().isState(NodeState.LOCKED) && ServerConfig.isObfuscateLockedEnchantments())
            component = ImmersiveEnchanting.styleWithAltFont(component);

        this.titleText = component;
    }

    /**
     * Returns the current title text of this tooltip.
     *
     * @return The styled title Component
     */
    public Component getTitleText() {
        return titleText;
    }

    /**
     * Renders the tooltip title on the screen.
     *
     * <p>Draws the NineSlice background, then the node's tier sprite, the optional icon,
     * and finally the title text. The title text is vertically centered relative to the node
     * sprite. A small horizontal offset is applied to position the title correctly.</p>
     *
     */
    @Override
    public void blitNineSliceSprite(GuiGraphicsX gx, int uvOffset) {
        setPos(x()+2, y());
        super.blitNineSliceSprite(gx, uvOffset);
        setPos(x()-2, y());

        Node node = tooltip.node();

        GuiGraphicsApi.blit(
                gx,
                node.getState().getSpriteForTier(node.getTier()).id(),
                x(), y(),
                Node.WIDTH, Node.HEIGHT
        );

        NodeIcon icon = node.getIcon();
        if(icon instanceof SpriteIcon sprite) {
            GuiGraphicsApi.blit(
                    gx,
                    sprite.id(),
                    x()+4, y()+4,
                    16, 16
            );
        } else if (icon instanceof ItemIcon item) {
            GuiGraphicsApi.itemStack(gx, item.stack(), tooltip.screen().getFont(), x()+4, y()+4);
        }

        int xo = Node.WIDTH;
        int yo = Node.HEIGHT/2 - Minecraft.getInstance().font.lineHeight/2;
        setTextStartPos(x() + xo, y() + yo);
        GuiGraphicsApi.text(gx, tooltip.screen().getFont(), titleText,
                getTextStartPos().x(), getTextStartPos().y(), true);
    }
}
