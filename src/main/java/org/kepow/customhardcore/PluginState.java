package org.kepow.customhardcore;

/**
 * Class representing the plugin's state.
 * 
 * @author Thomas Churchman
 *
 */
public final class PluginState 
{
    private static CustomHardcore plugin = null;
    private static WorldConfig worldConfig = null;

    private static CustomConfig data;
    private static CustomConfig messages;

    /**
     * Prepare the custom config objects.
     */
    public static void prepareCustomConfigs()
    {
        PluginState.data = new CustomConfig("data.yml");
        PluginState.messages = new CustomConfig("messages.yml");

        data.saveDefaultConfig();
        messages.saveDefaultConfig();
    }

    public static CustomConfig getDataCustomConfig()
    {
        return PluginState.data;
    }
    
    public static CustomConfig getMessagesCustomConfig()
    {
        return PluginState.messages;
    }

    public static void setPlugin(CustomHardcore plugin)
    {
        PluginState.plugin = plugin;
    }

    public static CustomHardcore getPlugin()
    {
        return PluginState.plugin;
    }

    public static void setWorldConfig(WorldConfig worldConfig)
    {
        PluginState.worldConfig = worldConfig; 
    }

    public static WorldConfig getWorldConfig()
    {
        return PluginState.worldConfig;
    }
}
