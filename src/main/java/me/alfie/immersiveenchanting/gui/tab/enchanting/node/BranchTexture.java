package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import com.mojang.blaze3d.platform.NativeImage;
import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dynamically creates line textures between nodes in a NodeBranch.
 */
public class BranchTexture extends CanvasRenderable {

    private final NodeBranch branch;

    private final List<Pixel> precomputedPixels = new ArrayList<>();
    private ResourceId textureId;
    private int textureWidth;
    private int textureHeight;

    public BranchTexture(NodeBranch branch, Canvas canvas) {
        super(canvas);
        this.branch = branch;
    }

    @Override
    public void render(GuiGraphicsX gx, MousePos mousePos) {
        int brightness = (canvas().screen().tooltipManager().hasActiveTooltip() &&
                branch.nodes().contains(canvas().screen().tooltipManager().getActiveTooltipNode()))
                ? Canvas.FULL_BRIGHTNESS
                : canvas().getCurrentBrightness();

        blit(gx, textureId, textureWidth, textureHeight, brightness);
    }

    /**
     * Draws connector lines between every consecutive node in the branch and from the
     * first node back to the canvas center, then bakes the result into a GPU texture.
     * Must be called after nodes have been positioned via {@link NodeBranch#placeNodesAlongLine()}.
     */
    public void calculateNodeConnections() {
        if (branch.nodes().isEmpty()) return;

        connectNodeToCenter(branch.nodes().get(0));

        for (int i = 1; i < branch.nodes().size(); i++) {
            connectNodes(branch.nodes().get(i - 1), branch.nodes().get(i));
        }
        bakeTexture();
    }

    private void connectNodeToCenter(Node node) {
        Vector2i a = canvas().getCenter();
        int bx = (int) Math.round(node.canvasX() + node.getScaledLength(Node.WIDTH) / 2.0);
        int by = (int) Math.round(node.canvasY() + node.getScaledLength(Node.HEIGHT) / 2.0);

        makeTexture(a.x(), a.y(), bx, by);
    }

    private void connectNodes(Node node1, Node node2) {
        int ax = (int) Math.round(node1.canvasX() + node1.getScaledLength(Node.WIDTH) / 2.0);
        int ay = (int) Math.round(node1.canvasY() + node1.getScaledLength(Node.HEIGHT) / 2.0);
        int bx = (int) Math.round(node2.canvasX() + node2.getScaledLength(Node.WIDTH) / 2.0);
        int by = (int) Math.round(node2.canvasY() + node2.getScaledLength(Node.HEIGHT) / 2.0);
        makeTexture(ax, ay, bx, by);
    }

    private void calculateBorderPixel(int x, int y, Set<Long> white, int color) {
        long k = key(x, y);
        if (!white.contains(k)) {
            precomputedPixels.add(new Pixel(x, y, color));
        }
    }

    /**
     * Rasterizes a 1-pixel-wide line between two canvas points using Bresenham's algorithm,
     * then adds a 1-pixel black border around every white pixel. The resulting pixels are
     * appended to {@link #precomputedPixels} for later baking.
     */
    private void makeTexture(int x1, int y1, int x2, int y2) {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        Set<Long> whitePixels = new HashSet<>();

        // --- PASS 1: Bresenham line ---
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int cx = x1;
        int cy = y1;

        while (true) {
            whitePixels.add(key(cx, cy));
            if (cx == x2 && cy == y2) break;

            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                cx += sx;
            }
            if (e2 < dx) {
                err += dx;
                cy += sy;
            }
        }

        // --- PASS 2: Add white pixels to precomputed list ---
        for (long p : whitePixels) {
            int px = (int) (p >> 32);
            int py = (int) (p & 0xFFFFFFFF);
            precomputedPixels.add(new Pixel(px, py, WHITE));
        }

        // --- PASS 3: Add black border pixels ---
        for (long p : whitePixels) {
            int px = (int) (p >> 32);
            int py = (int) (p & 0xFFFFFFFF);

            calculateBorderPixel(px - 1, py, whitePixels, BLACK);
            calculateBorderPixel(px + 1, py, whitePixels, BLACK);
            calculateBorderPixel(px, py - 1, whitePixels, BLACK);
            calculateBorderPixel(px, py + 1, whitePixels, BLACK);

            // Optional corners:
            calculateBorderPixel(px - 1, py - 1, whitePixels, BLACK);
            calculateBorderPixel(px + 1, py - 1, whitePixels, BLACK);
            calculateBorderPixel(px - 1, py + 1, whitePixels, BLACK);
            calculateBorderPixel(px + 1, py + 1, whitePixels, BLACK);
        }
    }

    private void bakeTexture() {
        if (precomputedPixels.isEmpty()) return;

        // compute bounds
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (Pixel p : precomputedPixels) {
            minX = Math.min(minX, p.x());
            minY = Math.min(minY, p.y());
            maxX = Math.max(maxX, p.x());
            maxY = Math.max(maxY, p.y());
        }

        textureWidth = maxX - minX + 1;
        textureHeight = maxY - minY + 1;
        setCanvasPos(minX, minY);

        // Create dynamic texture
        String label = "branch_connection";
        DynamicTexture bakedTexture = new DynamicTexture(textureWidth, textureHeight, true);
        textureId = new ResourceId(ImmersiveEnchanting.MODID, label + hashCode());
        Minecraft.getInstance().getTextureManager().register(textureId.mc(), bakedTexture);


        NativeImage image = bakedTexture.getPixels();

        // fill with transparent
        image.fillRect(0, 0, textureWidth, textureHeight, 0x00000000);

        // draw all pixels into texture
        for (Pixel p : precomputedPixels) {
            int px = p.x() - minX;
            int py = p.y() - minY;
            image.setPixelRGBA(px, py, p.color());
        }

        bakedTexture.upload(); // upload to GPU
    }

    /** Packs two ints into a single long for use as a hash-set key. */
    private long key(int x, int y) {
        return (((long) x) << 32) | (y & 0xFFFFFFFFL);
    }
}
