package Walmart.Round2;

public interface CacheEvictionPolicy<K> {
    void keyAccessed(K key);
    K evictkey();
}
