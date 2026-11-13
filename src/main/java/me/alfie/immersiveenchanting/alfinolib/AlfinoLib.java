package me.alfie.immersiveenchanting.alfinolib;

import com.mojang.logging.LogUtils;
import me.alfie.immersiveenchanting.alfinolib.commands.Commands;
import me.alfie.immersiveenchanting.alfinolib.datapacks.DatapackRegistry;
import me.alfie.immersiveenchanting.alfinolib.datapacks.Datapacks;
import me.alfie.immersiveenchanting.alfinolib.debug.command.CheckDatapackSyncCommand;
import me.alfie.immersiveenchanting.alfinolib.debug.datapack.TestDatapack;
import me.alfie.immersiveenchanting.alfinolib.networking.Networking;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;

/**
 * AlfinoLib 1.2.0 by alfino (alfiehanks), GPLv3, built into this Immersive Enchanting fork (fork change).
 * <p>
 * Upstream it's its own mod ({@code @Mod("alfinolib")}); here it's a plain library that Immersive Enchanting starts
 * from its own constructor, so the pack doesn't need the separate AlfinoLib jar. Everything was moved from
 * {@code me.alfie.alfinolib} to {@code me.alfie.immersiveenchanting.alfinolib}, and its id (network channel, debug
 * command, test datapack) from {@code alfinolib} to {@code ie_alfinolib}, so it can't clash with a standalone
 * AlfinoLib jar that some other mod brings along. Apart from that, the code is upstream's.
 */
public class AlfinoLib {

    /** Namespace for the library's own channel, command and test data (upstream: "alfinolib"). */
    public static final String MODID = "ie_alfinolib";
    public static final Logger LOGGER = LogUtils.getLogger();

    private AlfinoLib() {
    }

    /** What upstream's mod constructor did. Must run before anything registers packets or datapacks. */
    public static void init(IEventBus modEventBus) {
        Networking.init(modEventBus);
        Datapacks.init(modEventBus);
        Commands.init(modEventBus);

        Commands.register(new CheckDatapackSyncCommand());

        DatapackRegistry.register(TestDatapack.DEFINITION, TestDatapack::new);
    }
}
