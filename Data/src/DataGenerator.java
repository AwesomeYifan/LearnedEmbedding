import java.io.File;
import java.io.IOException;

public class DataGenerator {

    public static void main(String[] args) throws IOException, InterruptedException {

        String dataset = args[0];
        int originalSampleDim = Integer.parseInt(args[1]);
        double originalSampleRatio = Double.parseDouble(args[2]);

//        String dataset = "SIFT";
//        int originalSampleDim =100;
//        double originalSampleRatio = 0.1;

        String pathToDataset = "./data/" + dataset;

        String suffix = dataset + "-" + String.valueOf(originalSampleDim);
        String path = "./data/originalVectors-" + suffix;
        File file = new File(path);

        if(!file.exists()) {
            RankData rd = new RankData(pathToDataset, dataset, originalSampleDim, 1.0);
            rd.generateRanks();
            //System.out.println("\n***********************\n* rank data generated *\n***********************");
        }

        TrainingData td = new TrainingData(pathToDataset, dataset, originalSampleRatio);
        td.generateSiameseSamples();
        //System.out.println("\n*****************************\n* siamese samples generated *\n*****************************");
    }
}
