import Utils.Utils;
import mtree.examples.Data;
import mtree.examples.MTreeClass;

import java.io.*;
import java.util.*;

public class MTree {
    static String pointFile = "reducedPoints";
    public static void main(String[] args) throws IOException {
        String dir = "./data/Gaussian/";
        MTreeClass mtree = buildTree(dir);
        double[][] thresholds = readThreasholds(dir);
        List<List<Set<Integer>>> kNNs = findkNN(mtree, dir, args);
        double[] accuracy = compareResults(kNNs, thresholds, dir);
        try(FileWriter fw = new FileWriter("./data/accuracy.csv", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)) {
            out.println(String.join("-", args));
            for(double d : accuracy) {
                out.print(String.valueOf(d) + ",");
            }
            out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Arrays.toString(accuracy));
    }

    private static MTreeClass buildTree(String dir) throws IOException {
        String file = dir + pointFile;
        //file = dir + "points";
        MTreeClass mtree = new MTreeClass();
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
        }
        br.close();
        return mtree;
    }

    static private double[][] readThreasholds(String dir) throws IOException {
        int testSize = Parameters.testSize;
        String file = dir + "kNNDist";
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        double[][] kNNDist = new double[testSize][Parameters.topK.length];
        for(int i = 0; i < testSize; i++) {
            String[] record = br.readLine().split(",");
            for(int j = 0; j < Parameters.topK.length; j++) {
                kNNDist[i][j] = Double.parseDouble(record[j]);
            }
        }
        return  kNNDist;
    }

    static List<List<Set<Integer>>> findkNN(MTreeClass mtree, String dir, String[] args) throws IOException{
        String file = dir + pointFile;
        //file = dir + "points";
        List<List<Set<Integer>>> results = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        double[] checkedCount = new double[Parameters.topK.length];
        double[] checkedLeafCount = new double[Parameters.topK.length];
        for(int i = 0; i < Parameters.testSize; i++) {
            List<Set<Integer>> neighbors = new ArrayList<>();
            String[] record = br.readLine().split(" ");
            double[] values = new double[record.length];
            for(int j = 0; j < record.length; j++) {
                values[j] = Double.parseDouble(record[j]);
            }
            Data point = new Data(values, i);
            for(int kID = 0; kID < Parameters.topK.length; kID++) {
                int k = Parameters.topK[kID];
                MTreeClass.Query query = mtree.getNearestByLimit(point, k + 1);
                Set<Integer> kNeighbors = new HashSet<>();
                for(MTreeClass.ResultItem ri : query) {
                    kNeighbors.add(ri.data.getID());
                }
                checkedCount[kID] += query.getCheckedCount();
                checkedLeafCount[kID] += query.getCheckedLeafCount();
                neighbors.add(kNeighbors);
            }
            results.add(neighbors);
        }
        for(int i = 0; i < checkedCount.length; i++) {
            checkedCount[i] /= Parameters.testSize;
            checkedLeafCount[i] /= Parameters.testSize;
        }
        try(FileWriter fw = new FileWriter("./data/pruning.csv", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)) {
            out.println(String.join("-", args));
            for(double d : checkedCount) {
                out.print(String.valueOf(d) + ",");
            }
            out.print("-,");
            for(double d : checkedLeafCount) {
                out.print(String.valueOf(d) + ",");
            }
            out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    static double[] compareResults(List<List<Set<Integer>>> kNNs, double[][] thresholds, String dir) throws IOException {
        //eps=0;
        double[] truePositiveCount = new double[Parameters.topK.length];
        double eps = Parameters.eps;
        String file = dir + "points";
        BufferedReader br1 = new BufferedReader(new FileReader(new File(file)));
        for(int i = 0; i < Parameters.testSize; i++) {
            String[] record1 = br1.readLine().split(" ");
            double[] p1 = new double[record1.length];
            for(int j = 0; j < record1.length; j++) {
                p1[j] = Double.parseDouble(record1[j]);
            }
            List<Set<Integer>> kNNOfThisPoint = kNNs.get(i);
            BufferedReader br2 = new BufferedReader(new FileReader(new File(file)));
            String line;
            int id = 0;
            while((line = br2.readLine()) != null) {
                String[] record2 = line.split(" ");
                double[] p2 = new double[record2.length];
                for(int j = 0; j < record2.length; j++) {
                    p2[j] = Double.parseDouble(record2[j]);
                }
                for(int kID = 0; kID < Parameters.topK.length; kID++) {
                    Set<Integer> kNNOfThisK = kNNOfThisPoint.get(kID);
                    if(kNNOfThisK.contains(id)) {
                        double dist = Utils.computeEuclideanDist(p1, p2);
                        //System.out.println(dist + "-" + thresholds[i][kID]);
                        if(dist <= (1 + eps) * thresholds[i][kID]) {
                            truePositiveCount[kID]++;
                        }
                    }
                }
                id++;
            }
        }
        for(int j = 0; j < truePositiveCount.length; j++) {
            truePositiveCount[j] -= kNNs.size();
            truePositiveCount[j] /= (kNNs.size() * (Parameters.topK[j]));
        }
        return truePositiveCount;
    }
}