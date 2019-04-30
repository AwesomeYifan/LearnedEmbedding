import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import Utils.PriorityQueue;

class RankData {
    private String path;
    private String[] files;
    private double maxDist;

    RankData(String path, String[] files, double maxDist) {
        this.path = path;
        this.files = files;
        this.maxDist = maxDist;
    }
    void generateRanks() throws Exception {
        BufferedReader reader1, reader2;

        String line1, line2;
        double[] vec1, vec2;
        BufferedWriter writer;

        for (String file : files) {
            writer = new BufferedWriter(new FileWriter(new File(path + "/rank-" + file)));
            //TreeMap<Double, Set<Integer>> rankList = new TreeMap<>();
            PriorityQueue rankQueue = new PriorityQueue(Integer.MAX_VALUE, "descending");
            reader1 = new BufferedReader(new FileReader(new File(path + "/" + file)));
            while ((line1 = reader1.readLine()) != null) {
                vec1 = Utils.getDoubles(line1, " ");
                reader2 = new BufferedReader(new FileReader(new File(path + "/" + file)));
                int idx = 0;
                while ((line2 = reader2.readLine()) != null) {
                    vec2 = Utils.getDoubles(line2, " ");
                    double sim = Utils.computeSimilarity(vec1, vec2, maxDist, "Euclidean", "stair");
                    //Utils.updatePriorityQueue(rankList, sim, idx);
                    rankQueue.insert(sim, idx);
                    idx++;
                }
                //Utils.writeDescending(writer, rankList);
                List<Integer> rankList = rankQueue.serialize();
                for(Integer i : rankList) {
                    writer.write(String.valueOf(i) + ",");
                }
                writer.write("\n");
                rankQueue.clear();
                reader2.close();
            }
            reader1.close();
            writer.flush();
            writer.close();
        }
    }
}
