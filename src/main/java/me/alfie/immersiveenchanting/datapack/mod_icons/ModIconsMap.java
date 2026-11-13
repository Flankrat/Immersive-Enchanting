package me.alfie.immersiveenchanting.datapack.mod_icons;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public record ModIconsMap(Map<String, ResourceLocation> map) {

    public static final Codec<ModIconsMap> CODEC =
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
                    .xmap(ModIconsMap::new, ModIconsMap::map);


    public static final StreamCodec<FriendlyByteBuf, ModIconsMap> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, ModIconsMap>() {
        @Override
        public void encode(FriendlyByteBuf buf, ModIconsMap modIconsMap) {
            Map<String, ResourceLocation> map = modIconsMap.map();

            buf.writeVarInt(map.size());

            for (Map.Entry<String, ResourceLocation> entry : map.entrySet()) {
                buf.writeUtf(entry.getKey());
                buf.writeResourceLocation(entry.getValue());
            }
        }

        @Override
        public ModIconsMap decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();

            Map<String, ResourceLocation> map = new HashMap<>(size);

            for (int i = 0; i < size; i++) {
                String key = buf.readUtf();
                ResourceLocation value = buf.readResourceLocation();

                map.put(key, value);
            }

            return new ModIconsMap(map);
        }
    };

    /**
     * Returns the id as an item stack. If there is none defined, falls back to ancient book.
     * @param modid
     * @return
     */
    public ItemStack getAsItemStack(String modid) {
        ResourceLocation id = map().get(modid);
        if (id == null) return new ItemStack(ModItems.ANCIENT_BOOK.get());

        return BuiltInRegistries.ITEM.containsKey(id)
                ? new ItemStack(BuiltInRegistries.ITEM.get(id))
                : ItemStack.EMPTY;
    }
}
