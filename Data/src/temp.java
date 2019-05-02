import java.io.IOException;
import java.sql.SQLOutput;
import java.util.Arrays;

import Utils.PriorityQueue;

public class temp {
    public static void main(String[] args) throws Exception {
        PriorityQueue pq = new PriorityQueue(5, "descending");
        for(Integer i = 0; i < 10; i++) {
            pq.insert(i, i);
        }
        pq.insert(1,10);
        System.out.println(Arrays.toString(pq.serialize().toArray()));
    }
}
