import Utils.*;
import Utils.PriorityQueue;

import java.io.*;
import java.util.*;

public class Point {

    public static void main(String[] args) throws InterruptedException, IOException {
        //double thres = Double.parseDouble(args[0]);
        double thres = 0.1;
        String dirOfDataset = "./data/Gaussian/";
        Point point = new Point();
        double od = point.findKNN(dirOfDataset);
        point.buildCluster(dirOfDataset);
        System.out.println(od);
        //System.out.println("kNN found");
        //long[] offset = point.computeOverlap(dirOfDataset);
        //System.out.println("overlaps computed");
        //point.getOriBenefitQueue(dirOfDataset, offset);
        //System.out.println("original queue obtained");
        //point.processWeights(dirOfDataset, thres);

        try(FileWriter fw = new FileWriter("./data/overlapdegree.csv", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(String.join("-", args));
            out.println(String.valueOf(od));
        } catch (IOException e) {
        }
    }

    private void buildCluster(String dir) throws IOException{
        int clusterSize = Parameters.numPoints / Parameters.numClusters;
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir + "clusterInfo")));
        for(int i = 0; i < Parameters.numPoints; i++) {
            writer.write(String.valueOf(i / clusterSize) + "\n");
        }
        writer.flush();
        writer.close();
    }

    //only need dir
    private double findKNN(String dir) throws InterruptedException, IOException {
        //find and save the kNN of all points
        //find and save the kNN distance for all tested k/all points
        //find and save the k-th NN distance (threshold) of all points
        int numInvaders = 0;
        int numThreads = Parameters.numThreads;
        KNNThread[] KT = new KNNThread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            KT[i] = new KNNThread(dir, i);
            KT[i].start();
        }
        for(int i = 0; i < numThreads; i++) {
            KT[i].join();
            numInvaders += KT[i].getNumInvaders();
        }
        double OD = (double)numInvaders/(Parameters.numPoints * Parameters.topK[Parameters.topK.length - 1]);

        //combine files
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(new File(dir + "kNNDist")));
        String line;
        File file;
        for(int i = 0; i < numThreads; i++) {
            file = new File(dir + "thread-" +String.valueOf(i) + "-kNNDist");
            BufferedReader trainReader = new BufferedReader(new FileReader(file));
            while((line = trainReader.readLine()) != null) {
                trainWriter.write(line + "\n");
            }
            trainReader.close();
            //file.delete();
        }
        trainWriter.flush(); trainWriter.close();
        return OD;
    }

    private long[] computeOverlap(String dir) throws InterruptedException, IOException {
        //find and save the overlap degree between each pair of points
        //save the position of each row of the overlapFile
        //combine overlap files and save the offsets
        //return the offset of each point on the file

        int numThreads = Parameters.numThreads;

        OverlapThread[] OT = new OverlapThread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            OT[i] = new OverlapThread(dir, i);
            OT[i].start();
        }
        for(int i = 0; i < numThreads; i++) {
            OT[i].join();
            System.out.println(i);
        }

        long[] offset = new long[Parameters.numPoints];
        int pID = 0;
        String overlapFile = dir + "overlap";
        File tempFile = new File(overlapFile);
        tempFile.delete();
        RandomAccessFile raf = new RandomAccessFile(overlapFile, "rw");
        for(int i = 0; i < numThreads; i++) {
            BufferedReader reader = new BufferedReader(new FileReader(new File(dir + "thread-" + String.valueOf(i) + "-overlap")));
            String line;
            while((line = reader.readLine()) != null) {
                offset[pID++] = raf.getFilePointer();
                raf.writeBytes(line.substring(0, line.length()-1) + "\n");
            }
        }
        return offset;
    }

    private void getOriBenefitQueue(String dir, long[] offsets) throws IOException {
        //get the original benefits of all points
        PriorityQueue queue = new PriorityQueue(Parameters.numPoints, "descending");
        //first, read all benefits from file
        String pathToOverlap = dir + "overlap";
        RandomAccessFile reader = new RandomAccessFile(pathToOverlap, "r");
        String line;
        int pID = 0;
        while((line = reader.readLine()) != null) {
            if(line.length() == 0) break;
            String[] record = line.split(" ");
            double sum = 0;
            for(String s : record) {
                sum += Integer.valueOf(s.split(":")[1]);
            }
            queue.insert(sum, pID++);
        }
        //System.out.println(Arrays.toString(queue.serialize().toArray()));
        List<Double> keyList = new ArrayList<>(queue.serializeKeys());
        List<Integer> eleList = new ArrayList<>(queue.serialize());
        //second, select the maximal element in the queue and update the rest elements
        int count = 0;
        reader = new RandomAccessFile(pathToOverlap, "r");
        double[] result = new double[Parameters.numPoints];
        //System.out.println(queue.getSize());
        double[] degrees = new double[Parameters.numPoints];
        Set<Integer> removeSet = new HashSet<>();
        while(!queue.isEmpty()) {
            double overlap = (double)queue.getTopKey();
            //System.out.println(overlap);
            degrees[count++] = overlap;
            int id = (int)queue.dequeue();
            removeSet.add(id);
            //System.out.println(count++);
            result[id] = overlap;
            reader.seek(offsets[id]);
            String[] record = reader.readLine().split(" ");
            //System.out.print(queue.getTopKey());
            for(String s : record) {
                String[] temp = s.split(":");
                int otherID = Integer.parseInt(temp[0]);
                double thisOverlap = Double.parseDouble(temp[1]);
                if(removeSet.contains(otherID)) continue;
                double oriWeight = (double)queue.findByValue(otherID);
                //System.out.print(id + "-" + queue.serialize().size());
                queue.removeByValue(otherID);
                //System.out.println( "-" + queue.serialize().size());
                queue.insert(oriWeight - thisOverlap, otherID);
            }
            //System.out.println("-" + queue.getTopKey());
        }
//        System.out.println("*******");
//        for(int i = 0; i < Parameters.numPoints; i++) {
//            System.out.println(keyList.get(i) + "-" + degrees[i]);
//        }
//        System.out.println("*******");
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir + "benefits")));
        for(double d : result) {
            writer.write(String.valueOf(d) + "\n");
        }
        writer.flush();
        writer.close();
    }

    private void processWeights(String dir, double thres) throws IOException {
        //scale and normalize the weights of all points
        //when introduce thres, the last thres% points have weight 0, the rest have 1

        //the range of weight
        double minW = 0.1;
        double maxW = 0.9;
        double scale = 1;

        //read benefit file
        double[] benefits = new double[Parameters.numPoints];
        BufferedReader reader = new BufferedReader(new FileReader(new File(dir + "benefits")));
        for(int i = 0; i < Parameters.numPoints; i++) {
            benefits[i] = Double.parseDouble(reader.readLine());
        }

        //info of the benefits
        double maxBenefit = 0.0;
        double minBenefit = Double.MAX_VALUE;
        //find the maximal banefit
        for(double d : benefits) {
            if(d > maxBenefit)
                maxBenefit = d;
            if(d < minBenefit)
                minBenefit = d;
        }

        //compute weights
        Double[] weights = new Double[benefits.length];
        double benefitWidth = maxBenefit - minBenefit;
        for(int i = 0; i < benefits.length; i++) {
            weights[i] = Math.pow((maxBenefit - benefits[i]) / benefitWidth, scale);
        }

        //squeeze into range
        //double weightWidth = maxW - minW;
        //for(int i = 0; i < weights.length; i++) {
        //    weights[i] = weights[i] * weightWidth + minW;
        //}

        //stair function
        Double[] weightsCopy = new Double[weights.length];
        System.arraycopy(weights, 0, weightsCopy, 0, weightsCopy.length);
        List<Double> list = Arrays.asList(weightsCopy);
        Collections.sort(list);
        double cut = list.get((int)Math.floor(thres * list.size()));
        for(int i = 0; i < weights.length; i++) {
            weights[i] = weights[i] < cut ? 0.0 : 1.0;
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir + "weights")));
        for(double d : weights) {
            writer.write(String.valueOf(d) + "\n");
        }

        writer.flush();
        writer.close();

