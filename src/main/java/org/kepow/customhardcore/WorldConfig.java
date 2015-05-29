package org.kepow.customhardcore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;

/**
 * Class that represents the World Configuration used in the plugin.
 * 
 * @author Thomas Churchman
 *
 */
public class WorldConfig 
{
    private final String DEFAULT_GROUP = "default";

    private Map<String, List<String>> worldGroups;
    private Map<String, String> worldGroupsAliases;
    private Map<String, Map<String, Object>> groupsConfig;

    /**
     * Constructor.
     * @param worldGroupsData A configuration map to construct the WorldConfig out of.
     */
    public WorldConfig(Map<String, Object> worldGroupsData)
    {
        worldGroups = new HashMap<String, List<String>>();
        worldGroupsAliases = new HashMap<String, String>();
        groupsConfig = new HashMap<String, Map<String, Object>>();

        MemorySection groupsDataSection = (MemorySection)worldGroupsData.get("groups");

        Map<String, Object> groupsData = groupsDataSection.getValues(false);
        for(String key : groupsData.keySet())
        {
            //worldGroups.put(key, (List<String>) groupsData.get(key));
            MemorySection groupDataSection = (MemorySection)groupsData.get(key);
            Map<String, Object> groupData = groupDataSection.getValues(false);

            if(groupData.containsKey("alias"))
            {
                worldGroupsAliases.put(key, groupDataSection.getString("alias"));
            }
            else
            {
                worldGroupsAliases.put(key, key);
            }
            
            if(groupData.containsKey("config"))
            {
                MemorySection groupConfigDataSection = (MemorySection)groupData.get("config");
                groupsConfig.put(key, groupConfigDataSection.getValues(false));
            }
            else
            {
                groupsConfig.put(key, new HashMap<String, Object>());
            }

            if(groupData.containsKey("worlds"))
            {
                worldGroups.put(key, groupDataSection.getStringList("worlds"));
            }
            else
            {
                worldGroups.put(key, new ArrayList<String>());
            }
        }
    }
    
    /**
     * Get world group from a group name. The group name given 
     * has its case ignored.
     * @param group The group's group name (case insensitive) to get the group name for.
     * @return The group's group name with correct case, or null if the group was not found.
     */
    public String getGroupFromGroupName(String group)
    {
        for(String groupName : this.worldGroups.keySet())
        {
            if(groupName.equalsIgnoreCase(group))
            {
                return groupName;
            }
        }
        
        return null;
    }

    /**
     * Get the group a world belongs to.
     * @param world The world to get the group for.
     * @return The group the world belongs to.
     */
    public String getGroupFromWorld(World world)
    {
        return getGroupFromWorld(world.getName());
    }

    /**
     * Get the group a world belongs to.
     * @param world  the world name of the world to get the group for.
     * @return The group the world belongs to.
     */
    public String getGroupFromWorld(String world)
    {
        String group = DEFAULT_GROUP;

        for(String grp : worldGroups.keySet())
        {
            if(worldGroups.get(grp).contains(world))
            {
                group = grp;
                break;
            }
        }

        return group;
    }

    /**
     * Get all world groups.
     * @return The set of world groups.
     */
    public Set<String> getWorldGroups()
    {
        return worldGroups.keySet();
    }
    
    /**
     * Get the group alias for a group.
     * @param group The group to get the alias for.
     * @return The group alias, or null if the group does not exist.
     */
    public String getGroupAlias(String group)
    {
        return this.worldGroupsAliases.get(group);
    }
    
    /**
     * Get whether the group is the default group.
     * @param group The group to get default group status for.
     * @return True if the group is the default group, false otherwise.
     */
    public boolean isDefaultGroup(String group)
    {
        return group.equals(this.DEFAULT_GROUP);
    }
    
    /**
     * Get the worlds that are in the specified group.
     * @param group The group to get the worlds of.
     * @return The list of worlds that are in the group, or null if the group was not found.
     */
    public List<String> getWorldsInGroup(String group)
    {
        return this.worldGroups.get(group);
    }

