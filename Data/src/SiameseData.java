import java.io.*;
import java.util.Random;

class SiameseData {
    private String path;
    private String[] files;
    private double maxDist;
    private double sampleRatio;
    private String dataType;

    SiameseData(String path, String[] files, double maxDist, double sampleRatio, String dataType) {
        this.path = path;
        this.files = files;
        this.maxDist = maxDist;
        this.sampleRatio = sampleRatio;
        this.dataType = dataType;
    }
    void generateSamples() throws IOException {
        Random random = new Random();
        BufferedReader p1Reader, p2Reader;
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "/SiameseData.csv")));
        writer.write("P1,P2,similarity\n");
        String[] lines = new String[2];
        Object[][] points = new Object[2][];

        //iterates each file
        for(int i = 0; i < files.length; i++) {
            p1Reader = new BufferedReader(new FileReader(new File(path + "/" + files[i])));
            int marker = 0;
            while((lines[0] = p1Reader.readLine()) != null) {
                marker++;
                if(marker % 100 == 0)
                    System.out.println(String.valueOf(marker) + " siamese samples generated");
                points[0] = Utils.getValuesFromLine(lines[0], " ", dataType);
                for(int j = i + 1; j < files.length; j++) {
                    p2Reader = new BufferedReader(new FileReader(new File(path + "/" + files[j])));
                    while((lines[1] = p2Reader.readLine()) != null) {
                        if(random.nextDouble() > Math.sqrt(sampleRatio))
                            continue;
                        points[1] = Utils.getValuesFromLine(lines[1], " ", dataType);
                        //double sim = Utils.computeSimilarity(points[0], points[1], maxDist, "Euclidean", "staircase");
                        double sim = Utils.computeEuclideanDist(points[0], points[1]);
                        writer.write(lines[0] + "," + lines[1] + "," + String.valueOf(sim) + "\n");
                    }
                    p2Reader.close();
                }
            }
            p1Reader.close();
        }
        writer.flush();
        writer.close();
    }
}
