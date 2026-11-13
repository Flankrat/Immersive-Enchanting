package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.google.gson.JsonElement;
import me.alfie.immersiveenchanting.alfinolib.datapacks.DatapackDefinition;
import me.alfie.immersiveenchanting.alfinolib.datapacks.DatapackKey;
import me.alfie.immersiveenchanting.alfinolib.datapacks.DatapackRegistry;
import me.alfie.immersiveenchanting.alfinolib.datapacks.ModDatapack;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;

public class NodeSoundsDatapack extends ModDatapack<NodeSoundMap, NodeSoundMap> {

    public static DatapackKey<NodeSoundMap> KEY = new DatapackKey<>(ImmersiveEnchanting.MODID, "sounds");
    public static DatapackDefinition<NodeSoundMap> DEFINITION = new DatapackDefinition<>(KEY, NodeSoundMap.STREAM_CODEC);

    private NodeSoundMap DATA = new NodeSoundMap(new HashMap<>());


    public NodeSoundsDatapack(RegistryAccess registryAccess) {
        super(NodeSoundMap.CODEC, DEFINITION, registryAccess);
    }

    @Override public NodeSoundMap getData() {
        return DATA;
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> input, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "node_sounds");
        DATA = parseOrDefault(input.get(key), new NodeSoundMap(new HashMap<>()));

        ImmersiveEnchanting.LOGGER.debug("Found {}", String.valueOf(DATA));
    }
}
