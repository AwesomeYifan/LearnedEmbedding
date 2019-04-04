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
    private int numPoints = 20;
    private int axisLength = 1;

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
                bw.write(String.valueOf(Math.round(random.nextDouble() * axisLength * 100.0) / 100.0));
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
        BufferedReader br1 = new BufferedReader(new FileReader(new File("./data/temp.csv")));
        BufferedReader br2 = new BufferedReader(new FileReader(new File("./data/temp.csv")));
        BufferedWriter bwTrain = new BufferedWriter(new FileWriter(new File("data/train.csv")));
        BufferedWriter bwTest = new BufferedWriter(new FileWriter(new File("./data/test.csv")));
        bwTrain.write("P1,P2,dist\n");
        String line1, line2;
        double[] vec1, vec2;
        int[] records = new int[10];
        double oriDist, transformedDist;
        TreeMap<Double, Set<Integer>> rankList = new TreeMap<>();
        while((line1 = br1.readLine()) != null) {
            vec1 = this.transform(line1, " ");
            int idx = 0;
            while((line2 = br2.readLine()) != null) {
                vec2 = this.transform(line2, " ");
                oriDist = this.computeDistance(vec1, vec2, "Euclidean");
                records[(int) Math.round(oriDist)]++;
                //build ranking list: ordered by
                Utils.updatePriorityQueue(rankList, oriDist, idx);
                idx ++;
                //build training data: vec1#vec2#expected distance in the reduced space
                transformedDist = this.scale(oriDist, "square");
                if(transformedDist == 0.0)
                    continue;
                else {
                    bwTrain.write(line1 + "," + line2 + "," +
                            String.valueOf(Math.round(transformedDist * 100.0) / 100.0) + "\n");
                }
            }
            Utils.writeAscending(bwTest, rankList);
            rankList.clear();
            br2.close();
            br2 = new BufferedReader(new FileReader(new File("./data/temp.csv")));
        }
        br1.close();
        br2.close();
        bwTrain.close();
        bwTest.close();
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

    private double computeDistance(double[] vec1, double[] vec2, String opt) {
        switch (opt) {
            case "Euclidean": {
                double sum = 0;
                for(int i = 0; i < vec1.length; i++) {
                    sum += Math.pow(vec1[i] - vec2[i], 2);
                }
                return Math.sqrt(sum);
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
        return bd.doubleValue();
    }
}
