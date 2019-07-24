package ambitious.but.rubbish.lib.test;
import static org.junit.Assert.*;
import ambitious.but.rubbish.lib.DataProcessing;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;

public class DataProcTest {
    private Double[] one;
    private Double[] two;
    private ArrayList<Double> list = new ArrayList<>();
    @Before
    public void setUP(){
       one = new Double[]{2.343243, 3.52624, 5.23144};
       two = new Double[]{1.33424, 2.22353, 3.21132};
       list.addAll(Arrays.asList(one));
    }
    @Test
    public void pearsonTest(){

        double res = DataProcessing.pearsonCorrelation(one ,two);
        assertEquals(1.9945671813118995, res, 0.0002);
    }
    @Test
    public void normalTest(){
        Double[] res = DataProcessing.normalDistribution(one);
        assertEquals(3.700307666666667,res[0], 0.0002);
        assertEquals(2.1081451421363333,res[1], 0.0002);
    }
    @Test
    public void normalListTest(){
        Double[] res = DataProcessing.normalDistribution(list);
        assertEquals(3.700307666666667,res[0], 0.0002);
        assertEquals(2.1081451421363333,res[1], 0.0002);
    }


}
