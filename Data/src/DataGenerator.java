import java.io.IOException;

public class DataGenerator {

    public static void main(String[] args) throws IOException, InterruptedException {

        int numThreads = 8;
        int fileSize = 3125; //numThreads * fileSize = numPoints

        int numDims = 20;
        int numPoints = numThreads * fileSize;
        int numClusters = 1;
        int topK = 50;

        String dataType = "Double";

        String path = "./data";
        String[] files = getFileNames(path, numThreads);
        //double maxDist = getMaxDist(numDims, "Euclidean");
        double trainRatio = getSampleRatio(topK+1, numPoints);

        //GaussianData gd = new GaussianData(files, fileSize, numDims, numClusters, numThreads);
        //UniformData ud = new UniformData(files, fileSize, numDims, numThreads);
        RankData rd = new RankData(files, fileSize, topK+1, dataType, numThreads);
        TrainingData td = new TrainingData(numThreads, fileSize, path, files, trainRatio, dataType);

        //gd.generateData();
        //ud.generateData();
        //System.out.println("******************\n* data generated *\n******************");

        rd.generateRanks();
        System.out.println("\n***********************\n* rank data generated *\n***********************");

        //td.generateSiameseSamples();
        td.generateTripletSamples();
        System.out.println("\n*****************************\n* siamese samples generated *\n*****************************");

        //td.generateTripletSamples();
        //System.out.println("\n*****************************\n* triplet samples generated *\n*****************************");
    }

    private static String[] getFileNames(String path, int numThreads) {
        String[] files = new String[numThreads];
        for(int i = 0; i < numThreads; i++) {
            files[i] = path + "/thread-" + String.valueOf(i);
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
        return (double)topK * 2 / (double)numPoints;
    }
}
