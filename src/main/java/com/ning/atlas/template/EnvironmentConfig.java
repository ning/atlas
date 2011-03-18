package com.ning.atlas.template;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnvironmentConfig
{
    private final Map<String, String> configVars = new LinkedHashMap<String, String>();
    private final Map<String, Object> deployVars = new LinkedHashMap<String, Object>();
    private final Map<String, Integer> cardinalityOverrides = new LinkedHashMap<String, Integer>();

    public void addConfigVar(String key, String value)
    {
        this.configVars.put(key, value);
    }

    public void addDeployVar(String key, List<String> value)
    {
        deployVars.put(key, value);
    }

    public Map<String, String> propsFor(String instanceName)
    {
        return new HashMap<String, String>(configVars);
    }

    public void overrideCardinality(String name, int cardinality)
    {
        cardinalityOverrides.put(name, cardinality);
    }

    public int cardinalityFor(String name, int defalt) {
        if (cardinalityOverrides.containsKey(name)){
            return cardinalityOverrides.get(name);
        }
        else {
            return defalt;
        }
    }
}
