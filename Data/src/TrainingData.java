import Utils.Utils;
import Utils.PriorityQueue;
import java.io.*;
import java.util.List;
import java.util.Random;

class TrainingData {
    private int numThreads;
    private int fileSize;
    private String path;
    private String[] files;
    private double trainRatio;
    private double testRatio;
    private String dataType;

    TrainingData(int numThreads, int fileSize, String path, String[] files, double sampleRatio, String dataType) {
        this.fileSize = fileSize;
        this.numThreads = numThreads;
        this.path = path;
        this.files = files;
        this.trainRatio = sampleRatio;
        this.testRatio = sampleRatio / 5;
        this.dataType = dataType;
    }
    void generateTripletSamples() throws InterruptedException, IOException {
        TripletDataThread[] tdT = new TripletDataThread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            tdT[i] = new TripletDataThread(i, path, files, trainRatio, dataType, fileSize);
            tdT[i].start();
        }
        for(int i = 0; i < numThreads; i++) {
            tdT[i].join();
        }
        System.out.println("subfiles generated");
        combineFiles();
    }

    void generateSiameseSamples() throws InterruptedException, IOException {
        SiameseDataThread[] sdT = new SiameseDataThread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            sdT[i] = new SiameseDataThread(i, path, files, trainRatio, dataType, fileSize);
            sdT[i].start();
        }
        for(int i = 0; i < numThreads; i++) {
            sdT[i].join();
        }
        System.out.println("subfiles generated");
        combineFiles();
    }

    void combineFiles() throws IOException {
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(new File(path + "/trainingData.csv")));
        //BufferedWriter validationWriter = new BufferedWriter(new FileWriter(new File(path + "/validationData.csv")));
        String line;
        File file;
        for(int i = 0; i < numThreads; i++) {
            file = new File(path + "/trainingData-" + String.valueOf(i) + ".csv");
            BufferedReader trainReader = new BufferedReader(new FileReader(file));
            if(i != 0) trainReader.readLine();
            while((line = trainReader.readLine()) != null) {
                trainWriter.write(line + "\n");
            }
            trainReader.close();
            file.delete();
//
//            BufferedReader validationReader = new BufferedReader(new FileReader(new File(path + "/validationData-" + String.valueOf(i) + ".csv")));
//            if(i != 0) validationReader.readLine();
//            while((line = validationReader.readLine()) != null) {
//                validationWriter.write(line + "\n");
//            }
//            validationReader.close();
        }
        trainWriter.flush(); trainWriter.close();
        //validationWriter.flush(); validationWriter.close();
    }

//    void generateTripletSamples() throws IOException {
//        Random random = new Random();
//        BufferedReader anchorReader, positiveReader, negativeReader;
//        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("TripletData.csv")));
//        writer.write("anchor,positive,negative,diff\n");
//        String[] lines = new String[3];//anchor, positive, negative
//        Object[][] points = new Object[3][];
//        //iterates each file
//        for(int i = 0; i < files.length; i++) {
//            anchorReader = new BufferedReader(new FileReader(new File(files[i])));
//            int marker = 0;
//            while((lines[0] = anchorReader.readLine()) != null) {
//                marker++;
//                if(marker % 100 == 0)
//                    System.out.println(String.valueOf(marker) + " triplet samples generated");
//                points[0] = Utils.getValuesFromLine(lines[0], " ", dataType);
//                positiveReader = new BufferedReader(new FileReader(new File(files[i])));
//                while((lines[1] = positiveReader.readLine()) != null) {
//                    if(random.nextDouble() > Math.sqrt(trainRatio))
//                        continue;
//                    points[1] = Utils.getValuesFromLine(lines[1], " ", dataType);
//                    for(int j = i + 1; j < files.length; j++) {
//
//                        negativeReader = new BufferedReader(new FileReader(new File(files[j])));
//                        while((lines[2] = negativeReader.readLine()) != null) {
//                            if(random.nextDouble() > Math.sqrt(trainRatio))
//                                continue;
//                            points[2] = Utils.getValuesFromLine(lines[2], " ", dataType);
//                            //double simAP = Utils.Utils.computeSimilarity(points[0], points[1], maxDist, "Euclidean", "staircase");
//                            //double simAN = Utils.Utils.computeSimilarity(points[0], points[2], maxDist, "Euclidean", "staircase");
//                            double simAP = Utils.computeEuclideanDist(points[0], points[1]);
//                            double simAN = Utils.computeEuclideanDist(points[0], points[2]);
//                            if(simAP < simAN) continue;
//                            writer.write(lines[0] + "," + lines[1] + "," + lines[2] + "," + String.valueOf(simAP - simAN) + "\n");
//                        }
//                        negativeReader.close();
//                    }
//                }
//                positiveReader.close();
//            }
//            anchorReader.close();
//        }
//        writer.flush();
//        writer.close();
//    }
}

class SiameseDataThread <T> extends Thread  {
    private int threadID;
    private int fileSize;
    private String path;
    private String[] files;
    private double trainRatio;
    private String dataType;

