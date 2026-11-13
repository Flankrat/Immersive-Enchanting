package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.api.node.NodePayload;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;


public record ModFilterNodeData(String modid) implements NodePayload {

    public static final String ALL_MODS = "all";
    public static final ResourceId TYPE = new ResourceId(ImmersiveEnchanting.MODID, "mod_filter");
    @Override public ResourceId type() {
        return TYPE;
    }

    /**
     * Creates a {@link NodeData} for a mod-filter node.
     *
     * <p>On click, sets the enchanting tab's active mod filter to {@code modid}
     * (or clears it when {@code modid} is {@link #ALL_MODS}) and rebuilds the branch list.
     */
    public static NodeData<ModFilterNodeData> create(String modid) {
        return new NodeData<>(
                TYPE,
                new ModFilterNodeData(modid),
                (data, context) -> {
                    EnchantingTableScreen screen = context.screen();

                    String id = ALL_MODS.equals(data.modid()) ? null : data.modid();

                    screen.enchantingTab().setFilteredModid(id);
                    screen.rebuildBranches();
                }
        );
    }
}
