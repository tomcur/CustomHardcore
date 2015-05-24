package org.kepow.customhardcore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.ChatColor;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;


/**
 * Main class of the plugin.
 * 
 * @author Thomas Churchman
 *
 */
public class CustomHardcore extends JavaPlugin implements Listener
{
    private Map<String, WorldManager> worldManagers;
    private CommandsManager<CommandSender> commands;
    private MVWorldManager mvWorldManager;
    
    /**
     * Called when the plugin has been loaded and is enabled.
     */
    @Override
    public void onEnable()
    {
        // Setup configuration serialization
        ConfigurationSerialization.registerClass(WorldManager.class);
        
        // Setup events
        getServer().getPluginManager().registerEvents(this, this);
        
        // Register the plugin in the plugin state
        PluginState.setPlugin(this);
        
        // Setup commands
        setupCommands();
        
        // Setup configs
        this.saveDefaultConfig();
        PluginState.prepareCustomConfigs();
        
        // Load world confiugration and register it with the plugin state
        WorldConfig worldConfig = new WorldConfig(getConfig().getConfigurationSection("worldConfig").getValues(false));
        PluginState.setWorldConfig(worldConfig);
        
        // Populate world managers
        worldManagers = new HashMap<String, WorldManager>();
        if(PluginState.getDataCustomConfig().getCustomConfig().contains("worldManagers"))
        {
            List<WorldManager> managers = (List<WorldManager>) PluginState.getDataCustomConfig().getCustomConfig().getList("worldManagers");
            for(WorldManager manager : managers)
            {
                worldManagers.put(manager.getWorldGroup(), manager);
            }
        }
        
        // Populate world managers with groups that have not been loaded from file but are specified
        // in the config
        for(String group : worldConfig.getWorldGroups())
        {
            if(!worldManagers.containsKey(group))
            {
                worldManagers.put(group, new WorldManager(group));
            }
        }
        
        // Clean world managers by deleting world managers for groups that are no longer configured
        for(String key : worldManagers.keySet())
        {
            if(!worldConfig.getWorldGroups().contains(key))
            {
                worldManagers.remove(key);
            }
        }
        
        // Get Multiverse if it is loaded
        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if(plugin != null)
        {
            MultiverseCore mv = (MultiverseCore) plugin;
            mvWorldManager = mv.getMVWorldManager();
            
        }
        // Set up scheduled task
        final long sleep = 20*60*1;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PluginState.getPlugin(), new Runnable()
        { 
            public void run() 
            {
                save();
            } 
        }, sleep, sleep);
    }
    
    /**
     * Gets the Multiverse World Manager.
     * @return The Multiverse World Manager.
     */
    public MVWorldManager getMVWorldManager()
    {
        return mvWorldManager;
    }
    
    /**
     * Sets up the commands for use in the command framework.
     */
    private void setupCommands() 
    {
        this.commands = new CommandsManager<CommandSender>() 
        {
            @Override
            public boolean hasPermission(CommandSender sender, String perm) 
            {
                return sender instanceof ConsoleCommandSender || sender.hasPermission(perm);
            }
        };
        CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, this.commands);
        cmdRegister.register(CustomHardcoreCommands.ParentCommand.class);
    } 
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try 
        {
            this.commands.execute(cmd.getName(), args, sender, sender);
        } 
        catch (CommandPermissionsException e) 
        {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } 
        catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } 
        catch (CommandUsageException e) 
        {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } 
        catch (WrappedCommandException e) 
        {
            if (e.getCause() instanceof NumberFormatException) 
            {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } 
            else 
            {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } 
        catch (CommandException e) 
        {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
 
        return true;
    }
    
    /**
     * Gets the world managers.
     * @return The world managers.
     */
    public Map<String, WorldManager> getWorldManagers()
    {
        return worldManagers;
    }
    
    /**
     * Gets the world managers that have hardcore mode enabled.
     * @return The world managers that have hardcore mode enabled.
     */
    public Map<String, WorldManager> getHardcoreWorldManagers()
    {
        HashMap<String, WorldManager> hardcore = new HashMap<String, WorldManager>();
        Map<String, WorldManager> worldManagers = PluginState.getPlugin().getWorldManagers();
        for(WorldManager worldManager : worldManagers.values())
        {
            if(worldManager.isEnabled())
            {
                hardcore.put(worldManager.getWorldGroup(), worldManager);
            }
        }
        
        return hardcore;
    }
    
    /**
     * Saves plugin data to file.
     */
    private void save()
    {
        List<WorldManager> managers = new ArrayList<WorldManager>(worldManagers.values());
        PluginState.getDataCustomConfig().getCustomConfig().set("worldManagers", managers);
        PluginState.getDataCustomConfig().saveCustomConfig();
    }
    
    /**
     * Called when the plugin is being disabled.
     */
    @Override
    public void onDisable()
    {
        save();
    }
    
    /**
     * Called when a player dies.
     * @param event
     */
    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        World world = player.getWorld();
        
        String worldGroup = PluginState.getWorldConfig().getGroupFromWorld(world);
        
        worldManagers.get(worldGroup).handleDeath(player, event);
    }
    
    /**
     * Called when a player changes worlds.
     * @param event
     */
    @EventHandler(priority=EventPriority.LOW)
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        String worldGroup = PluginState.getWorldConfig().getGroupFromWorld(world);
        
        worldManagers.get(worldGroup).handleWorldJoin(player);
    }
    
    /**
     * Called when a player respawns.
     * @param event
     */
    @EventHandler(priority=EventPriority.LOW)
    public void onPlayerRespawnEvent(PlayerRespawnEvent event)
    {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        String worldGroup = PluginState.getWorldConfig().getGroupFromWorld(world);
        
        worldManagers.get(worldGroup).handleRespawn(player, event);
    }
    
    /**
     * Called when a player joins the game.
     * @param event
     */
    @EventHandler(priority=EventPriority.LOW)
    public void onPlayerJoinEvent(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        String worldGroup = PluginState.getWorldConfig().getGroupFromWorld(world);
        
        worldManagers.get(worldGroup).handleWorldJoin(player);
    }
}
