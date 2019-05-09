import Utils.PriorityQueue;

import java.io.*;
import java.util.List;

class RankData <T> {
    private String path;
    private String[] files;
    private double maxDist;
    private String dataType;
    private Double lowestRankScore;
    private int topK;

    RankData(String path, String[] files, double maxDist, int topK, String dataType) {
        this.path = path;
        this.files = files;
        this.maxDist = maxDist;
        this.dataType = dataType;
        this.topK = topK;
        lowestRankScore = 0.0;
    }

    void generateRanks() throws IOException {
        BufferedReader reader1, reader2;

        String line1, line2;
        Object[] vec1, vec2;
        BufferedWriter writerOfRank, writerOfThreshold;


        for (String file : files) {
            writerOfRank = new BufferedWriter(new FileWriter(new File(path + "/rank-" + file)));
            writerOfThreshold = new BufferedWriter(new FileWriter(new File(path + "/threshold-" + file)));
            //TreeMap<Double, Set<Integer>> rankList = new TreeMap<>();
            //PriorityQueue rankQueue = new PriorityQueue("descending");
            PriorityQueue rankQueue = new PriorityQueue(topK,"ascending");
            reader1 = new BufferedReader(new FileReader(new File(path + "/" + file)));
            while ((line1 = reader1.readLine()) != null) {
                vec1 = Utils.getValuesFromLine(line1, " ", dataType);
                reader2 = new BufferedReader(new FileReader(new File(path + "/" + file)));
                int idx = 0;
                while ((line2 = reader2.readLine()) != null) {
                    vec2 = Utils.getValuesFromLine(line2, " ", dataType);
                    //double sim = Utils.computeSimilarity(vec1, vec2, maxDist, "Euclidean", "stair");
                    double sim = Utils.computeEuclideanDist(vec1, vec2);
                    //Utils.updatePriorityQueue(rankList, sim, idx);
                    rankQueue.insert(sim, idx);
                    idx++;
                }
                //System.out.println(rankQueue.getBottomKey());
                writerOfThreshold.write(String.valueOf(rankQueue.getBottomKey()) + "\n");
                //Utils.writeDescending(writer, rankList);
                List<T> rankList = rankQueue.serialize();
                for(T i : rankList) {
                    writerOfRank.write(String.valueOf(i) + ",");
                }
                writerOfRank.write("\n");

                rankQueue.clear();
                reader2.close();
            }
            reader1.close();
            writerOfRank.flush(); writerOfRank.close();
            writerOfThreshold.flush(); writerOfThreshold.close();
        }
    }
    public Double getLowestRankScore() {
        return this.lowestRankScore;
    }
}
