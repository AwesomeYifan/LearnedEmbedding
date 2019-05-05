import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;

public class Utils {

    static double[][] getGaussianPoints(int numPoints, double centers[], double deviation) {
        int numDims = centers.length;
        double deviationEachAxis = Math.sqrt(Math.pow(deviation, 2)/numDims);
        double[][] results = new double[numPoints][numDims];
        Random random = new Random();
        for(int i = 0; i < numPoints; i++) {
            for(int j = 0; j < numDims; j++) {

                do results[i][j] = random.nextGaussian() * deviationEachAxis + centers[j]; while(results[i][j] < 0 || results[i][j] >1);
            }
        }
        return results;
    }
    static double[][] getUniformPoints(int numPoints, int dim) {
        double[][] results = new double[numPoints][dim];
        Random random = new Random();
        for(int i = 0; i < numPoints; i++) {
            for(int j = 0; j < dim; j++) {
                results[i][j] = random.nextDouble();
            }
        }
        return results;
    }

    static double computeEuclideanDist(Object[] vec1, Object[] vec2) {
        double sumDist = 0;
        for(int i = 0; i < vec1.length; i++) {
            sumDist += Math.pow((Double)vec1[i] - (Double)vec2[i], 2);
        }
        return Math.sqrt(sumDist);
    }

    static double computeSimilarity(Object[] vec1, Object[] vec2, double maxDist, String distOpt, String scaleOpt) {
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

    static Object[] getValuesFromLine(String line, String separator, String opt) {
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
