package Caching;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class LRUNode<K> {
    LRUNode<K> prev;
    LRUNode<K> next;
    K key;

    public LRUNode(K key) {
        this.prev = null;
        this.next = null;
        this.key = key;
    }
}
public class LRUCachePolicy<K> implements CacheEvictionPolicy<K> {

    Map<K, LRUNode<K>> map;
    LRUNode<K> head;
    LRUNode<K> tail;
    LRUCachePolicy() {
        map = new ConcurrentHashMap<>();
        head = new LRUNode<>(null);
        tail = new LRUNode<>(null);
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void onGet(K key) {
        if(!map.containsKey(key)) {
            return;
        }
        LRUNode<K> node = map.get(key);
        remove(node);
        addToHead(node);
    }

    @Override
    public void onPut(K key) {
        if(map.containsKey(key)) {
            onGet(key);
            return;
        }
        map.put(key, new LRUNode<>(key));
        addToHead(map.get(key));
    }

    @Override
    public void onRemove(K key) {
        if(!map.containsKey(key)) {
            return;
        }
        LRUNode<K> node = map.get(key);
        remove(node);
        map.remove(key);
    }

    @Override
    public K evict() {
        if(map.isEmpty()) return null;
        K keyToevict = tail.prev.key;
        map.remove(keyToevict);
        remove(tail.prev);
        return keyToevict;
    }

    void remove(LRUNode<K> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    void addToHead(LRUNode<K> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }
}
