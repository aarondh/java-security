package org.daisleyharrison.security.services.cypher.internal;

public class KeyPathHelper {
    public static String join(String pathA, String pathB){
        StringBuffer result = new StringBuffer(pathA);
        if(!pathB.startsWith("/")){
            pathB = "/" + pathB;
        }
        if(result.indexOf(pathB)<0){
            int versionSep = result.indexOf("@");
            if(versionSep >= 0){
                result.insert(versionSep, pathB);
            } else if(pathA.endsWith("/")){
                result.setLength(result.length()-1);
                result.append(pathB);
            } else {
                result.append(pathB);
            }
        }
        return result.toString();
    }
}