    public SiameseDataThread(int threadID, String path, String[] files, double sampleRatio, String dataType, int fileSize) {
        this.fileSize = fileSize;
        this.threadID = threadID;
        this.path = path;
        this.files = files;
        this.trainRatio = sampleRatio;
        this.dataType = dataType;
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
        BufferedReader p1Reader, p2Reader, thresholdReader;
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(new File(path + "/trainingData-" + String.valueOf(threadID) + ".csv")));
        //BufferedWriter testWriter = new BufferedWriter(new FileWriter(new File(path + "/validationData-" + String.valueOf(threadID) + ".csv")));
        //format: P1, P2, distance, kNN distance of P1
        trainWriter.write("P1,P2,distance,cutoff\n");
        //testWriter.write("P1,P2,distance,cutoff\n");
        String[] lines = new String[2];
        Object[][] points = new Object[2][];

        //iterates each file
        double marker = 0;
        String file = files[threadID];
        p1Reader = new BufferedReader(new FileReader(new File(file)));
        thresholdReader = new BufferedReader(new FileReader(new File(file + "-threshold")));
        String threshold;
        while((lines[0] = p1Reader.readLine()) != null &&
                (threshold = thresholdReader.readLine()) != null) {
            marker++;
            if(marker % 100 == 0 && threadID == 0) {
                System.out.println(String.valueOf(Math.round(marker / fileSize * 100)) + "% points processed...");
            }
            if(random.nextDouble() < 0.8) continue;
            points[0] = Utils.getValuesFromLine(lines[0], " ", dataType);
            double thres = Double.parseDouble(threshold);
            for(int j = threadID; j < files.length; j++) {
                p2Reader = new BufferedReader(new FileReader(new File(files[j])));
                while((lines[1] = p2Reader.readLine()) != null) {
                    points[1] = Utils.getValuesFromLine(lines[1], " ", dataType);
                    //double sim = Utils.Utils.computeSimilarity(points[0], points[1], maxDist, "Euclidean", "staircase");
                    double dist = Utils.computeEuclideanDist(points[0], points[1]);
                    if(dist==0) continue;
                    if(random.nextDouble() < trainRatio || dist < thres)
                    trainWriter.write(lines[0] + "," + lines[1] + "," + String.valueOf(dist) + "," + threshold + "\n");
                    //if(random.nextDouble() < testRatio || dist < thres)
                    //testWriter.write(lines[0] + "," + lines[1] + "," + String.valueOf(dist) + "," + threshold + "\n");
                }
                p2Reader.close();
            }
        }
        p1Reader.close();
        trainWriter.flush(); trainWriter.close();
        //testWriter.flush(); testWriter.close();
    }
}

class TripletDataThread <T> extends Thread  {
    private int threadID;
    private int fileSize;
    private String path;
    private String[] files;
    private double trainRatio;
    private String dataType;

    public TripletDataThread(int threadID, String path, String[] files, double sampleRatio, String dataType, int fileSize) {
        this.fileSize = fileSize;
        this.threadID = threadID;
        this.path = path;
        this.files = files;
        this.trainRatio = sampleRatio;
        this.dataType = dataType;
    }

    public void run() {
        try {
            generateTripletSamples();
            //root.print();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    void generateTripletSamples() throws IOException {
        Random random = new Random();
        BufferedReader p1Reader, p2Reader;
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(new File(path + "/trainingData-" + String.valueOf(threadID) + ".csv")));
        //BufferedWriter testWriter = new BufferedWriter(new FileWriter(new File(path + "/validationData-" + String.valueOf(threadID) + ".csv")));
        trainWriter.write("anchor,positive,negative\n");
        //testWriter.write("P1,P2,distance,cutoff\n");
        String[] lines = new String[2];
        Object[][] points = new Object[2][];

        //iterates each file
        double marker = 0;
        String file = files[threadID];
        p1Reader = new BufferedReader(new FileReader(new File(file)));
        while((lines[0] = p1Reader.readLine()) != null) {
            marker++;
            if(marker % 100 == 0 && threadID == 0) {
                System.out.println(String.valueOf(Math.round(marker / fileSize * 100)) + "% points processed...");
            }
            if(random.nextDouble() < 0.9) continue;
            points[0] = Utils.getValuesFromLine(lines[0], " ", dataType);
            PriorityQueue rankQueue = new PriorityQueue(51,"ascending");
            for(int j = threadID; j < files.length; j++) {
                p2Reader = new BufferedReader(new FileReader(new File(files[j])));
                while((lines[1] = p2Reader.readLine()) != null) {
                    points[1] = Utils.getValuesFromLine(lines[1], " ", dataType);
                    //double sim = Utils.Utils.computeSimilarity(points[0], points[1], maxDist, "Euclidean", "staircase");
                    double dist = Utils.computeEuclideanDist(points[0], points[1]);
                    if(dist==0) continue;
                    rankQueue.insert(dist, lines[1]);
                }
                p2Reader.close();
            }
            List<String> kNNs = rankQueue.serialize();
            for(int j = 0; j < kNNs.size(); j++) {
                for(int m = j + 1; m < kNNs.size(); m++) {
                    trainWriter.write(lines[0] + "," + kNNs.get(j) + "," + kNNs.get(m) + "\n");
                }
                //trainWriter.write(lines[0] + "," + kNNs.get(j) + "," + kNNs.get(j+1) + "\n");
            }
        }
        p1Reader.close();
        trainWriter.flush(); trainWriter.close();
        //testWriter.flush(); testWriter.close();
    }
}
