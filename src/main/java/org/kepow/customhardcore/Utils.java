package org.kepow.customhardcore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

/**
 * Class containing some utility methods.
 * 
 * @author Thomas Churchman
 *
 */
public class Utils
{
    /**
     * Get the current system time (unix) in seconds.
     * @return The  current system time in seconds.
     */
    public static long getCurrentTime()
    {
        return System.currentTimeMillis()/1000L;
    }
    
    /**
     * Find a player by their (last used) name, regardless of whether they are
     * online or offline.
     * @param name The name of the player.
     * @return The OfflinePlayer if they have been seen by the server, or null.
     */
    public static OfflinePlayer getPlayer(String name)
    {
        OfflinePlayer[] players = PluginState.getPlugin().getServer().getOfflinePlayers();

        OfflinePlayer ret = null;

        for(OfflinePlayer player : players)
        {
            if(player != null && player.getName() != null)
            {
                if(player.getName().equalsIgnoreCase(name))
                {
                    ret = player;
                    break;
                }
            }
        }

        return ret;
    }
    
    /**
     * Round a number to the given number of decimals.
     * @param d The number.
     * @param decimals The number of decimals to round to.
     * @return The rounded number.
     */
    public static double round(double d, int decimals)
    {
        double tenToTheD = Math.pow(10, decimals);
        return Math.round(d * tenToTheD) / tenToTheD;
    }
    
    /**
     * Prepare time strings.
     * @param t The unix time in seconds to set the strings to.
     * @param year The year representing the time.
     * @param month The month representing the time.
     * @param day The day representing the time.
     * @param hour The hour representing the time.
     * @param minute The minute representing the time.
     * @param second The second representing the time.
     */
    public static void prepareTimeStrings(long t, StringBuilder year, StringBuilder month, StringBuilder day, 
        StringBuilder hour, StringBuilder minute, StringBuilder second)
    {
        Date date = new Date(t*1000L);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(PluginState.getPlugin().getConfig().getString("timezone")));
        calendar.setTime(date);
        
        year.append(calendar.get(Calendar.YEAR));
        
        month.append(calendar.get(Calendar.MONTH)+1);
        if(month.length() == 1)
        {
            month.insert(0, "0");
        }
        
        day.append(calendar.get(Calendar.DAY_OF_MONTH));
        if(day.length() == 1)
        {
            day.insert(0, "0");
        }
        
        hour.append(calendar.get(Calendar.HOUR_OF_DAY));
        if(hour.length() == 1)
        {
            hour.insert(0, "0");
        }
        
        minute.append(calendar.get(Calendar.MINUTE));
        if(minute.length() == 1)
        {
            minute.insert(0, "0");
        }
        
