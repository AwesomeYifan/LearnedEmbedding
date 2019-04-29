import java.io.*;
import java.util.Set;
import java.util.TreeMap;

class RankData {
    private String path;
    private String[] files;
    private double maxDist;

    RankData(String path, String[] files, double maxDist) {
        this.path = path;
        this.files = files;
        this.maxDist = maxDist;
    }
    void generateRanks() throws IOException {
        BufferedReader reader1, reader2;

        String line1, line2;
        double[] vec1, vec2;
        BufferedWriter writer;

        for (String file : files) {
            writer = new BufferedWriter(new FileWriter(new File(path + "/rank-" + file)));
            TreeMap<Double, Set<Integer>> rankList = new TreeMap<>();
            reader1 = new BufferedReader(new FileReader(new File(path + "/" + file)));
            while ((line1 = reader1.readLine()) != null) {
                vec1 = Utils.transform(line1, " ");
                reader2 = new BufferedReader(new FileReader(new File(path + "/" + file)));
                int idx = 0;
                while ((line2 = reader2.readLine()) != null) {
                    vec2 = Utils.transform(line2, " ");
                    double sim = Utils.computeSimilarity(vec1, vec2, maxDist, "Euclidean", "stair");
                    Utils.updatePriorityQueue(rankList, sim, idx);
                    idx++;
                }
                Utils.writeDescending(writer, rankList);
                rankList.clear();
                reader2.close();
            }
            reader1.close();
            writer.flush();
            writer.close();
        }
    }
}
