import java.io.*;

import Utils.Utils;

import java.util.*;

import Utils.*;
import mtree.examples.Data;
import mtree.examples.MTreeClass;

public class temp {
//    public static void main(String[] args) throws IOException, InterruptedException {
//        DataSet ds = new DataSet(1000,50,20,1);
//        ds.generateGaussian();
//    }
    public static void main(String[] args) throws IOException, InterruptedException {
//        String dir = "./data/Gaussian/";
//        double[][] thresholds = readThreasholds(dir);
//        compareResults(thresholds, dir);
        //Utils.splitFile("./data/SIFT/points", "./data/SIFT", 8);

//        int clusterSize = Parameters.numPoints / Parameters.numClusters;
//        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./data/Gaussian/clusterInfo")));
//        for(int i = 0; i < Parameters.numPoints; i++) {
//            writer.write(String.valueOf(i / clusterSize) + "\n");
//        }
//        writer.flush();
//        writer.close();

        BufferedReader reader = new BufferedReader(new FileReader(new File("./data/Gaussian/clusterInfo")));
        int clusterSize = Parameters.numPoints / Parameters.numClusters;
        int lastValue = -1;
        int lastPosition = -1;
        int errorCount = 0;
        for(int i = 0; i < Parameters.numPoints; i++) {
            int v = Integer.parseInt(reader.readLine());
            if(lastPosition != -1) {
                if(lastPosition / clusterSize == i / clusterSize && lastValue != v) {
                    errorCount++;
                    System.out.println("error in " + String.valueOf(lastPosition));
                }
            }
            lastValue = v;
            lastPosition = i;
        }
        System.out.println(errorCount);
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

    static void compareResults(double[][] thresholds, String dir) throws IOException {
        //eps=0;
        double[] counts = new double[Parameters.topK.length];
        String file = dir + "points";
        BufferedReader br1 = new BufferedReader(new FileReader(new File(file)));
        for(int i = 0; i < Parameters.testSize; i++) {
            String[] record1 = br1.readLine().split(" ");
            double[] p1 = new double[record1.length];
            for(int j = 0; j < record1.length; j++) {
                p1[j] = Double.parseDouble(record1[j]);
            }
            BufferedReader br2 = new BufferedReader(new FileReader(new File(file)));
            String line;
            while((line = br2.readLine()) != null) {
                String[] record2 = line.split(" ");
                double[] p2 = new double[record2.length];
                for(int j = 0; j < record2.length; j++) {
                    p2[j] = Double.parseDouble(record2[j]);
                }
                double dist = Utils.computeEuclideanDist(p1, p2);
                for(int k = 0; k < counts.length; k++) {
                    if(dist <= thresholds[i][k]) {
                        counts[k]++;
                    }
                }
            }
        }
        System.out.println(Arrays.toString(counts));
    }
}
