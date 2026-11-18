package me.alfie.immersiveenchanting.api.description.internal;

import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLayout;
import me.alfie.immersiveenchanting.api.description.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.api.description.internal.lines.*;
import me.alfie.immersiveenchanting.api.node.internal.EnchantmentNodeData;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.BranchFactory;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.TierUpgrades;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;

/**
 * Populates the tooltip description for enchantment nodes based on their current state:
 * <ul>
 *   <li>UNOBTAINED – shows the enchantment cost (materials, fuel, XP levels)</li>
 *   <li>OBTAINED – shows "Equipped" or, while the remove key is held, a removal progress bar</li>
 *   <li>LOCKED – shows what's missing: the previous tier's max level, bookshelf power, its Tome of Knowledge</li>
 * </ul>
 * No-ops for non-enchantment nodes.
 */
public class EnchantLayoutExtension implements DescriptionLayoutExtension {

    /** Fork: bookshelf power the node's enchantment tier needs. */
    private static int requiredPower(NodeTooltip tooltip) {
        var access = tooltip.screen().registryAccess();
        var holder = EnchantmentUtil.toHolder(tooltip.node().branchId(), access);
        return BranchFactory.requiredBookshelfPower(CostRegistry.client(), holder, access);
    }

    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip tooltip) {
        if(!tooltip.node().isDataType(EnchantmentNodeData.TYPE)) return;

        description.widthPadding = 16;

        Node node = tooltip.node();
        EnchantingTableScreen screen = tooltip.screen();

        if(node.isState(NodeState.UNOBTAINED)) {
            DescriptionHelper.insertCostLines(tooltip, description, 0);
        } else if(node.isState(NodeState.OBTAINED)) {
            if(screen.tooltipManager().isHoldingTooltip()) {
                description.insertLine(0, new RemovingLine(tooltip));
                description.insertLine(1, new RemoveProgressLine(tooltip));
            } else {
                description.insertLine(0, new EquippedLine(tooltip));

                if(tooltip.node().canRemove()) {
                    description.insertLine(1, new RemoveHintLine(tooltip));
                }

            }
        } else if(node.isState(NodeState.LOCKED)) {
            // fork: one line for each thing that's missing, instead of just "Unknown"
            var access = screen.registryAccess();
            var holder = EnchantmentUtil.toHolder(node.branchId(), access);
            CostRegistry registry = CostRegistry.client();
            ItemStack stack = screen.getMenu().getToolSlot().getItem();
            int line = 0;

            // a tier upgrade (e.g. Advanced Sharpness I) needs the previous tier at its max level or higher first
            Holder<Enchantment> previous = TierUpgrades.missingPreviousTier(registry, stack, holder, access);
            if(previous != null) {
                Component previousName = previous.value().getFullname(TierUpgrades.maxLevel(registry, previous));
                description.insertLine(line++, new PreviousTierLine(tooltip, previousName));
            }

            int required = requiredPower(tooltip);
            int current = screen.getMenu().getBookshelfPower();
            if(current < required) description.insertLine(line++, new BookshelfPowerLine(tooltip, required, current));

            if(BranchFactory.isMissingTome(screen.getMenu(), registry, holder, access) || line == 0)
                description.insertLine(line, new UnavailableEnchantmentLine(tooltip));
        }
    }
}
