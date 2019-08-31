import Utils.PriorityQueue;
import Utils.Utils;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.util.*;

class RankData {
    private String dir;
    private String dataset;
    int numThreads = Parameters.numThreads;
    private int dim;
    private double sampleSize;

    //sampleSize: we use this sample as the original data
    RankData(String dir, String dataset, int dim, double sampleSize) {
        this.dir = dir;
        this.dataset = dataset;
        this.dim = dim;
        this.sampleSize = sampleSize;
    }

    void generateRanks() throws InterruptedException, IOException {

        RankDataThread[] rdT = new RankDataThread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            rdT[i] = new RankDataThread(dir, i, dim, sampleSize);
            rdT[i].start();
        }
        for(int i = 0; i < numThreads; i++) {
            rdT[i].join();
        }
        printDists(rdT[0]);
        combineFiles();
    }

    void printDists(RankDataThread rdT) throws IOException {
        try(FileWriter fw = new FileWriter("./data/distances.csv", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(String.valueOf(dataset + "-" + dim + "-" + sampleSize));
            List<List<Double>> record = rdT.getDists();
            double[] dists = new double[Parameters.topK[Parameters.topK.length-1]];

            for(List<Double> distances : record) {
                //System.out.println("***************" + distances.size());
                for(int i = 0; i < distances.size(); i++) {
                    dists[i] += distances.get(i);
                }
            }
            for(int i = 0; i < dists.length; i++) {
                dists[i] /= record.size();
                out.print(String.valueOf(dists[i]) + ",");
            }
            out.println();
        } catch (IOException e) {
        }
    }

    void combineFiles() throws IOException {
        String suffix = dataset + "-" + String.valueOf(dim);
        String line;
        File file;
        BufferedWriter bwOfThread = new BufferedWriter((new FileWriter(new File("./data/originalVectors-" + suffix))));
        for(int i = 0; i < numThreads; i++) {
            file = new File(dir + "/thread-" + String.valueOf(i));
            if(!file.exists()) break;
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((line = br.readLine()) != null) {
                bwOfThread.write(line + "\n");
            }
            br.close();
        }
        bwOfThread.flush(); bwOfThread.close();

        BufferedWriter writer = new BufferedWriter((new FileWriter(new File("./data/kNNDist-" + suffix))));
        for(int i = 0; i < numThreads; i++) {
            file = new File(dir + "/thread-" + String.valueOf(i) + "-kNNDist");
            if(!file.exists()) break;
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((line = br.readLine()) != null) {
                writer.write(line + "\n");
            }
            br.close();
        }
        writer.flush(); writer.close();
    }
}

class RankDataThread <T> extends Thread  {
    private String dir;
    private int threadID;
    private int dim;
    private double sampleSize;
    private List<List<Double>> dists;

    public RankDataThread(String dir, int threadID, int dim, double sampleSize) {
        this.dir = dir;
        this.threadID = threadID;
        this.dim = dim;
        this.sampleSize = sampleSize;
        this.dists = new ArrayList<>();
    }

    public void run() {
        try {
            generateRanks();
            //root.print();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public List<List<Double>> getDists() {
        return this.dists;
    }
    void generateRanks() throws IOException {
        BufferedReader reader1, reader2;
        String path = dir + "/thread-" + String.valueOf(threadID);
        shapeData(path);
        reader1 = new BufferedReader(new FileReader(new File(path)));
        int fileSize = Utils.getNumLines(path);

        BufferedWriter writerOfDist, writerOfThreshold, writerOfMinDist;
        writerOfDist = new BufferedWriter(new FileWriter(new File(path + "-kNNDist")));
        writerOfThreshold = new BufferedWriter(new FileWriter(new File(path + "-threshold")));
        writerOfMinDist = new BufferedWriter(new FileWriter(new File(path + "-mindist")));

        String line1, line2;
        double[] vec1, vec2;
        double marker = 0;
        while((line1 = reader1.readLine()) != null) {
            marker++;
            //if(marker > 1000) break;
            if(threadID == 0 && marker % 100 == 0)
                System.out.println(Math.round(marker / fileSize * 100) + "% points processed...");
            vec1 = Utils.getValuesFromLine(line1, " ");
            int maxQueueSize = Parameters.topK[Parameters.topK.length - 1];
            PriorityQueue rankQueue = new PriorityQueue(maxQueueSize,"ascending");
            for (int fileID = 0; fileID < Parameters.numThreads; fileID++) {
                String file = dir + "/thread-" + String.valueOf(fileID);;
                reader2 = new BufferedReader(new FileReader(new File(file)));
                int objID = 0;
                while ((line2 = reader2.readLine()) != null) {
                    vec2 = Utils.getValuesFromLine(line2, " ");
                    double dist = Utils.computeEuclideanDist(vec1, vec2);
                    //Utils.Utils.updatePriorityQueue(rankList, sim, idx);
                    if (dist==0) continue;
                    rankQueue.insert(dist, fileID * fileSize + objID);
                    objID++;
                }
                reader2.close();
            }
            if(threadID == 0 && marker < 100)
                dists.add(rankQueue.serializeKeys());
            writerOfThreshold.write(String.valueOf((double)rankQueue.getBottomKey()) + "\n");
            writerOfMinDist.write(String.valueOf(rankQueue.getTopKey() + "\n"));
            if(threadID == 0 && marker <= Parameters.testSize) {
                String[] distances = new String[Parameters.topK.length];
                for(int i = Parameters.topK.length - 1; i >= 0; i--) {
                    int newSize = Parameters.topK[i];
                    rankQueue.reSize(newSize);
                    distances[i] = String.valueOf(rankQueue.getBottomKey());
                    //System.out.println(Arrays.toString(rankQueue.serialize().toArray()));
                }
                for(String dist : distances) {
                    writerOfDist.write(dist + ",");
                }
                writerOfDist.write("\n");
            }
        }
        reader1.close();
        writerOfDist.flush(); writerOfDist.close();
        writerOfMinDist.flush();writerOfDist.close();
        writerOfThreshold.flush(); writerOfThreshold.close();
    }
    //sample dimensions
    void shapeData(String path) throws IOException{
        File oldF = new File(path + "-ORI");
        File newF = new File(path);
        Random random = new Random();
        BufferedReader reader = new BufferedReader(new FileReader(oldF));
        int oriDim = reader.readLine().split(" ").length;
        Set<Integer> set = new TreeSet<>();
        while(set.size() < dim) {
            set.add(random.nextInt(oriDim));
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(newF));
        reader = new BufferedReader(new FileReader(oldF));
        String line;
        while((line = reader.readLine()) != null) {
            if(random.nextDouble() < sampleSize) {
                String[] record = line.split(" ");
                StringBuilder sb = new StringBuilder();
                for(Integer i : set) {
                    sb.append(String.valueOf(record[i]) + " ");
                }
                String str = sb.substring(0, sb.length()-1);
                writer.write(str + "\n");
            }
        }
        reader.close();
        writer.flush();
        writer.close();
    }
}