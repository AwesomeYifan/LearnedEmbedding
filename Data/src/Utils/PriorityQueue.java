package Utils;

import java.util.*;

public class PriorityQueue<K extends Comparable,V> {
    private int limit;
    private int size;
    private TreeMap<K, Set<V>> queue;
    private boolean isAscending;

    public PriorityQueue(int limit, String opt) {
        prepareQueue(limit, opt);
    }

    public PriorityQueue(String opt) {
        int limit = Integer.MAX_VALUE;
        prepareQueue(limit, opt);

    }

    private void prepareQueue(int limit, String opt) {
        this.limit = limit;
        this.size = 0;
        this.queue = new TreeMap<>();
        switch (opt) {
            case "ascending":
            case "high":
                isAscending = true;
                break;
            case "descending":
            case "low":
                isAscending = false;
                break;
            default:
                throw new IllegalArgumentException("Priority queue option error!");
        }
    }

    public void insert(K key, V value) {
        if(size < limit) {
            updateQueue(key, value);
            size++;
        }
        else {
            if((isAscending && key.compareTo(queue.lastKey()) <= 0) ||
                    (!isAscending && key.compareTo(queue.firstKey()) >= 0)) {
                updateQueue(key, value);
                cutQueue(queue);
                size = limit;
            }
        }
    }
    public List<K> serializeKeys() {
        List<K> queueList = new ArrayList<>();
        if(isAscending) {
            while(!queue.isEmpty()) {
                K k = queue.firstKey();
                int count = queue.get(k).size();
                for(int i = 0; i < count; i++) {
                    queueList.add(k);
                }
                queue.pollFirstEntry();
            }
        }
        else {
            while(!queue.isEmpty()) {
                K k = queue.lastKey();
                int count = queue.get(k).size();
                for(int i = 0; i < count; i++) {
                    queueList.add(k);
                }
                queue.pollLastEntry();
            }
        }
        return queueList;
    }
    public List<V> serialize() {
        List<V> queueList = new ArrayList<>();
        if(isAscending) {
            while(!queue.isEmpty()) {
                K k = queue.firstKey();
                Set<V> tempSet = queue.get(k);
                queueList.addAll(tempSet);
                queue.pollFirstEntry();
            }
        }
        else {
            while(!queue.isEmpty()) {
                K k = queue.lastKey();
                Set<V> tempSet = queue.get(k);
                queueList.addAll(tempSet);
                queue.pollLastEntry();
            }
        }
        return queueList;
    }
    public K getBottomKey() {
        if(isAscending)
            return this.queue.lastKey();
        else
            return this.queue.firstKey();
    }
    public K getTopKey() {
        if(isAscending)
            return this.queue.firstKey();
        else
            return this.queue.lastKey();
    }
    public void clear() {
        this.size = 0;
        this.queue.clear();
    }

    private void updateQueue(K key, V value) {
        if(queue.containsKey(key)) {
            queue.get(key).add(value);
        }
        else {
            Set<V> tempSet = new HashSet<>();
            tempSet.add(value);
            queue.put(key, tempSet);
        }
    }
    private void cutQueue(TreeMap<K, Set<V>> queue) {
        if(isAscending) {
            Set<V> tempSet = queue.get(queue.lastKey());
            V elementToRemove = tempSet.stream().findFirst().orElse(null);
            queue.get(queue.lastKey()).remove(elementToRemove);
            if(queue.get(queue.lastKey()).size() == 0)
                queue.pollLastEntry();
        }
        else {
            Set<V> tempSet = queue.get(queue.firstKey());
            V elementToRemove = tempSet.stream().findFirst().orElse(null);
            queue.get(queue.firstKey()).remove(elementToRemove);
            if(queue.get(queue.firstKey()).size() == 0)
                queue.pollFirstEntry();
        }
    }
}
