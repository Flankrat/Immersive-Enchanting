package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.CommonCodecs;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodecBuilder;
import me.alfie.immersiveenchanting.alfinolib.util.codec.ItemCost;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.List;

/**
 * One way to pay for a level. Two optional fork additions change its fuel:
 * <ul>
 *   <li>{@code fuel_count}: how many of the enchanting_fuels file's fuel this level takes; 0 (default) uses that
 *       file's amount for the level, as before. Tier upgrades use it for their lapis cost.</li>
 *   <li>{@code fuel}: a different fuel for this level, an item or tag with its own count, e.g.
 *       {@code "fuel": {"tag": "immersiveenchanting:supreme_fuel", "count": 2}}. It replaces the enchanting_fuels
 *       fuel (and {@code fuel_count}) entirely.</li>
 * </ul>
 */
public record Cost(ItemCost itemCost, int xpLevels, int fuelCount, ItemCost fuel) {

    /** A cost without a fuel override, as before the fork. */
    public Cost(ItemCost itemCost, int xpLevels) {
        this(itemCost, xpLevels, 0, ItemCost.EMPTY);
    }

    /** A cost with only a fuel amount override. */
    public Cost(ItemCost itemCost, int xpLevels, int fuelCount) {
        this(itemCost, xpLevels, fuelCount, ItemCost.EMPTY);
    }

    /** Whether this level names its own fuel item instead of the enchanting_fuels one. */
    public boolean hasFuelOverride() {
        return !ItemCost.EMPTY.equals(fuel);
    }

    public static final Codec<Cost> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemCost.CODEC.fieldOf("item_cost").forGetter(Cost::itemCost),
                    Codec.INT.optionalFieldOf("xp_levels", 0).forGetter(Cost::xpLevels),
                    Codec.INT.optionalFieldOf("fuel_count", 0).forGetter(Cost::fuelCount),
                    ItemCost.CODEC.optionalFieldOf("fuel", ItemCost.EMPTY).forGetter(Cost::fuel)
            ).apply(instance, (ItemCost item, Integer xp, Integer fuelCount, ItemCost fuel) -> new Cost(item, xp, fuelCount, fuel))
    );

    public static final StreamCodec<FriendlyByteBuf, Cost> STREAM_CODEC =
            StreamCodecBuilder.<FriendlyByteBuf, Cost>create()
                    .add(ItemCost.STREAM_CODEC, Cost::itemCost)
                    .add(CommonCodecs.VAR_INT, Cost::xpLevels)
                    .add(CommonCodecs.VAR_INT, Cost::fuelCount)   // synced so the tooltip shows the right fuel
                    .add(ItemCost.STREAM_CODEC, Cost::fuel)       // same, for a fuel item override
                    .build((ItemCost item, Integer xp, Integer fuelCount, ItemCost fuel) -> new Cost(item, xp, fuelCount, fuel));

    public static final Cost EMPTY = new Cost(ItemCost.EMPTY, 0, 0, ItemCost.EMPTY);

    /**Convenience method*/
    public List<ItemStack> getItemStacks() {
        return itemCost.getItemStacks();
    }

    /**
     * Returns this cost if it is valid, null if none found
     */
    public @Nullable Cost test(ItemStack testStack, Player player) {
        if( (itemCost().isValid(testStack) || itemCost().getItems().contains(Items.AIR)) && player.experienceLevel >= xpLevels) {
            return this;
        }
        return null;
    }
}
