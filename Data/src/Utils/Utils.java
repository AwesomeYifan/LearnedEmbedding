package Utils;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.Random;

public class Utils {

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
    public static double[][] getUniformPoints(int numPoints, int dim) {
        DecimalFormat f = new DecimalFormat("##.0000");
        double[][] results = new double[numPoints][dim];
        Random random = new Random();
        for(int i = 0; i < numPoints; i++) {
            for(int j = 0; j < dim; j++) {
                results[i][j] = Math.round((random.nextDouble() * 10000.0))/ 10000.0;
                //results[i][j] = random.nextDouble();
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
    public static int splitFile(String sourcePath, String targetPath, int numThreads) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(sourcePath)));
        BufferedWriter[] bws = new BufferedWriter[numThreads];
        for(int i = 0; i < numThreads; i++) {
            bws[i] = new BufferedWriter(new FileWriter(new File(targetPath + "\\thread-" + String.valueOf(i))));
        }
        String[] lines = new String[numThreads];
        int fileSize = 0;
        while(true) {
            for(int i = 0; i < numThreads; i++) {
                String line = br.readLine();
                if(line == null) {
                    for(int j = 0; j < numThreads; j++) {
                        bws[j].flush();
                        bws[j].close();
                    }
                    return fileSize;
                }
                lines[i] = line;
            }
            for(int i = 0; i < numThreads; i++) {
                bws[i].write(lines[i].replace(",", " ") + "\n");
            }
            fileSize++;
//            if(fileSize == 1000){
//                for(int j = 0; j < numThreads; j++) {
//                    bws[j].flush();
//                    bws[j].close();
//                }
//                return fileSize;
//            }
        }
    }

    public static Object[] getValuesFromLine(String line, String separator, String opt) {
        String[] record = line.split(separator);
        Object[] result = new Object[record.length];
        int idx = 0;
        switch (opt) {
            case "Double":
            case "double": {
                for(String s : record) {
                    result[idx] = Double.valueOf(s);
                    idx ++;
                }
                break;
            }
            case "Integer":
            case "integer":{
                for(String s : record) {
                    result[idx] = Integer.valueOf(s);
                    idx ++;
                }
                break;
            }
            case "String":
            case "string":{
                result = record;
                break;
            }
        }
        return result;
    }
}
