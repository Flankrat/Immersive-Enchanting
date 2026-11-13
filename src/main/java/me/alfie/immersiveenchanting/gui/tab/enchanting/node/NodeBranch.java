package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.api.node.NodeTemplate;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;

import java.util.ArrayList;
import java.util.List;

public class NodeBranch extends CanvasRenderable {

    private final List<Node> nodes;
    private float angle;
    private BranchTexture texture;
    private final ResourceId id;

    /**
     * Constructor for API users - angles are automatically calculated by ImmersiveEnchanting.
     * @param canvas
     * @param nodeTemplates
     */
    public NodeBranch(Canvas canvas, ResourceId id, List<NodeTemplate> nodeTemplates) {
        super(canvas);
        this.id = id;

        List<Node> builtNodes = new ArrayList<>();
        for(NodeTemplate nodeTemplate : nodeTemplates) {
            builtNodes.add(new Node(
                    nodeTemplate.title(),
                    nodeTemplate.level(),
                    canvas,
                    nodeTemplate.state(),
                    nodeTemplate.tier(),
                    nodeTemplate.icon(),
                    this,
                    nodeTemplate.data()
            ));
        }
        nodes = builtNodes;

        texture = new BranchTexture(this, canvas);
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public List<Node> nodes() {
        return nodes;
    }

    public float angle() {
        return angle;
    }

    /**
     * Positions each node along the branch's angle at equal step intervals from the canvas center,
     * then recalculates the connector line textures between them.
     */
    public void placeNodesAlongLine() {
        double stepX = Math.cos(angle) * BranchManager.getNodeStep();
        double stepY = Math.sin(angle) * BranchManager.getNodeStep();

        for (int i = 0; i < nodes.size(); i++) {
            int x = (int) Math.round(canvas().getCenter().x() + stepX * (i+1));
            int y = (int) Math.round(canvas().getCenter().y() + stepY * (i+1));

            Node node = nodes().get(i);
            node.setCanvasPos(x - (float) Node.WIDTH / 2, y - (float) Node.HEIGHT / 2);
            node.setScaleKeepPos(BranchManager.getNodeBranchScale(),
                    node.getState().getSpriteForTier(node.getTier()));
        }

        texture.calculateNodeConnections();
    }

    @Override
    public void render(GuiGraphicsX gx, MousePos mousePos) {
        texture.render(gx, mousePos);

        for(Node node : nodes) {
            node.render(gx, mousePos);
        }
    }


    public ResourceId id() {
        return id;
    }
}
