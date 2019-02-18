import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
public class Trajectory {
    //assume the area is a square

    //the length of the square, km
    public static double totalLength = 5;
    public static int numThreads = 1;
    static int hierarchyLevel = 1;

    //partition the area into cells
    //each cell is a base spatial unit

    //the length of the cell, km
    public static double cellLength = 1;

    public static int numCellPerRow = (int)(totalLength/cellLength);

    public static int numEntities = Utils.numEntities;

    //total duration, hour
    public static int duration = 17 * 2;

    //model parameters
    public static double alpha = 0.55;
    public static double beta = 0.8;
    public static double rho = 0.6;
    public static double gamma = 0.21;
    public static double zeta = 1.2;
    //discretize pattern
    public static int[] stayTime = new int[1000];//hour
    public static double[] jumpDistance = new double[10000];//km

    public static int[][] hierarchy = new int[numCellPerRow*numCellPerRow][hierarchyLevel+1];
    public static int[][][][] records = new int[numThreads][][][];
    //control the group of base spatial units
    //partition numCellPerRow into numGroupPerRow parts
    //follows exp(a*0):exp(a*1):...exp(a*(numGroupPerRow-1));
    //b controls the number of units per level
    //more units at finer level
    //follows exp(b*0):exp(b*1):...exp(b*hierarchyLevel-1)
    public static int numGroupPerRow = 3;
    public static double a = 2;
    public static double b = 2;

    public static void generate() throws IOException, InterruptedException {
        stayTime = new int[1000];//hour
        jumpDistance = new double[10000];//km
        hierarchy = new int[numCellPerRow*numCellPerRow][hierarchyLevel];
        records = new int[numThreads][][][];
        getParas();
        generator[] gr = new generator[numThreads];
        for(int i = 0; i < numThreads; i++) {
            gr[i] = new generator(i, numEntities/numThreads, duration, totalLength, cellLength, rho, gamma, zeta, stayTime, jumpDistance);
            gr[i].start();
        }
        int start = 0;
        for(int i = 0; i < numThreads; i++) {
            gr[i].join();
            records[i] = gr[i].getRecord();
        }
        //buildhierarchy();
    }

    //discretize stay time and jump distance
    private static void getParas() throws IOException{
        int timeCutoff = 17;
        double sumD = 0;
        for (int i = 1; i <= timeCutoff; i++) {
            sumD += Math.pow(i, -1 - alpha);
        }
        int sum = 0;
        for(int i = 1; i <= timeCutoff; i++) {
            int num = (int) Math.ceil(stayTime.length * (Math.pow(i, -1 - alpha)/sumD));
            if(sum + num >= stayTime.length) num = stayTime.length - sum - 1;
            if(i < timeCutoff) {
                for(int j = sum; j < sum + num; j++) {
                    stayTime[j] = i;
                }
                sum += num;
            }
            if(sum == stayTime.length-1) break;
            else {
                for(int j = sum; j < stayTime.length; j++) {
                    stayTime[j] = i;
                }
            }
        }
        //System.out.println(Arrays.toString(stayTime));
        //System.in.read();
        sum = 0; sumD = 0;
        int distanceCutoff = 100;
        for(double i = cellLength; i <= distanceCutoff; i+= cellLength) {
            sumD += Math.pow(i, -1 - beta);
        }
        for(double i = cellLength; i <= distanceCutoff; i+= cellLength) {
            int num = (int) Math.ceil(jumpDistance.length * (Math.pow(i, -1 - beta)/sumD));
            if(sum + num >= jumpDistance.length) num = jumpDistance.length - sum - 1;
            if (i < distanceCutoff) {
                for(int j = sum; j < sum + num; j++) {
                    jumpDistance[j] = i;
                }
                sum += num;
            }
            if(sum == jumpDistance.length-1) break;
            else {
                for(int j = sum; j < jumpDistance.length; j++) {
                    jumpDistance[j] = i;
                }
            }
        }
        //System.out.println(Arrays.toString(jumpDistance));
        //System.in.read();
    }

    private static int[] getnumUnitsPerLevel() throws IOException {
        int[] numUnitPerLevel = new int[hierarchyLevel];
        numUnitPerLevel[0] = numGroupPerRow;
        for(int i = 1; i < hierarchyLevel; i++) {
            int temp = (int)(Math.pow(i,b))*numGroupPerRow;
            if(temp >= numCellPerRow || temp < 0)
                temp = numCellPerRow;
            numUnitPerLevel[i] = temp;
        }
        return numUnitPerLevel;
    }