    /**
     * Get the specified configuration option for the specified world group,
     * or for the default group if the world group does not have the option
     * specified.
     * @param group The world group to get the configuration option for.
     * @param option The option's option key to get the value for.
     * @param defaults Map containing the default options that will be used
     * if both the group in question and the default group do not specify the
     * options explicitly. 
     * @return The value of the configuration option.
     */
    private <T> T get(String group, String option, Map<String, Object> defaults)
    {
        if(groupsConfig.get(group).containsKey(option) || option == "alias")
        {
            T ret = (T) groupsConfig.get(group).get(option);
            if(ret == null)
            {   // Alias is not set; return group name
                return (T) group;
            }
            return (T) groupsConfig.get(group).get(option);    
        }
        else if(groupsConfig.get(DEFAULT_GROUP).containsKey(option))
        {
            // Get default group setting
            return (T) groupsConfig.get(DEFAULT_GROUP).get(option);
        }
        else
        {
            return (T) defaults.get(option);
        }
    }
    
    /**
     * Get whether hardcore mode is enabled for the group.
     * @param group The group to get hardcore mode status for.
     * @return True if hardcore is enabled for the group, false if it is disabled, 
     * or the same for the default group if the group has no option set, or the
     * hard-coded default if the default group does not have the option specified. 
     */
    public boolean getEnabled(String group)
    {
        return this.get(group, "enabled", Default.VALUES);
    }
    
    /**
     * Get the number of lives configured for this world group.
     * @param group The group to get the number of lives for.
     * @return The number of lives on the group, or that of the default group
     * if the group does not have the option specified, or the
     * hard-coded default if the default group does not have the option specified.
     */
    public int getLives(String group)
    {
        return ((Number) this.get(group, "lives", Default.VALUES)).intValue();
    }

    /**
     * Get the banish time configured for this world group.
     * @param group The group to get the banish time for.
     * @return The banish time on the group, or that of the default group
     * if the group does not have the option specified, or the
     * hard-coded default if the default group does not have the option specified.
     */
    public double getBanishTime(String group)
    {
        return ((Number) this.get(group, "banishTime", Default.VALUES)).doubleValue();
    }
    
    /**
     * Get the life regeneration time configured for this world group.
     * @param group The group to get the life regeneration time for.
     * @return The life regeneration time configured for the group, or that of the
     * default group if the group does not have the option specified, or the
     * hard-coded default if the default group does not have the option specified.
     */
    public double getLifeRegenerationTime(String group)
    {
        return ((Number) this.get(group, "lifeRegenerationTime", Default.VALUES)).doubleValue();
    }
    
    /**
     * Get the banish location configured for this world group.
     * @param group The group to get the banish location for.
     * @return The banish location on the group, or that of the default group
     * if the group does not have the option specified, or the
     * hard-coded default if the default group does not have the option specified.
     */
    public Location getBanishLocation(String group)
    {
        Object banishLocationObj = this.get(group, "banishLocation", Default.VALUES);
        if(banishLocationObj instanceof MemorySection)
        {
            MemorySection banishLocationSection = (MemorySection) banishLocationObj;
            Map<String, Object> banishLocation = banishLocationSection.getValues(false);
            
            String world = (String) banishLocation.get("world");
            double x = ((Number) banishLocation.get("x")).doubleValue();
            double y = ((Number) banishLocation.get("y")).doubleValue();
            double z = ((Number) banishLocation.get("z")).doubleValue();
            
            return new Location(Bukkit.getWorld(world), x, y, z);
        }
        else
        {
            Map<String, Object> banishLocation = (Map<String, Object>) banishLocationObj;
            
            String world = (String) banishLocation.get("world");
            double x = ((Number) banishLocation.get("x")).doubleValue();
            double y = ((Number) banishLocation.get("y")).doubleValue();
            double z = ((Number) banishLocation.get("z")).doubleValue();
            
            return new Location(Bukkit.getWorld(world), x, y, z);
        }
    }

    
}
