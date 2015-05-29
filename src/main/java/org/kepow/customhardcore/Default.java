package org.kepow.customhardcore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that holds the default values 
 * for the configuration options. 
 * @author Thomas Churchman
 *
 */
public class Default
{
    public static final Map<String, Object> VALUES;
    
    static
    {
        Map<String, Object> values = new HashMap<String, Object>();
        
        values.put("timezone", "Europe/Amsterdam");
        values.put("enabled", false);
        values.put("lives", 1);
        values.put("banishTime", 3.0);
        values.put("lifeRegenerationTime", 0.0);
        
        Map<String, Object> banishLocation = new HashMap<String, Object>();
        banishLocation.put("world", "world");
        banishLocation.put("x", 0.0);
        banishLocation.put("y", 65.0);
        banishLocation.put("z", 0.0);
        values.put("banishLocation", Collections.unmodifiableMap(banishLocation));
        		
        VALUES = Collections.unmodifiableMap(values);
    }
}
