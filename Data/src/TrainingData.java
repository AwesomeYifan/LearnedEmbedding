import Utils.Utils;
import Utils.PriorityQueue;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

class TrainingData {
    private int numThreads = Parameters.numThreads;
    private String datasetDir;
    private double trainRatio;
    private String dataset;

    TrainingData(String pathToDataset, String dataset, double sampleRatio) {
        this.datasetDir = pathToDataset;
        this.trainRatio = sampleRatio;
        this.dataset = dataset;
    }

    void generateSiameseSamples() throws InterruptedException, IOException {
        SiameseDataThread[] sdT = new SiameseDataThread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            sdT[i] = new SiameseDataThread(i, datasetDir, trainRatio);
            sdT[i].start();
        }
        for(int i = 0; i < numThreads; i++) {
            sdT[i].join();
        }

        this.combineTrainingFiles();
    }

    void combineTrainingFiles() throws IOException {
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(new File("./data/trainingData-" + dataset)));
        String line;
        File file;
        for(int i = 0; i < numThreads; i++) {
            file = new File("./data/trainingData-siamese" + "-" + String.valueOf(i));
            if(!file.exists()) break;
            BufferedReader trainReader = new BufferedReader(new FileReader(file));
            if(i != 0) trainReader.readLine();
            while((line = trainReader.readLine()) != null) {
                trainWriter.write(line + "\n");
            }
            trainReader.close();
            file.delete();
        }
        trainWriter.flush(); trainWriter.close();
    }
}

class SiameseDataThread extends Thread  {
    private int threadID;
    private String datasetDir;
    private double sampleRatio;

    public SiameseDataThread(int threadID, String datasetDir, double sampleRatio) {
        this.threadID = threadID;
        this.datasetDir = datasetDir;
        this.sampleRatio = sampleRatio;
    }

    public void run() {
        try {
            generateSiameseSamples();
            //root.print();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    void generateSiameseSamples() throws IOException {
        Random random = new Random();
        BufferedReader p1Reader, p2Reader, thresholdReader, mindistReader;
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(new File("./data/trainingData-siamese-" + String.valueOf(threadID))));
        //format: P1, P2, distance, kNN distance of P1
        trainWriter.write("P1,P2,distance,mindist,cutoff\n");
        String[] lines = new String[2];
        double[][] points = new double[2][];
        double marker = 0;
        String file = datasetDir + "/thread-" + String.valueOf(threadID);
        int fileSize = Utils.getNumLines(file);
        double negativeRatio = 1.0 * Parameters.topK[Parameters.topK.length - 1] / fileSize / Parameters.numThreads;
        p1Reader = new BufferedReader(new FileReader(new File(file)));
        thresholdReader = new BufferedReader(new FileReader(new File(file + "-threshold")));
        mindistReader = new BufferedReader(new FileReader(new File(file + "-mindist")));
        String threshold;
        String mindist;
        while((lines[0] = p1Reader.readLine()) != null &&
                (threshold = thresholdReader.readLine()) != null &&
                (mindist = mindistReader.readLine()) != null) {
            marker++;
            //if(marker > 1000) break;
            if(marker % 100== 0 && threadID == 0) {
                System.out.println(String.valueOf(Math.round(marker / fileSize * 100)) + "% training samples generated...");
            }
            if(random.nextDouble() > sampleRatio) continue;
            points[0] = Utils.getValuesFromLine(lines[0], " ");
            double thres = Double.parseDouble(threshold);
            for(int j = threadID; j < Parameters.numThreads; j++) {
                p2Reader = new BufferedReader(new FileReader(new File(datasetDir + "/thread-" + String.valueOf(j))));
                while((lines[1] = p2Reader.readLine()) != null) {
                    points[1] = Utils.getValuesFromLine(lines[1], " ");
                    double dist = Utils.computeEuclideanDist(points[0], points[1]);
                    if(dist==0) continue;
                    if(random.nextDouble() < negativeRatio || dist < (1+Parameters.eps)*thres ){
                        trainWriter.write(lines[0] + "," + lines[1] + "," + String.valueOf(dist) + "," + mindist + "," + String.valueOf((1+Parameters.eps)*thres) + "\n");
                        //trainWriter.write(lines[0] + "," + lines[1] + "," + String.valueOf(dist/thres) + "," + String.valueOf(1.0) + "\n");
                    }
                }
                p2Reader.close();
            }
        }
        p1Reader.close();
        trainWriter.flush(); trainWriter.close();
        //testWriter.flush(); testWriter.close();
    }
}
