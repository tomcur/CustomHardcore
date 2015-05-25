package org.kepow.customhardcore;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;

public class MultiverseWrapper
{
    public static String getAlias(String world)
    {
        MultiverseCore multiverseCore = (MultiverseCore) PluginState.getPlugin().getMultiverseCore();
        MVWorldManager mvWorldManager = multiverseCore.getMVWorldManager();
        return mvWorldManager.getMVWorld(world).getColoredWorldString();
    }
}
