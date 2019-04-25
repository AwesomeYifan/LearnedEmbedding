package mtree.examples;

import java.io.*;
import java.util.*;

public class Main {
    static int[] topK = {2,5,10};

    public static void main(String[] args) throws IOException {
        String path = "../Model/SiameseLSTM/DR/data/reducedVectors.txt";
        //String path = "data/temp.csv";
        MTreeClass mtree = new MTreeClass();
        //Set<Data> allData = new HashSet<Data>();
        List<Data> allData = new ArrayList<Data>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        int ID = 0;
        String line = "";
        while((line = br.readLine()) != null) {
            String[] records = line.split(" ");
            double[] values = new double[records.length];
            for(int i = 0; i < records.length; i++) {
                values[i] = Double.parseDouble(records[i]);
            }
            Data data = new Data(values, ID++);
            mtree.add(data);
            allData.add(data);
        }
        br.close();
        BufferedWriter bw = new BufferedWriter(new FileWriter("data/mtree.csv"));
        double startTime = System.currentTimeMillis();
        for(Data data: allData) {
            //bw.write(data.getID() + "");
            //System.out.print(data.getID() + ": ");
            MTreeClass.Query query = mtree.getNearestByLimit(data, 10);
            for(MTreeClass.ResultItem ri : query) {
                //System.out.print(ri.data.getID() + ", ");
                bw.write(ri.data.getID() + ",");
            }
            bw.write("\r\n");
           //System.out.println(" ");
        }
        double endTime = System.currentTimeMillis();
        bw.flush();bw.close();
        System.out.println(endTime - startTime);
        Main.compareRank();
    }
    private static double[] compareRank() throws IOException{
        double[] perf = new double[topK.length];
        BufferedReader br1 = new BufferedReader(new FileReader("data/rank.csv"));
        BufferedReader br2 = new BufferedReader(new FileReader("data/mtree.csv"));
        String line1, line2 = "";
        int count = 0;
        while((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
            String[] records1 = line1.split(",");
            String[] records2 = line2.split(",");
            List<Integer> list1 = toIntList(records1, topK[topK.length-1]);
            List<Integer> list2 = toIntList(records2, topK[topK.length-1]);
            //System.out.println(list1.size());
            for(int i = 0; i < topK.length; i++) {
                List<Integer> subList1 = list1.subList(0,topK[i]);
                List<Integer> subList2 = list2.subList(0,topK[i]);
                List<Integer> subListTemp = new ArrayList<>(subList1);
                subListTemp.retainAll(subList2);
                perf[i] += subListTemp.size();
            }
            count++;
        }
        for(int i = 0; i < topK.length; i++) {
            perf[i] -= count;
            perf[i] /= (count * (topK[i]-1));
        }
        System.out.println(Arrays.toString(perf));
        return perf;
    }
    private static List<Integer> toIntList(String[] input, int limit) {
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < limit; i++) {
            list.add(Integer.parseInt(input[i]));
        }
        return list;
    }
}
