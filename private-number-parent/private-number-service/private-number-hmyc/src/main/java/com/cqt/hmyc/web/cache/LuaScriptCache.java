package com.cqt.hmyc.web.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/3/22 17:52
 * lua脚本缓存
 */
public class LuaScriptCache {

    /**
     * key 脚本名称 numType:key个数
     * value 脚本内容
     */
    public final static Map<String, String> LUA_SCRIPT_MAP = new ConcurrentHashMap<>(32);

    /**
     * key 脚本名称 numType:key个数
     * value 脚本sha2加密字符串
     */
    public final static Map<String, String> LUA_SCRIPT_SHA1_MAP = new ConcurrentHashMap<>(32);


    public static String getScript(String key) {

        return LUA_SCRIPT_MAP.get(key);
    }

    public static void putScript(String key, String value) {

        LUA_SCRIPT_MAP.put(key, value);
    }

    public static Integer sizeScript() {

        return LUA_SCRIPT_MAP.size();
    }

    public static void clearScript() {

        LUA_SCRIPT_MAP.clear();
    }

    public static String getSha1(String key) {

        return LUA_SCRIPT_SHA1_MAP.get(key);
    }

    public static void putSha1(String key, String value) {

        LUA_SCRIPT_SHA1_MAP.put(key, value);
    }

    public static Integer sizeSha1() {

        return LUA_SCRIPT_SHA1_MAP.size();
    }

    public static void clearSha1() {

        LUA_SCRIPT_SHA1_MAP.clear();
    }
}
