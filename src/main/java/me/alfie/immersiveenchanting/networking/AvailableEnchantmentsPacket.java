package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.alfinolib.networking.NetworkPacket;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Clientbound packet that syncs available enchantments from nearby bookshelves.
 *
 * <p>This packet is sent from the server to update the client-side enchanting
 * table UI with the enchantments currently accessible in the world.</p>
 *
 * <p>If ancient books are disabled in configuration, the client is instead
 * given the full enchantment list.</p>
 *
 * <p>Used to keep the enchanting screen in sync with server-side bookshelf state.</p>
 */
public record AvailableEnchantmentsPacket(List<ResourceKey<Enchantment>> availableEnchantments,
                                          int bookshelfPower) implements NetworkPacket<AvailableEnchantmentsPacket> {

    public static final StreamCodec<FriendlyByteBuf, AvailableEnchantmentsPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NotNull FriendlyByteBuf buf, AvailableEnchantmentsPacket packet) {
            buf.writeInt(packet.availableEnchantments.size());

            for (ResourceKey<Enchantment> enchantmentKey : packet.availableEnchantments) {
                ModPackets.ENCHANTMENT_CODEC.encode(buf, enchantmentKey);
            }
            buf.writeVarInt(packet.bookshelfPower); // fork: bookshelf power around the table, for tier requirements
        }

        @Override
        public @NotNull AvailableEnchantmentsPacket decode(@NotNull FriendlyByteBuf buf) {
            int size = buf.readInt();
            List<ResourceKey<Enchantment>> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                result.add(ModPackets.ENCHANTMENT_CODEC.decode(buf));
            }

            return new AvailableEnchantmentsPacket(result, buf.readVarInt());
        }

    };

    @Override
    public void exec(NetworkEvent.Context context) {
        if(!context.getDirection().getReceptionSide().isClient()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if(!(player.containerMenu instanceof EnchantingTableMenu menu)) return;

        List<Holder<Enchantment>> enchantmentHolders = EnchantmentUtil.toHolders(
                availableEnchantments, player.level().registryAccess());

        menu.setBookshelfPower(bookshelfPower);

        if(ServerConfig.areAncientBooksRequired()) {
            menu.setAvailableEnchantments(enchantmentHolders);
        } else {
            menu.setAvailableEnchantments(CostRegistry.server().getAllEnabledEnchantmentHolders());
        }
    }

}
