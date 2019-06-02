import Utils.PriorityQueue;
import Utils.Utils;

import java.io.*;
import java.util.List;

class RankData <T> {
    private String[] files;
    private String dataType;
    private int topK;
    private int numThreads;
    private int fileSize;

    RankData(String[] files, int fileSize, int topK, String dataType, int numThreads) {
        this.files = files;
        this.fileSize = fileSize;
        this.dataType = dataType;
        this.topK = topK;
        this.numThreads = numThreads;
    }
    void generateRanks() throws InterruptedException {
        RankDataThread[] rdT = new RankDataThread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            rdT[i] = new RankDataThread(files, i, dataType, topK, fileSize);
            rdT[i].start();
        }
        for(int i = 0; i < numThreads; i++) {
            rdT[i].join();
        }

    }

}
class RankDataThread <T> extends Thread  {
    private String[] files;
    private int threadID;
    private String dataType;
    private int topK;
    private int fileSize;

    public RankDataThread(String[] files, int threadID, String dataType, int topK, int fileSize) {
        this.files = files;
        this.fileSize = fileSize;
        this.threadID = threadID;
        this.dataType = dataType;
        this.topK = topK;
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
        String path = files[threadID];
        reader1 = new BufferedReader(new FileReader(new File(path)));

        BufferedWriter writerOfRank, writerOfThreshold;
        writerOfRank = new BufferedWriter(new FileWriter(new File(path + "-rank")));
        writerOfThreshold = new BufferedWriter(new FileWriter(new File(path + "-threshold")));

        String line1, line2;
        Object[] vec1, vec2;
        double marker = 0;
        while((line1 = reader1.readLine()) != null) {
            marker++;
            if(threadID == 0 && marker % 100 == 0)
                System.out.println(Math.round(marker / fileSize * 100) + "% points processed...");
            PriorityQueue rankQueue = new PriorityQueue(topK,"ascending");
            vec1 = Utils.getValuesFromLine(line1, " ", dataType);

            for (int fileID = 0; fileID < files.length; fileID++) {
                String file = files[fileID];
                reader2 = new BufferedReader(new FileReader(new File(file)));
                int objID = 0;
                while ((line2 = reader2.readLine()) != null) {
                    vec2 = Utils.getValuesFromLine(line2, " ", dataType);
                    double dist = Utils.computeEuclideanDist(vec1, vec2);
                    //Utils.Utils.updatePriorityQueue(rankList, sim, idx);
                    if (dist==0) continue;
                    rankQueue.insert(dist, fileID * fileSize + objID);

                    objID++;
                }
                reader2.close();
            }
            writerOfThreshold.write(String.valueOf(rankQueue.getBottomKey()) + "\n");
            //writerOfThreshold.write(String.valueOf(rankQueue.getTopKey()) + "\n");
            //Utils.Utils.writeDescending(writer, rankList);
            List<T> rankList = rankQueue.serialize();
            for(T i : rankList) {
                writerOfRank.write(String.valueOf(i) + ",");
            }
            writerOfRank.write("\r\n");
            rankQueue.clear();

        }
        reader1.close();
        writerOfRank.flush(); writerOfRank.close();
        writerOfThreshold.flush(); writerOfThreshold.close();
    }
}