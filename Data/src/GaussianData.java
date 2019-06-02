import Utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class GaussianData {
    private int numDims;
    private String[] files;
    private int fileSize;
    private double[][] centers;
    private double[] deviations;
    private int numThreads;

    GaussianData(String[] files, int fileSize, int numDims, int numClusters, int numThreads) {
        this.files = files;
        this.fileSize = fileSize;
        this.numDims = numDims;
        this.centers = this.getCenters(numClusters);
        this.deviations = this.getDeviation(numClusters);
        this.numThreads = numThreads;
    }

    void generateData() throws IOException {
        BufferedWriter bw = null;
        int numClusters = centers.length;
        for(int i = 0; i < files.length; i++) {
            bw = new BufferedWriter(new FileWriter(new File(files[i])));
            double[][] points = Utils.getGaussianPoints(fileSize, centers[i/numThreads], deviations[i/numThreads]);
            writeVectors(bw, points, fileSize, numDims);
            bw.flush();
            bw.close();
        }
    }

    static void writeVectors(BufferedWriter bw, double[][] points, int fileSize, int numDims) throws IOException {
        for(int j = 0; j < fileSize; j++) {
            for(int k = 0; k < numDims - 1; k++) {
                bw.write(String.valueOf(points[j][k]) + " ");
            }
            bw.write(String.valueOf(points[j][numDims -1]));
            bw.write("\r\n");
        }
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
