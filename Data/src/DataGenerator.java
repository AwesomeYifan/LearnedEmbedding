import java.io.IOException;

public class DataGenerator {

    public static void main(String[] args) throws IOException {

        int numDims = 20;
        int numPoints = 200;
        int numClusters = 1;
        int topK = 50;
        String dataType = "Double";

        String path = "./data";
        String[] files = getFileNames(numClusters);
        double maxDist = getMaxDist(numDims, "Euclidean");
        double trainRatio = getSampleRatio(topK, numPoints);

        //go to this class if need to chance mean and deviation.
        GaussianData gd = new GaussianData(path, files, numDims, numPoints, numClusters);
        RankData rd = new RankData(path, files, maxDist, topK, dataType);
        TrainingData td = new TrainingData(path, files, maxDist, trainRatio, dataType);

        gd.generateData();
        System.out.println("******************\n* data generated *\n******************");

        rd.generateRanks();
        System.out.println("\n***********************\n* rank data generated *\n***********************");

        //td.generateTripletSamples();
        System.out.println("\n*****************************\n* triplet samples generated *\n*****************************");

        td.generateSiameseSamples();
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
