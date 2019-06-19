import java.io.IOException;

public class DataGenerator {

    public static void main(String[] args) throws IOException, InterruptedException {

        int fileSize = Parameters.numPoints / Parameters.numThreads; //numThreads * fileSize = numPoints
        int numThreads = Parameters.numThreads;
        int numPoints = numThreads * fileSize;
        int numClusters = 1;

        String dataType = "Double";

        //String pathToDataset = "./data/siftsmall";
        String pathToDataset = "./data/Gist";

        double trainRatio = getSampleRatio(numPoints);

        RankData rd = new RankData(pathToDataset, fileSize, dataType, numThreads);
        TrainingData td = new TrainingData(numThreads, fileSize, pathToDataset, trainRatio, dataType);

        rd.generateRanks();
        System.out.println("\n***********************\n* rank data generated *\n***********************");

        td.generateSiameseSamples();
        System.out.println("\n*****************************\n* siamese samples generated *\n*****************************");

        //td.generateTripletSamples();
        System.out.println("\n*****************************\n* triplet samples generated *\n*****************************");
        td.clean();
    }

//    public static void main(String[] args) throws IOException, InterruptedException {
//        String[] files = new String[8];
//        int fileSize = 3000;
//        int numDims = 100;
//        for(int i = 0; i < files.length; i++) {
//            files[i] = "./data/uniformData/thread-" + String.valueOf(i);
//        }
//        UniformData ud = new UniformData(files, fileSize, numDims);
//        ud.generateData();
//        //GaussianData gd = new GaussianData(files, fileSize, numDims, numClusters, numThreads);
//        //gd.generateData();
//    }

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
