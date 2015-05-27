package org.kepow.customhardcore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Class that represents a banishment manager for a world group.
 * 
 * @author Thomas Churchman
 *
 */
public class WorldManager implements ConfigurationSerializable
{
    private final String worldGroup;
    
    private final boolean enabled;
    private final boolean regeneration;
    private final int numLives;
    private final double banishTime;
    private final double regenerationTime;
    private final Location banishLocation;
    
    private final Map<OfflinePlayer, Long> banishedUntil;
    private final Map<OfflinePlayer, Long> regenerationStartTime;
    private final Map<OfflinePlayer, Integer> deaths;

    
    /**
     * Constructor.
     * @param worldGroup The world group this manager is for.
     */
    public WorldManager(String worldGroup)
    {
        this.worldGroup = worldGroup;
        
        enabled = PluginState.getWorldConfig().getEnabled(worldGroup);
        numLives = PluginState.getWorldConfig().getLives(worldGroup);
        banishTime = PluginState.getWorldConfig().getBanishTime(worldGroup);
        banishLocation = PluginState.getWorldConfig().getBanishLocation(worldGroup);
        regenerationTime = PluginState.getWorldConfig().getLifeRegenerationTime(worldGroup);
        regeneration = (regenerationTime > 0);
        
        deaths = new HashMap<OfflinePlayer, Integer>();
        banishedUntil = new HashMap<OfflinePlayer, Long>();
        regenerationStartTime = new HashMap<OfflinePlayer, Long>();
    }
    
    /**
     * Constructor.
     * @param mapO A configuration map to construct the WorldSimulator out of.
     */
    public WorldManager(Map<String, Object> mapO)
    {
        this.worldGroup = (String) mapO.get("worldGroup");
        
        enabled = PluginState.getWorldConfig().getEnabled(worldGroup);
        numLives = PluginState.getWorldConfig().getLives(worldGroup);
        banishTime = PluginState.getWorldConfig().getBanishTime(worldGroup);
        banishLocation = PluginState.getWorldConfig().getBanishLocation(worldGroup);
        regenerationTime = PluginState.getWorldConfig().getLifeRegenerationTime(worldGroup);
        regeneration = (regenerationTime > 0);
        
        this.banishedUntil = new HashMap<OfflinePlayer, Long>();
        this.regenerationStartTime = new HashMap<OfflinePlayer, Long>();
        this.deaths = new HashMap<OfflinePlayer, Integer>();
        
        Map<String, Long> banishedUntil = (Map<String, Long>) mapO.get("banishedUntil");
        Map<String, Long> regenerationStartTime = (Map<String, Long>) mapO.get("regenerationStartTime");
        Map<String, Integer> deaths = (Map<String, Integer>) mapO.get("deaths");
        
        if(banishedUntil != null)
        {
            for(String uuid : banishedUntil.keySet())
            {
                UUID u = UUID.fromString(uuid);
                this.banishedUntil.put(PluginState.getPlugin().getServer().getOfflinePlayer(u), ((Number) banishedUntil.get(uuid)).longValue());
            }
        }
        
        if(deaths != null)
        {
            for(String uuid : deaths.keySet())
            {
                UUID u = UUID.fromString(uuid);
                this.deaths.put(PluginState.getPlugin().getServer().getOfflinePlayer(u), deaths.get(uuid));
            }
        }
        
        if(regenerationStartTime != null)
        {
            for(String uuid : regenerationStartTime.keySet())
            {
                UUID u = UUID.fromString(uuid);
                this.regenerationStartTime.put(PluginState.getPlugin().getServer().getOfflinePlayer(u), regenerationStartTime.get(uuid));
            }
        }
    }
    
    /**
     * Handles the world join (server join / teleport / etc) of a player.
     * @param player The player to handle the world join of.
     */
    public void handleWorldJoin(Player player)
    {
        if(enabled && isBanished(player))
        {
            enforceBanishment(player);
        }
    }
    

    /**
     * Handles the respawn event of a player.
     * @param player The player to handle the respawn event of.
     * @param event The respawn event.
     */
    public void handleRespawn(Player player, PlayerRespawnEvent event)
    {
        if(enabled && isBanished(player))
        {
            enforceBanishment(player, event);
        }
    }
    
