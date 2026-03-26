import java.util.HashMap;
import java.util.Map;

class Node {
    int key;
    int value;
    Node prev;
    Node next;
    int freq;

    public Node(int key, int value) {
        this.key = key;
        this.value = value;
        this.prev = null;
        this.next = null;
        this.freq = 1;
    }
}

public class LFU {
    Map<Integer, Node> lfuCache;
    Map<Integer, Node>  freqMap;
    int capacity;
    int minFreq;

    public LFU(int capacity) {
        this.freqMap = new HashMap<>();
        this.lfuCache = new HashMap<>();
        this.capacity = capacity;
        minFreq = 1;
    }

    public int get(int key) {
        if(!lfuCache.containsKey(key)) return -1;
        Node node = lfuCache.get(key);
        Node head = freqMap.get(node.freq);
        Node tail = head.prev;
        remove(tail.prev);
        if(head.next == tail) minFreq++;
        node.freq++;
        addNodeToHead(node, freqMap.get(node.freq));
        return node.value;
    }

    public void put(int key, int value) {
        if(lfuCache.containsKey(key)) {
            // get()

            //update()
            lfuCache.get(key).value = value;
            return;
        }
        // new key
        if(capacity >= lfuCache.size()) {
            // remove
            Node head = freqMap.get(minFreq);
            Node tail = head.prev;

            remove(tail.prev);

        }
        minFreq = 1;
        Node node = new Node(key, value);
        addNodeToHead(node, freqMap.get(minFreq));
        lfuCache.put(key, node);
    }

    void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node = null;
    }

    void addNodeToHead(Node node, Node head) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }
}
