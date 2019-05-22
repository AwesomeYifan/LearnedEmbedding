package mtree.examples;

import java.io.*;
import java.util.*;

public class MTreeTest {
    static int[] topK = {1, 10, 49};
    //static int[] topK = {1};
    static int numThreads = 8;
    //static int[] topK = {2};

    public static void main(String[] args) throws IOException {

//        MTreeClass mtree = new MTreeClass();
//        List<Data> allData = new ArrayList<Data>();
//        double buildStartTime = System.currentTimeMillis();
//        //buildTree("original", mtree, allData);
//        buildTree("embedded", mtree, allData);
//        double buildEndTime = System.currentTimeMillis();
//        System.out.println("build time: " + (buildEndTime - buildStartTime));
//
//        BufferedWriter bw = new BufferedWriter(new FileWriter("../Data/data/mtree.csv"));
//        int checkedCount = 0;
//        System.gc();
//        double startTime = System.currentTimeMillis();
//        for(Data data: allData) {
//            //bw.write(data.getID() + "");
//            //System.out.print(data.getID() + ": ");
//            MTreeClass.Query query = mtree.getNearestByLimit(data, topK[topK.length - 1]+1);
//            //System.out.println(query.getCheckedCount());
//
//            for(MTreeClass.ResultItem ri : query) {
//                //System.out.print(ri.data.getID() + ", ");
//                bw.write(ri.data.getID() + ",");
//            }
//            checkedCount += query.getCheckedCount();
//
//            bw.write("\r\n");
//           //System.out.println(" ");
//        }
//        double endTime = System.currentTimeMillis();
//        double totalTime = endTime - startTime;
//        bw.flush();bw.close();
//        System.out.println("Average computations: " + (double)checkedCount / allData.size());
//        System.out.println("Average search time: " + (double)totalTime / allData.size());
        MTreeTest.compareRank("approximate");
    }
    private static void buildTree(String opt, MTreeClass mtree, List<Data> allData) throws IOException {

        String filePrefix = opt.equals("original")? "data/thread-" : "data/siamese-reducedVectors-";
        //String filePrefix = "data/siamese-reducedVectors-";

        int ID = 0;
        for(int fileID = 0; fileID < numThreads; fileID++) {
        //for(int fileID = 0; fileID < 1; fileID++) {
            String file = filePrefix + String.valueOf(fileID);
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
                if(fileID == 0)
                    allData.add(data);
            }
            br.close();
        }
    }

    private static double[] compareRank(String opt) throws IOException{
        double[] perf = new double[topK.length];
        BufferedReader br1 = new BufferedReader(new FileReader("data/thread-0-rank"));
        BufferedReader br2 = new BufferedReader(new FileReader("../Data/data/mtree.csv"));
        String line1, line2 = "";
        int count = 0;
        switch (opt) {
            case "exact": {
                while((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
                    String[] records1 = line1.split(",");
                    String[] records2 = line2.split(",");
                    List<Integer> list1 = toIntList(records1, topK[topK.length-1] + 1);
                    List<Integer> list2 = toIntList(records2, topK[topK.length-1] + 1);
                    //System.out.println(list1.size());
                    for(int i = 0; i < topK.length; i++) {
                        List<Integer> subList1 = list1.subList(0,topK[i] + 1);
                        List<Integer> subList2 = list2.subList(0,topK[i] + 1);
                        List<Integer> subListTemp = new ArrayList<>(subList1);
                        subListTemp.retainAll(subList2);
                        perf[i] += subListTemp.size();
                    }
                    count++;
                }
                break;
            }
            default: {
                while((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
                    String[] records1 = line1.split(",");
                    String[] records2 = line2.split(",");
                    List<Integer> list1 = toIntList(records1, -1);
                    List<Integer> list2 = toIntList(records2, topK[topK.length-1] + 1);
                    //System.out.println(list1.size());
                    for(int i = 0; i < topK.length; i++) {
                        int toIndex = (topK[i] + 1) < list2.size()? topK[i] + 1 : list2.size();
                        List<Integer> subList2 = list2.subList(0,toIndex);
                        //List<Integer> subList2 = list2.subList(0,topK[i]);
                        List<Integer> subListTemp = new ArrayList<>(list1);
                        subListTemp.retainAll(subList2);
                        perf[i] += subListTemp.size();
                    }
                    count++;
                }
                break;
            }
        }

        for(int i = 0; i < topK.length; i++) {
            perf[i] -= count;
            perf[i] /= (count * (topK[i]));
        }
        System.out.println(Arrays.toString(perf));
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
