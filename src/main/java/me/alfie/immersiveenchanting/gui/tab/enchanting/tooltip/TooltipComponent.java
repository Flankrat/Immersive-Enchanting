package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import org.joml.Vector2i;

public abstract class TooltipComponent extends NineSliceBox {

    private Vector2i textStartPos;

    public TooltipComponent(ResourceId texture) {
        super(texture);
    }

    public void setTextStartPos(int x, int y) {
        textStartPos = new Vector2i(x, y);
    }

    public Vector2i getTextStartPos() {
        return textStartPos;
    }
}
