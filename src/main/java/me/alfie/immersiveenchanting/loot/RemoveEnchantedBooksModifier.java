package me.alfie.immersiveenchanting.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import javax.swing.plaf.synth.Region;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Loot modifier that removes enchanted books from loot when disabled in config.
 */
public class RemoveEnchantedBooksModifier extends LootModifier {

    public static final Codec<RemoveEnchantedBooksModifier> CODEC = RecordCodecBuilder
            .create(inst ->
                    LootModifier.codecStart(inst).apply(inst, RemoveEnchantedBooksModifier::new));




    public RemoveEnchantedBooksModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }


    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {

        if(ServerConfig.isEnchantedBookLootMode(ServerConfig.EnchantedBookLootMode.REPLACE)) {
            for (Iterator<ItemStack> it = generatedLoot.iterator(); it.hasNext();) {
                ItemStack stack = it.next();

                if (stack.is(Items.ENCHANTED_BOOK)) {
                    List<Holder<Enchantment>> enchantments = EnchantmentUtil.enchantmentsAsHolders(stack, context.getLevel().registryAccess());
                    it.remove();

                    if (enchantments.isEmpty()) continue;

                    for(Holder<Enchantment> enchantmentHolder : enchantments) {
                        ItemStack ancientBook = new ItemStack(ModItems.ANCIENT_BOOK.get());
                        EnchantmentUtil.setStoredEnchantment(ancientBook, enchantmentHolder);

                        generatedLoot.add(ancientBook);
                    }
                }
            }
        } else if(ServerConfig.isEnchantedBookLootMode(ServerConfig.EnchantedBookLootMode.REMOVE)) {
            generatedLoot.removeIf(stack -> stack.is(Items.ENCHANTED_BOOK));
        }
        // fork: NOTHING falls through and leaves the loot as it is


        return generatedLoot;
    }
}
