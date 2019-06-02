package LSH;

import java.io.*;

public class LSHTest {
    public static void main(String[] args) throws IOException {
        int numPoints = 25000;
        int inputDim = 128;
        int embeddedDim = 20;
        int numThreads = 8;

        LSH lsh = new LSH(numPoints, inputDim, embeddedDim);

        for(int fileID = 0; fileID < numThreads; fileID++) {
            String path = "data/thread-" + String.valueOf(fileID);
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            String line;
            while((line = reader.readLine()) != null) {
                lsh.add(line);
            }
        }
        int fileSize = numPoints / numThreads;
        int count = 0;
        for(int fileID = 0; fileID < numThreads; fileID++) {
            String path = "data/hash-reducedVectors-" + String.valueOf(fileID);
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
            for(int i = 0; i < fileSize; i++) {
                boolean[] temp = lsh.points[count];
                for(boolean b : temp) {
                    writer.write((b? 1 : 0) + " ");
                }
                count++;
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }

//        String path = "data/thread-0";
//        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
//        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data/lsh")));
//        double startTime = System.currentTimeMillis();
//        String line;
//        int count = 0;
//        while ((line = reader.readLine()) != null) {
//            Object[] results = lsh.query(line, 50);
//            //System.out.print(idx + ": ");
//            count++;
//            for(Object o : results) {
//                writer.write(String.valueOf(o) + ",");
//            }
//            writer.write("\n");
//        }
//        double endTime = System.currentTimeMillis();
//        double totalTime = endTime - startTime;
//        System.out.println(totalTime/count);
    }
}
