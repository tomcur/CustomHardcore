package org.kepow.customhardcore;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Class to represent a custom config manager.
 *
 * See: http://wiki.bukkit.org/Configuration_API_Reference
 * (Accessed 2015-05-20)
 *
 */
public class CustomConfig 
{
    private FileConfiguration shopData = null;
    private File shopDataFile = null;
    private String dataFileName = null;

    /**
     * Constructor.
     * @param fileName The filename of the custom config file (e.g., "players.yml").
     */
    public CustomConfig(String fileName)
    {
        dataFileName = fileName;
    }

    /**
     * Reload the custom config from file and defaults from the jar.
     */
    public void reloadCustomConfig() 
    {
        if (shopDataFile == null) 
        {
            shopDataFile = new File(PluginState.getPlugin().getDataFolder(), dataFileName);
        }
        shopData = YamlConfiguration.loadConfiguration(shopDataFile);

        // Look for defaults in the jar
        Reader defConfigStream;
        try 
        {
            defConfigStream = new InputStreamReader(PluginState.getPlugin().getResource(dataFileName), "UTF8");

            if (defConfigStream != null) 
            {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                shopData.setDefaults(defConfig);
            }
        } 
        catch (UnsupportedEncodingException e) 
        {
            e.printStackTrace();
        }
    }

    /**
     * Get the config object.
     * @return The config object.
     */
    public FileConfiguration getCustomConfig() 
    {
        if (shopData == null) 
        {
            reloadCustomConfig();
        }
        return shopData;
    }

    /**
     * Save the custom config to file.
     */
    public void saveCustomConfig() 
    {
        if (shopData == null || shopDataFile == null) 
        {
            return;
        }
        try 
        {
            getCustomConfig().save(shopDataFile);
        } 
        catch (IOException ex) 
        {
            PluginState.getPlugin().getLogger().severe("Could not save config to " + shopDataFile);
        }
    }

    /**
     * Save the default config to file if the file does not exist yet.
     */
    public void saveDefaultConfig() 
    {
        if (shopDataFile == null) 
        {
            shopDataFile = new File(PluginState.getPlugin().getDataFolder(), dataFileName);
        }
        if (!shopDataFile.exists()) 
        {
            PluginState.getPlugin().saveResource(dataFileName, false);
        }
    }
}
