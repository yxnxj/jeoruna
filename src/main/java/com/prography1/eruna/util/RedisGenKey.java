package com.prography1.eruna.util;

public class RedisGenKey {
    private static final String GROUP_KEY = "group";

    public static String generateGroupKey(Long groupId){
        return GROUP_KEY + ":" + groupId;
    }
}
