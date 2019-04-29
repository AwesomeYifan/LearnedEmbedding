import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

public class Utils {
    static int topK = 100;
    static int numEntities = 20;
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
    public static double getNextGaussian(double mean, double deviation) {
        Random random = new Random();
        return random.nextGaussian() * deviation + mean;
    }
    public static double[][] getGaussianPoints(int numPoints, double centers[], double deviation) {
        int numDims = centers.length;
        double deviationEachAxis = Math.sqrt(Math.pow(deviation, 2)/numDims);
        double[][] results = new double[numPoints][numDims];
        Random random = new Random();
        for(int i = 0; i < numPoints; i++) {
            for(int j = 0; j < numDims; j++) {

                do {
                    results[i][j] = random.nextGaussian() * deviationEachAxis + centers[j];
                } while(results[i][j] < 0 || results[i][j] >1);
            }
        }
        return results;
    }
    public static double[][] getUniformPoints(int numPoints, int dim) {
        double[][] results = new double[numPoints][dim];
        Random random = new Random();
        for(int i = 0; i < numPoints; i++) {
            for(int j = 0; j < dim; j++) {
                results[i][j] = random.nextDouble();
            }
        }
        return results;
    }
    public static double computeSimilarity(double[] vec1, double[] vec2, double maxDist, String distOpt, String scaleOpt) {
        double sim = 0;
        switch (distOpt) {
            case "Euclidean": {
                double sumDist = 0;
                for(int i = 0; i < vec1.length; i++) {
                    sumDist += Math.pow(vec1[i] - vec2[i], 2);
                }
                sim = (maxDist - Math.sqrt(sumDist)) / maxDist;
                break;
            }
            case "Manhattan": {
                double sumDist = 0;
                for(int i = 0; i < vec1.length; i++) {
                    sumDist += Math.abs(vec1[i] - vec2[i]);
                }
                sim = (maxDist - sumDist)/maxDist;
                break;
            }
            default: {
                sim = 0.0;
            }
        }

        switch (scaleOpt) {
            case "square": {
                sim = Math.pow(sim, 2);
                break;
            }
            case "stair": {
                BigDecimal bd = new BigDecimal(sim);
                bd = bd.round(new MathContext(3));
                //return bd.doubleValue();
                sim = Math.pow(bd.doubleValue(),2);
                break;
            }
            default: {
                sim = sim;
            }
        }
        return sim;
    }

    public static double[] transform(String line, String separator) {
        String[] record = line.split(separator);
        double[] result = new double[record.length];
        int idx = 0;
        for(String s : record) {
            result[idx] = Double.valueOf(s);
            idx ++;
        }
        return result;
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
    public static <T, F> void writeDescending(BufferedWriter bw, TreeMap<T, Set<F>> map) throws IOException {
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

    public static <T, F> void writeAscending(BufferedWriter bw, TreeMap<T, Set<F>> map) throws IOException {
        while(!map.isEmpty()) {
            T t = map.firstKey();
            Set<F> set = map.get(t);
            for(F f : set) {
                bw.write(f + ",");
            }
            map.pollFirstEntry();
        }
        bw.write("\n");
    }
    public static <T,F> void updatePriorityQueue(TreeMap<T, Set<F>> map, T key, F value) {
        if(map.containsKey(key)) {
            map.get(key).add(value);
        }
        else {
            Set<F> set = new HashSet<>();
            set.add(value);
            map.put(key, set);
        }
    }
}
