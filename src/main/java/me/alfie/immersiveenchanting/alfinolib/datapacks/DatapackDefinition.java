package me.alfie.immersiveenchanting.alfinolib.datapacks;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;

public record DatapackDefinition<T>(DatapackKey<T> key, StreamCodec<FriendlyByteBuf, T> streamCodec) {
}