    //return spatial hierarchy in format: locID - [_locID in level 1; _locID in level 2...]
    private static int[][] buildhierarchy() throws IOException {
        int[] numUnitPerLevel = getnumUnitsPerLevel();
        for(int i = 0; i < numCellPerRow*numCellPerRow; i++) {
            Arrays.fill(hierarchy[i], i);
        }
        int[] branch = {0,0,numCellPerRow,numCellPerRow};
        return parRec(branch,hierarchy,0,numUnitPerLevel);
    }

    private static int[][] parRec(int[] branch, int[][] hierarchy, int level, int[] numUnitPerLevel) throws IOException {
        int numUnits = numUnitPerLevel[level];
        double[] numCellPerUnitDouble = new double[numUnits];
        int rangeXFrom = branch[0];	int rangeXTo = branch[2]; int range = rangeXTo - rangeXFrom;
        int rangeYFrom = branch[1];

        List<int[]> parThisLevel = new ArrayList<int[]>(numUnits);
        double last = 1; double sum = 0;
        for(int i = 0; i < numUnits; i++) {
            last = Math.pow(i+1, a);
            numCellPerUnitDouble[i] = last;
            sum += last;
        }
        int _sum = 0;
        //System.out.println(range);
        List<Integer> numCellPerUnit = new ArrayList<Integer>(numUnits);
        for(int i = 0; i < numUnits; i++) {
            if(i == numUnits - 1) {
                numCellPerUnit.add(range-_sum);
            }
            else {
                numCellPerUnit.add((int)(numCellPerUnitDouble[i]*range/sum));
                _sum += numCellPerUnit.get(i);
            }
        }
        Collections.sort(numCellPerUnit);
        Collections.reverse(numCellPerUnit);
        //System.out.println(numCellPerUnit);
        //System.in.read();
        //System.out.print(range + "-" + numCellPerUnit + ": ");
        int xLast = rangeXFrom;
        int yLast = rangeYFrom;
        int currentLength = 0;
        for(int i = 0; i < numUnits; i++) {
            int unitXFrom = xLast;
            int unitLength = numCellPerUnit.get(i);
            if(unitLength == 0) continue;
            int unitXTo = unitXFrom + unitLength;
            xLast = unitXTo;
            currentLength += unitLength;
            for(int y = rangeYFrom; y < rangeYFrom + currentLength - unitLength + 1; y+= unitLength) {
                if(unitXFrom == y) continue;
                //System.out.print("(" + unitXFrom + "," + y + "," +unitXTo + "," + (y + unitLength)+ ","+")");
                int[] par = {unitXFrom, y, unitXTo, y + unitLength};
                parThisLevel.add(par);
                int _loc = unitXFrom*numCellPerRow + y;
                for(int xPos = unitXFrom; xPos < unitXTo; xPos++) {
                    for(int yPos = y; yPos < y+unitLength; yPos++) {
                        int loc = xPos*numCellPerRow + yPos;
                        hierarchy[loc][level] = _loc;
                    }
                }
            }

            //System.out.print(",");
            int unitYFrom = yLast;
            int unitYTo = unitYFrom + unitLength;
            yLast = unitYTo;
            for(int x = rangeXFrom; x < rangeXFrom + currentLength - unitLength + 1; x += unitLength) {
                //System.out.print("(" + x + "," + unitYFrom + "," + (x + unitLength) + "," + unitYTo + ")");
                int[] par = {x, unitYFrom, x + unitLength, unitYTo};
                parThisLevel.add(par);
                int _loc = x*numCellPerRow + unitYFrom;
                for(int xPos = x; xPos < x+unitLength; xPos++) {
                    for(int yPos = unitYFrom; yPos < unitYTo; yPos++) {
                        int loc = xPos*numCellPerRow + yPos;
                        hierarchy[loc][level] = _loc;
                    }
                }
            }
        }
        //System.out.println();
        if(level == hierarchyLevel-1) {
            return hierarchy;
        }
        else {
            for(int[] par : parThisLevel) {
                parRec(par, hierarchy, level + 1, numUnitPerLevel);
            }
        }
        return hierarchy;
    }
}

class generator extends Thread{
    int threadID;
    int numEntities;
    int duration;
    double totalLength;
    double cellLength;
    int numCellPerRow;
    double rho;
    double gamma;
    double zeta;
    int[] stayTime;
    double[] jumpDistance;
    String path;
    int[][][] record;

