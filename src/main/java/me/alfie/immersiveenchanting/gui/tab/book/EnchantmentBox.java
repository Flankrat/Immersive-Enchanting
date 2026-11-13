package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.alfinolib.gui.GuiGraphicsX;
import me.alfie.immersiveenchanting.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.immersiveenchanting.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.util.EnchantmentTextureHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;

/**
 * Represents a single enchantment entry in the book tab UI.
 <P>
 * <p>Each box displays:</p>
 <P>
 * <ul>
 *     <li>A background indicating locked/unlocked state</li>
 *     <li>The enchantment name (optionally obfuscated)</li>
 *     <li>An icon or locked indicator</li>
 * </ul>
 <P>
 * <p>If the enchantment holder is {@code null}, an empty placeholder box is rendered.</p>
 */
public class EnchantmentBox {

    public final BookTab bookTab;
    private final Holder<Enchantment> enchantmentHolder;

    /**
     * Creates a new enchantment box for rendering.
     <P>
     * @param bookTab the parent {@link BookTab}
     * @param enchantmentHolder the enchantment to display, or {@code null} for an empty slot
     */
    public EnchantmentBox(BookTab bookTab, @Nullable Holder<Enchantment> enchantmentHolder) {
        this.bookTab = bookTab;
        this.enchantmentHolder = enchantmentHolder;
    }

    public void render(GuiGraphicsX gx, int x, int y) {
        Sprite boxSprite = bookTab.screen().getMenu().isEnchantmentAvailable(enchantmentHolder) ?
                Sprite.ENCHANTMENT_BOX_UNLOCKED : Sprite.ENCHANTMENT_BOX_LOCKED;

        GuiGraphicsApi.blit(
                gx, boxSprite.id(),
                x, y,
                boxSprite.width(), boxSprite.height()
        );

        if(enchantmentHolder != null){
            renderText(gx, x, y);
            renderIcon(gx, x, y);
        }
    }


    private void renderText(GuiGraphicsX gx, int x, int y) {
        final int padding = 4;
        gx.graphics().pose().pushPose();

        float scale = 1.0f;

        Component enchantmentName = Component.translatable(enchantmentHolder.value().getDescriptionId());
        if(enchantmentName.getString().length() > 15) {
            scale = 0.75f;
            gx.graphics().pose().scale(scale, scale, 1);
        }

        int scaledX = (int)((x + padding) / scale);
        int scaledY = (int)((y + padding) / scale);

        boolean isAvailable = bookTab.screen().getMenu().isEnchantmentAvailable(enchantmentHolder);
        Component enchantmentTitle;
        if(!isAvailable && ServerConfig.isObfuscateLockedEnchantments()) {
            enchantmentTitle = ImmersiveEnchanting.styleWithAltFont(
                    enchantmentName.copy().withStyle(ChatFormatting.GRAY));
        } else {
            enchantmentTitle = enchantmentName.copy().withStyle(ChatFormatting.WHITE);
        }

        GuiGraphicsApi.text(gx,
                bookTab.screen().getFont(),
                enchantmentTitle,
                scaledX, scaledY, true);
        gx.graphics().pose().popPose();
    }

    private void renderIcon(GuiGraphicsX gx, int x, int y) {

        if(bookTab.screen().getMenu().isEnchantmentAvailable(enchantmentHolder)) {
            ResourceId icon = EnchantmentTextureHelper.getTexture(EnchantmentUtil.toId(enchantmentHolder));

            final int xPos = x + 91;
            final int yPos = y + 2;

            GuiGraphicsApi.blit(
                    gx,
                    icon,
                    xPos, yPos,
                    16, 16
            );

        } else {
            final int xPos = x + 89;
            GuiGraphicsApi.blit(
                    gx,
                    Sprite.LOCKED_ENCHANTMENT.id(),
                    xPos, y,
                    Sprite.LOCKED_ENCHANTMENT.width(), Sprite.LOCKED_ENCHANTMENT.height()
            );
        }
    }
}
