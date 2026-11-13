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
 * One way to pay for a level. {@code fuel_count} (fork addition, optional) overrides how much enchanting fuel this
 * level takes; 0 (default) uses the enchanting_fuels file's amount for the level, as before. Tier upgrades use it
 * for their flat lapis cost (e.g. 5 to Advanced, 10 to Supreme).
 */
public record Cost(ItemCost itemCost, int xpLevels, int fuelCount) {

    /** A cost without a fuel override, as before the fork. */
    public Cost(ItemCost itemCost, int xpLevels) {
        this(itemCost, xpLevels, 0);
    }

    public static final Codec<Cost> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemCost.CODEC.fieldOf("item_cost").forGetter(Cost::itemCost),
                    Codec.INT.optionalFieldOf("xp_levels", 0).forGetter(Cost::xpLevels),
                    Codec.INT.optionalFieldOf("fuel_count", 0).forGetter(Cost::fuelCount)
            ).apply(instance, Cost::new)
    );

    public static final StreamCodec<FriendlyByteBuf, Cost> STREAM_CODEC =
            StreamCodecBuilder.<FriendlyByteBuf, Cost>create()
                    .add(ItemCost.STREAM_CODEC, Cost::itemCost)
                    .add(CommonCodecs.VAR_INT, Cost::xpLevels)
                    .add(CommonCodecs.VAR_INT, Cost::fuelCount)   // synced so the tooltip shows the right fuel
                    .build((ItemCost item, Integer xp, Integer fuel) -> new Cost(item, xp, fuel));

    public static final Cost EMPTY = new Cost(ItemCost.EMPTY, 0, 0);

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
