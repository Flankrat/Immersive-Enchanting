package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.Cost;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantingTableContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Server-authoritative menu for the custom enchanting table.
 *
 * <p>This menu handles:
 * <ul>
 *     <li>Inventory slot layout (tool, fuel, cost, and player inventory)</li>
 *     <li>Detection of nearby chiseled bookshelves</li>
 *     <li>Extraction of "ancient book" enchantments from those shelves</li>
 *     <li>Maintaining a list of available enchantments based on environment</li>
 * </ul>
 *
 * <p><b>Client/Server Notes:</b>
 * <ul>
 *     <li>Enchantment discovery logic runs only on the server</li>
 * </ul>
 */
public class EnchantingTableMenu extends AbstractContainerMenu {

    public enum Slots {
        TOOL(0),
        ENCHANTING_FUEL(1),
        COST(2);

        private final int id;

        Slots(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }
    }

    private final BlockPos blockPos;
    private final ContainerLevelAccess access;
    private final Container container;
    private final Level level;

    private List<Holder<Enchantment>> availableEnchantments = new ArrayList<>();

    /** Fork: bookshelf power around the table as last sent by the server (client side), for the tier requirement. */
    private int bookshelfPower = 0;

    public void setBookshelfPower(int bookshelfPower) {
        this.bookshelfPower = bookshelfPower;
    }

    public int getBookshelfPower() {
        return bookshelfPower;
    }

