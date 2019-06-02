import java.io.*;
import java.util.*;

public class LabeledData {
    public static void main(String[] args) throws IOException {
        double sampleRatioPoints = 0.1;
        double sampleRatioInClass = 0.005;
        double sampleRatioOutsideClass = sampleRatioInClass / 5;
        String readFile = "./data/MNIST/mnist_train.csv";
        String writeFile = "./data/trainingData.csv";
        String writeFileAll = "./data/mnist.csv";
        BufferedReader reader = new BufferedReader(new FileReader(new File(readFile)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(writeFile)));
        BufferedWriter bwOfTest = new BufferedWriter(new FileWriter(new File("./data/mnist-test")));
        BufferedWriter writerAll = new BufferedWriter(new FileWriter(new File(writeFileAll)));
        String line1, line2;
        writer.write("P1,P2,label1,label2\n");
        Random random = new Random();
        List<List<float[]>> points = new ArrayList();
        List<Set<Integer>> pointsInEachClass = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            points.add(new ArrayList<>());
            pointsInEachClass.add(new HashSet<>());
        }
        int idx = 0;
        while((line1 = reader.readLine()) != null) {
            String[] record1 = line1.split(",");
            int label1 = Integer.parseInt(record1[0]);
            pointsInEachClass.get(label1).add(idx);
            record1 = Arrays.copyOfRange(record1, 1, record1.length);
            float[] data1 = toFloat(record1);
            points.get(label1).add(data1);
            writerAll.write(toString(data1) + "\n");
            if(idx < 1000) {
                bwOfTest.write(toString(data1) + "\n");
            }
            idx++;
        }
        writerAll.flush();
        writerAll.close();
        bwOfTest.flush();
        bwOfTest.close();
        writeLabels(pointsInEachClass, "data/test-label");
        for(int i = 0; i < 10; i++) {
            int size = points.get(i).size();
            for(int j = 0; j < size; j++) {
                if(random.nextDouble() > sampleRatioPoints)
                    continue;
                System.out.println(i + "-" + j);
                float[] p1 = points.get(i).get(j);
                String P1 = toString(p1);
                for(int k = i; k < 10; k++) {
                    int limit;
                    if(k == i) {
                        limit = (int)(points.get(k).size() * sampleRatioInClass);
                    }
                    else {
                        limit = (int)(points.get(k).size() * sampleRatioOutsideClass);
                    }
                    Integer[] positions = getRandomNumbers(limit, points.get(k).size());
                    for(Integer position : positions) {
                        float[] p2 = points.get(k).get(position);
                        String P2 = toString(p2);
                        writer.write(P1+ "," + P2 + "," + String.valueOf(i) + "," + String.valueOf(k) + "\n");
                    }
                }
            }
            //break;
        }
        writer.flush();
        writer.close();
    }

    private static void writeLabels(List<Set<Integer>> pointsInEachClass, String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
        for(Set<Integer> thisClass : pointsInEachClass) {
            for(Integer pointID : thisClass) {
                writer.write(String.valueOf(pointID) + ",");
            }
            writer.write("\n");
        }
        writer.flush();
        writer.close();
    }

    private static float[] toFloat(String[] data) {
        float[] result = new float[data.length];
        for(int i = 0; i < data.length; i++) {
            result[i] = Float.parseFloat(data[i])/255;
        }
        return result;
    }
    private static String toString(float[] data) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < data.length; i++) {
            if(i != data.length - 1) {
                sb.append(String.valueOf(data[i]) + " ");
            }
            else {
                sb.append(String.valueOf(data[i]));
            }
        }
        return sb.toString();
    }
    private static Integer[] getRandomNumbers(int limit, int max) {
        Random random = new Random();
        Set<Integer> result = new HashSet<>();
        while(result.size()<limit) {
            Integer temp = random.nextInt(max);
            if(!result.contains(temp)) {
                result.add(temp);
            }
        }
        return result.toArray(new Integer[0]);
    }
}
