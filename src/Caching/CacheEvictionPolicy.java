package Caching;

public interface CacheEvictionPolicy<K> {
    void onGet(K key); // when we get key
    void onPut(K key); // put a new key
    void onRemove(K key); //remove key by user
    K evict(); //if exceed capacity then evict
}
