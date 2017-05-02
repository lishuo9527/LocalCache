package me.lishuo.cache;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by lis on 17/5/2.
 */
public class LRULocalCache {

    /**
     * 默认有效时长,单位:秒
     */
    private static final int DEFUALT_TIMEOUT = 3600;

    private static final long SECOND_TIME = 1000;

    private static final Map<String, Object> map;

    private static final Timer timer;

    /**
     * 读写锁
     */
    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private static final Lock rLock = readWriteLock.readLock();

    private static final Lock wLock = readWriteLock.writeLock();

    /**
     * 默认缓存容量
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

    /**
     * 加载因子
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 初始化
     */
    static {
        timer = new Timer();
        map = new LinkedHashMap<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true);
    }

    /**
     * 私有构造函数,工具类不允许实例化
     */
    private LRULocalCache() {

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
    public static void add(String key, Object value) {
        wLock.lock();
        try {
            map.put(key, value);
            timer.schedule(new LocalCache.CleanWorkerTask(key), DEFUALT_TIMEOUT);
        } finally {
            wLock.unlock();
        }
    }


    /**
     * 增加缓存
     *
     * @param key
     * @param value
     * @param timeout 有效时长
     */
    public static void add(String key, Object value, int timeout) {
        wLock.lock();
        try {
            map.put(key, value);
            timer.schedule(new LocalCache.CleanWorkerTask(key), timeout * SECOND_TIME);
        } finally {
            wLock.unlock();
        }
    }

    /**
     * 增加缓存
     *
     * @param key
     * @param value
     * @param expireTime 过期时间
     */
    public static void add(String key, Object value, Date expireTime) {
        wLock.lock();
        try {
            map.put(key, value);
            timer.schedule(new LocalCache.CleanWorkerTask(key), expireTime);
        } finally {
            wLock.unlock();
        }
    }


    /**
     * 批量增加缓存
     *
     * @param m
     */
    public static void addAll(Map<String, Object> m) {
        wLock.lock();
        try {
            map.putAll(m);
            for (String key : m.keySet()) {
                timer.schedule(new LocalCache.CleanWorkerTask(key), DEFUALT_TIMEOUT);
            }
        } finally {
            wLock.unlock();
        }
    }

    /**
     * 批量增加缓存
     *
     * @param m
     */
    public static void addAll(Map<String, Object> m, int timeout) {
        wLock.lock();
        try {
            map.putAll(m);
            for (String key : m.keySet()) {
                timer.schedule(new LocalCache.CleanWorkerTask(key), timeout * SECOND_TIME);
            }
        } finally {
            wLock.unlock();
        }
    }

    /**
     * 批量增加缓存
     *
     * @param m
     */
    public static void addAll(Map<String, Object> m, Date expireTime) {
        wLock.lock();
        try {
            map.putAll(m);
            for (String key : m.keySet()) {
                timer.schedule(new LocalCache.CleanWorkerTask(key), expireTime);
            }
        } finally {
            wLock.unlock();
        }
    }

    /**
     * 获取缓存
     *
     * @param key
     * @return
     */
    public static Object get(String key) {
        rLock.lock();
        try {
            return map.get(key);
        } finally {
            rLock.unlock();
        }

    }

    /**
     * 查询缓存是否包含key
     *
     * @param key
     * @return
     */
    public static boolean containsKey(String key) {
        rLock.lock();
        try {
            return map.containsKey(key);
        } finally {
            rLock.unlock();
        }

    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public static void remove(String key) {
        wLock.lock();
        try {
            map.remove(key);
        } finally {
            wLock.unlock();
        }

    }

    /**
     * 删除缓存
     *
     * @param o
     */
    public static void remove(Object o) {
        wLock.lock();
        try {
            map.remove(o);
        } finally {
            wLock.unlock();
        }
    }

    /**
     * 返回缓存大小
     *
     * @return
     */
    public static int size() {
        rLock.lock();
        try {
            return map.size();
        } finally {
            rLock.unlock();
        }
    }

    /**
     * 清除所有缓存
     *
     * @return
     */
    public static void clear() {
        wLock.lock();
        try {
            if (size() > 0) {
                map.clear();
            }
            timer.cancel();
        } finally {
            wLock.unlock();
        }

    }
}
