import Utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class UniformData {

    private int numDims;
    private String[] files;
    private int fileSize;
    private int numThreads;

    UniformData(String[] files, int fileSize, int numDims) {
        this.files = files;
        this.fileSize = fileSize;
        this.numDims = numDims;
        this.numThreads = files.length;
    }

    void generateData() throws IOException {
        BufferedWriter bw = null;
        for(int i = 0; i < files.length; i++) {
            bw = new BufferedWriter(new FileWriter(new File(files[i])));
            int[][] points = Utils.getUniformPoints(fileSize, numDims);
            writeVectors(bw, points, fileSize, numDims);
            bw.flush();
            bw.close();
        }
    }

    static void writeVectors(BufferedWriter bw, int[][] points, int fileSize, int numDims) throws IOException {
        for(int j = 0; j < fileSize; j++) {
            for(int k = 0; k < numDims - 1; k++) {
                bw.write(String.valueOf(points[j][k]) + " ");
            }
            bw.write(String.valueOf(points[j][numDims -1]));
            bw.write("\r\n");
        }
    }

}
