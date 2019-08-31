package Algorithms;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import Utils.Utils;
import Utils.PriorityQueue;
//assume vectors are normalized into range [0,1]
public class VAFile {
    long[] offsets;
    int[][] VA; // the vector approximation of each point
    int numBytes; //number of bits per axis, <=4
    int numPoints;
    int numDims;
    double binWidth;
    double scale;

    public VAFile(String pathToFile, int numBytes) throws IOException {
        this.numBytes = numBytes;
        int[] metaInfo = getMetaInfo(pathToFile);
        this.numPoints = metaInfo[0];
        this.numDims = metaInfo[1];
        this.binWidth = 1.0 / Math.pow(2, 8*numBytes) + 1e-9;
        this.VA = new int[numPoints][numDims];
        this.offsets = new long[numPoints];

        this.scale = Math.pow(Math.pow(2, 8 * numBytes), 2);
        build(pathToFile);
        //System.out.println("***" + Arrays.toString(offsets));
//        for(int[] testVA : VA) {
//            System.out.println(Arrays.toString(testVA));
//        }
    }

    public double findKNN(double[] query, int k, String pathToFile) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(pathToFile, "r");
        PriorityQueue candidates = findCandidates(query, k);
        int candSize = candidates.getSize();
        //System.out.println(Arrays.toString(candidates.serialize().toArray()));
        //System.out.println(candidates.getSize());
        PriorityQueue results = new PriorityQueue(k,"ascending");
        for(int i = 0; i < candidates.getNumEntries(); i++) {
            if(results.getSize() >= k && results.getBottomKey().compareTo(candidates.getKey(i)) < 0) {
                //System.out.println(results.getBottomKey() + "-" +candidates.getKey(i));
                //System.out.println(i);
                //return results;
                return i;
            }
            Set<Object> set = candidates.getValues(i);
            //System.out.println(Arrays.toString(set.toArray()));
            for(Object obj : set) {
                int idx = (int)obj;
                raf.seek(offsets[idx]);
                String[] record = raf.readLine().split(" ");
                //System.out.println(String.valueOf(idx) + "-" + Arrays.toString(record));
                //System.in.read();
                double[] cand = new double[record.length];
                for(int j = 0; j < cand.length; j++) {
                    cand[j] = Double.parseDouble(record[j]);
                }
                double dist = Utils.computeEuclideanDist(query, cand);
                results.insert(dist, idx);
            }
        }
        //return results;
        System.out.println(candSize);
        return candSize;
    }
    private double computeEuclideanDist(int[] vec1, int[] vec2) {
        //System.out.println(Arrays.toString(vec1));
        //int[] diffs = new int[vec1.length];
        double sumDist = 0;
        for(int i = 0; i < vec1.length; i++) {
            //diffs[i] = vec1[i] - vec2[i];
            sumDist += (vec1[i] - vec2[i]) * (vec1[i] - vec2[i]);
        }
//        for(int i = 0; i < vec1.length; i++) {
//            sumDist += diffs[i] * diffs[i];
//        }
        return Math.sqrt(sumDist);    }



    public PriorityQueue tempFindCandidates(int idx, int k) {

        int[] queryVA = VA[idx];
        PriorityQueue queue = new PriorityQueue(k,"ascending");
        double sum = 0;
        for(int i = 0; i < VA.length; i++) {
            double dist = Utils.computeEuclideanDist(queryVA, VA[i]);

//            for(int i = 0; i < query.length; i++) {
//                diff += queryVA[i] - point[i];
//            }
            //queue.insert(dist, i);
            sum += dist;
        }
        //System.out.println(sum);
        //System.out.println(dist);
        return queue;
    }

    public PriorityQueue findCandidates(double[] query, int k) {
        PriorityQueue CandQueue = new PriorityQueue("ascending");
        PriorityQueue UBQueue = new PriorityQueue(k, "ascending");
        int[] queryVA = getVA(query);
        for(int i = 0; i < VA.length; i++) {
            double[] dist = computeDist(queryVA, VA[i]);
            //System.out.println(Arrays.toString(dist));
            if(CandQueue.getSize() < k) {
                CandQueue.insert(dist[0], i);
                UBQueue.insert(dist[1], "x");
            }
            else if(dist[0] <= (double)UBQueue.getBottomKey()) {
                //System.out.println("yes");
                CandQueue.insert(dist[0], i);
                if(dist[1] < (double)UBQueue.getBottomKey()) {
                    UBQueue.insert(dist[1], "x");
                }
            }
        }
        //System.out.println(CandQueue.getSize());
        return CandQueue;
    }

    //compute the distance between two tiles: LB and UB
    private double[] computeDist(int[] query, int[] cand) {
        double dist = 0;
        double diff = 0;
        for(int i = 0; i < query.length; i++) {
            double diffTemp = Math.abs(query[i] - cand[i]);
            diff += diffTemp;
            dist += Math.pow(diffTemp,2);
        }
        //System.out.println(dist + "-" + scale);
        return new double[]{Math.sqrt((dist - 2 * diff + query.length) / scale), Math.sqrt((dist + 2 * diff + query.length) / scale)};
    }

    private void build(String pathToFile) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(pathToFile, "r");
        for(int id = 0; id < numPoints; id++) {
            this.offsets[id] = raf.getFilePointer();
            String line = raf.readLine();
            String[] record = line.split(" ");
            double[] vector = new double[record.length];
            for(int i = 0; i < record.length; i++) {
                vector[i] = Double.parseDouble(record[i]);
            }
            VA[id] = getVA(vector);
        }
        raf.close();
    }
    private int[] getVA(double[] vectors) {
        int[] result = new int[numDims];
        for(int i = 0; i < numDims; i++) {
//            System.out.println(axisRanges[i][1]);
//            System.out.println(widths[i]);
//            System.out.println(vectors[i]);
            result[i] = (int)Math.floor(vectors[i] / binWidth);
            //System.out.println(result[i]);
        }
        return result;
    }

    private int[] getMetaInfo(String pathToFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(pathToFile)));
        int numDims = br.readLine().split(" ").length;

        int numPoints = 0;
        br = new BufferedReader(new FileReader(new File(pathToFile)));
        while(br.readLine() != null) {
            numPoints++;
        }
        //System.out.println("number of points: " + String.valueOf(numPoints));
        //System.out.println("dimensionality: " + String.valueOf(numDims));
        return new int[] {numPoints, numDims};
    }
}
