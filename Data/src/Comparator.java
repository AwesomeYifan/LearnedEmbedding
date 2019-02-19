import java.io.*;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class Comparator {
    public static void main (String[] args) throws IOException {
        int[] checks = {2,10,20,30,50,100};
        double[] performs = new double[checks.length];
        Comparator cmp = new Comparator();
        cmp.formatter();
        String path1 = "./data/ranks";
        String path2 = "./data/ranks2";
        BufferedReader r1 = new BufferedReader(new FileReader(path1));
        BufferedReader r2 = new BufferedReader(new FileReader(path2));
        String line1 = "", line2 = "";
        String[] list1, list2;
        Set<String> set1, set2;
        int sum = 0;
        while ((line1 = r1.readLine()) != null && (line2 = r2.readLine()) != null) {
            list1 = line1.split(",");
            list2 = line2.split(",");
            for(int i = 0; i < checks.length; i++) {
                int end = checks[i];
                set1 = new HashSet<>(Arrays.asList(list1).subList(0, end));
                set2 = new HashSet<>(Arrays.asList(list2).subList(0, end));
                set1.retainAll(set2);
                performs[i] += set1.size();
            }
            sum ++;
        }
        for(int i = 0; i < checks.length; i++) {
            performs[i] -= sum;
            performs[i] /= (checks[i] * sum);
        }
        System.out.println(Arrays.toString(performs));
    }
    public void formatter() throws IOException {
        String inPath = "../Model/SiameseLSTM/data/scores";
        String outPath = "./data/ranks2";
        BufferedReader r = new BufferedReader(new FileReader(inPath));
        BufferedWriter w = new BufferedWriter(new FileWriter(outPath));
        TreeMap<Double, Set<String>> treeMap = new TreeMap<>();
        String line;
        double score;
        int idx = 0, count = 0;
        while((line = r.readLine()) != null) {
            score = Double.parseDouble(line);
            count = Utils.updateQueue(treeMap, count, Utils.topK, score, Integer.toString(idx));
            idx++;
            if(idx == 1000) {
                Utils.writeFileReverse(w,treeMap);
                treeMap = new TreeMap<>();
                idx = 0;
                count = 0;
            }
        }
        r.close();
        w.flush();
        w.close();
    }
}
