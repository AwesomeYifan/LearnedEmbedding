package mtree.examples;

import java.io.*;
import java.util.*;

public class MTreeTest {
    static int[] topK = {1,10,50};
    static int numTestSamples = 1000;

    public static void main(String[] args) throws IOException {

        MTreeClass mtree = new MTreeClass();
        List<Data> allData = new ArrayList<Data>();
        String buildOption;
        buildOption = "siameseNet";
        //buildOption = "tripletNet";
        //buildOption = ""; //build with original vectors

        double buildStartTime = System.currentTimeMillis();
        buildTree(buildOption, mtree, allData, numTestSamples);
        double buildEndTime = System.currentTimeMillis();
        System.out.println("Build time: " + (buildEndTime - buildStartTime));

        for(int resultSize : topK) {
            System.out.println("********************************************");
            System.out.println("Result size: " + String.valueOf(resultSize));
            double checkedCount = 0;
            List<List<String>> kNNs = new ArrayList<>();
            System.gc();
            double startTime = System.currentTimeMillis();
            for(Data data: allData) {
                MTreeClass.Query query = mtree.getNearestByLimit(data, resultSize + 1);//
                List<String> thisKNNs = new ArrayList<>();
                for(MTreeClass.ResultItem ri : query) {
                    if(ri.data.getID() == data.getID()) continue;//result excludes the query point
                    thisKNNs.add(String.valueOf(ri.data.getID()));
                }
                checkedCount += query.getCheckedCount();
                kNNs.add(thisKNNs);
            }
            double endTime = System.currentTimeMillis();
            double totalTime = endTime - startTime;
            System.out.println("Average computations: " + checkedCount / allData.size());
            System.out.println("Average search time: " + totalTime / allData.size());
            MTreeTest.compareRank(kNNs);
        }
    }

    private static void buildTree(String opt, MTreeClass mtree, List<Data> allData, int numTestSamples) throws IOException {
        String file;
        if(opt.equals("")) file = "data/originalVectors";
        else file = "data/reducedVectors-" + opt;
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int ID = 0;
        while((line = br.readLine()) != null) {
            String[] records = line.split(" ");
            double[] values = new double[records.length];
            for(int i = 0; i < records.length; i++) {
                values[i] = Double.parseDouble(records[i]);
            }
            Data data = new Data(values, ID++);
            mtree.add(data);
            if(ID <= numTestSamples)
                allData.add(data);
        }
        br.close();
    }

    private static double compareRank(List<List<String>> kNNs) throws IOException{
        double perf = 0;
        BufferedReader br = new BufferedReader(new FileReader("data/originalRank"));
        for(List<String> thiskNNs : kNNs) {
            String line = br.readLine();
            String[] record = line.split(",");
            List<String> recordCopy = new ArrayList<>(Arrays.asList(record));
            recordCopy.retainAll(thiskNNs);
            perf += recordCopy.size();
        }
        perf /= (kNNs.size() * kNNs.get(0).size());
        System.out.println("Accuracy: " + perf);
        return perf;
    }
    private static List<Integer> toIntList(String[] input, int limit) {
        if(limit == -1) limit = input.length;
        if(limit>input.length) limit = input.length;
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < limit; i++) {
            list.add(Integer.parseInt(input[i]));
        }
        return list;
    }
}
