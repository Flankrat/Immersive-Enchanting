package me.alfie.immersiveenchanting.networking;

import me.alfie.alfinolib.networking.NetworkPacket;
import me.alfie.alfinolib.networking.codec.StreamCodec;
import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.CostHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import me.alfie.immersiveenchanting.util.TierUpgrades;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Client-to-server packet requesting an enchantment to be applied.
 *
 * <p>Validates the request against server-side rules before applying:
 * <ul>
 *     <li>Enchantment compatibility</li>
 *     <li>Level progression rules</li>
 *     <li>Bookshelf availability</li>
 *     <li>Cost and fuel requirements</li>
 * </ul>
 *
 * <p>If valid, the enchantment is applied to the item and resources are consumed.
 * On success, visual and audio feedback is triggered on the client.</p>
 */
public record EnchantPacket(ResourceKey<Enchantment> enchantmentKey, int level) implements NetworkPacket<EnchantPacket> {

    public static StreamCodec<FriendlyByteBuf, EnchantPacket> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, EnchantPacket>() {
        @Override
        public void encode(FriendlyByteBuf buf, EnchantPacket packet) {
            ModPackets.ENCHANTMENT_CODEC.encode(buf, packet.enchantmentKey);
            buf.writeInt(packet.level);
        }

        @Override
        public EnchantPacket decode(FriendlyByteBuf buf) {
            return new EnchantPacket(ModPackets.ENCHANTMENT_CODEC.decode(buf), buf.readInt());
        }
    };

    @Override
    public void exec(NetworkEvent.Context context) {
        Player player = context.getSender();
        if(!(player.containerMenu instanceof EnchantingTableMenu menu)) return;

        Holder<Enchantment> enchantmentHolder = EnchantmentUtil.toHolder(enchantmentKey,
                player.level().registryAccess());

        if(tryEnchant(menu, enchantmentHolder, level, player)) {
            boolean isHighestTier = level == CostRegistry.server()
                    .get(enchantmentHolder)
                    .levelCosts()
                    .maxLevel();
            FxHelper.playEnchantSuccess(context.getSender(), menu.getBlockPos(), isHighestTier);
        }
    }

    /**
     * Checks and pays for the enchantment, then applies it to the item in the tool slot. Returns whether it was
     * applied. Split out of {@link #exec} by the fork so it can be tested without a network context.
     */
    public static boolean tryEnchant(EnchantingTableMenu menu, Holder<Enchantment> enchantmentHolder, int level, Player player) {
        ItemStack stack = menu.getToolSlot().getItem();
        if(!CostHelper.canEnchant(menu, enchantmentHolder, level, player)) return false;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        enchantments.remove(enchantmentHolder.value());

        // fork: buying level 1 of a tier upgrade replaces the previous tier (Sharpness V -> Advanced Sharpness I)
        Holder<Enchantment> previous = TierUpgrades.previousTier(CostRegistry.server(), enchantmentHolder,
                player.level().registryAccess());
        if(previous != null && level == 1) enchantments.remove(previous.value());

        enchantments.put(enchantmentHolder.get(), level);

        // fork fix: setEnchantments only ADDS stored enchantments on an enchanted book, so a removed one would
        // stay. Clearing them first makes the book end up with exactly the new set.
        if(stack.is(Items.ENCHANTED_BOOK)) stack.removeTagKey("StoredEnchantments");
        EnchantmentHelper.setEnchantments(enchantments, stack);
        menu.getToolSlot().setChanged();

        menu.getToolSlot().set(EnchantmentUtil.tryConvertVanillaBook(stack));

        return true;
    }
}
