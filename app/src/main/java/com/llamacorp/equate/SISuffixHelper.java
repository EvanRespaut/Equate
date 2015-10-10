package com.llamacorp.equate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class used to give the name equivelent of decades of numbers
 * For example 1E6 is mega, and E-9 is nano
 */
public class SISuffixHelper {
   private static final Map<String, String> mSISuffixMap;

   static {
      Map<String, String> aMap = new HashMap<>();
      aMap.put("E24",  "yotta");
      aMap.put("E21",  "zetta");
      aMap.put("E18",  "exa");
      aMap.put("E15",  "peta");
      aMap.put("E12",  "tera");
      aMap.put("E9",   "giga");
      aMap.put("E6",   "mega");
      aMap.put("E3",   "kilo");
      aMap.put("E2",   "hecto");
      aMap.put("E1",   "deca");
      aMap.put("E-1",  "deci");
      aMap.put("E-2",  "centi");
      aMap.put("E-3",  "milli");
      aMap.put("E-6",  "micro");
      aMap.put("E-9",  "nano");
      aMap.put("E-12", "pico");
      aMap.put("E-15", "femto");
      aMap.put("E-18", "atto");
      aMap.put("E-21", "zepto");
      mSISuffixMap = Collections.unmodifiableMap(aMap);
   }

   /**
    * Get the SI unit name for the exponent attached to the given String
    * representation of a number
    * @param number given in string format
    * @return SI unit name (for 2E9 return giga, for 1E-12 pico)
    */
   public static String getSuffixName(String number) {
      //if we have more that one E for some reason, leave
      if(number.matches(".*E.*E.*")) return "";

      String result = mSISuffixMap.get(number.substring(number.indexOf("E")));

      if(result != null) return result;
      else return "";
   }
}
