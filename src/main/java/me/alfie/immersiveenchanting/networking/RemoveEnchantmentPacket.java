package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.alfinolib.networking.NetworkPacket;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Client-to-server packet requesting removal of an enchantment position.
 *
 * <p>If allowed by server configuration, reduces the specified enchantment by one position.
 * If the resulting position reaches zero, the enchantment is fully removed from the item.</p>
 *
 * <p>No action is taken if:
 * <ul>
 *     <li>Enchantment removal is disabled in config</li>
 *     <li>The item has no matching enchantment</li>
 *     <li>The enchantment position is already zero</li>
 * </ul>
 *
 * <p>On success, the item is updated server-side and feedback effects are triggered.</p>
 */
public record RemoveEnchantmentPacket(ResourceKey<Enchantment> enchantmentKey, int level) implements NetworkPacket<RemoveEnchantmentPacket> {

    public static StreamCodec<FriendlyByteBuf, RemoveEnchantmentPacket> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, RemoveEnchantmentPacket>() {
        @Override
        public void encode(FriendlyByteBuf buf, RemoveEnchantmentPacket packet) {
            ModPackets.ENCHANTMENT_CODEC.encode(buf, packet.enchantmentKey);
            buf.writeInt(packet.level);
        }

        @Override
        public RemoveEnchantmentPacket decode(FriendlyByteBuf buf) {
            return new RemoveEnchantmentPacket(ModPackets.ENCHANTMENT_CODEC.decode(buf), buf.readInt());
        }
    };

    @Override
    public void exec(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (!(player.containerMenu instanceof EnchantingTableMenu menu)) return;
        if(!ServerConfig.isEnchantmentRemovalAllowed()) return;

        Holder<Enchantment> enchantmentHolder = EnchantmentUtil.toHolder(enchantmentKey,
                player.level().registryAccess());

        ItemStack stack = menu.getToolSlot().getItem();
        Map<Enchantment, Integer> itemEnchantments = EnchantmentUtil.getEnchantments(stack);

        int currentLevel = itemEnchantments.get(enchantmentHolder.get());
        if (currentLevel <= 0) return;

        int newLevel = currentLevel - 1;

        itemEnchantments.remove(enchantmentHolder.value());
        if (newLevel > 0) {
            itemEnchantments.put(enchantmentHolder.value(), level());
        }

        EnchantmentHelper.setEnchantments(itemEnchantments, stack);

        applyEnchantments(stack, itemEnchantments);

        menu.getToolSlot().set(EnchantmentUtil.tryConvertVanillaBook(stack));

        FxHelper.playEnchantmentRemove(context.getSender().level(), menu.getBlockPos());
    }

    private static void applyEnchantments(ItemStack stack, Map<Enchantment, Integer> map) {
        if (stack.is(Items.ENCHANTED_BOOK)) {
            ListTag list = new ListTag();

            for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                Enchantment ench = entry.getKey();
                int lvl = entry.getValue();

                CompoundTag tag = new CompoundTag();
                tag.putString("id", BuiltInRegistries.ENCHANTMENT.getKey(ench).toString());
                tag.putShort("lvl", (short) lvl);

                list.add(tag);
            }

            stack.addTagElement("StoredEnchantments", list);
        } else {
            EnchantmentHelper.setEnchantments(map, stack);
        }
    }
}
