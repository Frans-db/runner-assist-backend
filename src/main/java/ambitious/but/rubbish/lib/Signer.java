package ambitious.but.rubbish.lib;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Signer {

    private String key;
    private ResourceManager rs = new ResourceManager(getClass().getResource("/server_credentials.txt"), new String[]{"type", "key"});

    /**
     * Constructor takes the prefix need to verify the signed string
     *
     * @param prefix
     */
    public Signer(String prefix) {
        this.key = prefix + rs.get("app_key", "key");
    }

    /**
     * Method for singing the app_key by the server
     *
     * @return Signd app_key
     */
    public String signKey(){
        KeyPair keyPair = getKeyPair();
        byte[] data = key.getBytes();
        try {
            Signature sig = Signature.getInstance("SHA256WithRSA");
            sig.initSign(keyPair.getPrivate());
            sig.update(data);
            byte[] signature = sig.sign();
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method for signing the app_key with the clients private key (Only for testing purposes)
     *
     * @return Signed app_key using client RSA private key
     */
    public String singKeyTesting() {
        byte[] data = key.getBytes();
        try {
            PrivateKey privateKey = getClientTest();
            Signature sig = Signature.getInstance("SHA256WithRSA");
            sig.initSign(privateKey);
            sig.update(data);
            byte[] signature = sig.sign();
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method for converting the RSA key string in resources to a PrivateKey object (Only for testing purposes)
     *
     * @return PrivateKey object from the private key string
     */
    private PrivateKey getClientTest() throws Exception{
        String publicKey = rs.get("app_private", "key");
        byte[] decodedPB = Base64.getDecoder().decode(publicKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPB);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    /**
     * Method for converting the client public key string to a PublicKey object used to verify signed string
     *
     * @return PublicKey object from the public key string
     */
    private PublicKey getClientKey() throws Exception {
        String publicKey = rs.get("app_public", "key");
        byte[] decodedPB = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedPB);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }

    /**
     * Verifies given string input to the already stored app_key with the prefix from the constructor added
     *
     * @param in String to be verified
     * @return True or False depending if the string is valid
     */
    public boolean verifyKey(String in) {
        byte[] data = key.getBytes();
        byte[] byteIn = Base64.getDecoder().decode(in);
        try {
            PublicKey publicKey = getClientKey();
            Signature sig = Signature.getInstance("SHA256WithRSA");
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(byteIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Constructs the server KeyPair to be used for signign
     *
     * @return The KeyPair object corresponding to the strings from resource
     */
    private KeyPair getKeyPair() {
        KeyPairBuilder builder = new KeyPairBuilder(rs.get("public", "key"), rs.get("private", "key"));
        return builder.generateKeyPair();
    }
}