    /**
     * Handles the death of a player.
     * @param player The player to handle the death of.
     */
    public void handleDeath(Player player, PlayerDeathEvent event)
    {
        if(!enabled)
        {
            return;
        }
        
        int livesLeft = this.getLivesLeft(player);
        if(!deaths.containsKey(player))
        {
            deaths.put(player, 0);
        }
        int deaths = this.deaths.get(player) + 1;
        --livesLeft;
        
        if(livesLeft <= 0)
        {
            this.banish(player);
            
            // Lightning
            player.getWorld().strikeLightningEffect(player.getLocation());
            
            // Message
            StringBuilder year = new StringBuilder();
            StringBuilder month = new StringBuilder();
            StringBuilder day = new StringBuilder();
            StringBuilder hour = new StringBuilder();
            StringBuilder minute = new StringBuilder();
            StringBuilder second = new StringBuilder();
            Utils.prepareTimeStrings(this.banishedUntil(player), year, month, day, hour, minute, second);
            
            String message = event.getDeathMessage() + "\n" + Utils.prepareMessage("broadcasts.banished", 
                "%worldGroup", this.getWorldGroup(),
                "%worldGroupAlias", this.getWorldGroupAlias(),
                "%player", player.getName(),
                "%days", banishTime,
                "%year", year,
                "%month", month,
                "%day", day,
                "%hours", hour,
                "%minutes", minute,
                "%seconds", second);
            event.setDeathMessage(message);
        }
        else
        {
            this.deaths.put(player, deaths);
            
            if(!this.regenerationStartTime.containsKey(player))
            {   // The player is not currently regenerating a life, start regenerating the life just lost.
                this.regenerationStartTime.put(player, Utils.getCurrentTime());
            }
            
            String message = event.getDeathMessage() + "\n" + Utils.prepareMessage("broadcasts.died", 
                "%worldGroup", this.getWorldGroup(),
                "%worldGroupAlias", this.getWorldGroupAlias(),
                "%player", player.getName(),
                "%lives", getLivesLeft(player));
            event.setDeathMessage(message);
        }
    }
    
    /**
     * Banishes a player for the default banish duration. 
     * @param player The player to banish.
     */
    public void banish(Player player)
    {
        banish(player, banishTime);
    }
    
    /**
     * Banishes a player.
     * @param player The player to banish.
     * @param banishTime The banish duration.
     */
    public void banish(Player player, double banishTime)
    {
        final long secondsPerDay = 60*60*24; // 60 seconds per minute, 60 minutes per hour, 24 hours per day
        
        long banishUntil = (long) (Utils.getCurrentTime() + secondsPerDay * banishTime);
        
        // Set banished
        this.banishedUntil.put(player, banishUntil);
        
        // Reset deaths
        this.deaths.remove(player);
        
        // Reset life regeneration
        this.regenerationStartTime.remove(player);
        
        if(!player.isDead())
        {
            enforceBanishment(player);
        }
    }
    
    /**
     * Debanishes a player.
     * @param player The player to debanish.
     */
    public void debanish(Player player)
    {
        if(this.banishedUntil.containsKey(player))
        {
            this.banishedUntil.remove(player);
        }
    }
    
