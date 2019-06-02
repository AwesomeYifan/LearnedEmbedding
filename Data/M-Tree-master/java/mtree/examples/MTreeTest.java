package mtree.examples;

import java.io.*;
import java.util.*;

public class MTreeTest {
    static int[] topK = {1,10,49};
    //static int[] topK = {1, 10, 50, 100};
    static int numThreads = 8;
    //static int[] topK = {2};

    public static void main(String[] args) throws IOException {

        MTreeClass mtree = new MTreeClass();
        List<Data> allData = new ArrayList<Data>();
        String buildOption;
        buildOption = "learn";
        //buildOption = "labeled";
        //buildOption = "hash";
        //buildOption = "original";
        double buildStartTime = System.currentTimeMillis();
        buildTree(buildOption, mtree, allData);
        double buildEndTime = System.currentTimeMillis();
        System.out.println("build time: " + (buildEndTime - buildStartTime));

        for(int resultSize : topK) {
            System.out.println("********************************************");
            System.out.println("Result size: " + String.valueOf(resultSize));
            BufferedWriter bw = new BufferedWriter(new FileWriter("../Data/data/mtree.csv"));
            int checkedCount = 0;
            Map<String, Set<String>> kNNs = new HashMap<>();
            System.gc();
            double startTime = System.currentTimeMillis();
            for(Data data: allData) {
                //bw.write(data.getID() + "");
                //System.out.print(data.getID() + ": ");
                MTreeClass.Query query = mtree.getNearestByLimit(data, resultSize+1);
                //System.out.println(query.getCheckedCount());
                Set<String> thisKNNs = new HashSet<>();
                boolean flag = true;
                for(MTreeClass.ResultItem ri : query) {
                    //System.out.print(ri.data.getID() + ", ");
                    //do not write itself.
                    if(flag) {
                        flag = false;
                        continue;
                    }
                    bw.write(ri.data.getID() + ",");
                    thisKNNs.add(String.valueOf(ri.data.getID()));
                }
                checkedCount += query.getCheckedCount();
                kNNs.put(String.valueOf(data.getID()), thisKNNs);
                bw.write("\r\n");
                //System.out.println(" ");
            }
            double endTime = System.currentTimeMillis();
            double totalTime = endTime - startTime;
            bw.flush();bw.close();
            System.out.println("Average computations: " + (double)checkedCount / allData.size());
            System.out.println("Average search time: " + (double)totalTime / allData.size());
            if(buildOption.equals("labeled")) MTreeTest.compareLabel(kNNs);
            else MTreeTest.compareRank("approximate", resultSize);
            //System.out.println("********************************************");
        }
    }
    private static void buildTree(String opt, MTreeClass mtree, List<Data> allData) throws IOException {

        String filePrefix;
        if(opt.equals("original")) { filePrefix = "data/thread-"; }
        else if(opt.equals("learn")) {filePrefix = "data/siamese-reducedVectors-";}
        else if(opt.equals("hash")) {filePrefix = "data/hash-reducedVectors-";}
        else if(opt.equals("labeled")) {filePrefix = "data/siamese-reducedVectors-MNIST";}
        //else if(opt.equals("labeled")) {filePrefix = "data/reducedVectors-LDA";}
        else {throw new IllegalArgumentException("which method to test?");}
        //String filePrefix = "data/siamese-reducedVectors-";

        int ID = 0;
        for(int fileID = 0; fileID < numThreads; fileID++) {
        //for(int fileID = 0; fileID < 1; fileID++) {
            String file;
            if(opt.equals("labeled")) {file = filePrefix;}
            else {file = filePrefix + String.valueOf(fileID);}
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null) {
                String[] records = line.split(" ");
                double[] values = new double[records.length];
                for(int i = 0; i < records.length; i++) {
                    values[i] = Double.parseDouble(records[i]);
                }
                Data data = new Data(values, ID++);
                mtree.add(data);
                if(ID <= 1000)
                    allData.add(data);
            }
            br.close();
            if(opt.equals("labeled")) break;
        }
    }

    private static double compareRank(String opt, int resultSize) throws IOException{
        double perf = 0;
        BufferedReader br1 = new BufferedReader(new FileReader("data/thread-0-rank"));
        BufferedReader br2 = new BufferedReader(new FileReader("../Data/data/mtree.csv"));
        String line1, line2 = "";
        int count = 0;
        switch (opt) {
            case "exact": {
                while((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
                    String[] records1 = line1.split(",");
                    String[] records2 = line2.split(",");
                    List<Integer> list1 = toIntList(records1, resultSize);
                    List<Integer> list2 = toIntList(records2, resultSize);
                    //System.out.println(list1.size());
                    List<Integer> subList1 = list1.subList(0,resultSize);
                    List<Integer> subList2 = list2.subList(0,resultSize);
                    List<Integer> subListTemp = new ArrayList<>(subList1);
                    subListTemp.retainAll(subList2);
                    perf += subListTemp.size();
                    count++;
                }
                break;
            }
            default: {
                while((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
                    String[] records1 = line1.split(",");
                    String[] records2 = line2.split(",");
                    List<Integer> list1 = toIntList(records1, -1);
                    List<Integer> list2 = toIntList(records2, resultSize);
                    //List<Integer> subList2 = list2.subList(0,topK[i]);
                    List<Integer> subListTemp = new ArrayList<>(list1);
                    subListTemp.retainAll(list2);
                    perf += subListTemp.size();
                    count++;
                }
                break;
            }
        }

        perf /= (count * resultSize);
        System.out.println("Accuracy: " + perf);
        return perf;
    }

    private static double compareLabel(Map<String, Set<String>> kNNs) throws IOException{
        double perf = 0;
        BufferedReader br1 = new BufferedReader(new FileReader("data/test-label"));
        BufferedReader br2 = new BufferedReader(new FileReader("data/mtree.csv"));
        String line1, line2 = "";
        int count = 0;
        while((line2 = br2.readLine()) != null) {
            String[] pointsInThisClassTemp = line2.split(",");
            Set<String> pointsInThisClass = new HashSet<>(Arrays.asList(pointsInThisClassTemp));
            for(String queryID : kNNs.keySet()) {
                if(pointsInThisClass.contains(queryID)) {
                    count += kNNs.get(queryID).size();
                    Set<String> temp = new HashSet<>(pointsInThisClass);
                    temp.retainAll(kNNs.get(queryID));
                    perf += temp.size();
                }
            }
        }
        perf = (perf - kNNs.size()) / (count - kNNs.size());
        System.out.println("Accuracy: " + String.valueOf(perf));
        return perf;
    }


//    private static double[] compareRank(String opt) throws IOException{
//        double[] perf = new double[topK.length];
//        BufferedReader br1 = new BufferedReader(new FileReader("data/thread-0-rank"));
//        BufferedReader br2 = new BufferedReader(new FileReader("../Data/data/mtree.csv"));
//        String line1, line2 = "";
//        int count = 0;
//        while((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
//            String[] records1 = line1.split(",");
//            String[] records2 = line2.split(",");
//            List<Integer> list1 = toIntList(records1, topK[topK.length-1] + 1);
//            List<Integer> list2 = toIntList(records2, topK[topK.length-1] + 1);
//            //System.out.println(list1.size());
//            for(int i = 0; i < topK.length; i++) {
//                List<Integer> subList1 = list1.subList(0,topK[i] + 1);
//                List<Integer> subList2 = list2.subList(0,topK[i] + 1);
//                List<Integer> subListTemp = new ArrayList<>(subList1);
//                subListTemp.retainAll(subList2);
//                perf[i] += subListTemp.size();
//            }
//            count++;
//        }
//        for(int i = 0; i < topK.length; i++) {
//            perf[i] -= count;
//            perf[i] /= (count * (topK[i]));
//        }
//        System.out.println(Arrays.toString(perf));
//        return perf;
//    }

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
