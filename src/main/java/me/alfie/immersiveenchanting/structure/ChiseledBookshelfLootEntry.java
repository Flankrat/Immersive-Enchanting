package me.alfie.immersiveenchanting.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.alfinolib.util.codec.ItemCost;
import me.alfie.immersiveenchanting.alfinolib.util.codec.ItemCostIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a simple loot entry used for probabilistic item generation.
 *
 * <p>Each entry defines an item (or tag group) and a per-slot chance of it appearing.
 */
public record ChiseledBookshelfLootEntry(Item item, float chancePerSlot) {

    public static final Codec<ChiseledBookshelfLootEntry> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ChiseledBookshelfLootEntry::item),
                    Codec.FLOAT.fieldOf("chancePerSlot").forGetter(ChiseledBookshelfLootEntry::chancePerSlot)
            ).apply(instance, ChiseledBookshelfLootEntry::new));
}
