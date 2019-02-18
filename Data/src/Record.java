import java.io.*;
import java.util.*;

public class Record {

    public static void main(String[] args) throws IOException, InterruptedException {
        Trajectory traj = new Trajectory();
        traj.generate();
        //System.out.println("Trajectory generated");
        //System.out.println("Trajectory generated");
        Record record = new Record();
        int topK;
        int outputSize;
        //record.reverse();
        //System.out.println("Record reversed");
        record.output();
        System.out.println("Jaccard computed");
//        BufferedReader br = new BufferedReader((new FileReader("AD")));
//        String line = br.readLine();
//        String[] scores = line.split(",");
//        Map<Double,Set<Integer>> map = new TreeMap<>();
//        for(int i = 0; i < scores.length; i++) {
//            double s = Double.valueOf(scores[i]);
//            if(map.containsKey(s)) {
//                map.get(s).add(i);
//            }
//            else {
//                Set<Integer> set = new HashSet<>();
//                set.add(i);
//                map.put(s,set);
//            }
//        }
//        for(Double d : map.keySet()) {
//            System.out.print(d + ": ");
//            for(Integer i : map.get(d)) {
//                System.out.print(i + ",");
//            }
//            System.out.println();
//        }
    }

//     private void reverse() throws IOException {
//         Map<String, Set<String>> ceMap = new HashMap<>();
//         BufferedReader br = new BufferedReader(new FileReader("0"));
//         while(true) {
//            String line = br.readLine();
//            if(line == null)
//                break;
//            String[] traj = line.split(" ");
//            String entity = traj[0];
//            for(int idx = 1; idx < traj.length - 1; idx++) {
//                String loc = traj[idx];
//                String stCell = loc + "#" + idx;
//                if(ceMap.containsKey(stCell)) {
//                    ceMap.get(stCell).add(entity);
//                }
//                else {
//                    Set<String> eSet = new HashSet<>();
//                    eSet.add(entity);
//                    ceMap.put(stCell, eSet);
//                }
//            }
//         }
//         File file = new File("r");
//         OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(file));
//         for(String stCell : ceMap.keySet()) {
//             //ow.write(stCell + ",");
//             for(String e : ceMap.get(stCell)) {
//                 ow.write(e + " ");
//             }
//             ow.write("\n ");
//         }
//         ow.flush();
//         ow.close();
//     }


     private void output() throws IOException {
        int[] scoreDistribution = new int[10];
        int numEntities = Trajectory.numEntities;
        BufferedReader br1 = new BufferedReader(new FileReader("./data/traces"));
        BufferedReader br2 = new BufferedReader(new FileReader("./data/traces"));
        File file = new File("./data/train.csv");
        BufferedWriter ow = new BufferedWriter(new FileWriter(file));
        BufferedWriter bwOfRank = new BufferedWriter(new FileWriter("./data/ranks"));
        BufferedWriter wTest = new BufferedWriter(new FileWriter("./data/test"));
        ow.write("id,qid1,qid2,question1,question2,is_duplicate\r\n");
        wTest.write("\"test_id\",\"question1\",\"question2\"\n");
        int idx1 = 0;
        int idx2 = 0;
        String line1, line2;
        Set<String> set1, set2, set3 = new HashSet();
        String[] record1, record2;
        double nomi, denomi, jac;
        int outputSize = Utils.outputSize;
        int topK = Utils.topK;
        int queueSize = 0;
        TreeMap<Double, Set<String>> rankedList = new TreeMap<>();
        while(true) {
            line1 = br1.readLine();
            System.out.println(idx1);
            if(line1 == null)
                break;
            record1 = line1.split(" ");
            set1 = new HashSet<String>(Arrays.asList(record1));
            set1.removeAll(Arrays.asList(""));
            if(idx1 < outputSize) {
                rankedList = new TreeMap<>();
            }
            while(true) {
                line2 = br2.readLine();
                if(line2 == null) {
                    br2.close();
                    br2 = new BufferedReader(new FileReader("./data/traces"));
                    break;
                }
                if(idx1 < outputSize) {
                    wTest.write(idx1 * numEntities + idx2 + "," + "\"" + line1 + "\",\"" + line2 + "\"\n");
                }
//                if(idx1 == idx2) {
//                    idx2++;
//                    continue;
//                }
                record2 = line2.split(" ");
                set2 = new HashSet<>(Arrays.asList(record2));
                set2.removeAll(Arrays.asList(""));
                set3 = new HashSet<String>(set2);
                set3.addAll(set1);
                nomi = set3.size();
                set2.retainAll(set1);
                denomi = set2.size();
                jac = denomi / nomi;
                Utils.updateQueue(rankedList, queueSize, topK, jac, Integer.toString(idx2));
                ow.write(idx1 * numEntities + idx2 + "," + idx1 + "," + idx2 + ","+line1 + "," + line2 + "," + jac + "\r\n");
                int idx = (int)Math.floor(jac * 10);
                if(jac == 1.0)
                    idx = 9;
                scoreDistribution[idx]++;
                idx2++;
            }
            idx1++;
            idx2 = 0;
            if(idx1 < outputSize)
                Utils.writeFileReverse(bwOfRank, rankedList);
        }
        wTest.flush();
        wTest.close();
        bwOfRank.flush();
        bwOfRank.close();
        ow.flush();
        ow.close();
        System.out.println(Arrays.toString(scoreDistribution));
     }
}
