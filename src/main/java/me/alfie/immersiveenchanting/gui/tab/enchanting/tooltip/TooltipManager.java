package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.alfinolib.networking.Networking;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.networking.RemoveEnchantmentPacket;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public class TooltipManager {

    private Node lockedTooltipNode;
    private NodeTooltip activeTooltip;
    private int activeTooltipPriority;
    private boolean isTooltipRequestedThisFrame;
    private final EnchantingTableScreen screen;

    private Node heldTooltipNode;
    private long holdStartTime;
    public static final long HOLD_TRESHOLD_MILLIS = 1000;

    public TooltipManager(EnchantingTableScreen screen) {
        this.screen = screen;
    }

    public void lockTooltip(Node node) {
        lockedTooltipNode = node;
    }

    public void unlockTooltip() {
        lockedTooltipNode = null;
    }

    public boolean isTooltipLocked() {
        return lockedTooltipNode != null;
    }

    public boolean isTooltipLockedFor(Node node) {
        return lockedTooltipNode == node;
    }

    public Node getActiveTooltipNode() {
        return activeTooltip != null ? activeTooltip.node() : null;
    }

    public NodeTooltip getActiveTooltip() {
        return activeTooltip;
    }

    public boolean hasActiveTooltip() {
        return activeTooltip != null;
    }

    public boolean isActiveTooltipFor(Node node) {
        return activeTooltip != null && activeTooltip.node().equals(node);
    }

    /**
     * Marks this frame as having a tooltip request and sets the tooltip for {@code node}
     * if the priority is high enough. Called by nodes and the active tooltip during rendering
     * so the manager knows not to clear the tooltip at end-of-frame.
     */
    public void requestTooltip(Node node, int priority) {
        if(priority < activeTooltipPriority) return;

        isTooltipRequestedThisFrame = true;
        setTooltip(node, priority);
    }

    public boolean isTooltipRequestedThisFrame() {
        return isTooltipRequestedThisFrame;
    }

    /**
     * Changes the active tooltip to the given node, unless the tooltip is locked
     * or the node is already the active tooltip. Also plays the node hover sound.
     */
    private void setTooltip(Node node, int priority) {
        if(isTooltipLocked()) return;
        if(hasActiveTooltip() && getActiveTooltipNode().equals(node)) return;

        activeTooltip = new NodeTooltip(screen, node);
        activeTooltipPriority = priority;
        FxHelper.playNodeHover(screen.player(), node);
    }

    /**
     * Clears the per-frame tooltip request flag. Called at the end of every render frame
     * so that the screen can detect when no node requested a tooltip and clear it.
     */
    public void resetFrameState() {
        isTooltipRequestedThisFrame = false;
    }

    public void clearActiveTooltip() {
        activeTooltip = null;
        activeTooltipPriority = 0;
    }


    public void startHold(Node node) {
        heldTooltipNode = node;
        holdStartTime = System.currentTimeMillis();
    }

    public void resetHold() {
        heldTooltipNode = null;
        holdStartTime = -1;
    }

    public boolean isHoldingTooltip() {
        return heldTooltipNode != null;
    }

    /**
     * Checks whether the hold threshold has been reached and fires the remove action if so.
     * Called each frame while a node is being held.
     */
    public void updateHold() {
        if(heldTooltipNode == null) return;

        if(getElapsedHeldTime() >= HOLD_TRESHOLD_MILLIS) triggerHeldTooltip();
    }

    public long getElapsedHeldTime() {
        return System.currentTimeMillis() - holdStartTime;
    }

    /**
     * Sends the enchantment removal packet for the currently held node and resets the hold state.
     * Called when the hold duration reaches {@link #HOLD_TRESHOLD_MILLIS}.
     */
    private void triggerHeldTooltip() {
        Networking.sendToServer(new RemoveEnchantmentPacket(
                ResourceKey.create(Registries.ENCHANTMENT, heldTooltipNode.branchId().mc()),
                heldTooltipNode.getPosition()));

        resetHold();
    }
}
