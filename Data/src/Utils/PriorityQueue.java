package Utils;

import java.util.*;

public class PriorityQueue<K extends Comparable,V> {
    private int limit;
    private int size;
    public TreeMap<K, Set<V>> queue;
    private boolean isAscending;
    public Map<V, K> invertedList;

    public PriorityQueue(int limit, String opt) {
        prepareQueue(limit, opt);
    }

    public PriorityQueue(String opt) {
        int limit = Integer.MAX_VALUE;
        prepareQueue(limit, opt);
    }

    public boolean isEmpty() {
        return queue.size() == 0;
    }

    public V dequeue() {
        if(isAscending) {
            Set<V> set = queue.get(queue.firstKey());
            for(V v : set) {
                set.remove(v);
                invertedList.remove(v);
                size--;
                if(set.size() == 0) {
                    queue.remove(queue.firstKey());
                }
                return v;
            }
        }
        else{
            Set<V> set = queue.get(queue.lastKey());
            for(V v : set) {
                set.remove(v);
                invertedList.remove(v);
                size--;
                if(set.size() == 0) {
                    queue.remove(queue.lastKey());
                }
                return v;
            }
        }
        System.out.println("No element to dequeue because the queue is empty!");
        return null;
    }

    public K findByValue(V element) {
        if(invertedList.containsKey(element)) {
            return invertedList.get(element);
        }
        System.out.println("Element " + element + " not found!");
        return null;
    }

    public void removeByValue(V elementToRemove) {
        if(!invertedList.containsKey(elementToRemove)) {
            System.out.println("The queue does not contain element " + elementToRemove);
        }
        else {
            K key = invertedList.get(elementToRemove);
            queue.get(key).remove(elementToRemove);
            size--;
            if(queue.get(key).isEmpty()) {
                queue.remove(key);
            }
            invertedList.remove(elementToRemove);
        }
    }

    private void prepareQueue(int limit, String opt) {
        this.limit = limit;
        this.size = 0;
        invertedList = new HashMap<>();
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

    //get the k-th element, k>=0
    public K getKey(int position) {
        Iterator<K> itr = queue.keySet().iterator();
        int id = 0;
        K current = null;
        while(itr.hasNext()) {
            current = itr.next();
            if(id == position) break;
            id++;
        }
        return current;
    }
    public Set<V> getValues(int position) {

        K key = getKey(position);

        return queue.get(key);
    }
    public void reSize(int newSize) {
        this.limit = newSize;
        while(this.size > newSize) {
            cutQueue();
            this.size--;
        }
    }

    public int getNumEntries() {
        return this.queue.size();
    }
    public int getSize() {
        return this.size;
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
                cutQueue();
                size = limit;
            }
        }
        invertedList.put(value, key);
    }
    public List<K> serializeKeys() {
        TreeMap<K, Set<V>> queue = new TreeMap<>(this.queue);
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
        TreeMap<K, Set<V>> queue = new TreeMap<>(this.queue);
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
    //the largest
    public K getBottomKey() {
        if(isAscending)
            return this.queue.lastKey();
        else
            return this.queue.firstKey();
    }

    //the smallest
    public K getTopKey() {
        if(isAscending)
            return this.queue.firstKey();
        else
            return this.queue.lastKey();
    }
    public Set<V> pop() {
        if(isAscending)
            return this.queue.pollFirstEntry().getValue();
        else
            return this.queue.pollLastEntry().getValue();
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
    private void cutQueue() {
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
