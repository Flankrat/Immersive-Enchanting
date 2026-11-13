# Immersive Enchanting, 1.20.1 fork

A fork of [Immersive Enchanting](https://github.com/alfiehanks/Immersive-Enchanting) 6.0.1 by alfino (alfiehanks),
for Minecraft 1.20.1. Licensed GPLv3 like the original. Version `6.0.1-fork.1`, still needs
AlfinoLib 1.2.0. Every change is marked with a `fork` comment in the code.

## Tier upgrades

An enchantment cost file can name the enchantment it upgrades from:

```json
{
  "upgrades_from": "minecraft:sharpness",
  "levels": {
    "1": [{"item_cost": {"item": "minecraft:diamond", "count": 2}, "xp_levels": 8, "fuel_count": 5}],
    "2": [{"item_cost": {"item": "minecraft:diamond", "count": 2}, "xp_levels": 16}]
  }
}
```

That makes it the next tier of that enchantment (Advanced Sharpness above, a Supreme one would upgrade from
`jlme:advanced_sharpness`):

- Level 1 only shows up, and can only be bought, when the item (or enchanted book) has the previous tier at its
  max level (the highest level in the previous tier's cost file, or the enchantment's own max without one), and the
  new enchantment fits with the item's other enchantments once the previous tier is gone. Buying it replaces the
  previous tier. Plain books can't get tier enchantments.
- Higher levels are bought one at a time like any enchantment.
- Tier enchantments don't need an Ancient Book: having the previous tier unlocks them. Ancient Books (loot,
  structures, `/giverandomancientbook`) never roll a tier enchantment.

## Bookshelf power

Enchanting needs bookshelf power around the table, more for each tier. Power is counted in the same area IE
already searches for Ancient Books (the ring two blocks out from the table, up to the search radius): normal
bookshelves give their enchanting power (1 for vanilla's), chiseled bookshelves give `chiseledBookshelfPower`
(1 by default), so the shelves holding the Ancient Books count. No air gap is needed. A Creative Bookshelf counts as
unlimited. Server config, `[bookshelves]`:

- `bookshelfPowerPerTier` (default `[15, 30, 45]`): normal enchantments, then each tier (Advanced, Supreme, ...).
  Higher tiers than listed use the last value; `[0]` turns it off.
- `chiseledBookshelfPower` (default `1.0`).

A node locked by power says "Needs X bookshelf power (has Y)" instead of "Unknown".

## Fuel override

A cost can set `fuel_count` to take that many of the level's fuel item instead of the enchanting_fuels amount
(0 or left out keeps the old behaviour). Tier steps use it for their flat lapis cost. The tooltip shows it.

## Fixes

- When a level had several cost alternatives, only the last one counted (the loop never stopped). Now the first
  alternative the player can pay, fuel included, is used.
- Removing an enchantment from an enchanted book didn't stick (vanilla `setEnchantments` only adds stored
  enchantments); stored enchantments are cleared first now.

## Other

- `EnchantPacket.tryEnchant` holds the check-pay-apply logic that used to be inline in `exec`, so it can be called
  (and tested) without a network context. `CostHelper.isEnchantmentNextLevel` keeps its signature because JLME's
  mixin hooks it.
- Built with `bash ./gradlew build`; the jar to ship is `build/libs/immersiveenchanting-<version>-all.jar`
  (it carries MixinExtras).
