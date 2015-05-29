package org.kepow.customhardcore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.NestedCommand;


public class CustomHardcoreCommands
{
    public static class ParentCommand
    {
        @Command(aliases = { "chc" }, desc = "All CustomHardcore commands.", min = 0, max = -1)
        @NestedCommand(CustomHardcoreCommands.class) // All commands will get passed on to CustomHardcoreCommands.class
        public static void myplugin(final CommandContext args, CommandSender sender)  throws Exception 
        {
        }
    }
    
    @Command(aliases={"info", "i"}, desc="Get player banishment info", usage = "[player] - The player to get information of", min = 0, max = 1)
    public static void info(final CommandContext args, CommandSender sender) throws Exception 
    {
        String target = args.getString(0, null);
        String who;
        Player targetPlayer = getPlayer(sender, target);
        if(target == null)
        {
            who = "self";
        }
        else
        {
            who = "other";
            
            if(!sender.hasPermission("customhardcore.info.player"))
            {
                throw(new CommandPermissionsException());
            }
        }
        
        ArrayList<WorldManager> banished = new ArrayList<WorldManager>();
        Map<String, WorldManager> worldManagers = PluginState.getPlugin().getHardcoreWorldManagers();
        for(WorldManager worldManager : worldManagers.values())
        {
            if(worldManager.isBanished(targetPlayer))
            {
                banished.add(worldManager);
            }
        }
        
        if(banished.size() == 0)
        {
            sender.sendMessage(Utils.prepareMessage("commands.banishInfoNotBanished", 
                "%player", targetPlayer.getName(),
                "%who", who));
        }
        else
        {
            sender.sendMessage(Utils.prepareMessage("commands.banishInfoBanishedHeader", 
                "%player", targetPlayer.getName(),
                "%who", who));
            
            for(WorldManager worldManager : banished)
            {
                StringBuilder year = new StringBuilder();
                StringBuilder month = new StringBuilder();
                StringBuilder day = new StringBuilder();
                StringBuilder hour = new StringBuilder();
                StringBuilder minute = new StringBuilder();
                StringBuilder second = new StringBuilder();
                Utils.prepareTimeStrings(worldManager.banishedUntil(targetPlayer), year, month, day, hour, minute, second);
                
                sender.sendMessage(Utils.prepareMessage("commands.banishInfoBanishedEntry", 
                    "%worldGroup", worldManager.getWorldGroup(),
                    "%worldGroupAlias", worldManager.getWorldGroupAlias(),
                    "%year", year,
                    "%month", month,
                    "%day", day,
                    "%hours", hour,
                    "%minutes", minute,
                    "%seconds", second));
            }
        }
    }
    
    @Command(aliases="list", desc="Get the hardcore world groups", min = 0, max = 0)
    public static void list(final CommandContext args, CommandSender sender) throws Exception 
    {
        if(!sender.hasPermission("customhardcore.info.list"))
        {
            throw(new CommandPermissionsException());
        }
        
        Map<String, WorldManager> hardcore = PluginState.getPlugin().getHardcoreWorldManagers();
        if(hardcore.size() == 0)
        {
            sender.sendMessage(Utils.prepareMessage("commands.hardcoreGroupsNoGroups"));
        }
        else
        {
            sender.sendMessage(Utils.prepareMessage("commands.hardcoreGroupsHeader"));
            for(WorldManager worldManager : hardcore.values())
            {
                sender.sendMessage(Utils.prepareMessage("commands.hardcoreGroupsEntry", 
                    "%worldGroup", worldManager.getWorldGroup(),
                    "%worldGroupAlias", worldManager.getWorldGroupAlias()));
            }
        }
    }
    
