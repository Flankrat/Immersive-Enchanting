package me.alfie.immersiveenchanting.api.description.internal.lines;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.api.description.DescriptionLine;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.TooltipManager;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public record RemoveProgressLine(NodeTooltip tooltip) implements DescriptionLine {
    /**
     * Renders a progress bar showing how far the player is through the hold-to-remove gesture.
     * The bar has a black background and a red fill that grows from left to right as
     * {@link TooltipManager#getElapsedHeldTime()} approaches {@link TooltipManager#HOLD_TRESHOLD_MILLIS}.
     * Also triggers the removal sound effect via {@link me.alfie.immersiveenchanting.util.FxHelper#playRemoveProgress}.
     */
    @Override
    public void render(GuiGraphicsX gx, int lineX, int lineY, MousePos mousePos) {
        final int width = Minecraft.getInstance().font.width(getText());
        final int height = Minecraft.getInstance().font.lineHeight;
        final int padding = 2;

        gx.graphics().fill(lineX, lineY, lineX+width, lineY+height, Color.BLACK.getRGB());

        float progress = tooltip.screen()
                .tooltipManager()
                .getElapsedHeldTime() / (float) TooltipManager.HOLD_TRESHOLD_MILLIS;
        progress = Math.min(1f, Math.max(0f, progress));

        int innerX = lineX + padding;
        int innerY = lineY + padding;
        int innerWidth = width - (padding * 2);
        int innerHeight = height - (padding * 2);
        int filledWidth = (int) (innerWidth * progress);

        gx.graphics().fill(innerX, innerY, innerX + filledWidth, innerY + innerHeight, 0xFFFF5555);

        FxHelper.playRemoveProgress(tooltip.screen().player(), progress);
    }

    @Override
    public @NotNull Component getText() {
        return Component.translatable("immersiveenchanting.tooltip.desc.equipped").withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}
