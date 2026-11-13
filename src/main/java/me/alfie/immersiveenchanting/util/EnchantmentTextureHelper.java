package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import net.minecraft.client.Minecraft;

public class EnchantmentTextureHelper {
    /**
     * Returns the texture identifier for the given enchantment's node icon.
     * Looks for {@code immersiveenchanting:textures/enchantment/<namespace>/<path>.png};
     * falls back to the generic {@code ancient_book.png} if no custom texture is found.
     */
    public static ResourceId getTexture(ResourceId enchantmentId) {
        String directory = "textures/enchantment/" + enchantmentId.namespace() + '/' + enchantmentId.path() + ".png";
        ResourceId iconId = new ResourceId("immersiveenchanting", directory);

        return Minecraft.getInstance().getResourceManager()
                .getResource(iconId.mc())
                .isPresent()
                ? iconId
                : new ResourceId("immersiveenchanting", "textures/item/ancient_book.png");
    }
}
