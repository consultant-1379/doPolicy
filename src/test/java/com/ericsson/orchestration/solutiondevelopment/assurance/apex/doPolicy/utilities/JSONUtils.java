package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONUtils
{

    /** Method to convert input json object into hash map with key and value mapped to input json fields
     * @param element- input json
     * @return HashMap- Converted json elements into key value pair
     * @throws Exception
     */
    public static Object convertToJsonElement(Object element)
    {

        if (element instanceof JSONObject)
        {
            JSONObject obj = (JSONObject) element;
            Iterator<?> keys = obj.keySet().iterator();
            Map<String, Object> jsonMap = new HashMap<String, Object>();
            while (keys.hasNext())
            {
                String key = (String) keys.next();
                jsonMap.put(key, convertToJsonElement(obj.get(key)));
            }
            return jsonMap;
        }
        else if (element instanceof JSONArray)
        {
            JSONArray arr = (JSONArray) element;
            Set<Object> jsonSet = new HashSet<>();
            for (int i = 0; i < arr.size(); i++)
            {
                jsonSet.add(convertToJsonElement(arr.get(i)));
            }
            return jsonSet;
        }
        else
        {
            return element;
        }
    }

    /** Method to filter data from original input
     * @param key - data that need to be filtered
     * @param originalData - original input
     * @return filtered map
     */
    public static Object filterData(String key, Object originalData)
    {

        if (originalData instanceof Map)
        {
            ((Map<?, ?>) originalData).remove(key);
            return originalData;

        }
        return originalData;

    }
}
