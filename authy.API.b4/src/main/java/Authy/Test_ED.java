package Authy;

import java.security.*;

public class Test_ED {

    private static PrivateKey privKey = null;
    private static PublicKey pubKey = null;
    private static KeyPair keypair = null;

    public static void main(String[] args) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA","BC");
          //  KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

            keyGen.initialize(2048,new SecureRandom());
            keypair = keyGen.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }

        pubKey = keypair.getPublic();
        privKey = keypair.getPrivate();

       // System.out.println(privKey);
       // System.out.println(pubKey);



        String data = "hello123.m.m.m";
        System.out.println("plain data is:"+data);
//
        byte[] e_data = EncryptDecrypt.encryptSendData(pubKey,data);
//
        String d_data = EncryptDecrypt.decryptRxData(privKey,e_data);
        System.out.println("decrypted data: "+d_data);


    }

}
