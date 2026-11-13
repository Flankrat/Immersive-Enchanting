package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.api.description.DescriptionLayout;
import me.alfie.immersiveenchanting.api.description.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.gui.core.NineSliceSprite;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;

public class TooltipDescription extends TooltipComponent {

    private final NodeTooltip tooltip;
    private final DescriptionLayout layout;

    public TooltipDescription(NodeTooltip tooltip) {
        super(NineSliceSprite.TOOLTIP_WIDGETS.id());
        this.tooltip = tooltip;
        this.layout = new DescriptionLayout(this);

        TooltipDescriptionExtensions.rebuild(tooltip, layout); //Prevents flicker
    }

    public void render(GuiGraphicsX gx, MousePos mousePos) {
        TooltipDescriptionExtensions.rebuild(tooltip, layout);

        int yOffset = Node.HEIGHT-10;
        setPos(x()+1, y()+yOffset);
        super.blitNineSliceSprite(gx, 52);

        setTextStartPos(x() + 4, y() + 12);
        layout.render(gx, getTextStartPos().x(), getTextStartPos().y(), mousePos);


        if(!tooltip.isLockingAllowed()) return;
        Sprite mouseSprite = tooltip.screen().tooltipManager().isTooltipLocked() ? Sprite.MOUSE_HINT_ON : Sprite.MOUSE_HINT_OFF;

        GuiGraphicsApi.blit(gx,
                mouseSprite.id(),
                x() + getWidth() - 10, y() + getHeight() - 14,
                mouseSprite.width(), mouseSprite.height());
    }

    public DescriptionLayout getDescriptionLayout() {
        return layout;
    }
}
