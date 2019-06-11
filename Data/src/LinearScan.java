import Utils.PriorityQueue;

import java.io.*;
import java.util.*;

import Utils.Utils;
public class LinearScan {

    public static void main(String[] args) throws IOException {
        int numPoints = 25000;
        int originalDim = 128;
        int reducedDim = 20;
        double[][] thresholds = readThreasholds();
        double[][] points = getPointsInReducedSpace(numPoints, reducedDim);
        List<List<Set<Integer>>> kNNs = getkNN(points);
        double[][] testPoints = getPointsInOriginalSpace(numPoints, originalDim);
        double[] acc = compareResults(testPoints, kNNs, thresholds);
        System.out.println(Arrays.toString(acc));
    }

    static double[] compareResults(double[][] points, List<List<Set<Integer>>> kNNs, double[][] thresholds) throws IOException {
        String file = "./data/originalVectors";
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        String line;
        Integer id = 0;
        double[] truePositiveCount = new double[Parameters.topK.length];
        while((line = br.readLine()) != null) {
            String[] record = line.split(" ");
            double[] vectors = new double[record.length];
            for(int i = 0; i < record.length; i++) {
                vectors[i] = Double.parseDouble(record[i]);
            }
            for(int i = 0; i < kNNs.size(); i++) {
                List<Set<Integer>> kNNOfThisPoint = kNNs.get(i);
                for(int j = 0; j < kNNOfThisPoint.size(); j++) {
                    Set<Integer> kNNOfThisK = kNNOfThisPoint.get(j);
                    if(kNNOfThisK.contains(id)) {
                        double dist = Utils.computeEuclideanDist(points[i], vectors);
                        //System.out.println(dist + "," + thresholds[i][j]);
                        if(dist < (1+Parameters.eps) * thresholds[i][j]) {
                            truePositiveCount[j]++;
                        }
                    }
                }
            }
            id++;
        }
        for(int j = 0; j < truePositiveCount.length; j++) {
            truePositiveCount[j] /= (kNNs.size() * Parameters.topK[j]);
        }
        return truePositiveCount;
    }

    static double[][] getPointsInReducedSpace(int numPoints, int dim) throws IOException {

        String file = "./data/reducedVectors-siameseNet";
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        String line;
        double[][] points = new double[numPoints][dim];
        int idx = 0;
        while((line = br.readLine()) != null) {
            String[] records = line.split(" ");
            double[] values = new double[records.length];
            for(int i = 0; i < records.length; i++) {
                values[i] = Double.parseDouble(records[i]);
            }
            points[idx] = values;
            idx++;
        }
        return points;
    }

    static double[][] getPointsInOriginalSpace(int numPoints, int dim) throws IOException {

        String file = "./data/originalVectors";
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        String line;
        double[][] points = new double[numPoints][dim];
        for(int i = 0; i < Parameters.testSize; i++) {
            String[] records = br.readLine().split(" ");
            for(int j = 0; j < records.length; j++) {
                points[i][j] = Double.parseDouble(records[j]);
            }
        }
        return points;
    }

    static List<List<Set<Integer>>> getkNN(double[][] points) throws IOException {
        List<List<Set<Integer>>> kNNs = new ArrayList<>();
        int maxK = Parameters.topK[Parameters.topK.length - 1];
        double startTime = System.currentTimeMillis();
        for(int i = 0; i < Parameters.testSize; i++) {
            List<Set<Integer>> list = new ArrayList<>();
            PriorityQueue pq = new PriorityQueue(maxK, "ascending");
            for(int j = 0; j < points.length; j++) {
                double dist = Utils.computeEuclideanDist(points[i], points[j]);
                if(dist == 0) continue;
                pq.insert(dist, j);
            }
            for(int kID = Parameters.topK.length - 1; kID >= 0 ; kID--) {
                int k = Parameters.topK[kID];
                pq.reSize(k);
                Set<Integer> set = new HashSet<>(pq.serialize());
                list.add(0, set);
            }
            kNNs.add(list);
        }
        double endTime = System.currentTimeMillis();
        System.out.println("Average search time: " + (endTime - startTime) / Parameters.testSize);
        return kNNs;
    }

    static private double[][] readThreasholds() throws IOException {
        String file = "./data/kNNDist";
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        double[][] kNNDist = new double[Parameters.testSize][Parameters.topK.length];
        String line;
        int i = 0;
        while((line = br.readLine()) != null) {
            String[] record = line.split(",");
            List<Double> list = new ArrayList<>();
            for(int j = 0; j < record.length; j++) {
                kNNDist[i][j] = Double.parseDouble(record[j]);
            }
            i++;
        }
        return  kNNDist;
    }

}
