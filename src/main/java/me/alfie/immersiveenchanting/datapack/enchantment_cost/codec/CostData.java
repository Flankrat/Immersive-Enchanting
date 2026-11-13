package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.CommonCodecs;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;

/**
 * One enchantment's cost file.
 *
 * <p>{@code upgrades_from} (fork addition, optional) makes this a tier enchantment: level 1 can only be bought on
 * an item that has that enchantment at its max level, and buying it replaces that enchantment
 * (e.g. {@code jlme:advanced_sharpness} with {@code "upgrades_from": "minecraft:sharpness"}). Empty for normal
 * enchantments. See {@link me.alfie.immersiveenchanting.util.TierUpgrades}.</p>
 */
public record CostData(boolean enabled, CostLevels levelCosts, String upgradesFrom) {

    /** Normal (non-tier) cost data, as before the fork. */
    public CostData(boolean enabled, CostLevels levelCosts) {
        this(enabled, levelCosts, "");
    }

    public static final Codec<CostData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(CostData::enabled),
                    CostLevels.CODEC.fieldOf("levels").forGetter(CostData::levelCosts),
                    Codec.STRING.optionalFieldOf("upgrades_from", "").forGetter(CostData::upgradesFrom)
            ).apply(instance, CostData::new));

    public static final StreamCodec<FriendlyByteBuf, CostData> STREAM_CODEC =
            StreamCodecBuilder.<FriendlyByteBuf, CostData>create()
                    .add(CommonCodecs.BOOL, CostData::enabled)
                    .add(CostLevels.STREAM_CODEC, CostData::levelCosts)
                    .add(CommonCodecs.STRING, CostData::upgradesFrom)   // synced so the client tree knows the tiers
                    .build((Boolean enabled, CostLevels levels, String upgradesFrom) -> new CostData(enabled, levels, upgradesFrom));

    public static final CostData EMPTY = new CostData(true, new CostLevels(new HashMap<>()), "");

    /** Whether this enchantment is a tier upgrade of another one. */
    public boolean isTier() {
        return !upgradesFrom.isEmpty();
    }
}
