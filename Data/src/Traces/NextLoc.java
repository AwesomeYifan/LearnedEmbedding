package Traces;

import java.io.*;
import java.util.*;

public class NextLoc {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<Set<String>> nextLocsList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\esoadmin\\Desktop\\predictions-10"));
        int entityID = 0;
        while(true) {
            String line = br.readLine();
            if (line == null)
                break;
            String[] locations = line.split(",");
            Set<String> set = new HashSet<String>(Arrays.asList(locations));
            nextLocsList.add(set);
            entityID++;
        }
        int numEntities = Trajectory.numEntities;
        int[][] pairSimMatrix = new int[numEntities][numEntities];
        for(int i = 0; i < numEntities - 1; i++) {
            for(int j = i + 1; j < numEntities; j++) {
                Set<String> set1 = new HashSet<>(nextLocsList.get(i));
                Set<String> set2 = new HashSet<>(nextLocsList.get(j));
                set1.retainAll(set2);
                pairSimMatrix[i][j] = set1.size();
                pairSimMatrix[j][i] = set1.size();
            }
        }
        List<List<Integer>> rankList = new ArrayList<>();
        for(int i = 0; i < numEntities; i++) {
            List<Integer> list = new ArrayList<>();
            Map<Integer, Set<Integer>> map = new TreeMap<>();
            for(int j = 0; j < numEntities; j++) {
                Integer sim = pairSimMatrix[i][j];
                if(map.containsKey(sim)) {
                    map.get(sim).add(j);
                }
                else {
                    Set<Integer> set = new HashSet<>(j);
                    map.put(sim,set);
                }
            }
            while(!map.isEmpty()) {
                list.addAll(map.get(((TreeMap<Integer, Set<Integer>>) map).lastKey()));
                ((TreeMap<Integer, Set<Integer>>) map).pollLastEntry();
            }
            rankList.add(list);
        }
        int[] topk = {1,2,5,10,20,40,50,60,70,80,90,100,200,300,500};
        double[] performance = new double[topk.length];
        br = new BufferedReader(new FileReader("rank"));
        int idx = 0;
        while(true) {
            String line = br.readLine();
            if (line == null)
                break;
            String[] entities = line.split(",");
            Integer[] ens = new Integer[topk[topk.length-1]];
            for(int i = 0; i < ens.length; i++) {
                ens[i] = Integer.parseInt(entities[i]);
            }
            for(int i = 0; i < topk.length; i++) {
                int k = topk[i];
                Set<Integer> set = new HashSet<Integer>(Arrays.asList(Arrays.copyOfRange(ens,0,k)));
                set.retainAll(rankList.get(idx).subList(0,k));
                int overlap = set.size();
                performance[i] += overlap;
            }
            idx ++;
        }
        File file = new File("rnnperf.csv");
        OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(file));
        for(int i = 0; i < performance.length; i++) {
            performance[i] = performance[i] / topk[i] / numEntities;
            ow.write("Top-" + (topk[i]) + ",");
        }
        ow.write("\r\n");
        for(int i = 0; i < performance.length; i++) {
            ow.write(performance[i] + ",");
        }
        ow.flush();
        ow.close();
    }
}
