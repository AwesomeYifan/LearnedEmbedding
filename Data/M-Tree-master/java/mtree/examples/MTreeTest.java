package mtree.examples;

import Utils.Utils;

import java.io.*;
import java.util.*;

public class MTreeTest {
    static int[] topK = {1,10,50};
    static int testSize = 1000;

    public static void main(String[] args) throws IOException {

        MTreeClass mtree = new MTreeClass();
        List<Data> allData = new ArrayList<Data>();
        String pathToRecucedVecs = "./data/reducedVectors-PCA-Uniform";
        String pathToOriVecs = "./data/originalVectors-Uniform";
        String pathToThres = "./data/kNNDist-Uniform";
        double eps = 0.1;
        //buildOption = "tripletNet";
        //buildOption = ""; //build with original vectors

        double buildStartTime = System.currentTimeMillis();
        buildTree(pathToRecucedVecs, mtree, allData, testSize);
        double buildEndTime = System.currentTimeMillis();
        System.out.println("Build time: " + (buildEndTime - buildStartTime));
        List<List<Set<Integer>>> kNNs = new ArrayList<>();//point-k-neighbors
        for(int i = 0; i < allData.size(); i++) {
            kNNs.add(new ArrayList<>());
        }
        double[] searchTime = new double[topK.length];
        double[] checkedCount = new double[topK.length];
        for(int kID = 0; kID < topK.length; kID++) {
            int resultSize = topK[kID];
            System.gc();
            double startTime = System.currentTimeMillis();
            for(int pID = 0; pID < allData.size(); pID++) {
                Data data = allData.get(pID);
                MTreeClass.Query query = mtree.getNearestByLimit(data, resultSize + 1);
                Set<Integer> thisKNNs = new HashSet<>();
                for(MTreeClass.ResultItem ri : query) {
                    if(ri.data.getID() == data.getID()) continue;
                    thisKNNs.add(ri.data.getID());
                }
                checkedCount[kID] += query.getCheckedCount();
                kNNs.get(pID).add(thisKNNs);
            }
            double endTime = System.currentTimeMillis();
            double totalTime = endTime - startTime;
            searchTime[kID] += totalTime;
        }
        double[][] queryVectors = getPointsInOriginalSpace(testSize, pathToOriVecs);
        double[][] thresholds = readThreasholds(pathToThres, testSize);
        double[] accuracy = compareResults(queryVectors, kNNs, thresholds, pathToOriVecs, eps, topK);
        System.out.println(Arrays.toString(searchTime));
        System.out.println(Arrays.toString(checkedCount));
        System.out.println(Arrays.toString(accuracy));
    }

    private static void buildTree(String dataset, MTreeClass mtree, List<Data> allData, int numTestSamples) throws IOException {
        String file;
        file = dataset;
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int ID = 0;
        while((line = br.readLine()) != null) {
            String[] records = line.split(" ");
            double[] values = new double[records.length];
            for(int i = 0; i < records.length; i++) {
                values[i] = Double.parseDouble(records[i]);
            }
            Data data = new Data(values, ID++);
            mtree.add(data);
            if(ID <= numTestSamples)
                allData.add(data);
        }
        br.close();
    }

    static double[][] getPointsInOriginalSpace(int testSize, String dataset) throws IOException {

        String file = dataset;
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        double[][] points = new double[testSize][];
        for(int i = 0; i < testSize; i++) {
            String[] records = br.readLine().split(" ");
            points[i] = new double[records.length];
            for(int j = 0; j < records.length; j++) {
                points[i][j] = Double.parseDouble(records[j]);
            }
        }
        return points;
    }

    static private double[][] readThreasholds(String dataset, int testSize) throws IOException {
        String file = dataset;
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        double[][] kNNDist = new double[testSize][];
        String line;
        int i = 0;
        while((line = br.readLine()) != null) {
            String[] record = line.split(",");
            double[] temp = new double[record.length];
            for(int j = 0; j < record.length; j++) {
                temp[j] = Double.parseDouble(record[j]);
            }
            kNNDist[i] = temp;
            i++;
        }
        return  kNNDist;
    }

    static double[] compareResults(double[][] points, List<List<Set<Integer>>> kNNs, double[][] thresholds, String dataset, double eps, int[] topK) throws IOException {
        String file = dataset;
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        String line;
        Integer id = 0;
        double[] truePositiveCount = new double[topK.length];
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
                        if(dist < (1 + eps) * thresholds[i][j]) {
                            truePositiveCount[j]++;
                        }
                    }
                }
            }
            id++;
        }
        for(int j = 0; j < truePositiveCount.length; j++) {
            truePositiveCount[j] /= (kNNs.size() * topK[j]);
        }
        return truePositiveCount;
    }
    private static List<Integer> toIntList(String[] input, int limit) {
        if(limit == -1) limit = input.length;
        if(limit>input.length) limit = input.length;
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < limit; i++) {
            list.add(Integer.parseInt(input[i]));
        }
        return list;
    }
}
