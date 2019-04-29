import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/**
* Keep 2 decimals for all vectors and distances to save space
*/

public class Vectors {
    private int dim = 6;
    private int numPoints = 2000;
    private int numClusters = 3;
    private double trainRatio = 0.00005;

    public static void main(String[] args) throws IOException, InterruptedException {

        String path = "./data";

        Vectors vec = new Vectors();
        String[] files = new String[vec.numClusters];
        for(int i = 0; i < vec.numClusters; i++) {
            files[i] = "class-" + String.valueOf(i) + ".csv";
        }
        vec.generateGaussian(vec.getCenters(), vec.getDeviation());
        System.out.println("points generated");
        RankData rd = new RankData(path, files, vec.getMaxDist("Euclidean"));
        rd.generateRanks();
        System.out.println("rank generated");
        TripletData td = new TripletData(path, files, vec.getMaxDist("Euclidean"), vec.trainRatio);
        td.generateSamples();
        System.out.println("triplet data generated");
        SiameseData sd = new SiameseData(path, files, vec.getMaxDist("Euclidean"), vec.trainRatio);
        sd.generateSamples();
        System.out.println("siamese data generated");
    }

    public void generateGaussian(double[][] centers, double[] deviations) throws IOException {
        BufferedWriter bw = null;
        Random random = new Random();
        int numClusters = centers.length;
        int numPointsEachCluster = numPoints / numClusters;
        for(int i = 0; i < numClusters; i++) {
            bw = new BufferedWriter(new FileWriter(new File("./data/class-" + String.valueOf(i) + ".csv")));
            double[][] points = Utils.getGaussianPoints(numPointsEachCluster, centers[i], deviations[i]);
            for(int j = 0; j < numPointsEachCluster; j++) {
                for(int k = 0; k < dim - 1; k++) {
                    bw.write(String.valueOf(points[j][k]) + " ");
                }
                bw.write(String.valueOf(points[j][dim-1]));
                bw.write("\r\n");
            }
            bw.flush();
        }
        bw.close();
    }
    private double[][] getCenters() {
        double[][] centers = new double[numClusters][dim];
        double center = 1.0 / (numClusters + 1);
        for(int i = 0; i < numClusters; i++) {
            for(int j = 0; j < dim; j++) {
                centers[i][j] = center * (i + 1);
            }
        }
        return centers;
    }
    private double[] getDeviation() {
        double[] deviation = new double[numClusters];
        double dev = 0.5 / numClusters;
        for(int i = 0; i < numClusters; i++) {
            deviation[i] = dev;
        }
        return deviation;
    }
    private double getMaxDist(String opt) {
        switch (opt) {
            case "Euclidean": {
                return Math.sqrt(dim);
            }
            default: {
                return Math.sqrt(dim);
            }

        }
    }

}
