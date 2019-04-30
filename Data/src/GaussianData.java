import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GaussianData {
    private int dim;
    private int numPoints;
    private double[][] centers;
    private double[] deviations;
    public GaussianData(int dim, int numPoints, int numClusters) {
        this.dim = dim;
        this.numPoints = numPoints;
        this.centers = this.getCenters(numClusters);
        this.deviations = this.getDeviation(numClusters);
    }
    public void generateData() throws IOException {
        BufferedWriter bw = null;
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

    private double[][] getCenters(int numClusters) {
        double[][] centers = new double[numClusters][dim];
        double center = 1.0 / (numClusters + 1);
        for(int i = 0; i < numClusters; i++) {
            for(int j = 0; j < dim; j++) {
                centers[i][j] = center * (i + 1);
            }
        }
        return centers;
    }
    private double[] getDeviation(int numClusters) {
        double[] deviation = new double[numClusters];
        //double dev = 0.5 / numClusters;
        double dev = 0.25;
        for(int i = 0; i < numClusters; i++) {
            deviation[i] = dev;
        }
        return deviation;
    }
}
