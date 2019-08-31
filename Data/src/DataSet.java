import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import Utils.*;

public class DataSet {
    int dim;
    int numClusters;
    double axisRange;
    int numPoints;
    int clusterSize;
    double[][] centers;

    public static void main(String[] args) throws IOException {
        //double axisRange = Double.parseDouble(args[0]);
        double axisRange = 10;
        DataSet ds = new DataSet(Parameters.numPoints, Parameters.dimensionality, Parameters.numClusters, axisRange);
        ds.generateGaussian();
    }

    public DataSet(int numPoints, int dim, int numClusters, double axisRange) {
        this.numClusters = numClusters;
        this.dim = dim;
        this.numPoints = numPoints;
        this.axisRange = axisRange;
        this.clusterSize = numPoints / numClusters;
        //this.centers = getCenters();
        this.centers = new double[][]{{2,0}, {0,2}};
    }

    public void generateGaussian() throws IOException {
        double variance = 0.1;
        Random random = new Random();
        String folder = "./data/Gaussian";
        String file = folder + "/points";
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(file)));
        for(int i = 0; i < numClusters; i++) {
            double[] center = centers[i];
            for(int j = 0; j < clusterSize; j++) {
                double temp;
                for(int k = 0; k < dim; k++) {
                    temp = random.nextGaussian() * variance + center[k];
                    if(k == dim - 1) {
                        writer.write(String.valueOf(temp));
                    }
                    else {
                        writer.write(String.valueOf(temp) + " ");
                    }

                }
                writer.write("\n");
            }
        }
        writer.flush();
        writer.close();
        Utils.splitFile(file, folder, Parameters.numThreads);
    }
    public double[][] getCenters() {
        double[][] centers = new double[numClusters][dim];
        Random random = new Random();
        for(int i = 0; i < numClusters; i++) {
            for(int j = 0; j < dim; j++) {
                centers[i][j] = random.nextDouble() * axisRange;
            }
        }
        return centers;
    }
}
