package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record NodeSound(ResourceLocation sound, float volume, float pitch) {

    public static final Codec<NodeSound> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("sound").forGetter(NodeSound::sound),
                    Codec.FLOAT.fieldOf("volume").forGetter(NodeSound::volume),
                    Codec.FLOAT.fieldOf("pitch").forGetter(NodeSound::pitch)
            ).apply(instance, NodeSound::new));

    public static final StreamCodec<FriendlyByteBuf, NodeSound> STREAM_CODEC =
            StreamCodec.of(NodeSound::encode, NodeSound::decode);

    private static void encode(FriendlyByteBuf buf, NodeSound sound) {
        buf.writeResourceLocation(sound.sound());
        buf.writeFloat(sound.volume());
        buf.writeFloat(sound.pitch());
    }

    private static NodeSound decode(FriendlyByteBuf buf) {
        return new NodeSound(
                buf.readResourceLocation(),
                buf.readFloat(),
                buf.readFloat()
        );
    }
}
