package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;

import java.util.*;
import java.util.stream.Collectors;

public record CostLevels(Map<Integer, CostHolder> levelCostMap) {

    public static final Codec<CostLevels> CODEC =
            Codec.unboundedMap(
                    Codec.STRING,  // Read JSON keys as strings
                    CostHolder.CODEC).xmap(
                    map -> {
                        // Convert string keys to int
                        Map<Integer, CostHolder> intMap = map.entrySet().stream()
                                .collect(Collectors.toMap(
                                        e -> Integer.parseInt(e.getKey()),
                                        Map.Entry::getValue
                                ));
                        return new CostLevels(intMap);
                    },
                    levels -> {
                        // Convert int keys back to strings when writing JSON
                        return levels.levelCostMap().entrySet().stream()
                                .collect(Collectors.toMap(
                                        e -> e.getKey().toString(),
                                        Map.Entry::getValue
                                ));
                    }
            );



    public static final StreamCodec<FriendlyByteBuf, CostLevels> STREAM_CODEC = new me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec<FriendlyByteBuf, CostLevels>() {
        @Override
        public void encode(FriendlyByteBuf buf, CostLevels costLevels) {
            Map<Integer, CostHolder> map = costLevels.levelCostMap();

            buf.writeVarInt(map.size());

            for(Map.Entry<Integer, CostHolder> entry : map.entrySet()) {
                buf.writeVarInt(entry.getKey());
                CostHolder.STREAM_CODEC.encode(buf, entry.getValue());
            }
        }

        @Override
        public CostLevels decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            Map<Integer, CostHolder> map = new HashMap<>();

            for (int i = 0; i < size; i++) {
                int key = buf.readVarInt();
                CostHolder value = CostHolder.STREAM_CODEC.decode(buf);

                map.put(key, value);
            }

            return new CostLevels(map);
        }
    };

    public CostHolder getLevel(int level) {
        if(!levelCostMap.containsKey(level)) return CostHolder.EMPTY;
        return levelCostMap.get(level);
    }

    public int maxLevel() {
        return Collections.max(levelCostMap.keySet());
    }

    /**
     * Returns a list of all valid costs for this enchantment, ignoring the position.
     * @return
     */
    public List<Cost> getAllLevels() {
        List<Cost> result = new ArrayList<>();

        for(CostHolder holder : levelCostMap().values()) {
            result.addAll(holder.costs());
        }

        return result;
    }

}
