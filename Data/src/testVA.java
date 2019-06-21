import Algorithms.VAFile;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import Utils.PriorityQueue;

public class testVA {
    public static void main(String[] args) throws IOException {
        String file = "./data/reducedVectors-siameseNet-Uniform-20-0-hard-normalized";
        String delimiter = " ";
        int numPoints = 24000;
        int testSize = 10;
        int k = 2;
        double[][] points = new double[numPoints][];
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        String line;
        int id = 0;
        while((line = br.readLine()) != null) {
            String[] record = line.split(delimiter);
            double[] temp = new double[record.length];
            for(int i = 0; i < record.length; i++) {
                temp[i] = Double.parseDouble(record[i]);
            }
            points[id] = temp;
            id++;
        }
        System.gc();
        double startTime1 = System.currentTimeMillis();
        for(int i = 0; i < testSize;i++) {
            //PriorityQueue queue = findKNN(points[i], file, k);
            //System.out.println(Arrays.toString(queue.serialize().toArray()));
        }
        double endTime1 = System.currentTimeMillis();

        VAFile vaFile = new VAFile(file, 1);
        double count = 0;
        double startTime2 = System.currentTimeMillis();
        for(int i = 0; i < testSize;i++) {
            //PriorityQueue queue = vaFile.findKNN(points[i], k, file);
            count += vaFile.findKNN(points[i], k, file);
            //System.out.println(Arrays.toString(queue.serialize().toArray()));
        }
        double endTime2 = System.currentTimeMillis();
        //System.out.println((endTime1 - startTime1)/testSize);
        System.out.println((endTime2 - startTime2)/testSize);
        System.out.println(count/testSize);
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
            double dist = Utils.Utils.computeEuclideanDist(point, cand);
            queue.insert(dist, idx);
            idx++;
        }
        br.close();
        return queue;
    }
}
