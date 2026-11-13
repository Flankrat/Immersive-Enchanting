package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.alfinolib.util.codec.ItemCost;
import net.minecraft.core.RegistryAccess;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.Cost;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostHolder;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

public class CostHelper {
    public static boolean canEnchant(EnchantingTableMenu menu,
                                     Holder<Enchantment> enchantmentHolder, int level,
                                     Player player) {
        if(!CostRegistry.server().isRegistered(enchantmentHolder) ||
            !CostRegistry.server().isRegistered(CostRegistry.ENCHANTING_FUELS)) return false;
        if(!CostRegistry.server().get(enchantmentHolder).enabled()) return false;

        // fork: tier upgrades (Advanced/Supreme...) are unlocked by having the previous tier, not by an Ancient Book
        CostRegistry registry = CostRegistry.server();
        RegistryAccess access = player.level().registryAccess();
        boolean isTier = TierUpgrades.isTier(registry, enchantmentHolder);
        if(!isTier && !isEnchantmentAvailableInBookshelves(enchantmentHolder, menu, player)) return false;

        // fork: each tier needs more bookshelf power around the table
        int requiredPower = TierUpgrades.requiredBookshelfPower(TierUpgrades.tierIndex(registry, enchantmentHolder, access));
        if(BookshelfChecker.getBookshelfPower(menu.getBlockPos(), player.level()) < requiredPower) return false;

        ItemStack stackToEnchant = menu.getToolSlot().getItem();
        boolean isBook = stackToEnchant.is(Items.BOOK) || stackToEnchant.is(Items.ENCHANTED_BOOK);

        if(isTier) {
            // Tier rules apply to books too: a book needs the previous tier maxed just like a sword does.
            Holder<Enchantment> previous = TierUpgrades.previousTier(registry, enchantmentHolder, access);
            if(previous == null) return false;
            // Always the next level (this is also where JLME's per-item level caps hook in)
            if(!isEnchantmentNextLevel(stackToEnchant, enchantmentHolder, level)) return false;
            if(level == 1) {
                // Level 1 replaces the previous tier, which has to be at its max
                if(!TierUpgrades.previousTierMaxed(registry, stackToEnchant, enchantmentHolder, access)) return false;
                if(!TierUpgrades.compatibleAfterUpgrade(stackToEnchant, enchantmentHolder.value(), previous.value())) return false;
            }
            if(!isBook && !enchantmentHolder.get().canEnchant(stackToEnchant)) return false;
        } else if(!isBook) {
            if(!isEnchantmentNextLevel(stackToEnchant, enchantmentHolder, level)) return false;
            if(!enchantmentHolder.get().canEnchant(stackToEnchant)) return false;
        }

        return tryConsumeValidCostAndFuel(
                EnchantmentUtil.toId(enchantmentHolder), level,
                menu, player);
    }

    public static boolean canTransmute(EnchantingTableMenu menu, Holder<Enchantment> oldEnchantment, Player player) {
        if(!CostRegistry.server().isRegistered(CostRegistry.TRANSMUTE)) return false;
        if(!CostRegistry.server().get(CostRegistry.TRANSMUTE).enabled()) return false;
        if(!isEnchantmentAvailableInBookshelves(oldEnchantment, menu, player)) return false;
        if(EnchantmentUtil.isReplicated(menu.getToolSlot().getItem())) return false;

        return tryConsumeValidCostAndFuel(
                CostRegistry.TRANSMUTE, 1,
                menu, player);
    }

    public static boolean canReplicate(EnchantingTableMenu menu, Player player) {
        if(!CostRegistry.server().isRegistered(CostRegistry.REPLICATE)) return false;
        if(!CostRegistry.server().get(CostRegistry.REPLICATE).enabled()) return false;

        return tryConsumeValidCostAndFuel(
                CostRegistry.REPLICATE, 1,
                menu, player);
    }

    private static boolean isEnchantmentAvailableInBookshelves(Holder<Enchantment> enchantmentHolder, EnchantingTableMenu menu, Player player) {
        List<Holder<Enchantment>> availableEnchantments = BookshelfChecker.getEnchantmentsInBookshelves(menu.getBlockPos(), player.level());
        return availableEnchantments.contains(enchantmentHolder);
    }

    private static boolean isEnchantmentNextLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder, int level) {
        int equippedLevel = EnchantmentUtil.getEnchantmentLevel(stack, enchantmentHolder);
        return level == equippedLevel + 1;
    }

    private static boolean tryConsumeValidCostAndFuel(ResourceId costId,
                                                      int costLevel,
                                                      EnchantingTableMenu menu,
                                                      Player player) {
        if(player.isCreative()) return true;
        CostHolder costHolder = CostRegistry.server().get(costId).levelCosts().getLevel(costLevel);
        CostHolder fuelHolder = CostRegistry.server().get(CostRegistry.ENCHANTING_FUELS).levelCosts().getLevel(costLevel);

        // fork fix: the original looped over every alternative without stopping, so only the LAST alternative's
        // result counted (a valid first alternative was thrown away). Now the first alternative the player can
        // pay, including its fuel, is used.
        for(Cost cost : costHolder.costs()) {
            if(cost.test(menu.getCostSlot().getItem(), player) == null) continue;

            for(Cost fuel : fuelFor(cost, fuelHolder)) {
                if(fuel.test(menu.getFuelSlot().getItem(), player) == null) continue;

                cost.itemCost().tryConsume(menu.getCostSlot().getItem());
                fuel.itemCost().tryConsume(menu.getFuelSlot().getItem());
                player.giveExperienceLevels(-cost.xpLevels());
                return true;
            }
        }
        return false;
    }

    /**
     * Fork addition: the fuel options for paying {@code cost}. Normally the enchanting_fuels file's options for the
     * level; when the cost names its own {@code fuel}, just that; when it has a {@code fuel_count} override, the same fuel items but that many of them (falling back
     * to the fuel file's level 1 items when it has no entry for this level). Used by the server and the tooltip.
     */
    public static List<Cost> fuelFor(Cost cost, CostHolder fuelForLevel) {
        if(cost.hasFuelOverride()) return List.of(new Cost(cost.fuel(), 0)); // its own fuel item, e.g. dragon blood
        if(cost.fuelCount() <= 0) return fuelForLevel.costs();
        List<Cost> fuelOptions = fuelForLevel.costs();
        if(fuelOptions.isEmpty() && CostRegistry.server().isRegistered(CostRegistry.ENCHANTING_FUELS)) {
            fuelOptions = CostRegistry.server().get(CostRegistry.ENCHANTING_FUELS).levelCosts().getLevel(1).costs();
        }
        return overrideCount(fuelOptions, cost.fuelCount());
    }

    /** Copies of the given fuel options with their item count replaced (fork addition). */
    public static List<Cost> overrideCount(List<Cost> fuelOptions, int count) {
        List<Cost> result = new ArrayList<>();
        for(Cost fuel : fuelOptions) {
            ItemCost item = fuel.itemCost();
            result.add(new Cost(new ItemCost(item.ingredient(), count, item.data()), fuel.xpLevels()));
        }
        return result;
    }
}
