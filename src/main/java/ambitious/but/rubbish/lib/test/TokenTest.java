package ambitious.but.rubbish.lib.test;
import static org.junit.Assert.*;
import ambitious.but.rubbish.lib.Token;
import org.junit.Test;

public class TokenTest {
    @Test
    public void conOneTest(){
        Token t = new Token(23);
        assertNotNull(t);
    }
    @Test
    public void conTwoTest(){
        Token t = new Token(23,"abdjsoe29r4kfew");
        assertNotNull(t);
    }
    @Test
    public void nextTest(){
        Token t = new Token(23);
        String tst = t.nextToken();
        assertNotNull(tst);
    }
}
