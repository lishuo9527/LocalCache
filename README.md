## Java LocalCache

### 使用场景

在`Java`应用中，对于访问频率高，更新少的数据，通常的方案是将这类数据加入缓存中。相对从数据库中读取来说，读缓存效率会有很大提升。

在集群环境下，常用的分布式缓存有`Redis`、`Memcached`等。但在某些业务场景上，可能不需要去搭建一套复杂的分布式缓存系统，在单机环境下，通常是会希望使用内部的缓存（`LocalCache`）。

### 实现

这里提供了两种`LocalCache`的实现，一种是基于`ConcurrentHashMap`实现基本本地缓存，另外一种是基于`LinkedHashMap`实现`LRU`策略的本地缓存。

#### 基于ConcurrentHashMap的实现

```java
    static {
        timer = new Timer();
        map = new ConcurrentHashMap<>();
    }
```

以`ConcurrentHashMap`作为缓存的存储结构。因为`ConcurrentHashMap`的线程安全的，所以基于此实现的`LocalCache`在多线程并发环境的操作是安全的。在`JDK1.8`中，`ConcurrentHashMap`是支持完全并发读，这对本地缓存的效率也是一种提升。通过调用`ConcurrentHashMap`对`map`的操作来实现对缓存的操作。
##### 私有构造函数

```java
    private LocalCache() {

    }
```
`LocalCache`是工具类，通过私有构造函数强化不可实例化的能力。

##### 缓存清除机制

```java
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
```

清理失效缓存是由`Timer`类实现的。内部类`CleanWorkerTask`继承于`TimerTask`用户清除缓存。每当新增一个元素的时候，都会调用`timer.schedule`加载清除缓存的任务。

#### 基于LinkedHashMap的实现

以`LinkedHashMap`作为缓存的存储结构。主要是通过`LinkedHashMap`的按照访问顺序的特性来实现`LRU`策略。

##### LRU
`LRU`是`Least Recently Used`的缩写，即最近最久未使用。`LRU`缓存将会利用这个算法来淘汰缓存中老的数据元素，从而优化内存空间。
##### 基于LRU策略的map
这里利用`LinkedHashMap`来实现基于`LRU`策略的`map`。通过调用父类`LinkedHashMap`的构造函数来实例化`map`。参数`accessOrder`设置为`true`保证其可以实现`LRU`策略。

```java
static class LRUMap<K, V> extends LinkedHashMap<K, V> {

        ...  // 省略部分代码
        
        public LRUMap(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor, true);
        }

        ... // 省略部分代码
        
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
```

##### 线程安全

```java
        /**
         * 读写锁
         */
        private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        private final Lock rLock = readWriteLock.readLock();

        private final Lock wLock = readWriteLock.writeLock();
```
`LinkedHashMap`并不是线程安全，如果不加控制的在多线程环境下使用的话，会有问题。所以在`LRUMap`中引入了`ReentrantReadWriteLock`读写锁，来控制并发问题。
##### 缓存淘汰机制

```java
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > DEFAULT_MAX_CAPACITY;
        }
```

此处重写`LinkedHashMap`中`removeEldestEntry`方法， 当缓存新增元素的时候,会判断当前`map`大小是否超过`DEFAULT_MAX_CAPACITY`,超过则移除map中最老的节点。

##### 缓存清除机制

缓存清除机制与`ConcurrentHashMap`的实现一致，均是通过`timer`实现。

