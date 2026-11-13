package me.alfie.immersiveenchanting.api.description.internal.lines;

import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLine;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Fork addition: tooltip line on a locked node whose tier needs more bookshelf power than the table has,
 * e.g. "Needs 30 bookshelf power (has 18)".
 */
public record BookshelfPowerLine(NodeTooltip tooltip, int required, int current) implements DescriptionLine {
    @Override
    public void render(GuiGraphicsX gx, int lineX, int lineY, MousePos mousePos) {
        DescriptionHelper.text(gx, getText(), lineX, lineY);
    }

    @Override
    public @NotNull Component getText() {
        return Component.translatable("immersiveenchanting.tooltip.desc.bookshelf_power", required, current)
                .withStyle(ChatFormatting.RED);
    }
}
