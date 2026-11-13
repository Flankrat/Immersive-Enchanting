package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.alfinolib.networking.NetworkPacket;
import me.alfie.immersiveenchanting.alfinolib.networking.codec.StreamCodec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.CostHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Client-to-server packet requesting item replication.
 *
 * <p>If allowed by configuration and cost validation, the currently held item is duplicated.
 * The original item is consumed and replaced by two dropped item entities:
 * one normal copy and one marked as "replicated".</p>
 *
 * <p>Visual and audio feedback is played on success, and the container is closed.</p>
 *
 * <p>This operation is server-authoritative and cannot be performed client-side.</p>
 */
public record ReplicatePacket() implements NetworkPacket<ReplicatePacket> {
    public static StreamCodec<FriendlyByteBuf, ReplicatePacket> STREAM_CODEC = StreamCodec.unit(new ReplicatePacket());

    @Override
    public void exec(NetworkEvent.Context context) {
        Player player = context.getSender();
        Level level = player.level();
        if(!(player.containerMenu instanceof EnchantingTableMenu menu)) return;

        if(CostHelper.canReplicate(menu, player)) {
            ItemStack oldStack = menu.getToolSlot().getItem().copyAndClear();
            menu.getToolSlot().setChanged();

            ItemStack newStack = oldStack.copy();
            EnchantmentUtil.setReplicated(newStack);

            BlockPos tablePos = menu.getBlockPos();
            for (int i = 0; i < 2; i++) {
                ItemStack stackToSpawn = (i == 0) ? oldStack : newStack;
                ItemEntity entity = new ItemEntity(
                        level,
                        tablePos.getX() + 0.5,
                        tablePos.getY() + 1,
                        tablePos.getZ() + 0.5,
                        stackToSpawn);
                entity.setPickUpDelay(40);
                entity.setDeltaMovement(Vec3.ZERO);
                level.addFreshEntity(entity);
            }

            FxHelper.playReplicate((ServerLevel) level, tablePos);
            player.closeContainer();
        }
    }
}
