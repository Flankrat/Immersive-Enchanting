package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.item.AncientBook;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EnchantmentUtil {

    public static final String REPLICATED_NBT_TAG = "replicated";

    public static Holder<Enchantment> toHolder(ResourceId id, RegistryAccess access) {
        return access.lookupOrThrow(Registries.ENCHANTMENT)
                .get(ResourceKey.create(Registries.ENCHANTMENT, id.mc()))
                .orElseThrow();
    }

    public static Holder<Enchantment> toHolder(ResourceKey<Enchantment> enchantmentKey, RegistryAccess access) {
        return toHolder(ResourceId.parse(enchantmentKey.location().toString()), access);
    }

    public static ResourceId toId(Holder<Enchantment> enchantmentHolder) {
        return ResourceId.parse(enchantmentHolder.unwrapKey().orElseThrow().location().toString());
    }

    public static List<ResourceKey<Enchantment>> toResourceKeys(List<Holder<Enchantment>> enchantmentHolders) {
        List<ResourceKey<Enchantment>> result = new ArrayList<>(enchantmentHolders.size());
        for(Holder<Enchantment> enchantmentHolder : enchantmentHolders) {
            result.add(enchantmentHolder.unwrapKey().orElseThrow());
        }
        return result;
    }

    public static List<Holder<Enchantment>> toHolders(List<ResourceKey<Enchantment>> enchantmentKeys, RegistryAccess registryAccess) {
        List<Holder<Enchantment>> result = new ArrayList<>(enchantmentKeys.size());
        for(ResourceKey<Enchantment> enchantmentKey : enchantmentKeys) {
            result.add(toHolder(ResourceId.parse(enchantmentKey.location().toString()), registryAccess));
        }
        return result;
    }

    public static void setStoredEnchantment(ItemStack ancientBook, Holder<Enchantment> enchantmentHolder) {
        ListTag listtag = new ListTag();
        ResourceLocation resourcelocation = enchantmentHolder.unwrapKey().get().location();

        listtag.add(EnchantmentHelper.storeEnchantment(resourcelocation, 1));

        ancientBook.getOrCreateTag().put(EnchantedBookItem.TAG_STORED_ENCHANTMENTS, listtag);
    }

    public static @Nullable Holder<Enchantment> getStoredEnchantment(ItemStack ancientBook, RegistryAccess registryAccess) {
        if(!(ancientBook.getItem() instanceof AncientBook)) return null;
        ListTag listTag = EnchantedBookItem.getEnchantments(ancientBook);
        if (listTag.isEmpty()) return null;

        CompoundTag tag = (CompoundTag) listTag.get(0);
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
        if(id == null) return null;

        return toHolder(ResourceId.parse(id.toString()), registryAccess);
    }

    /**
     * Universal enchantment level accessor for stored_enchantments and enchantments.
     */
    public static List<Holder<Enchantment>> enchantmentsAsHolders(ItemStack stack, RegistryAccess registryAccess) {
        Map<Enchantment, Integer> enchantments;
        if(stack.is(Items.ENCHANTED_BOOK)) {
            enchantments = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack));
        } else {
            enchantments = stack.getAllEnchantments();
        }

        List<Holder<Enchantment>> result = new ArrayList<>();
        for(Enchantment enchantment : enchantments.keySet()) {
            Holder<Enchantment> enchantmentHolder = registryAccess
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .wrapAsHolder(enchantment);
            result.add(enchantmentHolder);
        }

        return result;
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments;
        if(stack.is(Items.ENCHANTED_BOOK)) {
            enchantments = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack));
        } else {
            enchantments = stack.getAllEnchantments();
        }

        return enchantments;
    }

    /**
     * Universal enchantment level accessor for stored_enchantments and enchantments.
     */
    public static int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder) {
        if(stack.is(Items.ENCHANTED_BOOK)) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack));
            return enchantments.getOrDefault(enchantmentHolder.get(), 0);
        } else {
            return stack.getEnchantmentLevel(enchantmentHolder.get());
        }
    }

    public static void setReplicated(ItemStack ancientBook) {
        CompoundTag tag = ancientBook.getOrCreateTag();
        tag.putBoolean(REPLICATED_NBT_TAG, true);    }

    public static boolean isReplicated(ItemStack stack) {
        if(stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if(tag.contains(REPLICATED_NBT_TAG)) {
                return tag.getBoolean(REPLICATED_NBT_TAG);
            }
        }
        return false;
    }

    public static List<Holder<Enchantment>> getAllEnchantmentsInRegistry(HolderLookup.Provider lookup) {
        return lookup.lookupOrThrow(Registries.ENCHANTMENT).listElements().map(holder -> (Holder<Enchantment>) holder).toList();
    }

    public static ItemStack tryConvertVanillaBook(ItemStack book) {
        if(book.is(Items.ENCHANTED_BOOK)
                && getEnchantments(book).isEmpty()) {
            return new ItemStack(Items.BOOK);

        } else if(book.is(Items.BOOK) && !getEnchantments(book).isEmpty()) {
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

            Map<Enchantment, Integer> enchantments = getEnchantments(book);
            EnchantmentHelper.setEnchantments(enchantments, enchantedBook);

            return enchantedBook;
        }
        return book;
    }
}