    /**
     * Checks whether a player is banished.
     * @param player The player to check banish status of.
     * @return True if the player is banished, false otherwise.
     */
    public boolean isBanished(Player player)
    {
        if(banishedUntil.containsKey(player))
        {
            long banishedUntil = this.banishedUntil.get(player);
            
            if(banishedUntil < Utils.getCurrentTime())
            {
                this.banishedUntil.remove(player);
                return false;
            }
            else
            {
               return true;
            }
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Returns the time until which a player is banished.
     * @param player The player to get the time for.
     * @return The time until which the player is banished, 
     * or -1 if the player is not banished.
     */
    public long banishedUntil(Player player)
    {
        if(banishedUntil.containsKey(player))
        {
            long banishedUntil = this.banishedUntil.get(player);
            
            if(banishedUntil < Utils.getCurrentTime())
            {
                this.banishedUntil.remove(player);
                return -1;
            }
            else
            {
               return banishedUntil;
            }
        }
        else
        {
            return -1;
        }
    }
    
    /**
     * Enforces banishment.
     * @param player The player to enforce banishment on.
     */
    private void enforceBanishment(Player player)
    {
        enforceBanishment(player, null);
    }
    
    /**
     * Enforces banishment.
     * @param player The player to enforce banishment on.
     * @param event The respawn event of the player, or null if it is
     * not a respawn event.
     */
    private void enforceBanishment(Player player, PlayerRespawnEvent event)
    {
        World world = player.getWorld();
        String worldGroup = PluginState.getWorldConfig().getGroupFromWorld(world);
        
        if(worldGroup.equals(this.worldGroup))
        {
            StringBuilder year = new StringBuilder();
            StringBuilder month = new StringBuilder();
            StringBuilder day = new StringBuilder();
            StringBuilder hour = new StringBuilder();
            StringBuilder minute = new StringBuilder();
            StringBuilder second = new StringBuilder();
            Utils.prepareTimeStrings(this.banishedUntil(player), year, month, day, hour, minute, second);
            
            player.sendMessage(Utils.prepareMessage("whispers.banished", 
                "%worldGroup", this.getWorldGroup(),
                "%worldGroupAlias", this.getWorldGroupAlias(),
                "%year", year,
                "%month", month,
                "%day", day,
                "%hours", hour,
                "%minutes", minute,
                "%seconds", second));
            
            if(event == null)
            {
                teleport(player);
            }
            else
            {
                event.setRespawnLocation(this.banishLocation);
            }
        }
    }
    
    /**
     * Teleports a player to the banishment location (should be at
     * another world).
     * @param player The player to teleport.
     */
    private void teleport(Player player)
    {
        player.teleport(banishLocation);
    }

    /**
     * Gets the world group name this manager is for.
     * @return The world group name this manager is for.
     */
    public String getWorldGroup()
    {
        return this.worldGroup;
    }
    
    /**
     * Gets the world group alias of the group this manager is for.
     * @return The world group alias of the group this manager is for.
     */
    public String getWorldGroupAlias()
    {
        return PluginState.getWorldConfig().getGroupAlias(this.worldGroup);
    }
    
    /**
     * Gets whether the world manager is enabled (i.e., whether the
     * world group is in hardcore mode).
     * @return True if the world manager is enabled, false otherwise.
     */
    public boolean isEnabled()
    {
        return this.enabled;
    }
    
    /**
     * Gets whether life regeneration is enabled.
     * @return True if life regeneration is enabled for this world group,
     * false otherwise.
     */
    public boolean isRegenerationEnabled()
    {
        return this.regeneration;
    }
    
    /**
     * Gets the number of lives players have on this world group.
     * @return The number of lives players have on this world group.
     */
    public int numLives()
    {
        return this.numLives;
    }
    
    /**
     * Gets the banish time on this world group.
     * @return The banish time on this world group.
     */
    public double banishTime()
    {
        return this.banishTime;
    }
    
    /**
     * Get the number of lives a player has left.
     * @param player The player to get the number of lives for.
     * @return The number of lives the player has left. 
     */
    public int getLivesLeft(Player player)
    {
        this.regeneration(player);
        
        int deaths = 0;
        if(this.deaths.containsKey(player))
        {
            deaths = this.deaths.get(player);
        }
        
        return this.numLives-deaths;
    }
    
    /**
     * Handle life regeneration.
     * @param player The player to handle life regeneration for.
     */
    private void regeneration(Player player)
    {
        if(!this.regeneration)
        {   // Life regeneration is disabled.
            return;
        }
        
        if(!this.deaths.containsKey(player) || this.deaths.get(player) <= 0)
        {   // The player has not died, and thus has no lives to regenerate.
            this.regenerationStartTime.remove(player);
            return;
        }
        
        final long secondsPerDay = 60*60*24; // 60 seconds per minute, 60 minutes per hour, 24 hours per day
        long regenerationTime = (long) (this.regenerationTime * secondsPerDay);
        
        if(this.regenerationStartTime.containsKey(player))
        {   // The player has a life to regenerate.
            long regenerationStartTime = this.regenerationStartTime.get(player);
            
            if(regenerationStartTime + regenerationTime <= Utils.getCurrentTime())
            {   // The player should regenerate a life now.
                
                this.deaths.put(player, this.deaths.get(player)-1);
                if(this.deaths.get(player) > 0)
                {   // The player has another live to regenerate, even after the regeneration that just finished.
                    this.regenerationStartTime.put(player, regenerationStartTime + regenerationTime);
                    
                    this.regeneration(player);
                }
                else
                {   // The player has no more lives to regenerate.
                    this.regenerationStartTime.remove(player);
                }
            }
        }
    }
    
    /**
     * Get the time a player will finish regenerating a life at, or -1 
     * if the player is not currently regenerating a life.
     * @param player The player to get the regeneration time for.
     * @return The time the player will finish regenerating a life at,
     * or -1 if the player is not currently regenerating a life.
     */
    public long regenerationAt(Player player)
    {
        if(this.regenerationStartTime.containsKey(player))
        {   // The player has a life to regenerate.
            return this.regenerationStartTime.get(player);
        }
        
        return -1;
    }
    
    /*
     * (non-Javadoc)
     * @see org.bukkit.configuration.serialization.ConfigurationSerializable#serialize()
     */
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        
        Map<String, Long> banishedUntil = new HashMap<String, Long>();
        Map<String, Integer> deaths = new HashMap<String, Integer>();
        
        
        for(OfflinePlayer key : this.banishedUntil.keySet())
        {
            banishedUntil.put(key.getUniqueId().toString(), this.banishedUntil.get(key));
        }
        
        for(OfflinePlayer key : this.deaths.keySet())
        {
            deaths.put(key.getUniqueId().toString(), this.deaths.get(key));
        }
        
        map.put("worldGroup", worldGroup);
        map.put("deaths", deaths);
        map.put("banishedUntil", banishedUntil);
        return map;
    }
}
