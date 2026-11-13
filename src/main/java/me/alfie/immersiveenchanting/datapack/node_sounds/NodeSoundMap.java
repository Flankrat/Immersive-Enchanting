package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.alfinolib.datapacks.client.ClientDatapackManager;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record NodeSoundMap(Map<ResourceLocation, NodeSound> enchantments) {

    public static NodeSoundMap client() {
        return ClientDatapackManager.get(NodeSoundsDatapack.KEY);
    }

    public static final Codec<NodeSoundMap> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, NodeSound.CODEC).fieldOf("enchantments").forGetter(NodeSoundMap::enchantments)
    ).apply(instance, NodeSoundMap::new));

    public static final StreamCodec<FriendlyByteBuf, NodeSoundMap> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, NodeSoundMap>() {
        @Override
        public void encode(FriendlyByteBuf buf, NodeSoundMap nodeSoundMap) {
            buf.writeInt(nodeSoundMap.enchantments.size());

            for (var entry : nodeSoundMap.enchantments.entrySet()) {
                buf.writeResourceLocation(entry.getKey());
                NodeSound.STREAM_CODEC.encode(buf, entry.getValue());
            }
        }

        @Override
        public NodeSoundMap decode(FriendlyByteBuf buf) {
            int size = buf.readInt();
            Map<ResourceLocation, NodeSound> map = new HashMap<>();

            for (int i = 0; i < size; i++) {
                ResourceLocation id = buf.readResourceLocation();
                NodeSound sound = NodeSound.STREAM_CODEC.decode(buf);
                map.put(id, sound);
            }

            return new NodeSoundMap(map);
        }
    };

    public boolean containsKey(ResourceId id) {
        return enchantments().containsKey(id.mc());
    }

    public NodeSound get(ResourceId id) {
        return enchantments.get(id.mc());
    }
}
