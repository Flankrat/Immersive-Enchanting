package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record CostHolder(List<Cost> costs) {

    public static final Codec<CostHolder> CODEC = Codec.list(
            Cost.CODEC)
            .xmap(
                    CostHolder::new,
                    CostHolder::costs
            );

    public static final StreamCodec<FriendlyByteBuf, CostHolder> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, CostHolder>() {
        @Override
        public void encode(FriendlyByteBuf buf, CostHolder costHolder) {
            List<Cost> costs = costHolder.costs;

            buf.writeVarInt(costs.size());
            for (Cost cost : costs) {
                Cost.STREAM_CODEC.encode(buf, cost);
            }
        }

        @Override
        public CostHolder decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();

            List<Cost> costs = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                costs.add(Cost.STREAM_CODEC.decode(buf));
            }

            return new CostHolder(costs);
        }
    };

    public static final CostHolder EMPTY = new CostHolder(List.of());
}
