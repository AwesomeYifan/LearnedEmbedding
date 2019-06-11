import java.io.IOException;

public class DataGenerator {

    static int numThreads = 8;

    public static void main(String[] args) throws IOException, InterruptedException {

        int fileSize = 3125; //numThreads * fileSize = numPoints

        int numDims = 20;
        int numPoints = numThreads * fileSize;
        int numClusters = 1;

        String dataType = "Double";

        String pathToDataset = "./data/siftsmall";
        //double maxDist = getMaxDist(numDims, "Euclidean");
        double trainRatio = getSampleRatio(numPoints);

        //GaussianData gd = new GaussianData(files, fileSize, numDims, numClusters, numThreads);
        //UniformData ud = new UniformData(files, fileSize, numDims, numThreads);
        RankData rd = new RankData(pathToDataset, fileSize, dataType, numThreads);
        TrainingData td = new TrainingData(numThreads, fileSize, pathToDataset, trainRatio, dataType);

        //gd.generateData();
        //ud.generateData();
        //System.out.println("******************\n* data generated *\n******************");

        //rd.generateRanks();
        System.out.println("\n***********************\n* rank data generated *\n***********************");

        td.generateSiameseSamples();
        System.out.println("\n*****************************\n* siamese samples generated *\n*****************************");

        //td.generateTripletSamples();
        System.out.println("\n*****************************\n* triplet samples generated *\n*****************************");
        //td.clean();
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
    private static double getSampleRatio(int numPoints) {
        int topK = Parameters.topK[Parameters.topK.length - 1];
        return (double)topK * 2 / (double)numPoints;
    }
}
