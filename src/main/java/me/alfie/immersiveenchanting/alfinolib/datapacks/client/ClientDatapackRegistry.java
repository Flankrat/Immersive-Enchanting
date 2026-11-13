package me.alfie.immersiveenchanting.alfinolib.datapacks.client;

import me.alfie.immersiveenchanting.alfinolib.datapacks.DataMap;
import me.alfie.immersiveenchanting.alfinolib.datapacks.DatapackKey;
import me.alfie.immersiveenchanting.alfinolib.datapacks.ModDatapack;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-sided datapack registration. The client cannot store a map of DatapackKeys and raw Datapack objects.
 * Instead, the client has access to the common DatapackKey and the processed DataMap
 */
public final class ClientDatapackRegistry {

    private static final Map<DatapackKey<?>, DataMap> DATAMAPS = new HashMap<>();

    public static void register() {

    }
}