    @Command(aliases={"lives", "l"}, desc="Get the number of lives a player has", usage="[player] - The player to get the number of lives for.", min = 0, max = 1)
    public static void lives(final CommandContext args, CommandSender sender) throws Exception 
    {
        String target = args.getString(0, null);
        String who;
        Player targetPlayer = getPlayer(sender, target);
        if(target == null)
        {
            who = "self";
        }
        else
        {
            who = "other";
            
            if(!sender.hasPermission("customhardcore.info.player"))
            {
                throw(new CommandPermissionsException());
            }
        }
        
        Map<String, WorldManager> hardcoreManagers = PluginState.getPlugin().getHardcoreWorldManagers();
        
        if(hardcoreManagers.size() == 0)
        {
            sender.sendMessage(Utils.prepareMessage("commands.hardcoreGroupsNoGroups", 
                "%player", targetPlayer.getName(),
                "%who", who));
        }
        else
        {
            sender.sendMessage(Utils.prepareMessage("commands.livesInfoHeader", 
                "%player", targetPlayer.getName(),
                "%who", who));
            
            for(WorldManager worldManager : hardcoreManagers.values())
            {
                sender.sendMessage(Utils.prepareMessage("commands.livesInfoEntry", 
                    "%worldGroup", worldManager.getWorldGroup(),
                    "%worldGroupAlias", worldManager.getWorldGroupAlias(),
                    "%lives", worldManager.getLivesLeft(targetPlayer)));
                if(worldManager.isRegenerationEnabled())
                {
                    long regenerationAt = worldManager.regenerationAt(targetPlayer);
                    
                    if(regenerationAt != -1)
                    {
                        StringBuilder year = new StringBuilder();
                        StringBuilder month = new StringBuilder();
                        StringBuilder day = new StringBuilder();
                        StringBuilder hour = new StringBuilder();
                        StringBuilder minute = new StringBuilder();
                        StringBuilder second = new StringBuilder();
                        Utils.prepareTimeStrings(regenerationAt, year, month, day, hour, minute, second);
                        
                        sender.sendMessage(Utils.prepareMessage("commands.livesInfoRegenerationEntry", 
                            "%worldGroup", worldManager.getWorldGroup(),
                            "%worldGroupAlias", worldManager.getWorldGroupAlias(),
                            "%lives", worldManager.getLivesLeft(targetPlayer),
                            "%year", year,
                            "%month", month,
                            "%day", day,
                            "%hours", hour,
                            "%minutes", minute,
                            "%seconds", second));
                    }
                }
            }
        }
    }
    
    @Command(aliases={"groupinfo", "ginfo", "gi"}, desc="Get a group's hardcore setting", usage = "[group] - The hardcore group to show settings for", min = 0, max = 1)
    public static void groupInfo(final CommandContext args, CommandSender sender) throws Exception 
    {
        if(!sender.hasPermission("customhardcore.info.group"))
        {
            throw(new CommandPermissionsException());
        }
        
        String target = args.getString(0, null);
        target = getWorldGroup(sender, target);
        
        WorldManager manager = PluginState.getPlugin().getWorldManagers().get(target);
        
        sender.sendMessage(Utils.prepareMessage("commands.groupInfoHeader", 
            "%worldGroup", manager.getWorldGroup(),
            "%worldGroupAlias", manager.getWorldGroupAlias()));
        if(manager.isEnabled())
        {
            sender.sendMessage(Utils.prepareMessage("commands.groupInfoHardcore", 
                "%hardcore", "true"));
            sender.sendMessage(Utils.prepareMessage("commands.groupInfoLives", 
                "%lives", manager.numLives()));
            sender.sendMessage(Utils.prepareMessage("commands.banishTime", 
                "%banishTime", manager.banishTime()));
        }
        else
        {
            sender.sendMessage(Utils.prepareMessage("commands.groupInfoHardcore", 
                "%hardcore", "false"));
        }
    }
    
    @Command(aliases={"worlds", "w"}, desc="Get the world associated with a group", usage = "[group] - The group to show worlds for", min = 0, max = 1)
    public static void worlds(final CommandContext args, CommandSender sender) throws Exception 
    {
        if(!sender.hasPermission("customhardcore.info.group"))
        {
            throw(new CommandPermissionsException());
        }
        
        String target = args.getString(0, null);
        target = getWorldGroup(sender, target);
        
        if(PluginState.getWorldConfig().isDefaultGroup(target))
        {
            sender.sendMessage(Utils.prepareMessage("commands.groupWorldsIsDefault", 
                "%worldGroup", target,
                "%worldGroupAlias", PluginState.getWorldConfig().getGroupAlias(target)));
            return;
        }
        
        List<String> worlds = PluginState.getWorldConfig().getWorldsInGroup(target);
        
        if(worlds.size() == 0)
        {
            sender.sendMessage(Utils.prepareMessage("commands.groupWorldsNoWorlds", 
                "%worldGroup", target,
                "%worldGroupAlias", PluginState.getWorldConfig().getGroupAlias(target)));
        }
        else
        {
            sender.sendMessage(Utils.prepareMessage("commands.groupWorldsHeader", 
                "%worldGroup", target,
                "%worldGroupAlias", PluginState.getWorldConfig().getGroupAlias(target)));
            
            for(String world : worlds)
            {
                if(PluginState.getPlugin().getServer().getWorld(world) != null)
                {
                    if(PluginState.getPlugin().getMultiverseCore() != null)
                    {
                        String alias = MultiverseWrapper.getAlias(world);
                        
                        sender.sendMessage(Utils.prepareMessage("commands.groupWorldsEntry", 
                            "%world", alias));
                    }
                    else
                    {
                        sender.sendMessage(Utils.prepareMessage("commands.groupWorldsEntry", 
                        "%world", world));
                    }
                }
            }
        }
    }
    
