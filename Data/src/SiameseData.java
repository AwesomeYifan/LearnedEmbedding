import java.io.*;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class SiameseData {
    String path;
    String[] files;
    double maxDist;
    double sampleRatio;
    public SiameseData(String path, String[] files, double maxDist, double sampleRatio) {
        this.path = path;
        this.files = files;
        this.maxDist = maxDist;
        this.sampleRatio = sampleRatio;
    }
    public void generateSamples() throws IOException {
        Random random = new Random();
        BufferedReader p1Reader, p2Reader;
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "/SiameseData.csv")));
        writer.write("P1,P2,dist\n");
        String[] lines = new String[2];
        double[][] points = new double[2][];

        //iterates each file
        for(int i = 0; i < files.length; i++) {
            p1Reader = new BufferedReader(new FileReader(new File(path + "/" + files[i])));
            int marker = 0;
            while((lines[0] = p1Reader.readLine()) != null) {
                marker++;
                if(marker % 100 == 0)
                    System.out.println(marker);
                points[0] = Utils.getDoubles(lines[0], " ");
                for(int j = i + 1; j < files.length; j++) {
                    p2Reader = new BufferedReader(new FileReader(new File(path + "/" + files[j])));
                    while((lines[1] = p2Reader.readLine()) != null) {
                        if(random.nextDouble() > Math.sqrt(sampleRatio))
                            continue;
                        points[1] = Utils.getDoubles(lines[1], " ");
                        double sim = Utils.computeSimilarity(points[0], points[1], maxDist, "Euclidean", "staircase");
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
