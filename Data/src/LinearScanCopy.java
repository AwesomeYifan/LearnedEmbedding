import Utils.PriorityQueue;
import Utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class LinearScanCopy {

    //args: dataset, d', epsilon, method
    private static int testSize;
    public static void main(String[] args) throws IOException {
        testSize = Parameters.testSize;
//        String dir = args[0];
//        int reducedDim = Integer.parseInt(args[1]);
        //String flag = args[0];

        String dir = "./data/Gaussian/";
        int reducedDim = 20;

        //String suffix = "-" + dataset + "-" + String.valueOf(reducedDim) + "-" + String.valueOf(traineps);
        String file = dir + "reducedPoints";
        //String file = "./data/reducedVectors-PCA-Cluster";
        int numPoints = Parameters.numPoints;

        double[][] thresholds = readThreasholds(dir);
        double[][] points = getPointsInReducedSpace(numPoints, reducedDim, file);
        List<List<Set<Integer>>> kNNs = getkNNFromVectors(points);
//        if(traineps == 0.1) {
//            String newFile = "./data/reducedVectors-siameseNet-" + dataset + "-" + originalSampleSize + "-" + originalSampleRatio + "-0.0";
//            double[][] newPoints = getPointsInReducedSpace(numPoints, reducedDim, newFile);
//            List<List<Set<Integer>>> newKNNs = getkNNFromVectors(newPoints);
//            double[] rankByDiffFunctions = compareResultsByFunction(kNNs, newKNNs);
//            try(FileWriter fw = new FileWriter("./data/rankingResults.csv", true);
//                BufferedWriter bw = new BufferedWriter(fw);
//                PrintWriter out = new PrintWriter(bw))
//            {
//
//                out.println(String.join("-", args));
//
//                for(double d : rankByDiffFunctions) {
//                    out.print(String.valueOf(d) + ",");
//                }
//                out.println();
//            } catch (IOException e) {
//            }
//
//        }


        //List<List<Set<Integer>>> kNNs = getkNNFromFile("./data/results-hnsw");
        double[][] testPoints = getPointsInOriginalSpace(numPoints, dir);
        double[] acc = compareResults(testPoints, kNNs, thresholds, dir);
        System.out.println(Arrays.toString(acc));
//        try(FileWriter fw = new FileWriter("./data/accuracy.csv", true);
//            BufferedWriter bw = new BufferedWriter(fw);
//            PrintWriter out = new PrintWriter(bw))
//        {
//            out.println(String.join("-", args));
//            for(double d : acc) {
//                out.print(String.valueOf(d) + ",");
//            }
//            out.println();
//        } catch (IOException e) {
//        }
        //testVA.testva(args, file);
    }

    static double[] compareResults(double[][] points, List<List<Set<Integer>>> kNNs, double[][] thresholds, String dir) throws IOException {
        //eps=0;
        double eps = Parameters.eps;
        String file = dir + "points";
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
//                        if(j == 0) {
//                            System.out.println(dist + "-" + (1 + eps) * thresholds[i][j]);
//                        }
                        if(dist <= (1 + eps) * thresholds[i][j]) {
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

    static double[][] getPointsInReducedSpace(int numPoints, int dim, String file) throws IOException {

        //String file = "./data/reducedVectors-PCA" + "-Uniform";
        //String file = "./data/originalVectors";
        //System.out.println("*********************" + testSize);
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

    static double[][] getPointsInOriginalSpace(int numPoints, String dir) throws IOException {

        String file = dir + "points";
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        double[][] points = new double[numPoints][];
        for(int i = 0; i < testSize; i++) {
            String[] records = br.readLine().split(" ");
            points[i] = new double[records.length];
            for(int j = 0; j < records.length; j++) {
                points[i][j] = Double.parseDouble(records[j]);
            }
        }
        return points;
    }

    static double[] compareResultsByFunction(List<List<Set<Integer>>> R1, List<List<Set<Integer>>> R2) {
        double[] result = new double[R1.get(0).size()];
        for(int i = 0; i < R1.size(); i++) {
            List<Set<Integer>> list1 = R1.get(i);
            List<Set<Integer>> list2 = R2.get(i);
            for(int j = 0; j < list1.size(); j++) {
                Set<Integer> set1 = new HashSet<>(list1.get(j));
                set1.retainAll(list2.get(j));
                result[j] += set1.size();
            }
        }
        for(int j = 0; j < result.length; j++) {
            result[j] /= (R1.size() * Parameters.topK[j]);
        }
        return result;
    }

    static List<List<Set<Integer>>> getkNNFromFile(String path) throws IOException {
        List<List<Set<Integer>>> kNNs = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(new File(path)));
        for(int id = 0; id < testSize; id++) {
            String[] record = br.readLine().split(" ");
            List<Set<Integer>> list = new ArrayList<>();
            for(int k : Parameters.topK) {
                Set<Integer> set = new HashSet<>();
                for(int i = 0; i < k; i++) {
                    set.add(Integer.parseInt(record[i]));
                }
                list.add(set);
            }
            kNNs.add(list);
        }
        return kNNs;
    }

    static List<List<Set<Integer>>> getkNNFromVectors(double[][] points) throws IOException {
        List<List<Set<Integer>>> kNNs = new ArrayList<>();
        int maxK = Parameters.topK[Parameters.topK.length - 1];
        double startTime = System.currentTimeMillis();
        for(int i = 0; i < testSize; i++) {
            List<Set<Integer>> list = new ArrayList<>();
            PriorityQueue pq = new PriorityQueue(maxK, "ascending");
            for(int j = 0; j < points.length; j++) {
                double dist = Utils.computeEuclideanDist(points[i], points[j]);
                //if(dist == 0) continue;
                pq.insert(dist, j);
            }
            for(int kID = Parameters.topK.length - 1; kID >= 0 ; kID--) {
                int k = Parameters.topK[kID];
                pq.reSize(k);
                //System.out.println(k + ": " + Arrays.toString(pq.serializeKeys().toArray()));
                //System.in.read();
                Set<Integer> set = new HashSet<>(pq.serialize());
                list.add(0, set);
            }
            kNNs.add(list);
        }
        double endTime = System.currentTimeMillis();
        System.out.println("Average search time: " + (endTime - startTime) / testSize);
        return kNNs;
    }

    static private double[][] readThreasholds(String dir) throws IOException {
        String file = dir + "kNNDist";
        //file = "./data/siftsmall-1-10-50/kNNDist";
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        double[][] kNNDist = new double[testSize][Parameters.topK.length];
        String line;
        for(int i = 0; i < testSize; i++) {
            String[] record = br.readLine().split(",");
            for(int j = 0; j < Parameters.topK.length; j++) {
                kNNDist[i][j] = Double.parseDouble(record[j]);
            }
        }
        return  kNNDist;
    }
}
