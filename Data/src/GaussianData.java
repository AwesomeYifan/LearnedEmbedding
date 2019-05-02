import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class GaussianData {
    private int numDims;
    private int numPoints;
    private String[] files;
    private String path;
    private double[][] centers;
    private double[] deviations;

    GaussianData(String path, String[] files, int numDims, int numPoints, int numClusters) {
        this.path = path;
        this.files = files;
        this.numDims = numDims;
        this.numPoints = numPoints;
        this.centers = this.getCenters(numClusters);
        this.deviations = this.getDeviation(numClusters);
    }

    void generateData() throws IOException {
        BufferedWriter bw = null;
        int numClusters = centers.length;
        int numPointsEachCluster = numPoints / numClusters;
        for(int i = 0; i < numClusters; i++) {
            bw = new BufferedWriter(new FileWriter(new File(path + "/" + files[i])));
            double[][] points = Utils.getGaussianPoints(numPointsEachCluster, centers[i], deviations[i]);
            for(int j = 0; j < numPointsEachCluster; j++) {
                for(int k = 0; k < numDims - 1; k++) {
                    bw.write(String.valueOf(points[j][k]) + " ");
                }
                bw.write(String.valueOf(points[j][numDims-1]));
                bw.write("\r\n");
            }
            bw.flush();
        }
        bw.close();
    }

    private double[][] getCenters(int numClusters) {
        double[][] centers = new double[numClusters][numDims];
        double center = 1.0 / (numClusters + 1);
        for(int i = 0; i < numClusters; i++) {
            for(int j = 0; j < numDims; j++) {
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
