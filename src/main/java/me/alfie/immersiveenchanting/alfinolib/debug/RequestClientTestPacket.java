package me.alfie.immersiveenchanting.alfinolib.debug;

import me.alfie.immersiveenchanting.alfinolib.AlfinoLib;
import me.alfie.immersiveenchanting.alfinolib.datapacks.DataMap;
import me.alfie.immersiveenchanting.alfinolib.datapacks.SyncClientDatapackPacket;
import me.alfie.immersiveenchanting.alfinolib.datapacks.client.ClientDatapackManager;
import me.alfie.immersiveenchanting.alfinolib.datapacks.server.ServerDatapackManager;
import me.alfie.immersiveenchanting.alfinolib.debug.datapack.TestData;
import me.alfie.immersiveenchanting.alfinolib.debug.datapack.TestDatapack;
import me.alfie.immersiveenchanting.alfinolib.networking.NetworkPacket;
import me.alfie.immersiveenchanting.alfinolib.networking.NetworkRegisterEvent;
import me.alfie.immersiveenchanting.alfinolib.networking.Networking;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodecBuilder;
import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

public record RequestClientTestPacket() implements NetworkPacket<RequestClientTestPacket> {

    public static final StreamCodec<FriendlyByteBuf, RequestClientTestPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestClientTestPacket());

    @Override
    public void exec(NetworkEvent.Context context) {
        String testString = ClientDatapackManager.get(TestDatapack.KEY).testString();
        testString = testString.replace("%s", "client");

        Minecraft.getInstance().player.sendSystemMessage(Component.literal(testString));
    }

    public static void register(NetworkRegisterEvent event) {
        event.register(RequestClientTestPacket.class, RequestClientTestPacket.STREAM_CODEC);
    }
}
