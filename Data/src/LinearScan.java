import Utils.PriorityQueue;
import java.io.*;
import java.lang.reflect.Parameter;
import java.util.*;
import Utils.Utils;
public class LinearScan {

    //args: dataset, d', epsilon, method
    private static int testSize;
    public static void main(String[] args) throws IOException {
        testSize = Parameters.testSize;

        String dir = "./data/Gaussian/";

        //String suffix = "-" + dataset + "-" + String.valueOf(reducedDim) + "-" + String.valueOf(traineps);
        String file = dir + "reducedPoints";
        file = dir + "points";
        int numPoints = Parameters.numPoints;
        double[][] points = getPointsInReducedSpace(numPoints, file);
        int[] clusterInfo = readClusterInfo(dir);
        double[] avgDists = getAvgDists(points, clusterInfo);
        System.out.println(Arrays.toString(avgDists));
        try(FileWriter fw = new FileWriter("./data/distances.csv", true);
              BufferedWriter bw = new BufferedWriter(fw);
              PrintWriter out = new PrintWriter(bw))
        {
            out.println(String.join("-", args));
            out.println(avgDists[0] + "," + avgDists[1]);
        } catch (IOException e) {
        }
    }

    static double[][] getPointsInReducedSpace(int numPoints, String file) throws IOException {

        //String file = "./data/reducedVectors-PCA" + "-Uniform";
        //String file = "./data/originalVectors";
        //System.out.println("*********************" + testSize);
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        String line;
        double[][] points = new double[numPoints][];
        int idx = 0;
        while((line = br.readLine()) != null) {
            String[] records = line.split(" ");
            double[] values = new double[records.length];
            for(int i = 0; i < records.length; i++) {
                values[i] = Double.parseDouble(records[i]);
            }
            points[idx] = values;
            idx++;
        }
        return points;
    }
    static double[] getAvgDists(double[][] points, int[] clusterInfo) throws IOException {
        List<List<Set<Integer>>> kNNs = new ArrayList<>();
        int maxK = Parameters.topK[Parameters.topK.length - 1];
        double[] avgDists = new double[2];
        int kNNCount = 0;
        int nonKNNCount = 0;
        for(int i = 0; i < points.length; i++) {
//            PriorityQueue pq = new PriorityQueue(maxK, "ascending");
//            for(int j = 0; j < points.length; j++) {
//                double dist = Utils.computeEuclideanDist(points[i], points[j]);
//                if(dist == 0) continue;
//                pq.insert(dist, j);
//            }
//            List<Integer> kNN = pq.serialize();
            for(int j = 0; j < points.length; j++) {
                double dist = Utils.computeEuclideanDist(points[i], points[j]);
                if(dist == 0) continue;
//                if(kNN.contains(j)) {
                if(clusterInfo[i] == clusterInfo[j]) {
                    kNNCount++;
                    avgDists[0] += dist;
                }
                else {
                    nonKNNCount++;
                    avgDists[1] += dist;
                }
            }
        }
        avgDists[0] /= kNNCount;
        avgDists[1] /= nonKNNCount;
        return avgDists;
    }
    private static int[] readClusterInfo(String dir) throws IOException {
        int[] clusterInfo = new int[Parameters.numPoints];
        BufferedReader reader = new BufferedReader(new FileReader(new File(dir + "clusterInfo")));
        for(int i = 0; i < Parameters.numPoints; i++) {
            clusterInfo[i] = Integer.parseInt(reader.readLine());
        }
        reader.close();
        return clusterInfo;
    }
}
