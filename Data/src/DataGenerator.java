import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class DataGenerator {

    public static void main(String[] args) throws Exception {

        int numDims = 6;
        int numPoints = 2000;
        int numClusters = 3;
        double trainRatio = 0.00005;
        String dataType = "Double";

        String path = "./data";
        String[] files = getFileNames(numClusters);
        double maxDist = getMaxDist(numDims, "Euclidean");

        //go to this class if need to chance mean and deviation.
        GaussianData gd = new GaussianData(path, files, numDims, numPoints, numClusters);
        RankData rd = new RankData(path, files, maxDist, dataType);
        TripletData td = new TripletData(path, files, maxDist, trainRatio, dataType);
        SiameseData sd = new SiameseData(path, files, maxDist, trainRatio, dataType);

        gd.generateData();
        System.out.println("\n******************\n* data generated *\n******************\n");

        rd.generateRanks();
        System.out.println("\n***********************\n* rank data generated *\n***********************\n");

        td.generateSamples();
        System.out.println("\n*****************************\n* triplet samples generated *\n*****************************\n");

        sd.generateSamples();
        System.out.println("\n*****************************\n* siamese samples generated *\n*****************************\n");
    }

    private static String[] getFileNames(int numFiles) {
        String[] files = new String[numFiles];
        for(int i = 0; i < numFiles; i++) {
            files[i] = "class-" + String.valueOf(i) + ".csv";
        }
        return files;
    }
    private static double getMaxDist(int dim, String opt) {
        switch (opt) {
            case "Euclidean":
                return Math.sqrt(dim);
            case "Jaccard":
                return 1.0;
            default: {
                return 1.0;
            }
        }
    }
}