    public generator(int threadID, int numEntities, int duration, double totalLength, double cellLength,
               double rho, double gamma, double zeta, int[] stayTime, double[] jumpDistance) {
        super();
        this.threadID = threadID;
        this.numEntities = numEntities;
        this.duration = duration;
        this.totalLength = totalLength;
        this.cellLength = cellLength;
        this.numCellPerRow = (int)(totalLength/cellLength);
        this.rho = rho;
        this.gamma = gamma;
        this.zeta = zeta;
        this.stayTime = stayTime;
        this.jumpDistance = jumpDistance;
        this.path = "PI" + String.valueOf(threadID);
        record = new int[numEntities][][];
    }
    public int[][][] getRecord() {
        return this.record;
    }
    public List<String> getTrajectory(int id) {
        Random rand = new Random();
        List<String> traj = new ArrayList<String>();
        //save all visited locations ordered by first visitation time
        List<Integer> oldLocs = new ArrayList<Integer>();
        //record the total mobility duration
        int dura = 0;
        //starting location
        int p = -1;
        while(dura < duration) {
            //if haven't started yet
            int startTime = dura;
            int d = stayTime[rand.nextInt(stayTime.length)];
            int endTime = startTime + d;
            if(endTime > duration) {
                endTime = duration;
                d = endTime - startTime;
            }
            if(p == -1) {
                p = rand.nextInt(numCellPerRow*numCellPerRow);
                for(int t =  startTime; t < endTime; t++) {
//                    String cell = t + "t" + p;
                    String cell = "t" + p;
                    traj.add(cell);
                }

            }
            else if(rand.nextInt(10)>-1) {
                //assume the entity is in the center of the cell
                double[] currentPos = {((double)p/numCellPerRow+0.5)*cellLength, (p-numCellPerRow*(p/numCellPerRow)+0.5)*cellLength};
                double Pnew = rho * Math.pow(oldLocs.size(), -gamma);
                boolean isExplore = (rand.nextDouble() < Pnew);
                if(isExplore) {
                    //System.out.println("explore");
                    p = chooseNewLoc(currentPos);
                    while(p < 0 || p >= numCellPerRow*numCellPerRow) {
                        p = chooseNewLoc(currentPos);
                    }
                    if(!oldLocs.contains(p)) {
                        oldLocs.add(p);
                    }
                }
                else{
                    //System.out.println("return");
                    p = chooseOldLoc(oldLocs);
                }
                for(int t =  startTime; t < endTime; t++) {
//                    String cell = t + "t" + p;
                    String cell = "t" + p;
                    traj.add(cell);
                }
            }
            dura += d;
        }

        return traj;
    }

    public int chooseNewLoc(double[] currentPos) {
        Random rand = new Random();
        double direction = rand.nextDouble()*2*Math.PI;
        double distance = jumpDistance[rand.nextInt(jumpDistance.length)];
        double horDsp = distance * Math.cos(direction);
        double verDsp = distance * Math.sin(direction);
        double[] desPos = {currentPos[0] + horDsp, currentPos[1] + verDsp};
        int result = numCellPerRow * (int)Math.floor((desPos[0]/cellLength))+(int)Math.floor((desPos[1]/cellLength));
        return result;
    }

    public int chooseOldLoc(List<Integer> oldLocs) {
        double freqSum = 0;
        int result = oldLocs.get(0);
        for(int i = 1; i <= oldLocs.size(); i++) {
            freqSum += Math.pow(i, -zeta);
        }
        double select = Math.random() * freqSum;
        double last = 0;
        double next = 0;
        for(int i = 1; i <= oldLocs.size(); i++) {
            next += Math.pow(i, -zeta);
            if(select >= last && select <= next) {
                result = oldLocs.get(i-1);
                break;
            }
            last = next;
        }
        return result;
    }

    public void run(){
        try {
            //File file = new File(String.valueOf(threadID));
            File file = new File(String.valueOf("./data/traces"));
            OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(file));
            int idx = 0;
            //System.out.println(startID + "#" + endID);
            for(int i = 0; i < numEntities; i++) {
                //ow.write(i + " ");
                List<String> traj = getTrajectory(i);
                for(int j = 0; j < traj.size(); j++) {
                    if(j == 0) {
                        ow.write(traj.get(j));
                    }
                    else {
                        ow.write(" " + traj.get(j));
                    }
                }
                ow.write("\n");
                idx++;
                if(threadID == 0 && idx%100000 == 0) {
                    System.out.println("Generate trajectory for " + idx* Trajectory.numThreads/1000 + "k entities");
                }
            }
            ow.flush();
            ow.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}