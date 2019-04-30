import java.io.*;
import java.util.Random;

class TripletData {
    private String path;
    private String[] files;
    private double maxDist;
    private double sampleRatio;
    TripletData(String path, String[] files, double maxDist, double sampleRatio) {
        this.path = path;
        this.files = files;
        this.maxDist = maxDist;
        this.sampleRatio = sampleRatio;
    }
    void generateSamples() throws IOException {
        Random random = new Random();
        BufferedReader anchorReader, positiveReader, negativeReader;
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "/TripletData.csv")));
        writer.write("anchor,positive,negative,diff\n");
        String[] lines = new String[3];//anchor, positive, negative
        double[][] points = new double[3][];
        //iterates each file
        for(int i = 0; i < files.length; i++) {
            anchorReader = new BufferedReader(new FileReader(new File(path + "/" + files[i])));
            int marker = 0;
            while((lines[0] = anchorReader.readLine()) != null) {
                marker++;
                if(marker % 100 == 0)
                    System.out.println(marker);
                points[0] = Utils.getDoubles(lines[0], " ");
                positiveReader = new BufferedReader(new FileReader(new File(path + "/" + files[i])));
                while((lines[1] = positiveReader.readLine()) != null) {
                    if(random.nextDouble() > Math.sqrt(sampleRatio))
                        continue;
                    points[1] = Utils.getDoubles(lines[1], " ");
                    for(int j = i + 1; j < files.length; j++) {

                        negativeReader = new BufferedReader(new FileReader(new File(path + "/" + files[j])));
                        while((lines[2] = negativeReader.readLine()) != null) {
                            if(random.nextDouble() > Math.sqrt(sampleRatio))
                                continue;
                            points[2] = Utils.getDoubles(lines[2], " ");
                            double simAP = Utils.computeSimilarity(points[0], points[1], maxDist, "Euclidean", "staircase");
                            double simAN = Utils.computeSimilarity(points[0], points[2], maxDist, "Euclidean", "staircase");
                            if(simAP < simAN) continue;
                            writer.write(lines[0] + "," + lines[1] + "," + lines[2] + "," + String.valueOf(simAP - simAN) + "\n");
                        }
                        negativeReader.close();
                    }
                }
                positiveReader.close();
            }
            anchorReader.close();
        }
        writer.flush();
        writer.close();
    }
}
