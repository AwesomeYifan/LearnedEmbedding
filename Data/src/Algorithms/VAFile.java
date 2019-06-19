package Algorithms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class VAFile {
    double[][] axisRanges; // the maximum and minimum of each axis
    byte[][] VA; // the vector approximation of each point
    int numBytes; //number of bits per axis, <=4
    int numPoints;
    int numDims;

    public VAFile(String pathToFile, int numBytes) throws IOException {
        int[] metaInfo = getMetaInfo(pathToFile);
        this.numPoints = metaInfo[0];
        this.numDims = metaInfo[1];
        axisRanges = getAxisRanges(pathToFile, numDims);

        this.numBytes = numBytes;
        this.VA = new byte[numPoints][numBytes*numDims];
        buildVA(pathToFile);
        //System.out.println(VA);
    }

    //compute the distance between two tiles: LB and UB
    private double[] computeDist(byte[] query, byte[] cand) {
        double[] distance= new double[2];
        for(int i = 0; i < numDims; i++) {
            long tempDist = 0;
            boolean findDiff = false;
            for(int j = 0; j < numBytes; j++) {
                byte temp = (byte)(query[j] - cand[j]);
                tempDist += Long.parseLong(Long.toBinaryString(temp << 8*(numBytes - 1 - j)));
            }
            if(tempDist != 0) {
                distance[0] = Math.pow(Math.abs(tempDist) - 1, 2);
                distance[1] = Math.pow(Math.abs(tempDist) + 1, 2);
            }
        }
        return distance;
    }

    private void buildVA(String pathToFile) throws IOException {
        double[] widths = new double[axisRanges.length];
        for(int i = 0; i < widths.length; i++) {
            widths[i] = getWidth(axisRanges[i][0], axisRanges[i][1], Math.pow(2, 8 * numBytes));
        }
        //System.out.println(Arrays.toString(widths));
        BufferedReader br = new BufferedReader(new FileReader(new File(pathToFile)));
        String line;
        int id = 0;
        while((line = br.readLine()) != null) {
            String[] record = line.split(" ");
            double[] vector = new double[record.length];
            for(int i = 0; i < record.length; i++) {
                vector[i] = Double.parseDouble(record[i]);
            }
            VA[id] = getVA(vector, widths);
        }

    }
    private byte[] getVA(double[] vectors, double[] widths) {
        byte[] result = new byte[numDims * numBytes];
        for(int i = 0; i < vectors.length; i++) {
            long binID = Math.round((vectors[i] - axisRanges[i][1]) / widths[i]);
            //System.out.println(binID);
            ByteBuffer buffer1 = ByteBuffer.allocate(Long.BYTES);
            buffer1.putLong(binID);
            byte[] tempVA = buffer1.array();
            for(int j = 0; j < numBytes; j++) {
                result[i * numBytes + j] = tempVA[tempVA.length - numBytes + j];
            }
        }
        return result;
    }
    private double getWidth(double max, double min, double bins) {
        return (max - min) / bins;
    }

    private int[] getMetaInfo(String pathToFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(pathToFile)));
        int numDims = br.readLine().split(" ").length;

        int numPoints = 0;
        br = new BufferedReader(new FileReader(new File(pathToFile)));
        while(br.readLine() != null) {
            numPoints++;
        }
        System.out.println("number of points: " + String.valueOf(numPoints));
        System.out.println("dimensionality: " + String.valueOf(numDims));
        return new int[] {numPoints, numDims};
    }

    //return the number of points and the dims
    private double[][] getAxisRanges(String pathToFile, int numDims) throws IOException {
        double[][] axisRanges = new double[numDims][2];
        BufferedReader br = new BufferedReader(new FileReader(new File(pathToFile)));
        String line;
        boolean flag = false;
        while((line = br.readLine()) != null) {
            String[] record = line.split(" ");
            if(!flag) {
                for(int i = 0; i < axisRanges.length; i++) {
                    axisRanges[i][0] = Double.parseDouble(record[i]);
                    axisRanges[i][1] = Double.parseDouble(record[i]);
                }
                flag = true;
            }
            else {
                for(int i = 0; i < axisRanges.length; i++) {
                    axisRanges[i][0] = Math.max(Double.parseDouble(record[i]), axisRanges[i][0]);
                    axisRanges[i][1] = Math.min(Double.parseDouble(record[i]), axisRanges[i][1]);
                }
            }
        }
        return axisRanges;
    }
}
