package me.alfie.immersiveenchanting.api.description.internal.lines;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLine;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Fork addition: tooltip line on a tier upgrade's locked level 1 node, saying which level of the previous tier the
 * item needs first, e.g. "Needs Sharpness V or higher on the item to upgrade".
 */
public record PreviousTierLine(NodeTooltip tooltip, Component previousName) implements DescriptionLine {
    @Override
    public void render(GuiGraphicsX gx, int lineX, int lineY, MousePos mousePos) {
        DescriptionHelper.text(gx, getText(), lineX, lineY);
    }

    @Override
    public @NotNull Component getText() {
        // plain name text, so the line has one colour (getFullname styles curses red and others gray)
        return Component.translatable("immersiveenchanting.tooltip.desc.previous_tier",
                        Component.literal(previousName.getString()))
                .withStyle(ChatFormatting.RED);
    }
}
