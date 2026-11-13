package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tier upgrades (fork addition): an enchantment whose cost file has {@code "upgrades_from": "<enchantment>"} is the
 * next tier of that enchantment, e.g. Advanced Sharpness upgrades from Sharpness.
 * <ul>
 *   <li>Level 1 of a tier enchantment can only be bought when the item has the previous tier at its max level
 *       (the highest level in the previous tier's cost file). Buying it replaces the previous tier.</li>
 *   <li>Its higher levels are bought like any other enchantment (each needs the level below).</li>
 *   <li>Tier enchantments need no Ancient Book: having the previous tier is what unlocks them.</li>
 *   <li>Each tier needs more bookshelf power around the table ({@code bookshelfPowerPerTier} in the server config):
 *       tier 0 is every normal enchantment, tier 1 upgrades from a tier-0 one (Advanced), tier 2 from a tier-1 one
 *       (Supreme), and so on.</li>
 * </ul>
 * Used by the client (to build the tree) and the server (to check and apply), so both always agree.
 */
public final class TierUpgrades {

    /** Longest chain followed when working out a tier, so a broken datapack with a loop can't hang the game. */
    private static final int MAX_DEPTH = 8;

    private TierUpgrades() {
    }

    /** Whether this enchantment is a tier upgrade of another one. */
    public static boolean isTier(CostRegistry registry, Holder<Enchantment> enchantment) {
        return registry.isRegistered(enchantment) && registry.get(enchantment).isTier();
    }

    /** The enchantment this one upgrades from, or null for a normal enchantment (or one naming a missing enchantment). */
    public static @Nullable Holder<Enchantment> previousTier(CostRegistry registry, Holder<Enchantment> enchantment, RegistryAccess access) {
        if (!isTier(registry, enchantment)) return null;
        String from = registry.get(enchantment).upgradesFrom();
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, ResourceId.parse(from).mc());
        var holder = access.registryOrThrow(Registries.ENCHANTMENT).getHolder(key);
        if (holder.isEmpty()) {
            ImmersiveEnchanting.LOGGER.warn("Enchantment cost file upgrades_from names {}, which isn't registered", from);
            return null;
        }
        return holder.get();
    }

    /** 0 for normal enchantments, 1 for one that upgrades from a normal one, 2 for the tier after that, ... */
    public static int tierIndex(CostRegistry registry, Holder<Enchantment> enchantment, RegistryAccess access) {
        int tier = 0;
        Holder<Enchantment> current = enchantment;
        while (tier < MAX_DEPTH) {
            Holder<Enchantment> previous = previousTier(registry, current, access);
            if (previous == null) break;
            current = previous;
            tier++;
        }
        return tier;
    }

    /** The highest level IE sells: the cost file's highest level, or the enchantment's own max without a cost file. */
    public static int maxLevel(CostRegistry registry, Holder<Enchantment> enchantment) {
        if (registry.isRegistered(enchantment) && !registry.get(enchantment).levelCosts().levelCostMap().isEmpty())
            return registry.get(enchantment).levelCosts().maxLevel();
        return enchantment.value().getMaxLevel();
    }

    /** Whether the item has this tier enchantment's previous tier at its max level, so level 1 can be bought. */
    public static boolean previousTierMaxed(CostRegistry registry, ItemStack stack, Holder<Enchantment> tier, RegistryAccess access) {
        Holder<Enchantment> previous = previousTier(registry, tier, access);
        if (previous == null) return false;
        return EnchantmentUtil.getEnchantmentLevel(stack, previous) >= maxLevel(registry, previous);
    }

    /**
     * Whether the tier enchantment fits next to the item's other enchantments once the previous tier is gone
     * (the previous tier itself is usually incompatible with it, but it's being replaced).
     */
    public static boolean compatibleAfterUpgrade(ItemStack stack, Enchantment tier, @Nullable Enchantment previous) {
        Set<Enchantment> others = new HashSet<>(EnchantmentUtil.getEnchantments(stack).keySet());
        others.remove(tier);
        if (previous != null) others.remove(previous);
        return EnchantmentHelper.isEnchantmentCompatible(others, tier);
    }

    /** Bookshelf power needed for an enchantment of this tier (the last configured value covers higher tiers). */
    public static int requiredBookshelfPower(int tierIndex) {
        List<? extends Integer> perTier = ServerConfig.getBookshelfPowerPerTier();
        if (perTier.isEmpty()) return 0;
        return perTier.get(Math.min(tierIndex, perTier.size() - 1));
    }
}
