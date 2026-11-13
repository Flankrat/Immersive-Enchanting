package me.alfie.immersiveenchanting.gui.core;

import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;

public enum NineSliceSprite {
    TOOLTIP_WIDGETS("textures/gui/sprites/tooltip/tooltip_widgets.png");

    private final ResourceId id;

    NineSliceSprite(String path) {
        this.id = new ResourceId(ImmersiveEnchanting.MODID, path);
    }

    public ResourceId id() {
        return id;
    }

}
