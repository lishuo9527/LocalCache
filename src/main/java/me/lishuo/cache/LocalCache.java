package me.lishuo.cache;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lis on 17/5/2.
 */
public class LocalCache {

    /**
     * 默认有效时长,单位:秒
     */
    private static final int DEFUALT_TIMEOUT = 3600;

    private static final long SECOND_TIME = 1000;

    private static final Map<String, Object> map;

    private static final Timer timer;

    /**
     * 初始化
     */
    static {
        timer = new Timer();
        map = new ConcurrentHashMap<>();
    }

    /**
     * 私有构造函数,工具类不允许实例化
     */
    private LocalCache() {

    }

    /**
     * 清除缓存任务类
     */
    static class CleanWorkerTask extends TimerTask {

        private String key;

        public CleanWorkerTask(String key) {
            this.key = key;
        }

        public void run() {
            LocalCache.remove(key);
        }
    }

    /**
     * 增加缓存
     *
     * @param key
     * @param value
     */
    public static void put(String key, Object value) {
        map.put(key, value);
        timer.schedule(new CleanWorkerTask(key), DEFUALT_TIMEOUT);
    }


    /**
     * 增加缓存
     *
     * @param key
     * @param value
     * @param timeout 有效时长
     */
    public static void put(String key, Object value, int timeout) {
        map.put(key, value);
        timer.schedule(new CleanWorkerTask(key), timeout * SECOND_TIME);
    }

    /**
     * 增加缓存
     *
     * @param key
     * @param value
     * @param expireTime 过期时间
     */
    public static void put(String key, Object value, Date expireTime) {
        map.put(key, value);
        timer.schedule(new CleanWorkerTask(key), expireTime);
    }


    /**
     * 批量增加缓存
     *
     * @param m
     */
    public static void putAll(Map<String, Object> m) {
        map.putAll(m);

        for (String key : m.keySet()) {
            timer.schedule(new CleanWorkerTask(key), DEFUALT_TIMEOUT);
        }
    }

    /**
     * 批量增加缓存
     *
     * @param m
     */
    public static void putAll(Map<String, Object> m, int timeout) {
        map.putAll(m);

        for (String key : m.keySet()) {
            timer.schedule(new CleanWorkerTask(key), timeout * SECOND_TIME);
        }
    }

    /**
     * 批量增加缓存
     *
     * @param m
     */
    public static void putAll(Map<String, Object> m, Date expireTime) {
        map.putAll(m);

        for (String key : m.keySet()) {
            timer.schedule(new CleanWorkerTask(key), expireTime);
        }
    }

    /**
     * 获取缓存
     *
     * @param key
     * @return
     */
    public static Object get(String key) {
        return map.get(key);
    }

    /**
     * 查询缓存是否包含key
     *
     * @param key
     * @return
     */
    public static boolean containsKey(String key) {
        return map.containsKey(key);
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public static void remove(String key) {
        map.remove(key);
    }

    /**
     * 删除缓存
     *
     * @param o
     */
    public static void remove(Object o) {
        map.remove(o);
    }

    /**
     * 返回缓存大小
     *
     * @return
     */
    public static int size() {
        return map.size();
    }

    /**
     * 清除所有缓存
     *
     * @return
     */
    public static void clear() {
        if (size() > 0) {
            map.clear();
        }
        timer.cancel();
    }

}
