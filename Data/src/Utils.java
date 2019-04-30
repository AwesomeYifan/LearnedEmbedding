import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

public class Utils {
    static int topK = 100;
    static int numEntities = 20;
    static int outputSize = 10;

    public static double[][] getGaussianPoints(int numPoints, double centers[], double deviation) {
        int numDims = centers.length;
        double deviationEachAxis = Math.sqrt(Math.pow(deviation, 2)/numDims);
        double[][] results = new double[numPoints][numDims];
        Random random = new Random();
        for(int i = 0; i < numPoints; i++) {
            for(int j = 0; j < numDims; j++) {

                do {
                    results[i][j] = random.nextGaussian() * deviationEachAxis + centers[j];
                } while(results[i][j] < 0 || results[i][j] >1);
            }
        }
        return results;
    }
    public static double[][] getUniformPoints(int numPoints, int dim) {
        double[][] results = new double[numPoints][dim];
        Random random = new Random();
        for(int i = 0; i < numPoints; i++) {
            for(int j = 0; j < dim; j++) {
                results[i][j] = random.nextDouble();
            }
        }
        return results;
    }
    public static String[] getFileNames(int numFiles) {
        String[] files = new String[numFiles];
        for(int i = 0; i < numFiles; i++) {
            files[i] = "class-" + String.valueOf(i) + ".csv";
        }
        return files;
    }
    public static double getMaxDist(int dim, String opt) {
        switch (opt) {
            case "Euclidean": {
                return Math.sqrt(dim);
            }
            default: {
                return Math.sqrt(dim);
            }
        }
    }
    public static double computeSimilarity(double[] vec1, double[] vec2, double maxDist, String distOpt, String scaleOpt) {
        double sim = 0;
        switch (distOpt) {
            case "Euclidean": {
                double sumDist = 0;
                for(int i = 0; i < vec1.length; i++) {
                    sumDist += Math.pow(vec1[i] - vec2[i], 2);
                }
                sim = (maxDist - Math.sqrt(sumDist)) / maxDist;
                break;
            }
            case "Manhattan": {
                double sumDist = 0;
                for(int i = 0; i < vec1.length; i++) {
                    sumDist += Math.abs(vec1[i] - vec2[i]);
                }
                sim = (maxDist - sumDist)/maxDist;
                break;
            }
            default: {
                sim = 0.0;
            }
        }

        switch (scaleOpt) {
            case "square": {
                sim = Math.pow(sim, 2);
                break;
            }
            case "stair": {
                BigDecimal bd = new BigDecimal(sim);
                bd = bd.round(new MathContext(3));
                //return bd.doubleValue();
                sim = Math.pow(bd.doubleValue(),2);
                break;
            }
            default: {
                sim = sim;
            }
        }
        return sim;
    }

    public static double[] getDoubles(String line, String separator) {
        String[] record = line.split(separator);
        double[] result = new double[record.length];
        int idx = 0;
        for(String s : record) {
            result[idx] = Double.valueOf(s);
            idx ++;
        }
        return result;
    }
}
