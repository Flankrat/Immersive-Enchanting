package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.immersiveenchanting.alfinolib.networking.Networking;
import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.api.node.NodePayload;
import me.alfie.immersiveenchanting.networking.TransmutePacket;


public record TransmuteNodeData() implements NodePayload {

    public static final ResourceId TYPE = new ResourceId(ImmersiveEnchanting.MODID, "transmute");
    @Override public ResourceId type() {
        return TYPE;
    }

    public static NodeData<TransmuteNodeData> create() {
        return new NodeData<>(
                TYPE,
                new TransmuteNodeData(),
                (data, context) -> {
                    Networking.sendToServer(new TransmutePacket());
                }
        );
    }

}
