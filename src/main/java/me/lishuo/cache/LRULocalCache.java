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
     * 初始化
     */
    static {
        timer = new Timer();
        map = new LRUMap<>();
    }

    /**
     * 私有构造函数,工具类不允许实例化
     */
    private LRULocalCache() {

    }

    /**
     * 基于LRU策略的map
     *
     * @param <K>
     * @param <V>
     */
    static class LRUMap<K, V> extends LinkedHashMap<K, V> {

        /**
         * 读写锁
         */
        private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        private final Lock rLock = readWriteLock.readLock();

        private final Lock wLock = readWriteLock.writeLock();

        /**
         * 默认缓存容量
         */
        private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

        /**
         * 默认最大缓存容量
         */
        private static final int DEFAULT_MAX_CAPACITY = 1 << 30;

        /**
         * 加载因子
         */
        private static final float DEFAULT_LOAD_FACTOR = 0.75f;

        public LRUMap() {
            super(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
        }

        public LRUMap(int initialCapacity) {
            super(initialCapacity, DEFAULT_LOAD_FACTOR);
        }


        public V get(String k) {
            rLock.lock();
            try {
                return super.get(k);
            } finally {
                rLock.unlock();
            }
        }

        public V put(K k, V v) {
            wLock.lock();
            try {
                return super.put(k, v);
            } finally {
                wLock.unlock();
            }
        }

        public void putAll(Map<? extends K, ? extends V> m) {
            wLock.lock();
            try {
                super.putAll(m);
            } finally {
                wLock.unlock();
            }
        }

        public V remove(Object k) {
            wLock.lock();
            try {
                return super.remove(k);
            } finally {
                wLock.unlock();
            }
        }


        public boolean containKey(K k) {
            rLock.lock();
            try {
                return super.containsKey(k);
            } finally {
                rLock.unlock();
            }
        }

        public int size() {
            rLock.lock();
            try {
                return super.size();
            } finally {
                rLock.unlock();
            }
        }


        public void clear() {
            wLock.lock();
            try {
                super.clear();
            } finally {
                wLock.unlock();
            }
        }


        /**
         * 重写LinkedHashMap中removeEldestEntry方法;
         * 新增元素的时候,会判断当前map大小是否超过DEFAULT_MAX_CAPACITY,超过则移除map中最老的节点;
         *
         * @param eldest
         * @return
         */
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > DEFAULT_MAX_CAPACITY;
        }

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
        map.put(key, value);
        timer.schedule(new LocalCache.CleanWorkerTask(key), DEFUALT_TIMEOUT);

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
        timer.schedule(new LocalCache.CleanWorkerTask(key), timeout * SECOND_TIME);
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
        timer.schedule(new LocalCache.CleanWorkerTask(key), expireTime);
    }


    /**
     * 批量增加缓存
     *
     * @param m
     */
    public static void putAll(Map<String, Object> m) {
        map.putAll(m);
        for (String key : m.keySet()) {
            timer.schedule(new LocalCache.CleanWorkerTask(key), DEFUALT_TIMEOUT);
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
            timer.schedule(new LocalCache.CleanWorkerTask(key), timeout * SECOND_TIME);
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
            timer.schedule(new LocalCache.CleanWorkerTask(key), expireTime);
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
