package me.alfie.immersiveenchanting.gui.canvas;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.immersiveenchanting.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for objects that can be rendered on a {@link Canvas} without
 * manually applying scroll offsets. Provides methods for position, scaling,
 * and drawing textures or sprites.
 */
public abstract class CanvasRenderable {

    private float canvasX;
    private float canvasY;
    private float scale = 1f;
    private final Canvas canvas;

    //Used when applying offsets i.e setScaleKeepPos()
    private float baseCanvasX;
    private float baseCanvasY;

    /**
     * Creates a new CanvasRenderable bound to the specified canvas.
     *
     * @param canvas The canvas this object will render on
     */
    public CanvasRenderable(Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Render this object on the canvas. Called every frame.
     */
    public abstract void render(GuiGraphicsX gx, MousePos mousePos);

    /**
     * Gets the canvas this object is attached to.
     *
     * @return The canvas
     */
    public Canvas canvas() {
        return canvas;
    }

    /**
     * Sets the canvas position of this object.
     *
     * @param x X coordinate on the canvas
     * @param y Y coordinate on the canvas
     */
    public void setCanvasPos(float x, float y) {
        this.canvasX = x;
        this.canvasY = y;
        this.baseCanvasX = x;
        this.baseCanvasY = y;
    }

    /**
     * Sets the object’s position so that it is centered on the canvas,
     * based on the texture’s width and height.
     *
     * @param textureWidth Width of the object
     * @param textureHeight Height of the object
     */
    public void setCenterPos(int textureWidth, int textureHeight) {
        setCanvasPos(canvas().getCenter().x() - (float) textureWidth / 2,
                canvas().getCenter().y() - (float) textureHeight / 2);
    }

    /**
     * Sets the object’s position so that it is centered on the canvas
     * based on a {@link Sprite}.
     *
     * @param sprite The sprite to center
     */
    public void setCenterPos(Sprite sprite) {
        setCenterPos(sprite.width(), sprite.height());
    }

    /**
     * Gets the current X coordinate on the canvas.
     *
     * @return Canvas X position
     */
    public float canvasX() {
        return canvasX;
    }

    /**
     * Gets the current Y coordinate on the canvas.
     *
     * @return Canvas Y position
     */
    public float canvasY() {
        return canvasY;
    }

    /**
     * Gets the current scale of the object.
     *
     * @return Scale factor
     */
    public float scale() {
        return scale;
    }

    /**
     * Sets the scale of this object.
     *
     * @param scale Scale factor
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Returns the length (i.e width/height) scaled by the current scale factor.
     *
     * @param length Original length (i.e width/height)
     * @return Scaled length
     */
    public float getScaledLength(int length) {
        return length * scale();
    }

    /**
     * Sets the scale while keeping the object centered in its original position.
     * Adjusts canvas coordinates accordingly.
     *
     * @param scale The new scale factor
     * @param sprite The sprite to scale
     */
    public void setScaleKeepPos(float scale, Sprite sprite) {
        this.scale = scale;
        float offsetX = (sprite.width() - sprite.width() * scale) / 2f;
        float offsetY = (sprite.height() - sprite.height() * scale) / 2f;

        canvasX = baseCanvasX + offsetX;
        canvasY = baseCanvasY + offsetY;
    }

    /**
     * Draws a {@link Sprite} at the object’s canvas position.
     */
    public void blit(GuiGraphicsX gx, Sprite sprite, int color) {
        blit(gx, sprite.id(), sprite.width(), sprite.height(), color);
    }

    /**
     * Draws a texture at the object’s canvas position.
     */
    public void blit(GuiGraphicsX gx, ResourceId id,
                     int width, int height, int color) {
        blit(gx, id, width, height, 0, 0, color);
    }

    /**
     * Draws a texture at the canvas position, optionally offset by pixels.
     *
     * WARNING: Offsets modify the transform matrix directly. Do NOT use this for
     * objects that rely on logical canvasX/Y (like mouse hover checks).
     * Use the other blit() overloads for those.
     */
    public void blit(GuiGraphicsX gx, ResourceId id,
                           int width, int height,
                           int offsetX, int offsetY, int color) {
        gx.graphics().pose().pushPose();

        gx.graphics().pose().translate(canvasX(), canvasY(), 0);
        gx.graphics().pose().scale(scale(), scale(), 1);
        gx.graphics().pose().translate(-canvasX(), -canvasY(), 0);

        setColor(gx.graphics(), color);

        GuiGraphicsApi.blit(
                gx,
                id,
                (int) canvasX() + offsetX, (int) canvasY() + offsetY,
                width, height
        );

        resetColor(gx.graphics());

        gx.graphics().pose().popPose();
    }

    public void item(GuiGraphicsX gx, ItemStack stack,
                     int offsetX, int offsetY, int color) {
        gx.graphics().pose().pushPose();

        gx.graphics().pose().translate(canvasX(), canvasY(), 0);
        gx.graphics().pose().scale(scale(), scale(), 1);
        gx.graphics().pose().translate(-canvasX(), -canvasY(), 0);

        GuiGraphicsApi.itemStack(gx, stack, canvas.screen().getFont(),
                (int) canvasX() + offsetX, (int) canvasY() + offsetY);

        gx.graphics().pose().popPose();
    }

    public static void setColor(GuiGraphics graphics, int color) {
        float r = ((color >> 16) & 0XFF) / 255f;
        float g = ((color >> 8) & 0XFF) / 255f;
        float b = (color & 0xFF) / 255f;

        graphics.setColor(r, g, b, 1f
        );
    }

    public static void resetColor(GuiGraphics graphics) {
        graphics.setColor(1f, 1f, 1f, 1f);
    }

}
