package mtree.examples;

import java.math.BigDecimal;
import java.math.MathContext;

public class Temp {
    public static void main (String[] args) {
        double v = 0.6825;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.round(new MathContext(3));
        System.out.println(bd);
        //return Math.pow(bd.doubleValue(),2);
    }
}