    /**
     * Client-side constructor used when the menu is opened.
     *
     * <p>Reads the {@link BlockPos} from the network buffer and resolves the position
     * from the player instance.</p>
     *
     * @param containerId     The container ID assigned by Minecraft
     * @param playerInventory The player's inventory
     * @param buf             Network buffer containing synced data (block position)
     */
    public EnchantingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level(), buf.readBlockPos());
    }

    /**
     * Primary constructor for the enchanting table menu.
     *
     * <p>Initializes the container, slot layout, and scans for available enchantments
     * if running on the server.</p>
     *
     * @param containerId     The container ID
     * @param playerInventory The player's inventory
     * @param level           The position the menu is operating in
     * @param pos             The position of the enchanting table block
     */
    public EnchantingTableMenu(int containerId, Inventory playerInventory, @Nullable Level level, @Nullable BlockPos pos) {
        super(ModMenus.ENCHANTING_TABLE_MENU.get(), containerId);
        this.blockPos = pos;
        this.level = level;
        this.access = ContainerLevelAccess.create(level, pos);

        Container chosenContainer = null;
        if (level != null && !level.isClientSide() && pos != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EnchantmentTableBlockEntity enchantingBE) {
                chosenContainer = new EnchantingTableContainer(enchantingBE);
            }
        }
        this.container = chosenContainer != null ? chosenContainer : new SimpleContainer(3);

        buildSlots(playerInventory);
    }

    /**
     * Gets the block position of the enchanting table associated with this menu.
     <P>
     * @return the {@link BlockPos} of the enchanting table
     */
    public BlockPos getBlockPos() {
        return blockPos;
    }

    /**
     * Builds and registers all menu slots, including internal container slots and player inventory.
     <P>
     * <p>Slot layout includes:</p>
     <P>
     * <ul>
     *     <li>Tool slot (max stack size 1)</li>
     *     <li>Enchanting fuel slot</li>
     *     <li>Cost slot</li>
     *     <li>Player inventory (3 rows)</li>
     *     <li>Hotbar (1 row)</li>
     * </ul>
     <P>
     * @param playerInventory the player's inventory used to populate inventory slots
     */
    private void buildSlots(Inventory playerInventory) {
        //Tool slot
        this.addSlot(new Slot(this.container, Slots.TOOL.id(), 233, 141) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        //Enchanting fuel slot
        this.addSlot(new Slot(this.container, Slots.ENCHANTING_FUEL.id(), 233, 199));

        //Cost slot
        this.addSlot(new Slot(this.container, Slots.COST.id(), 233, 170));

        //Add player inventory slots
        int startX = 17;
        int startY = 140;

        //Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }

        //Hotbar slots
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, startX + col * 18, startY + 58));
        }
    }

    /**
     * Handles shift-click (quick move) logic for transferring items between slots.
     <P>
     * <p>Behaviour:</p>
     <P>
     * <ul>
     *     <li>Items in menu slots are moved to player inventory</li>
     *     <li>Enchantable items and ancient books go to the tool slot</li>
     *     <li>Valid enchanting fuels go to the fuel slot</li>
     *     <li>All other items go to the cost slot</li>
     * </ul>
     <P>
     * @param player the player performing the action
     * @param i the index of the clicked slot
     * @return the moved {@link ItemStack}, or {@link ItemStack#EMPTY} if the move failed
     */
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int i) {
        ItemStack stack = ItemStack.EMPTY;
        if(level == null) return stack;

        Slot slot = getSlot(i);

        if(!slot.hasItem()) return stack;
        stack = slot.getItem();

        //Quick move into inventory
        if(i == Slots.TOOL.id()) {
            if(!this.moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
            slot.setChanged(); // notify the BE-backed container so the renderer updates
        } else if (i == Slots.ENCHANTING_FUEL.id()) {
            if (!this.moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
            slot.setChanged();
        } else if (i == Slots.COST.id()) {
            if (!this.moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
            slot.setChanged();
        } else {
            CostRegistry registry;
            if(level != null && !level.isClientSide()) {
                registry = CostRegistry.server();
            } else {
                registry = CostRegistry.client();
            }

            List<Cost> enchantingFuels = registry.get(CostRegistry.ENCHANTING_FUELS)
                    .levelCosts().getAllLevels();

            Set<Item> enchantingFuelItems = new HashSet<>();
            for(Cost fuelCost : enchantingFuels) {
                for(ItemStack fuelStack : fuelCost.getItemStacks()) {
                    enchantingFuelItems.add(fuelStack.getItem());
                }
            }
            // fork: fuels that cost files name themselves (e.g. dragon blood for Supreme) shift-click into the fuel slot too
            for(Holder<Enchantment> enchantmentHolder : registry.getAllEnchantmentHolders()) {
                for(Cost cost : registry.get(enchantmentHolder).levelCosts().getAllLevels()) {
                    if(!cost.hasFuelOverride()) continue;
                    for(ItemStack fuelStack : cost.fuel().getItemStacks()) enchantingFuelItems.add(fuelStack.getItem());
                }
            }

            //Quick move into menu
            if(stack.getItem().isEnchantable(stack) || stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK) || stack.is(ModItems.ANCIENT_BOOK.get()) || stack.is(ModItems.CREATIVE_BOOKSHELF_ITEM.get())) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else if(enchantingFuelItems.contains(stack.getItem())) {
                if (!this.moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(stack, 2, 3, false)) return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack;
    }

    /**
     * Determines whether the player can continue interacting with this menu.
     <P>
     * <p>This checks that the player is still within usable distance of the enchanting table.</p>
     <P>
     * @param player the player interacting with the menu
     * @return {@code true} if the menu is still valid and usable
     */
    @Override
    public boolean stillValid(@NotNull Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    /**
     * Called when the menu is closed.
     <P>
     * <p>On the server side, all items in the internal container are returned to the player.
     * If the player's inventory is full, the items are dropped into the world.</p>
     <P>
     * @param player the player closing the menu
     */
    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);

        if (player.level().isClientSide()) return;

        // BE-backed container: leave the items in the block. They persist in
        // NBT and will be visible to the next player who opens the table.
        if (this.container instanceof EnchantingTableContainer) return;

        // Fallback path: return items to the player so they aren't lost.
        for (int i = 0; i < this.container.getContainerSize(); i++) {
            ItemStack stack = this.container.removeItemNoUpdate(i);
            if (!stack.isEmpty()) player.getInventory().placeItemBackInInventory(stack);
        }
    }

    /**
     * Retrieves a slot by its enum type.
     <P>
     * @param slot the {@link Slots} enum representing the desired slot
     * @return the corresponding {@link Slot}
     */
    public Slot getSlot(Slots slot) {
        return this.getSlot(slot.id());
    }

    /**
     * Gets the tool slot used for placing the item to be enchanted.
     <P>
     * @return the tool {@link Slot}
     */
    public Slot getToolSlot() {
        return this.getSlot(Slots.TOOL.id());
    }

    /**
     * Gets the slot used for enchanting fuel items.
     <P>
     * @return the fuel {@link Slot}
     */
    public Slot getFuelSlot() {
        return this.getSlot(Slots.ENCHANTING_FUEL.id());
    }

    /**
     * Gets the slot used for cost payment items.
     <P>
     * @return the cost {@link Slot}
     */
    public Slot getCostSlot() {
        return this.getSlot(Slots.COST.id());
    }

    /**
     * Replaces the current list of available enchantments.
     <P>
     * <p>This is typically called when the server determines available enchantments
     * based on nearby bookshelves or configuration rules.</p>
     <P>
     * @param availableEnchantments the new list of available enchantments
     */
    public void setAvailableEnchantments(List<Holder<Enchantment>> availableEnchantments) {
        this.availableEnchantments.clear();
        this.availableEnchantments.addAll(availableEnchantments);
    }

    /**
     * Retrieves the list of enchantments currently available for use.
     <P>
     * @return a list of available {@link Enchantment} holders
     */
    public List<Holder<Enchantment>> getAvailableEnchantments() {
        return availableEnchantments;
    }

    /**
     * Checks whether a specific enchantment is available in this menu.
     <P>
     * @param enchantmentHolder the enchantment to check
     * @return {@code true} if the enchantment is available
     */
    public boolean isEnchantmentAvailable(Holder<Enchantment> enchantmentHolder) {
        return getAvailableEnchantments().contains(enchantmentHolder);
    }
}
