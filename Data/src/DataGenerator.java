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

public class DataGenerator {

    public static void main(String[] args) throws Exception {

        int numDims = 6;
        int numPoints = 2000;
        int numClusters = 3;
        double trainRatio = 0.00005;

        String path = "./data";
        String[] files = Utils.getFileNames(numClusters);
        double maxDist = Utils.getMaxDist(numDims, "Euclidean");

        GaussianData gd = new GaussianData(numDims, numPoints, numClusters);
        RankData rd = new RankData(path, files, maxDist);
        TripletData td = new TripletData(path, files, maxDist, trainRatio);
        SiameseData sd = new SiameseData(path, files, maxDist, trainRatio);

        gd.generateData();
        System.out.println("data generated");

        rd.generateRanks();
        System.out.println("rank generated");

        td.generateSamples();
        System.out.println("triplet data generated");

        sd.generateSamples();
        System.out.println("siamese data generated");
    }

}
