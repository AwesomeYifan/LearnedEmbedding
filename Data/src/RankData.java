import Utils.PriorityQueue;
import Utils.Utils;

import java.io.*;
import java.sql.ParameterMetaData;
import java.util.Arrays;
import java.util.List;

class RankData <T> {
    private String dir;
    private String dataType;
    private int topK;
    private int numThreads;
    private int fileSize;

    RankData(String dir, int fileSize, String dataType, int numThreads) {
        this.dir = dir;
        this.fileSize = fileSize;
        this.dataType = dataType;
        this.topK = Parameters.topK[Parameters.topK.length - 1];
        this.numThreads = numThreads;
    }
    void generateRanks() throws InterruptedException {
        RankDataThread[] rdT = new RankDataThread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            rdT[i] = new RankDataThread(dir, i, dataType, topK, fileSize);
            rdT[i].start();
        }
        for(int i = 0; i < numThreads; i++) {
            rdT[i].join();
        }
    }
}
class RankDataThread <T> extends Thread  {
    private String dir;
    private int threadID;
    private String dataType;
    private int fileSize;

    public RankDataThread(String dir, int threadID, String dataType, int topK, int fileSize) {
        this.dir = dir;
        this.fileSize = fileSize;
        this.threadID = threadID;
        this.dataType = dataType;
        this.fileSize = fileSize;
    }

    public void run() {
        try {
            generateRanks();
            //root.print();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    void generateRanks() throws IOException {
        BufferedReader reader1, reader2;
        String path = dir + "/thread-" + String.valueOf(threadID);
        reader1 = new BufferedReader(new FileReader(new File(path)));

        BufferedWriter writerOfDist, writerOfThreshold;
        writerOfDist = new BufferedWriter(new FileWriter(new File("./data/kNNDist")));
        writerOfThreshold = new BufferedWriter(new FileWriter(new File(path + "-threshold")));

        String line1, line2;
        Object[] vec1, vec2;
        double marker = 0;
        while((line1 = reader1.readLine()) != null) {
            marker++;
            if(threadID == 0 && marker % 100 == 0)
                System.out.println(Math.round(marker / fileSize * 100) + "% points processed...");
            int maxQueueSize = Parameters.topK[Parameters.topK.length - 1];
            PriorityQueue rankQueue = new PriorityQueue(maxQueueSize,"ascending");
            vec1 = Utils.getValuesFromLine(line1, " ", dataType);
            for (int fileID = 0; fileID < DataGenerator.numThreads; fileID++) {
                String file = dir + "/thread-" + String.valueOf(fileID);;
                reader2 = new BufferedReader(new FileReader(new File(file)));
                int objID = -1;
                while ((line2 = reader2.readLine()) != null) {
                    objID++;
                    vec2 = Utils.getValuesFromLine(line2, " ", dataType);
                    double dist = Utils.computeEuclideanDist(vec1, vec2);
                    //Utils.Utils.updatePriorityQueue(rankList, sim, idx);
                    if (dist==0) continue;
                    rankQueue.insert(dist, fileID * fileSize + objID);
                }
                reader2.close();
            }
            writerOfThreshold.write(String.valueOf(rankQueue.getBottomKey()) + "\n");
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
        writerOfThreshold.flush(); writerOfThreshold.close();
    }
}