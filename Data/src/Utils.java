import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Utils {
    static int topK = 100;
    static int numEntities = 1000;
    static int outputSize = 10;
    public static int updateQueue(TreeMap<Double, Set<String>> queue, int count, int capacity, double score, String element) {
        if(count < capacity) {
            insert(queue, score, element);
            return count + 1;
        }
        else {
            if(queue.firstKey() < score) {
                if(queue.firstEntry().getValue().size() > 1) {
                    String tmpStr = "";
                    for(String s : queue.firstEntry().getValue()) {
                        tmpStr = s;
                        break;
                    }
                    queue.firstEntry().getValue().remove(tmpStr);
                }
                else {
                    queue.pollFirstEntry();
                }
                insert(queue, score, element);
            }
            return count;
        }
    }
    private static void insert(TreeMap<Double, Set<String>> queue, double score, String element) {
        if(queue.containsKey(score)) {
            queue.get(score).add(element);
        }
        else {
            Set<String> tmpSet = new HashSet<>();
            tmpSet.add(element);
            queue.put(score, tmpSet);
        }
    }
    public static <T, F> void writeFileReverse(BufferedWriter bw, TreeMap<T, Set<F>> map) throws IOException {
        while(!map.isEmpty()) {
            T t = map.lastKey();
            Set<F> set = map.get(t);
            for(F f : set) {
                bw.write(f + ",");
            }
            map.pollLastEntry();
        }
        bw.write("\n");
    }
}
