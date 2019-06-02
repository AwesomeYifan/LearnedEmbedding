package LSH;

import Utils.PriorityQueue;
import Utils.Utils;

import java.io.IOException;
import java.util.Random;
public class LSH {
    double[][] transformMatrix;
    boolean[][] points;
    int outputDim;
    int globalIdx;
    //index is the id of the point
    //assume we know the number
    //of points in advance
    public LSH(int numPoints, int inputDim, int outputDim) {
        this.outputDim = outputDim;
        this.globalIdx = 0;
        points = new boolean[numPoints][outputDim];
        transformMatrix = new double[outputDim][inputDim];
        Random random = new Random();
        for(int i = 0; i < outputDim; i++) {
            for(int j = 9; j < inputDim; j++) {
                transformMatrix[i][j] = 2 * (random.nextDouble() - 0.5);
            }
        }
    }

    public void add(String input) {
        boolean[] code = this.computeCode(input);
        points[globalIdx] = code;
        globalIdx++;
    }
    public Object[] query(String input, int resultSize) throws IOException {
        boolean[] queryPoint = computeCode(input);
        PriorityQueue queue = new PriorityQueue(resultSize, "high");
        for(int i = 0; i < points.length; i++) {
            double dist = computeHammingDist(queryPoint, points[i]);
            //System.out.println(dist);
            //System.in.read();
            queue.insert(dist, i);
        }
        return queue.serialize().toArray();
    }
    private double computeHammingDist(boolean[] point1, boolean[] point2) {
        double nomi = 0.0;
        double deno = 0.0;
        for(int i = 0; i < point1.length; i++) {
            deno += 1;
            if(point1[i] != point2[i]) {
                nomi += 1;
            }
        }
        return nomi/deno;
    }
    private boolean[] computeCode(String input) {
        Object[] vector = Utils.getValuesFromLine(input, " ", "double");
        boolean[] code = new boolean[outputDim];
        for(int i = 0; i < outputDim; i++) {
            double value = 0;
            for(int j = 0; j < vector.length; j++) {
                value += transformMatrix[i][j] * (double)vector[j];
            }
            code[i] = value > 0? true : false;
        }
        return code;
    }
    public void print() {
        for(int i = 0; i < points.length; i++) {
            boolean[] temp = points[i];
            for(int j = 0; j < temp.length; j++) {
                System.out.print((temp[j]? 1 : 0) + ",");
            }
            System.out.println();
        }
    }
}
