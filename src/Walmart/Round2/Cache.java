package Walmart.Round2;

import java.util.Map;

public class Cache<K,V> {
    private  final  int capacity;;
    private final Map<K,V> storage;
    private final CacheEvictionPolicy<K> cacheEvictionPolicy;

    public Cache(int capacity, Map<K, V> storage, CacheEvictionPolicy<K> cacheEvictionPolicy) {
        this.capacity = capacity;
        this.storage = storage;
        this.cacheEvictionPolicy = cacheEvictionPolicy;
    }

    public V get(K key) {
        if(!storage.containsKey(key)) return null;
        cacheEvictionPolicy.keyAccessed(key);
        return storage.get(key);
    }

    public void put(K key, V value) {
        if(storage.size() >= capacity) {
            K evicKey = cacheEvictionPolicy.evictkey();
            if(evicKey !=null) {
                storage.remove(evicKey);
            }
        }
//        if(!storage.containsKey(key)) {
//            storage.put(key, value);
//            cacheEvictionPolicy.keyAccessed(key);
//            return;
//        }
        storage.put(key, value);
        cacheEvictionPolicy.keyAccessed(key);
    }
}
