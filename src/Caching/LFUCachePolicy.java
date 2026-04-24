package Caching;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

class LFUNode<K> {
    K key;
    int frequency;

    LFUNode(K key) {
        this.key = key;
        this.frequency = 1;
    }
}

public class LFUCachePolicy<K> implements CacheEvictionPolicy<K> {

    Map<K, LFUNode<K>> keyToNode;
    Map<Integer, LinkedHashSet<K>> freqToKeys;
    int minFreq;

    LFUCachePolicy() {
        keyToNode = new HashMap<>();
        freqToKeys = new HashMap<>();
        minFreq = 0;
    }

    @Override
    public void onGet(K key) {
        if (!keyToNode.containsKey(key)) return;
        incrementFrequency(key);
    }

    @Override
    public void onPut(K key) {
        // fresh insert — frequency starts at 1
        LFUNode<K> node = new LFUNode<>(key);
        keyToNode.put(key, node);
        freqToKeys.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
        minFreq = 1;
    }

    @Override
    public void onRemove(K key) {
        if (!keyToNode.containsKey(key)) return;
        LFUNode<K> node = keyToNode.remove(key);
        freqToKeys.get(node.frequency).remove(key);
    }

    @Override
    public K evict() {
        if (keyToNode.isEmpty()) return null;
        LinkedHashSet<K> minFreqKeys = freqToKeys.get(minFreq);
        // oldest key at minFreq — LinkedHashSet iterator returns insertion order
        K keyToEvict = minFreqKeys.iterator().next();
        minFreqKeys.remove(keyToEvict);
        keyToNode.remove(keyToEvict);
        return keyToEvict;
    }

    private void incrementFrequency(K key) {
        LFUNode<K> node = keyToNode.get(key);
        int oldFreq = node.frequency;

        // remove from old frequency bucket
        LinkedHashSet<K> oldBucket = freqToKeys.get(oldFreq);
        oldBucket.remove(key);

        // if minFreq bucket is now empty, increment minFreq
        if (oldFreq == minFreq && oldBucket.isEmpty()) {
            minFreq++;
        }

        // add to new frequency bucket
        node.frequency++;
        freqToKeys.computeIfAbsent(node.frequency, k -> new LinkedHashSet<>()).add(key);
    }
}
