package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.immersiveenchanting.alfinolib.networking.Networking;
import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.api.node.NodePayload;
import me.alfie.immersiveenchanting.networking.ReplicatePacket;


public record ReplicateNodeData() implements NodePayload {

    public static final ResourceId TYPE = new ResourceId(ImmersiveEnchanting.MODID, "replicate");
    @Override public ResourceId type() {
        return TYPE;
    }


    public static NodeData<ReplicateNodeData> create() {
        return new NodeData<>(
                TYPE,
                new ReplicateNodeData(),
                (data, context) -> {
                    Networking.sendToServer(new ReplicatePacket());
                }
        );
    }

}
