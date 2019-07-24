package ambitious.but.rubbish.lib;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Generates RSA KeyPair
 */
class KeyPairBuilder {

    private String publicKey;
    private String privateKey;

    /**
     * Constructor takes input and initiates global variables for the key pair.
     *
     * @param publicKey The Public Key in String Format
     * @param privateKey The Private Key in String Format
     */
    KeyPairBuilder(String publicKey ,String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * Constructs the KeyPair object with the specified public and private keys.
     *
     * @return KeyPair Object Made from the Specified Inputs
     */
    KeyPair generateKeyPair() {
        try {
            PublicKey publickey = this.genPublicKey(this.publicKey);
            PrivateKey privatekey = this.genPrivateKey(this.privateKey);
            return new KeyPair(publickey, privatekey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Takes the public key given and converts it into the PublicKey object needed to instantiate the KeyPair.
     *
     * @param publicKey The Public Key in Base64 Encoded String Form
     * @return PublicKey Object Corresponding to the String Input
     * @throws java.security.NoSuchAlgorithmException Throws exception if specified cryptographic algorithm does not exist.
     * @throws java.security.spec.InvalidKeySpecException Throws exception if the KeySpec is Invalid.
     */
    private PublicKey genPublicKey(String publicKey) throws Exception{
        byte[] decodedPB = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedPB);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }

    /**
     * Takes the private key given and converts it into a PrivateKey object needed to infatuate the KeyPair.
     *
     * @param privateKey Private Key in PKSC8 Format, Base64 encoded String Form
     * @return PrivateKey Object Corresponding to the String Input
     * @throws java.security.NoSuchAlgorithmException Throws exception if specified cryptographic algorithm does not exist.
     * @throws java.security.spec.InvalidKeySpecException Throws exception if the KeySpec is Invalid.
     */
    private PrivateKey genPrivateKey(String privateKey) throws Exception{
        byte[] decodedPR = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPR);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    public String getPublicKey(){ return publicKey; }
    public String getPrivateKey(){ return privateKey; }
}
