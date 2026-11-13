package me.alfie.immersiveenchanting.api.description.internal.lines;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLine;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.RenderedCost;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record LevelsLine(NodeTooltip tooltip) implements DescriptionLine {

    /**
     * Renders the "XP Levels:" label, followed by an XP orb icon sprite, then the
     * required level count in green to the right of the icon.
     */
    @Override
    public void render(GuiGraphicsX gx, int lineX, int lineY, MousePos mousePos) {
        DescriptionHelper.text(gx, getText(), lineX, lineY);

        EnchantingTableScreen screen = tooltip.screen();
        RenderedCost renderedCost = screen.enchantmentCostRenderer().getCurrentRenderedCost();

        GuiGraphicsApi.blit(gx,
                Sprite.XP_LEVEL.id(),
                lineX + Minecraft.getInstance().font.width(getText().getString()),
                lineY - 4,
                Sprite.XP_LEVEL.width(), Sprite.XP_LEVEL.height());

        gx.graphics().drawString(
                Minecraft.getInstance().font,
                Component.literal(String.valueOf(renderedCost.xpLevels())),
                lineX + Minecraft.getInstance().font.width(getText().getString()) + 10,
                lineY + 4,
                0xC8FF8F //Don't use AlfinoLib so we can have color overload.
        );

    }

    @Override
    public @NotNull Component getText() {
        return Component.translatable("immersiveenchanting.tooltip.desc.xp_levels").withStyle(ChatFormatting.GRAY);
    }
}
