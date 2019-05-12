import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class UniformData {

    private int numDims;
    private String[] files;
    private int fileSize;
    private int numThreads;

    UniformData(String[] files, int fileSize, int numDims, int numThreads) {
        this.files = files;
        this.fileSize = fileSize;
        this.numDims = numDims;
        this.numThreads = numThreads;
    }

    void generateData() throws IOException {
        BufferedWriter bw = null;
        for(int i = 0; i < files.length; i++) {
            bw = new BufferedWriter(new FileWriter(new File(files[i])));
            double[][] points = Utils.getUniformPoints(fileSize, numDims);
            GaussianData.writeVectors(bw, points, fileSize, numDims);
            bw.flush();
            bw.close();
        }
    }

}
