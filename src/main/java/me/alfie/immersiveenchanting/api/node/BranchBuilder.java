package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeBranch;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for constructing a {@link me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeBranch}
 * from a sequence of {@link NodeTemplate} descriptors.
 *
 * <p>Use {@link #of} to create an instance, chain {@link #node}/{@link #nodes} calls to add
 * node templates in order, then call {@link #build} to produce the branch.
 */
public class BranchBuilder {

    private final Canvas canvas;
    private final ResourceId id;
    private final List<NodeTemplate> nodeTemplates = new ArrayList<>();

    private BranchBuilder(Canvas canvas, ResourceId id) {
        this.canvas = canvas;
        this.id = id;
    }

    public static BranchBuilder of(Canvas canvas, ResourceId id) {
        return new BranchBuilder(canvas, id);
    }

    public NodeBranch build() {
        return new NodeBranch(canvas, id, new ArrayList<>(nodeTemplates));
    }

    public BranchBuilder node(NodeTemplate template) {
        nodeTemplates.add(template);
        return this;
    }

    public BranchBuilder nodes(List<NodeTemplate> templates) {
        nodeTemplates.addAll(templates);
        return this;
    }

}
