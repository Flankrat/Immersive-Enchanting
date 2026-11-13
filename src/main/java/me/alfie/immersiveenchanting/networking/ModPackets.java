package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.alfinolib.networking.NetworkRegisterEvent;
import me.alfie.immersiveenchanting.alfinolib.networking.Networking;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModPackets {

    public static final StreamCodec<FriendlyByteBuf, ResourceKey<Enchantment>> ENCHANTMENT_CODEC = new StreamCodec<FriendlyByteBuf, ResourceKey<Enchantment>>() {
        @Override
        public void encode(FriendlyByteBuf buf, ResourceKey<Enchantment> enchantment) {
            buf.writeResourceKey(enchantment);
        }

        @Override
        public ResourceKey<Enchantment> decode(FriendlyByteBuf buf) {
            return buf.readResourceKey(Registries.ENCHANTMENT);
        }
    };

    public static void register(NetworkRegisterEvent event) {
        event.register(AvailableEnchantmentsPacket.class, AvailableEnchantmentsPacket.STREAM_CODEC);
        event.register(EnchantPacket.class, EnchantPacket.STREAM_CODEC);
        event.register(RemoveEnchantmentPacket.class, RemoveEnchantmentPacket.STREAM_CODEC);
        event.register(ReplicatePacket.class, ReplicatePacket.STREAM_CODEC);
        event.register(TransmutePacket.class, TransmutePacket.STREAM_CODEC);
        event.register(UpdateToolSlotPacket.class, UpdateToolSlotPacket.STREAM_CODEC);
    }
}