        second.append(calendar.get(Calendar.SECOND));
        if(second.length()==1)
        {
            second.insert(0, "0");
        }
    }
    
    
    /**
     * Prepare a message string for outputting.
     * @param message The message string key (without the "messages." prefix) to use.
     * @param replacementsArray An array of consecutive {String, Object} pairs, e.g. {"%value", 2.06}.
     * @return The prepared message string.
     */
    public static String prepareMessage(String message, Object... replacementsArray)
    {
        HashMap<String, Object> replacements = new HashMap<String, Object>();

        // Feed replacements and replaceWiths into the replacement map.
        if(replacementsArray.length % 2 == 0)
        {
            for(int i = 0; i < replacementsArray.length; i+=2)
            {
                String replace = (String) replacementsArray[i];
                replacements.put(replace, replacementsArray[i+1]);
            }
        }

        // Get placeholder message.
        String placeholder = PluginState.getMessagesCustomConfig().getCustomConfig().getString("messages." + message);
        if(placeholder == null)
        {
            placeholder = "String not found: " + message;
        }

        // First parse the message for any potential &word|dependent% occurrences.
        // The words are replaced with the value the word codes for (in config.yml)
        // with the given switch (e.g., "&currency|1%" could code for "%currencySingular"). 
        String placeholderCopy = placeholder; 

        // Find all &...|...% occurrences
        Pattern pattern = Pattern.compile("&[a-zA-Z]+\\|[a-zA-Z]+%");
        Matcher m = pattern.matcher(placeholder);

        while(m.find())
        {   // For every occurrence:
            // Find the part of the string that matched
            String match = m.group(0);

            // Removed the first (&) and last (%) parts of the string and
            // then split the string on |
            String[] matches = match.substring(1, match.length()-1).split("\\|");

            // The first part is the word we're finding the value for
            String word = matches[0];
            // The second part is the case of the word we want (e.g., case "1" of currency)
            String dependent = matches[1];


            if(replacements.containsKey("%" + dependent))
            {   // We know the value of the case
                Object obj = replacements.get("%" + dependent);
                String replaceWith = null;

                // Find the type of the case's value and generate the case value to use
                if(obj instanceof Integer)
                {
                    int val = (Integer) obj;
                    replaceWith = PluginState.getMessagesCustomConfig().getCustomConfig().getString("words." + word + "." + val);
                } 
                else if(obj instanceof Double)
                {
                    double val = Utils.round((Double) obj, PluginState.getMessagesCustomConfig().getCustomConfig().getInt("display.numDecimals"));

                    if ((val == Math.floor(val)) && !Double.isInfinite(val)) {
                        int floor = (int) Math.floor(val);
                        replaceWith = PluginState.getMessagesCustomConfig().getCustomConfig().getString("words." + word + "." + floor);
                    }
                }
                else
                {
                    String val = obj.toString();
                    replaceWith = PluginState.getMessagesCustomConfig().getCustomConfig().getString("words." + word + "." + val);
                }

                // No (existing) case found, default to case "other"
                if(replaceWith == null)
                {
                    replaceWith = PluginState.getMessagesCustomConfig().getCustomConfig().getString("words." + word + ".other");
                }

                placeholderCopy = placeholderCopy.replace(match, replaceWith);
            }
            else
            {   // We don't know the value of the case, default to "other".
                String replaceWith = PluginState.getMessagesCustomConfig().getCustomConfig().getString("words." + word + ".other");
                if(replaceWith != null)
                {
                    placeholderCopy = placeholderCopy.replace(match, replaceWith);
                }
            }
        }

        // Find all &...% occurrences
        pattern = Pattern.compile("&[a-zA-Z]+%");
        m = pattern.matcher(placeholder);

        while(m.find())
        {   // For every occurrence:
            // Find the part of the string that matched
            String match = m.group(0);

            // Removed the first (&) and last (%) parts of the string
            String word = match.substring(1, match.length()-1);     

            String replaceWith = PluginState.getMessagesCustomConfig().getCustomConfig().getString("words." + word + ".other");

            placeholderCopy = placeholderCopy.replace(match, replaceWith);
        }

        placeholder = placeholderCopy;

        // Sort the to-be-replaced strings by length in descending order to make sure that
        // strings such as "%test" and "%te" won't interfere (if "%te" were to be evaluated 
        // first, the result on "%test"would be "{%te}st").
        String[] replacementKeys = replacements.keySet().toArray(new String[0]);
        Arrays.sort(replacementKeys, new Comparator<String>()
            {
            public int compare(String s1, String s2)
            {
                return s2.length() - s1.length();
            }
            });

        for(String replace : replacementKeys)
        {   // For each to-be-replaced string

            // Find the string value of the replacement...
            Object obj = replacements.get(replace);
            String replaceWith = null;
            if(obj instanceof Integer)
            {
                replaceWith = ""+(Integer) obj;
            } 
            else if(obj instanceof Double)
            {
                replaceWith = String.format("%."+PluginState.getPlugin().getConfig().getInt("display.numDecimals")+"f", (Double) obj);
            }
            else
            {
                replaceWith = obj.toString();
            }

            // ... and replace.
            placeholder = placeholder.replaceAll(replace, replaceWith);
        }

        // Replace string formatting codes
        placeholder = placeholder.replaceAll("&0", ChatColor.BLACK.toString());
        placeholder = placeholder.replaceAll("&1", ChatColor.DARK_BLUE.toString());
        placeholder = placeholder.replaceAll("&2", ChatColor.DARK_GREEN.toString());
        placeholder = placeholder.replaceAll("&3", ChatColor.DARK_AQUA.toString());
        placeholder = placeholder.replaceAll("&4", ChatColor.DARK_RED.toString());
        placeholder = placeholder.replaceAll("&5", ChatColor.DARK_PURPLE.toString());
        placeholder = placeholder.replaceAll("&6", ChatColor.GOLD.toString());
        placeholder = placeholder.replaceAll("&7", ChatColor.GRAY.toString());
        placeholder = placeholder.replaceAll("&8", ChatColor.DARK_GRAY.toString());
        placeholder = placeholder.replaceAll("&9", ChatColor.BLUE.toString());
        placeholder = placeholder.replaceAll("&a", ChatColor.GREEN.toString());
        placeholder = placeholder.replaceAll("&b", ChatColor.AQUA.toString());
        placeholder = placeholder.replaceAll("&c", ChatColor.RED.toString());
        placeholder = placeholder.replaceAll("&d", ChatColor.LIGHT_PURPLE.toString());
        placeholder = placeholder.replaceAll("&e", ChatColor.YELLOW.toString());
        placeholder = placeholder.replaceAll("&f", ChatColor.WHITE.toString());

        placeholder = placeholder.replaceAll("&k", ChatColor.MAGIC.toString());
        placeholder = placeholder.replaceAll("&l", ChatColor.BOLD.toString());
        placeholder = placeholder.replaceAll("&m", ChatColor.STRIKETHROUGH.toString());
        placeholder = placeholder.replaceAll("&n", ChatColor.UNDERLINE.toString());
        placeholder = placeholder.replaceAll("&o", ChatColor.ITALIC.toString());
        placeholder = placeholder.replaceAll("&r", ChatColor.RESET.toString());
        
        placeholder = placeholder.replaceAll("\\n", "\n");

        return placeholder;
    }

}
