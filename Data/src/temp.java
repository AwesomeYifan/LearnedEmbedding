import Algorithms.VAFile;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class temp {
    public static void main(String[] args) throws IOException {
        //VAFile vaFile = new VAFile("./data/test",2);
        String s = Integer.toBinaryString(2 << 0);
        int i = Integer.parseInt(s, 2);
        System.out.println(i);
    }
}
