import Utils.Utils;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class Pair {

    public static void main(String[] args) throws IOException, InterruptedException {
        //double trainRatio = Double.parseDouble(args[0]);
        double trainRatio = 1.0;
        String dir = "./data/Gaussian/";
        Pair pair = new Pair();
        pair.sample(dir, trainRatio);
        pair.generate(dir, trainRatio);
    }

    public void generate(String dir, double ratio) throws IOException, InterruptedException {
        double[] weights = readPointWeights(dir);
        int[] clusterInfo = readClusterInfo(dir);
        //System.out.println(Arrays.toString(weights));
        int numThreads = Parameters.numThreads;
        PairThread[] PT = new PairThread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            PT[i] = new PairThread(i, dir, ratio, weights, clusterInfo);
            PT[i].start();
        }
        for(int i = 0; i < numThreads; i++) {
            PT[i].join();
        }

        System.out.println("threads completed");
        //combine training files
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(new File(dir + "training")));
        String line;
        File file;

        for(int i = 0; i < numThreads; i++) {
            file = new File(dir + "thread-" +String.valueOf(i) + "-training");
            BufferedReader trainReader = new BufferedReader(new FileReader(file));
            if(i != 0) trainReader.readLine();
            while((line = trainReader.readLine()) != null) {
                trainWriter.write(line + "\n");
            }
            trainReader.close();
            file.delete();
            System.out.println("combined file " + String.valueOf(i));
        }
        trainWriter.flush(); trainWriter.close();
    }

    private void sample(String dir, double sampleRate) throws IOException {
        int max = (int)(Parameters.numPoints / Parameters.numThreads * sampleRate );
        int clusterSize = Parameters.numPoints / Parameters.numClusters;
        Random random = new Random();
        int lastClusterID = -1;
        int count = 0;
        int id = 0;
        for(int i = 0; i < Parameters.numThreads; i++) {
            String inFile = dir + "thread-" + String.valueOf(i);
            String outFile = inFile + "-sample";
            BufferedReader reader = new BufferedReader(new FileReader(new File(inFile)));
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFile)));
            while(true) {
                String line = reader.readLine();
                if(line == null)
                    break;
                if(id++ / clusterSize != lastClusterID) {
                    lastClusterID = id / clusterSize;
                    count = 0;
                }
                if(count++ < clusterSize * sampleRate) {
                    writer.write(line + "," + String.valueOf(id / clusterSize) + "\n");
                }
            }
            writer.flush();
            writer.close();
            reader.close();
        }
    }

    private double[] readPointWeights(String dir) throws IOException{
        double[] weights = new double[Parameters.numPoints];
//        BufferedReader reader = new BufferedReader(new FileReader(new File(dir + "weights")));
//
//        for(int i = 0; i < Parameters.numPoints; i++) {
//            double w = Double.parseDouble(reader.readLine());
//            weights[i] = w;
//        }
        return weights;
    }
    private int[] readClusterInfo(String dir) throws IOException {
        int[] clusterInfo = new int[Parameters.numPoints];
        BufferedReader reader = new BufferedReader(new FileReader(new File(dir + "clusterInfo")));
        for(int i = 0; i < Parameters.numPoints; i++) {
            clusterInfo[i] = Integer.parseInt(reader.readLine());
        }
        reader.close();
        return clusterInfo;
    }
}
class PairThread extends Thread {

    private int threadID;
    private String dir;
    private double ratio;
    private double[] weights;
    private int[] clusterInfo;

    public PairThread(int threadID, String dir, double ratio, double[] weights, int[] clusterInfo) {
        this.threadID = threadID;
        this.dir = dir;
        this.ratio = ratio;
        this.weights = weights;
        this.clusterInfo = clusterInfo;
    }

    public void run() {
        try {
            execute();
            //root.print();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    private void execute() throws IOException{
        int numPointsEachCluster = Parameters.numPoints / Parameters.numClusters;
        double crossClusterSampleRate = 1.0 / (Parameters.numClusters - 1);
        Random random = new Random();
        BufferedReader p1Reader, p2Reader, thresholdReader;
        String filePrefix = dir + "thread-" + String.valueOf(threadID);
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePrefix + "-training")));
        //format: P1, P2, distance, kNN distance of P1
        writer.write("P1,P2,distance,cutoff,thisCluster,otherCluster\n");
        String[] lines = new String[2];
        double[][] points = new double[2][];
        int marker = 0;

        int fileSize = Utils.getNumLines(filePrefix);
        double negativeRatio = 200 * 1.0 * Parameters.topK[Parameters.topK.length - 1] / fileSize / Parameters.numThreads;
        p1Reader = new BufferedReader(new FileReader(new File(filePrefix + "-sample")));
        thresholdReader = new BufferedReader(new FileReader(new File(filePrefix + "-threshold")));
        String threshold;
        while((lines[0] = p1Reader.readLine()) != null) {
            int thisID = fileSize * threadID + marker;
            marker++;
            //if(marker > 1000) break;
            if(marker % 100== 0) {
                System.out.println(threadID + "-" + String.valueOf(Math.round((double)marker / fileSize * 100)) + "% training samples generated...");
            }
            //if(random.nextDouble() > ratio) continue;
            String[] temp1 = lines[0].split(",");
            String pos1 = temp1[0];
            String thisClusterID = temp1[1];
            points[0] = Utils.getValuesFromLine(pos1, " ");
            double thres = 0;
            int otherID = 0;
            for(int j = 0; j < Parameters.numThreads; j++) {
                p2Reader = new BufferedReader(new FileReader(new File(dir + "thread-" + String.valueOf(j)  + "-sample")));
                while((lines[1] = p2Reader.readLine()) != null) {
                    String[] temp2 = lines[1].split(",");
                    String pos2 = temp2[0];
                    String otherClusterID = temp2[1];
                    points[1] = Utils.getValuesFromLine(pos2, " ");
                    double dist = Utils.computeEuclideanDist(points[0], points[1]);
                    if(dist==0) continue;
                    //if(random.nextDouble() < negativeRatio || dist <= (1+Parameters.eps)*thres ){
                    //if(otherID % numPointsEachCluster == 0 || (clusterInfo[thisID] == clusterInfo[otherID] && random.nextDouble() < 0.5) ){
//                        if(dist <= (1+Parameters.eps)*thres && clusterInfo[thisID] != clusterInfo[otherID])
//                            System.out.println("&*&*&*^*&^*&^*&%^*%$^&&*&*&*^*&^*&^*&%^*%$^&&*&*&*^*&^*&^*&%^*%$^&");
                    //if(random.nextDouble() < negativeRatio || clusterInfo[thisID] == clusterInfo[otherID] ){
                        //double weight = selectWeight(weights[thisID], weights[otherID]);
                    if(thisClusterID.equals(otherClusterID) || random.nextDouble() < crossClusterSampleRate) {
                        writer.write(pos1 + ","
                                + pos2 + ","
                                + String.valueOf(dist) + ","
                                + String.valueOf((1+Parameters.eps)*thres) + ","
                                + thisClusterID + ","
                                + otherClusterID + "\n");
                    }
                    //}
                    otherID++;
                }
                p2Reader.close();
            }
        }
        p1Reader.close();
        writer.flush(); writer.close();
        //testWriter.flush(); testWriter.close();
    }

    private double selectWeight(double w1, double w2) {
        return Math.min(w1, w2);
        //return Math.max(w1, w2);
        //return (w1 + w2) / 2;
    }
}
