import Utils.Utils;
import Utils.PriorityQueue;

import java.io.*;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.List;

public class Distribution {
    public static void main(String[] args) throws IOException {

        String file = "./data/originalVectors-Cluster";
        file = "./data/originalVectors-Uniform";
        double[] distribution = getDistances(file);
//        double[] epsValues = {0.05,0.1};
//        int[] kValues = {1,5,10,20,30,40,50};
//        int testSize = 100;
//        double[][] thresholds = getThresholds(file, testSize, kValues);
//        System.out.println("got thresholds");
//        double[][] numPoints = getDistribution(file, testSize, thresholds, epsValues);
//        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./data/distribution.csv")));
//        for(int k : kValues) {
//            bw.write("," + String.valueOf(k));
//        }
//        bw.write("\n");
//        for(int i = 0; i < numPoints.length; i++) {
//            double[] d = numPoints[i];
//            bw.write(String.valueOf(epsValues[i]) + ",");
//            for(double v : d) {
//                bw.write(String.valueOf(v) + ",");
//            }
//            bw.write("\n");
//        }
//        bw.flush(); bw.close();

    }
    static private double[][] getThresholds(String file, int testSize, int[] kValues) throws IOException {
        double[][] thresholds = new double[testSize][kValues.length];
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        for(int i = 0; i < testSize; i++) {
            String line1 = br.readLine();
            double[] vec1 = Utils.getValuesFromLine(line1, " ");
            PriorityQueue rankQueue = new PriorityQueue(kValues[kValues.length - 1],"ascending");
            BufferedReader br2 = new BufferedReader(new FileReader(new File(file)));
            String line2;
            int idx = 0;
            while((line2 = br2.readLine()) != null) {
                double[] vec2 = Utils.getValuesFromLine(line2, " ");
                double dist = Utils.computeEuclideanDist(vec1, vec2);
                if (dist==0) continue;
                rankQueue.insert(dist, idx);
                idx++;
            }
            for(int j = kValues.length - 1; j >= 0; j--) {
                int newSize = kValues[j];
                rankQueue.reSize(newSize);
                thresholds[i][j] = (double)rankQueue.getBottomKey();
            }
        }
        return thresholds;
    }
    static private double[][] getDistribution(String file, int testSize, double[][] thresholds, double[] epsValues) throws IOException {
        double[][] numPoints = new double[epsValues.length][thresholds[0].length];
        BufferedReader br1 = new BufferedReader(new FileReader(new File(file)));
        for(int i = 0; i < testSize; i++) {
            System.out.println(i);
            String line1 = br1.readLine();
            double[] vec1 = Utils.getValuesFromLine(line1, " ");
            BufferedReader br2 = new BufferedReader(new FileReader(new File(file)));
            String line2;
            while((line2 = br2.readLine()) != null) {
                double[] vec2 = Utils.getValuesFromLine(line2, " ");
                double dist = Utils.computeEuclideanDist(vec1, vec2);
                if (dist==0) continue;
                for(int j = 0; j < epsValues.length; j++) {
                    for(int k = 0; k < thresholds[i].length; k++) {
                        if(dist <= thresholds[i][k] * (1 + epsValues[j])) {
                            numPoints[j][k] += 1;
                        }
                    }
                }
            }
        }
        for(int i = 0; i < numPoints.length; i++) {
            for(int j = 0; j < numPoints[i].length; j++) {
                numPoints[i][j] /= testSize;
            }
        }
        return numPoints;
    }
//    static private double[] getDistances(String file) throws IOException{
//        PriorityQueue queue = new PriorityQueue("ascending");
//        BufferedReader br1 = new BufferedReader(new FileReader(new File(file)));
//        int testSize = 200;
//        for(int i = 0; i < testSize; i++) {
//            String line1 = br1.readLine();
//            double[] vec1 = Utils.getValuesFromLine(line1, " ");
//            BufferedReader br2 = new BufferedReader(new FileReader(new File(file)));
//            String line2;
//            int id = 0;
//            while((line2 = br2.readLine()) != null) {
//                double[] vec2 = Utils.getValuesFromLine(line2, " ");
//                double dist = Utils.computeEuclideanDist(vec1, vec2);
//                if (dist==0) continue;
//                queue.insert(dist, id++);
//            }
//        }
//        List<Double> distances = queue.serializeKeys();
//        double maxDist = (double)queue.getBottomKey();
//        double minDist = (double)queue.getTopKey();
//        System.out.println(maxDist);
//        System.out.println(minDist);
//        int numBins = 50;
//        double[] distribution = new double[numBins];
//        double width = (maxDist - minDist) / (numBins -1);
//        for(Double dist : distances) {
//            int id = (int) Math.floor((dist - minDist) / width);
//            distribution[id]++;
//        }
//        System.out.println(Arrays.toString(distribution));
//        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./data/distances.csv")));
//        for(int i = 0; i < numBins; i++) {
//            bw.write(String.valueOf(minDist + i * width) + ",");
//        }
//        bw.write("\n");
//        for(double dist : distribution) {
//            bw.write(String.valueOf(dist/ testSize) + ",");
//        }
//        bw.flush(); bw.close();
//        return distribution;
//    }
    static private double[] getDistances(String file) throws IOException{
        int numNeighbors = 1000;

        double[] overallDist = new double[numNeighbors];
        BufferedReader br1 = new BufferedReader(new FileReader(new File(file)));
        int testSize = 200;
        for(int i = 0; i < testSize; i++) {
            PriorityQueue queue = new PriorityQueue(numNeighbors,"ascending");
            String line1 = br1.readLine();
            double[] vec1 = Utils.getValuesFromLine(line1, " ");
            BufferedReader br2 = new BufferedReader(new FileReader(new File(file)));
            String line2;
            int id = 0;
            while((line2 = br2.readLine()) != null) {
                double[] vec2 = Utils.getValuesFromLine(line2, " ");
                double dist = Utils.computeEuclideanDist(vec1, vec2);
                if (dist==0) continue;
                queue.insert(dist, id++);
            }
            List<Double> distances = queue.serializeKeys();
            for(int j = 0; j < distances.size(); j++) {
                overallDist[j] += distances.get(j);
            }
        }
        for(int i = 0; i < overallDist.length; i++) {
            overallDist[i] /= testSize;
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./data/distances.csv")));
        for(double dist : overallDist) {
            bw.write(String.valueOf(dist) + ",");
        }
        bw.write("\n");
        bw.flush(); bw.close();
        return overallDist;
    }
}