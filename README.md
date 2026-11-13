# Immersive Enchanting, 1.20.1 fork

A fork of [Immersive Enchanting](https://github.com/alfiehanks/Immersive-Enchanting) 6.0.1 (branch `forge-1.20.1`) by
alfino (alfiehanks), for Minecraft 1.20.1 (Forge 47.x). Version `6.0.1-fork.1`.
Every change from the original is marked with a `fork` comment in the code.

## What's different from the original

| | Original IE 6.0.1 | This fork |
|---|---|---|
| Tier upgrades (Sharpness V → Advanced Sharpness I → Supreme) | Not supported; one branch per enchantment, no way to turn one enchantment into another | Cost files can say `"upgrades_from"`; the next tier shows up once the previous one is maxed and replaces it ([details](#tier-upgrades)) |
| Bookshelf power | Not used; only chiseled bookshelves with Ancient Books matter | Each tier needs more bookshelf power around the table, default 15 / 30 / 45; bookshelves and chiseled bookshelves both count ([details](#bookshelf-power)) |
| Fuel per level | Always the `enchanting_fuels` amount for that level | A cost can set `fuel_count` for a flat amount, e.g. 5 lapis to go Advanced ([details](#fuel-override)) |
| AlfinoLib | Separate required mod | Built in, so one jar ([details](#alfinolib-built-in)) |
| Enchanted books in loot | `REPLACE` (with Ancient Books) or `REMOVE` | Also `KEEP`, which leaves them alone like 5.x could ([details](#enchanted-book-loot)) |
| Cost alternatives | Only the last alternative of a level was accepted | The first one the player can pay is used |
| Removing an enchantment from an enchanted book | Didn't stick | Works |
| License text | Declared GPLv3, but no `LICENSE` file | `LICENSE` in the repo and in the jar |

Everything else (the tree, Ancient Books, transmute/replicate, mod filters, cost files without the new fields)
works exactly as in the original, and existing 6.x datapacks load unchanged.

## Building

`bash ./gradlew build`. The jar to ship is `build/libs/immersiveenchanting-<version>-all.jar` (it carries
MixinExtras). No AlfinoLib jar is needed next to it.

## License

GNU General Public License v3.0, the license upstream's `forge-1.20.1` branch (and 6.0.1's mod listing) is released
under. The full text is in `LICENSE` and ships inside the jar. Original code copyright alfino (alfiehanks); fork
changes by Flankrat, under the same license. Upstream's later NeoForge 26.1 branch switched to
CC BY-NC 4.0; that doesn't change the license of the 1.20.1 code this fork is built from, and nothing from that
branch is used here.

## AlfinoLib built in

[AlfinoLib](https://github.com/alfiehanks/AlfinoLib) 1.2.0 (branch `forge-1.20.1`, also alfino's, also GPLv3) is part
of this jar, so the separate AlfinoLib mod isn't needed. Its source is in
`src/main/java/me/alfie/immersiveenchanting/alfinolib`, unchanged except:

- moved from `me.alfie.alfinolib` to `me.alfie.immersiveenchanting.alfinolib`, and its id (network channel, debug
  command `/ie_alfinolib`, test datapack folder `data/ie_alfinolib`) from `alfinolib` to `ie_alfinolib`, so it can't
  clash with a standalone AlfinoLib jar another mod might bring;
- it's no longer a `@Mod`: `AlfinoLib.init(modEventBus)` does what its mod constructor did, and Immersive
  Enchanting calls it first thing in its own constructor.

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

## Enchanted book loot

`enchantedBookLootMode` gains `KEEP`, which leaves vanilla enchanted books in loot as they are (what 5.x's
`enableEnchantedBookLootTables = true` did; 6.0.1 only had `REPLACE` and `REMOVE`).

## Fixes

- When a level had several cost alternatives, only the last one counted (the loop never stopped). Now the first
  alternative the player can pay, fuel included, is used.
- Removing an enchantment from an enchanted book didn't stick (vanilla `setEnchantments` only adds stored
  enchantments); stored enchantments are cleared first now.

## For developers

- `EnchantPacket.tryEnchant` holds the check-pay-apply logic that used to be inline in `exec`, so it can be called
  (and tested) without a network context. `CostHelper.isEnchantmentNextLevel` keeps its signature because JLME's
  mixin hooks it.
- Compatible with JLME ver2a's Immersive Enchanting hook (per-item level caps): every level, tier level 1
  included, still goes through `isEnchantmentNextLevel`.
