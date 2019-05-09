import java.io.IOException;

public class DataGenerator {

    public static void main(String[] args) throws IOException {

        int numDims = 6;
        int numPoints = 2000;
        int numClusters = 1;
        int topK = 10;
        String dataType = "Double";

        String path = "./data";
        String[] files = getFileNames(numClusters);
        double maxDist = getMaxDist(numDims, "Euclidean");
        double trainRatio = getSampleRatio(topK, numPoints);

        //go to this class if need to chance mean and deviation.
        GaussianData gd = new GaussianData(path, files, numDims, numPoints, numClusters);
        RankData rd = new RankData(path, files, maxDist, topK, dataType);
        TripletData td = new TripletData(path, files, maxDist, trainRatio, dataType);
        SiameseData sd = new SiameseData(path, files, maxDist, trainRatio, dataType);

        gd.generateData();
        System.out.println("******************\n* data generated *\n******************");

        rd.generateRanks();
        System.out.println("\n***********************\n* rank data generated *\n***********************");

        //td.generateSamples();
        System.out.println("\n*****************************\n* triplet samples generated *\n*****************************");

        sd.generateSamples();
        System.out.println("\n*****************************\n* siamese samples generated *\n*****************************");
        //System.out.println("maximal knn distance: " + rd.getLowestRankScore());
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
    //balance positive (kNN) and negative (non-kNN) points
    private static double getSampleRatio(int topK, int numPoints) {
        return (double)topK / (double)numPoints * 3;
    }
}
