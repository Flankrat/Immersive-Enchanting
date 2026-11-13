package me.alfie.immersiveenchanting.api.description.internal.lines;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLine;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record ModFilterLine(NodeTooltip tooltip) implements DescriptionLine {
    @Override
    public void render(GuiGraphicsX gx, int lineX, int lineY, MousePos mousePos) {
        DescriptionHelper.text(gx, getText(), lineX, lineY);
    }

    @Override
    public @NotNull Component getText() {
        if(tooltip.node().isState(NodeState.OBTAINED)) {
            return Component.translatable("immersiveenchanting.mod_filter.selected", tooltip().node().getTitle()).withStyle(ChatFormatting.LIGHT_PURPLE);
        } else if(tooltip.node().isState(NodeState.UNOBTAINED)) {
            return Component.translatable("immersiveenchanting.mod_filter.desc", tooltip().node().getTitle()).withStyle(ChatFormatting.WHITE);
        }

        return Component.empty();
    }
}
