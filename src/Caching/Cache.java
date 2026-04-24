package Caching;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache<K,V> {
    CacheEvictionPolicy<K> policy;
    Map<K,V> map;
    int capacity;
    Cache(CacheEvictionPolicy<K> policy, int capacity) {
        this.policy = policy;
        this.capacity = capacity;
        this.map = new ConcurrentHashMap<>();
    }

    V get(K key) {
        if(!map.containsKey(key)) throw new RuntimeException("Key not present in cache");
        policy.onGet(key);
        return map.get(key);
    }

    void put(K key, V value) {
        if(map.containsKey(key)) {
            policy.onGet(key);
            map.put(key, value);
            return;
        }
        if(map.size() >= capacity) {
           K evictedKey = policy.evict();
           map.remove(evictedKey);
        }
        map.put(key, value);
        policy.onPut(key);
    }
}