    @Command(aliases={"banish", "b"}, desc="Banish a player", usage="<player> - player to banish. [group] - The group to banish the player from.", min = 1, max = 2)
    public static void banish(final CommandContext args, CommandSender sender) throws Exception 
    {
        if(!sender.hasPermission("customhardcore.moderator.banish"))
        {
            throw(new CommandPermissionsException());
        }
        
        String targetPlayerString = args.getString(0);
        Player targetPlayer = getPlayer(sender, targetPlayerString);
        
        String targetGroup = args.getString(1, null);
        targetGroup = getWorldGroup(sender, targetGroup);
        
        
        Map<String, WorldManager> hardcoreManagers = PluginState.getPlugin().getHardcoreWorldManagers();
        
        if(!hardcoreManagers.keySet().contains(targetGroup))
        {
            throw(new CommandException("That world group does not have hardcore mode enabled."));
        }
        
        WorldManager manager = hardcoreManagers.get(targetGroup);
        manager.banish(targetPlayer);
        
        sender.sendMessage(Utils.prepareMessage("commands.banish",
            "%worldGroup", manager.getWorldGroup(),
            "%worldGroupAlias", manager.getWorldGroupAlias(),
            "%player", targetPlayer.getName()));
    }
    
    @Command(aliases={"unbanish", "unb", "u"}, desc="Unbanish a player", usage="<player> - player to banish. [group] - The group to unbanish the player from.", min = 1, max = 2)
    public static void unbanish(final CommandContext args, CommandSender sender) throws Exception 
    {
        if(!sender.hasPermission("customhardcore.moderator.unbanish"))
        {
            throw(new CommandPermissionsException());
        }
        
        String targetPlayerString = args.getString(0);
        Player targetPlayer = getPlayer(sender, targetPlayerString);
        
        String targetGroup = args.getString(1, null);
        targetGroup = getWorldGroup(sender, targetGroup);
        
        
        Map<String, WorldManager> hardcoreManagers = PluginState.getPlugin().getHardcoreWorldManagers();
        
        if(!hardcoreManagers.keySet().contains(targetGroup))
        {
            throw(new CommandException("That world group does not have hardcore mode enabled."));
        }
        
        WorldManager manager = hardcoreManagers.get(targetGroup);
        manager.debanish(targetPlayer);
        
        sender.sendMessage(Utils.prepareMessage("commands.unbanish",
            "%worldGroup", manager.getWorldGroup(),
            "%worldGroupAlias", manager.getWorldGroupAlias(),
            "%player", targetPlayer.getName()));
    }
    
    @Command(aliases="reload", desc="Reload the configuration (but not the data!)", min = 0, max = 0)
    public static void reload(final CommandContext args, CommandSender sender) throws Exception 
    {
        if(!sender.hasPermission("customhardcore.admin.reload"))
        {
            throw(new CommandPermissionsException());
        }
        
        PluginState.getPlugin().reload();
        sender.sendMessage(Utils.prepareMessage("commands.reload"));
    }
    
    private static Player getPlayer(CommandSender sender, String target) throws Exception
    {
        if(target == null)
        {
            if(!(sender instanceof Player))
            {
                throw(new CommandException("You have to provide a player name."));
            }
            
            return (Player) sender;
        }
        else
        {
            OfflinePlayer offlinePlayer = Utils.getPlayer(target);
            if(offlinePlayer == null)
            {
                throw(new CommandException("That player was not found."));
            }
            
            return offlinePlayer.getPlayer();
        }
    }
    
    private static String getWorldGroup(CommandSender sender, String target) throws Exception
    {
        if(target == null)
        {
            if(!(sender instanceof Player))
            {
                throw(new CommandException("You have to provide a world group name."));
            }
            
            target = PluginState.getWorldConfig().getGroupFromWorld(((Player) sender).getWorld());
        }
        else
        {
            target = PluginState.getWorldConfig().getGroupFromGroupName(target);
            if(target == null)
            {
                throw(new CommandException("That world group was not found."));
            }
        }
        
        return target;
    }
}