//        List<Double> list = Arrays.asList(weights);
//        Collections.sort(list);
//        try(FileWriter fw = new FileWriter("./data/weights.csv", true);
//            BufferedWriter bw = new BufferedWriter(fw);
//            PrintWriter out = new PrintWriter(bw))
//        {
//            out.println(scale);
//            for(double d : list) {
//                out.print(String.valueOf(d) + ",");
//            }
//            out.println();
//        } catch (IOException e) {
//        }
    }
}

class KNNThread extends Thread  {
    private String dir;//the folder to save all files
    private int threadID;
    private int numInvaders;

    public KNNThread(String dir, int threadID) {
        this.dir = dir;
        this.threadID = threadID;
        numInvaders = 0;
    }

    public int getNumInvaders() {
        return numInvaders;
    }

    public void run() {
        try {
            execute();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    void execute() throws IOException {
        String locationPrefix = dir + "thread-" + String.valueOf(threadID);
        String pathToOriFile = locationPrefix;
        String pathToKNN = locationPrefix + "-kNN";
        String pathToKNNDist = locationPrefix + "-kNNDist";
        String pathToThreshold = locationPrefix + "-threshold";
        BufferedReader reader1, reader2;
        reader1 = new BufferedReader(new FileReader(new File(pathToOriFile)));
        int fileSize = Utils.getNumLines(pathToOriFile);

        BufferedWriter writerOfDist, writerOfThreshold, writerOfKNN;
        writerOfDist = new BufferedWriter(new FileWriter(new File(pathToKNNDist)));
        writerOfKNN = new BufferedWriter(new FileWriter(new File(pathToKNN)));
        writerOfThreshold = new BufferedWriter(new FileWriter(new File(pathToThreshold)));

        String line1, line2;
        double[] vec1, vec2;
        double marker = 0;
        int clusterSize = Parameters.numPoints / Parameters.numClusters;
        while((line1 = reader1.readLine()) != null) {
            int pointID = fileSize * threadID + (int)marker;
            int thisClusterID = pointID / clusterSize;
            marker++;
            //if(marker > 1000) break;
            if(threadID == 0 && marker % 100 == 0)
                System.out.println(Math.round(marker / fileSize * 100) + "% points processed...");
            vec1 = Utils.getValuesFromLine(line1, " ");
            int maxQueueSize = Parameters.topK[Parameters.topK.length - 1];
            PriorityQueue rankQueue = new PriorityQueue(maxQueueSize,"ascending");
            int objID = 0;
            for (int fileID = 0; fileID < Parameters.numThreads; fileID++) {
                String otherFile = dir + "thread-" + String.valueOf(fileID);
                reader2 = new BufferedReader(new FileReader(new File(otherFile)));
                while ((line2 = reader2.readLine()) != null) {
                    vec2 = Utils.getValuesFromLine(line2, " ");
                    double dist = Utils.computeEuclideanDist(vec1, vec2);
                    //Utils.Utils.updatePriorityQueue(rankList, sim, idx);
                    if (dist==0) continue;
                    rankQueue.insert(dist, objID);
                    objID++;
                }
                reader2.close();
            }

            writerOfThreshold.write(String.valueOf((double)rankQueue.getBottomKey()) + "\n");

            List<Integer> tempList = rankQueue.serialize();
            for(Integer temp : tempList) {
                if(temp / clusterSize != thisClusterID) {
                    numInvaders++;
                }
                writerOfKNN.write(temp + " ");
            }
            writerOfKNN.write("\n");
            double[] distance = new double[Parameters.topK.length];
            for(int i = Parameters.topK.length - 1; i >= 0; i--) {
                int newSize = Parameters.topK[i];
                rankQueue.reSize(newSize);
                distance[i] = (double)rankQueue.getBottomKey();
            }
            for(int i = 0; i < distance.length; i++) {
                writerOfDist.write(String.valueOf(distance[i]) + ",");
            }
            writerOfDist.write("\n");
        }
        reader1.close();
        writerOfDist.flush(); writerOfDist.close();
        writerOfKNN.flush();writerOfKNN.close();
        writerOfThreshold.flush(); writerOfThreshold.close();
    }

}
class OverlapThread extends Thread {
    private String dir;
    private int threadID;

    public OverlapThread(String dir, int threadID) {
        this.dir = dir;
        this.threadID = threadID;
    }

    public void run() {
        try {
            execute();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void execute() throws IOException{
        String locationPrefix = dir + "thread-" + String.valueOf(threadID);
        String pathToKNN = locationPrefix + "-kNN";
        String pathToOverlap = locationPrefix + "-overlap";
        BufferedReader reader1, reader2;
        reader1 = new BufferedReader(new FileReader(new File(pathToKNN)));
        int fileSize = Utils.getNumLines(locationPrefix);

        BufferedWriter writerOfOverlap;
        writerOfOverlap = new BufferedWriter(new FileWriter(new File(pathToOverlap)));

        String line1, line2;
        double marker = 0;
        while((line1 = reader1.readLine()) != null) {
            marker++;
            //if(marker > 1000) break;
            if(threadID == 0 && marker % 100 == 0)
                System.out.println(Math.round(marker / fileSize * 100) + "% points processed...");
            Set<String> kNN1 = new HashSet<>(Arrays.asList(line1.split(" ")));
            int[] overlap = new int[Parameters.numPoints];
            int objID = 0;
            for (int fileID = 0; fileID < Parameters.numThreads; fileID++) {
                String otherFile = dir + "thread-" + String.valueOf(fileID) + "-kNN";
                reader2 = new BufferedReader(new FileReader(new File(otherFile)));
                while ((line2 = reader2.readLine()) != null) {
                    Set<String> kNN2 = new HashSet<>(Arrays.asList(line2.split(" ")));
                    kNN2.retainAll(kNN1);
                    overlap[objID++] = kNN2.size();
                }
                reader2.close();
            }
            for(int i = 0; i < overlap.length; i++) {
                if(overlap[i] != 0) {
                    writerOfOverlap.write(String.valueOf(i) + ":" + String.valueOf(overlap[i]) + " ");
                }
            }
            writerOfOverlap.write("\n");
        }
        reader1.close();
        writerOfOverlap.flush(); writerOfOverlap.close();
    }
}