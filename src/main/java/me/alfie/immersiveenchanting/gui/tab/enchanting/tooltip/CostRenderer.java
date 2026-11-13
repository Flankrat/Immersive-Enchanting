package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.Cost;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostHolder;
import me.alfie.immersiveenchanting.util.CostHelper;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CostRenderer {

    private CostHolder holder;
    private CostHolder fuelHolder;

    private List<RenderedCost> renderedCosts;
    private List<RenderedCost> renderedFuelCosts;

    private final CostRegistry costRegistry;

    public CostRenderer(CostRegistry costRegistry) {
        this.costRegistry = costRegistry;
    }

    /**
     * Loads the cost and fuel data for the given enchantment ID and level so it can be
     * cycled and displayed in the tooltip. Must be called whenever the active node changes.
     */
    public void setCostToRender(ResourceId id, int level) {
        holder = Optional.ofNullable(costRegistry.get(id))
                .map(entry -> entry.levelCosts().getLevel(level))
                .orElse(CostHolder.EMPTY);

        this.renderedCosts = getRenderedCosts(holder);

        fuelHolder = costRegistry.get(CostRegistry.ENCHANTING_FUELS).levelCosts().getLevel(level);

        // fork: a cost with fuel_count shows that many of the fuel item (e.g. 10 lapis to go Supreme)
        int fuelCount = holder.costs().isEmpty() ? 0 : holder.costs().get(0).fuelCount();
        if(fuelCount > 0) {
            List<Cost> fuels = fuelHolder.costs().isEmpty()
                    ? costRegistry.get(CostRegistry.ENCHANTING_FUELS).levelCosts().getLevel(1).costs()
                    : fuelHolder.costs();
            fuelHolder = new CostHolder(CostHelper.overrideCount(fuels, fuelCount));
        }
        this.renderedFuelCosts = getRenderedCosts(fuelHolder);
    }

    /**
     * Flattens a {@link CostHolder} into a list of {@link RenderedCost} entries, one per
     * item stack variant across all costs. Used to build the carousel list.
     */
    private List<RenderedCost> getRenderedCosts(CostHolder holder) {
        List<RenderedCost> entries = new ArrayList<>();

        for(Cost cost : holder.costs()) {
            for(ItemStack stack : cost.getItemStacks()) {
                entries.add(new RenderedCost(stack, cost.xpLevels()));
            }
        }

        return entries;
    }

    public RenderedCost getCurrentRenderedCost() {
        return GuiGraphicsApi.getCycledElement(renderedCosts, ClientConfig.getItemCarouselSpeed());
    }

    public RenderedCost getCurrentRenderedFuel() {
        return GuiGraphicsApi.getCycledElement(renderedFuelCosts, ClientConfig.getItemCarouselSpeed());
    }
}
