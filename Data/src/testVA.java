import Algorithms.VAFile;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import Utils.PriorityQueue;
import Utils.Utils;

public class testVA {
    public static void testva(String[] args, String file) throws IOException {
        //public static void main(String[] args) throws IOException {

//        String file = "./data/originalVectors-Uniform";
//        Utils.normalizeData(file);
//        file = file + "-normalized";
//        String delimiter = " ";
//        int numPoints = 24000;
//        int testSize = 10;
//        int k = 2;
//        double[][] points = new double[numPoints][];
//        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
//        String line;
//        int id = 0;
//        while((line = br.readLine()) != null) {
//            String[] record = line.split(delimiter);
//            double[] Cluster = new double[record.length];
//            for(int i = 0; i < record.length; i++) {
//                Cluster[i] = Double.parseDouble(record[i]);
//            }
//            points[id] = Cluster;
//            id++;
//        }
//        System.gc();
//        double startTime1 = System.currentTimeMillis();
//        for(int i = 0; i < testSize;i++) {
//            //PriorityQueue queue = findKNN(points[i], file, k);
//            //System.out.println(Arrays.toString(queue.serialize().toArray()));
//        }
//        double endTime1 = System.currentTimeMillis();



        Utils.normalizeData(file);
        file = file + "-normalized";
        int testSize = 100;
        VAFile vaFile = new VAFile(file, 1);
        double[][] points = new double[testSize][];

        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        for(int id = 0; id < testSize; id++) {
            String[] record = br.readLine().split(" ");
            double[] temp = new double[record.length];
            for(int i = 0; i < record.length; i++) {
                temp[i] = Double.parseDouble(record[i]);
            }
            points[id] = temp;
        }
        br.close();
            try(FileWriter fw = new FileWriter("./data/efficiency.csv", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(String.join("-", args));

                for(int k : Parameters.topK) {
                    k = k + 1;
                    double count = 0;
                    double startTime2 = System.currentTimeMillis();
                    for (int i = 0; i < testSize; i++) {
                        //PriorityQueue queue = vaFile.findKNN(points[i], k, file);
                        count += vaFile.findKNN(points[i], k, file);
                        //System.out.println(Arrays.toString(queue.serialize().toArray()));
                    }
                    count -= testSize;
                    double endTime2 = System.currentTimeMillis();
                    out.println("k=" + String.valueOf(k) + "," + String.valueOf((endTime2 - startTime2)/testSize) + "," + String.valueOf(count/testSize));
                    System.out.println(count);
                }
            } catch (IOException e) {
            }

    }
    public static PriorityQueue findKNN(double[] point, String file, int k) throws IOException {
        PriorityQueue queue = new PriorityQueue(k, "ascending");
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        String line;
        int idx = 0;
        while((line = br.readLine()) != null) {
            String[] record = line.split(" ");
            double[] cand = new double[record.length];
            for(int i = 0; i < cand.length; i++) {
                cand[i] = Double.parseDouble(record[i]);
            }
            double dist = Utils.computeEuclideanDist(point, cand);
            queue.insert(dist, idx);
            idx++;
        }
        br.close();
        return queue;
    }
}
