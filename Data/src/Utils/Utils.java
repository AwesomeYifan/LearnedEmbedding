package Utils;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.Buffer;
import java.text.DecimalFormat;
import java.util.Random;

public class Utils {
    public static double[][] getGaussianPoints(int numPoints, int numClusters, int numDims) {
        int numBinsPerAxis = (int)Math.ceil(Math.pow(numClusters, 1.0/numDims));
        double binWidth = 10;//one standard deviation each side
        Random random = new Random();
        double[][] centers = new double[numClusters][numDims];
        for(int i = 0; i < numClusters; i++) {
            for(int j = 0; j < numDims; j++) {
                centers[i][j] = random.nextInt(numBinsPerAxis) * binWidth;
            }
        }
        double[][] results = new double[numPoints][numDims];
        for(int i = 0; i < numPoints; i++) {
            int centerID = random.nextInt(numClusters);
            for(int j = 0; j < numDims; j++) {
                double center = centers[centerID][j];
                double value = random.nextGaussian() + center;
                results[i][j] = value;
            }
        }
        return results;
    }

    public static double[][] getGaussianPoints(int numPoints, double centers[], double deviation) {
        int numDims = centers.length;
        double deviationEachAxis = Math.sqrt(Math.pow(deviation, 2)/numDims);
        double[][] results = new double[numPoints][numDims];
        Random random = new Random();
        for(int i = 0; i < numPoints; i++) {
            for(int j = 0; j < numDims; j++) {
                do results[i][j] = random.nextGaussian() * deviationEachAxis + centers[j]; while(results[i][j] < 0 || results[i][j] >1);
                results[i][j] = Math.round(results[i][j] * 10000.0) / 10000.0;
            }
        }
        return results;
    }
    public static int[][] getUniformPoints(int numPoints, int dim) {
        int[][] results = new int[numPoints][dim];
        Random random = new Random();
        for(int i = 0; i < numPoints; i++) {
            for(int j = 0; j < dim; j++) {
                results[i][j] = (int)Math.round((random.nextDouble() * 100.0));
            }
        }
        return results;
    }

    public static double computeEuclideanDist(Object[] vec1, Object[] vec2) {
        double sumDist = 0;
        for(int i = 0; i < vec1.length; i++) {
            sumDist += Math.pow((Double)vec1[i] - (Double)vec2[i], 2);
        }
        return Math.sqrt(sumDist);
    }



    public static double computeEuclideanDist(double[] vec1, double[] vec2) {
        double sumDist = 0;
        for(int i = 0; i < vec1.length; i++) {
            sumDist += Math.pow(vec1[i] - vec2[i], 2);
        }
        return Math.sqrt(sumDist);
    }

    public static double computeEuclideanDist(int[] vec1, int[] vec2) {
        double sumDist = 0;
        for(int i = 0; i < vec1.length; i++) {
            sumDist += (vec1[i] - vec2[i]) * (vec1[i] - vec2[i]);
            //sumDist += Math.pow(vec1[i] - vec2[i], 2);
        }
        return Math.sqrt(sumDist);
    }

    public static void normalizeData(String file) throws IOException {
        String newFile = file + "-normalized";
        double maxValue = 0;
        double minValue = Double.MAX_VALUE;
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        String line;
        while((line = br.readLine()) != null) {
            String[] record = line.split(" ");
            for(int i = 0; i < record.length; i++) {
                maxValue = Math.max(maxValue, Double.parseDouble(record[i]));
                minValue = Math.min(minValue, Double.parseDouble(record[i]));
            }
        }
        double width = maxValue - minValue;
        br = new BufferedReader(new FileReader(new File(file)));
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(newFile)));
        while((line = br.readLine()) != null) {
            String[] record = line.split(" ");
            double[] data = new double[record.length];
            for(String s : record) {
                bw.write(String.valueOf((Double.parseDouble(s) - minValue) / width) + " ");
            }
            bw.write("\n");
        }
        bw.flush();
        bw.close();
        br.close();
    }

    public static double computeSimilarity(Object[] vec1, Object[] vec2, double maxDist, String distOpt, String scaleOpt) {
        double sim;
        switch (distOpt) {
            case "Euclidean": {
                double sumDist = computeEuclideanDist(vec1, vec2);
                sim = (maxDist - sumDist) / maxDist;
                break;
            }
            case "Manhattan": {
                double sumDist = 0;
                for(int i = 0; i < vec1.length; i++) {
                    sumDist += Math.abs((Double)vec1[i] - (Double)vec2[i]);
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
            default: {}
        }
        return sim;
    }

    //return number of records per file
    public static void splitFile(String sourcePath, String targetPath, int numThreads) throws IOException {
        int numLines = 0;

        BufferedReader br = new BufferedReader(new FileReader(new File(sourcePath)));
        while(br.readLine() != null) {
            numLines++;
        }
        br = new BufferedReader(new FileReader(new File(sourcePath)));
        int numLinesPerFile = numLines / numThreads;
        BufferedWriter[] bws = new BufferedWriter[numThreads];
        for(int i = 0; i < numThreads; i++) {
            bws[i] = new BufferedWriter(new FileWriter(new File(targetPath + "\\thread-" + String.valueOf(i))));
        }
        for(int i = 0; i < numLines; i++) {
            String line = br.readLine().replace(",", " ");
            bws[i / numLinesPerFile].write(line + "\n");
        }
        for(int i = 0; i < numThreads; i++) {
            bws[i].flush();
            bws[i].close();
        }
    }

    public static int getNumLines(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
        int result = 0;
        String line;
        while((line = reader.readLine()) != null)
            result++;
        return result;
    }

    public static double[] getValuesFromLine(String line, String separator) {
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
