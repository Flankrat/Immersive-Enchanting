package me.alfie.immersiveenchanting.alfinolib.datapacks.client;

import me.alfie.immersiveenchanting.alfinolib.AlfinoLib;
import me.alfie.immersiveenchanting.alfinolib.datapacks.DataMap;
import me.alfie.immersiveenchanting.alfinolib.datapacks.DatapackKey;
import me.alfie.immersiveenchanting.alfinolib.datapacks.Datapacks;
import me.alfie.immersiveenchanting.alfinolib.datapacks.server.ServerDatapackRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;

public class ClientDatapackManager {

    private static DataMap dataMap = new DataMap(new HashMap<>());

    public static void setDataMap(DataMap newDataMap) {
        dataMap = newDataMap;
        MinecraftForge.EVENT_BUS.post(new ClientDatapackUpdatedEvent(Minecraft.getInstance().player));
    }

    public static void onServerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        Datapacks.LOGGER.info("Client disconnected, clearing client dataMap");
        dataMap = new DataMap(new HashMap<>());
    }

    public static <T> T get(DatapackKey<T> key) {
        return dataMap.get(key);
    }
}
