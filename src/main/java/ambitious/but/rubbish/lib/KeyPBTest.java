
package ambitious.but.rubbish.lib;
import static org.junit.Assert.*;

import ambitious.but.rubbish.lib.KeyPairBuilder;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class KeyPBTest {
    private String some;
    private String other;
    private KeyPairBuilder kpb;

    @Before
    public void setUp(){
        some = "this";
        other = "that";
        kpb = new KeyPairBuilder(some,other);
    }
    @Test
    public void getterTest(){
        assertEquals(some,kpb.getPublicKey());
        assertEquals(other,kpb.getPrivateKey());
    }

}
