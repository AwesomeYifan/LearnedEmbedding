import java.io.*;
import java.util.Arrays;
import java.util.Random;
import Utils.Utils;

public class Cluster {
//    public static void main(String[] args) throws IOException {
//        String file = "./data/mnist_train.csv";
//        double sampleRate = 0.1;
//        Random random = new Random();
//        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
//        String writeTo = "./data/MNIST.csv";
//        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(writeTo)));
//        while(true) {
//            String line = br.readLine();
//            if (line == null)
//                break;
//            if(random.nextDouble() < sampleRate) {
//                bw.write(line + "\r\n");
//            }
//        }
//        bw.flush();
//        bw.close();
//        br.close();
//    }

    //divide by thread
public static void main(String[] args) throws IOException {
    generateGaussian();
    //String file = "./data/MNIST/MNIST.csv";
    String file = "./data/Cluster/clusters.csv";
    double sampleRate = 0.1;
    Random random = new Random();
    BufferedReader br = new BufferedReader(new FileReader(new File(file)));
    String writeTo = "./data/Cluster/thread-";
    BufferedWriter[] bws = new BufferedWriter[8];
    for(int i = 0; i < 8; i++) {
        bws[i] = new BufferedWriter(new FileWriter(new File(writeTo + String.valueOf(i))));
    }
    int count = 0;
    while(true) {
        String line = br.readLine();
        if (line == null)
            break;
        bws[count%8].write(line + "\r\n");
        count++;
    }
    for(int i = 0; i < 8; i++) {
        bws[i].flush();
        bws.clone();
    }
    br.close();
}
    public static void generateGaussian() throws IOException {
        int numPoints = 10000;
        int numClusters = 200;
        int numDim = 50;
        double[][] points = Utils.getGaussianPoints(numPoints, numClusters, numDim);
        String file = "./data/Cluster/clusters.csv";
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
        for(double[] point : points) {
            StringBuilder sb = new StringBuilder();
            for(double p : point) {
                sb.append(String.valueOf(p) + " ");
            }
            bw.write(sb.substring(0, sb.length() - 1) + "\r\n");
        }
        bw.flush();
        bw.close();
    }
}
