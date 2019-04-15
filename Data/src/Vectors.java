import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/**
* Keep 2 decimals for all vectors and distances to save space
*/

public class Vectors {
    private int dim = 2;
    private int numPoints = 200;
    private int axisLength = 1;
    private double trainRatio = 0.1;

    public static void main(String[] args) throws IOException, InterruptedException {
        Vectors vec = new Vectors();
        vec.generate();
    }

    public void generate() throws IOException {
        //generate initial vectors
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./data/temp.csv")));
        Random random = new Random();
        for(int i = 0; i < numPoints; i++) {
            for(int j = 0; j < dim; j++) {
                //bw.write(String.valueOf(Math.round(random.nextDouble() * axisLength * 100.0) / 100.0));
                bw.write(String.valueOf(random.nextDouble() * axisLength));
                if(j != dim - 1)
                    bw.write(" ");
                else
                    bw.write("\r\n");
            }
        }
        bw.close();
        this.createTrain();
    }

    public void createTrain() throws IOException {
        Random random = new Random();
        BufferedReader br1 = new BufferedReader(new FileReader(new File("./data/temp.csv")));
        BufferedReader br2 = new BufferedReader(new FileReader(new File("./data/temp.csv")));
        BufferedWriter bwOrigin = new BufferedWriter(new FileWriter(new File("data/origin.csv")));
        BufferedWriter bwTrain = new BufferedWriter(new FileWriter(new File("data/train.csv")));
        BufferedWriter bwRank = new BufferedWriter(new FileWriter(new File("./data/rank.csv")));
        bwOrigin.write("P1,P2\n");
        bwTrain.write("P1,P2,dist\n");
        String line1, line2;
        double[] vec1, vec2;
        int[] records = new int[11];
        double oriSim, transformedSim;
        TreeMap<Double, Set<Integer>> rankList = new TreeMap<>();
        while((line1 = br1.readLine()) != null) {
            bwOrigin.write(line1 + "," + line1 + "\n");
            vec1 = this.transform(line1, " ");
            int idx = 0;
            while((line2 = br2.readLine()) != null) {
                vec2 = this.transform(line2, " ");
                oriSim = this.computeSimilarity(vec1, vec2, "Euclidean");
//                if(oriSim == 1.0)
//                    continue;
                records[(int) Math.round(oriSim * 10)]++;
                //build ranking list: ordered by
                Utils.updatePriorityQueue(rankList, oriSim, idx);
                idx ++;
                //build training data: vec1#vec2#expected distance in the reduced space
                transformedSim = this.scale(oriSim, "origin");
                if(random.nextDouble() < trainRatio) {
                    bwTrain.write(line1 + "," + line2 + "," +
                            //String.valueOf(Math.round(transformedSim * 100.0) / 100.0) + "\n");
                            String.valueOf(transformedSim) + "\n");
                }
            }
            Utils.writeDescending(bwRank, rankList);
            rankList.clear();
            br2.close();
            br2 = new BufferedReader(new FileReader(new File("./data/temp.csv")));
        }
        br1.close();
        br2.close();
        bwOrigin.flush();bwOrigin.close();
        bwTrain.flush();bwTrain.close();
        bwRank.flush();bwRank.close();
        System.out.println(Arrays.toString(records));
    }

    private double[] transform(String line, String separator) {
        String[] record = line.split(separator);
        double[] result = new double[record.length];
        int idx = 0;
        for(String s : record) {
            result[idx] = Double.valueOf(s);
            idx ++;
        }
        return result;
    }

    private double computeSimilarity(double[] vec1, double[] vec2, String opt) {
        switch (opt) {
            case "Euclidean": {
                double sumDist = 0;
                double maxDist = Math.sqrt(dim);
                for(int i = 0; i < vec1.length; i++) {
                    sumDist += Math.pow(vec1[i] - vec2[i], 2);
                }
                return (maxDist - Math.sqrt(sumDist)) / maxDist;
            }
            case "Manhattan": {
                double sumDist = 0;
                double maxDist = dim;
                for(int i = 0; i < vec1.length; i++) {
                    sumDist += Math.abs(vec1[i] - vec2[i]);
                }
                return (maxDist - sumDist)/maxDist;
            }
            default:
                return 0.0;
        }
    }

    private double scale(double oldValue, String opt) {
        switch (opt) {
            case "square": {
                return Math.pow(oldValue, 2);
            }
            case "stair": {
                return stairTransform(oldValue);
            }
            default:
                return oldValue;
        }
    }
    private double stairTransform(double v) {
        BigDecimal bd = new BigDecimal(v);
        bd = bd.round(new MathContext(3));
        //return bd.doubleValue();
        return Math.pow(bd.doubleValue(),2);
    }
}
