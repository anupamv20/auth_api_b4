package Authy;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.security.SignatureException;

 //Tx: content --> encrypt(content,other publlic key) --> encode --> send(thru GC, CM)
 //Rx: de-code --> decrypt(encrypted_content, own private key) --> return data to GC,SMS,WebUI

class EncryptDecrypt {

    private PublicKey pub = null;
    private PrivateKey priv = null;



    static byte[] encryptSendData(PublicKey pub, String data) throws Exception {
        // using own public key

        System.out.println("starting encryption");
 //       Cipher cipher = Cipher.getInstance("RSA"); // AM // default
//        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding"); // VoIP // x
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // SMS // specific one



        cipher.init(Cipher.ENCRYPT_MODE, pub);
        byte[] encryptedMessageBytes = cipher.doFinal(data.getBytes());
        String outMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);

        System.out.println("encrypted msg is:"+outMessage);
        return encryptedMessageBytes;

    }

    static String decryptRxData( PrivateKey prv, byte[] RxData) throws Exception {
        //using own private key
        System.out.println("starting decryption");

        String decryptedMessage = "";
//            Cipher decryptCipher = Cipher.getInstance("RSA");
//        Cipher decryptCipher = Cipher.getInstance("RSA/ECB/NoPadding");
        Cipher decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");


        decryptCipher.init(Cipher.DECRYPT_MODE, prv);
            byte[] decryptedMessageBytes;
            try {
                decryptedMessageBytes = decryptCipher.doFinal(RxData);

                decryptedMessage = new String(decryptedMessageBytes);
                System.out.println("decrypted data is: "+decryptedMessage);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("oops...private key mismatch");
                return null;
            }


        return decryptedMessage;
    }



    // this function accepts input data in byte and generates digital signature using
    //its private key, the return format of signature is bytes
    private byte[] getSignature(byte[] input)  {


        Signature sign = null;
        try {
            sign = Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        PrivateKey self_privkey = VerifyCerts.returnMyPK();

        try {
            sign.initSign(self_privkey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
        byte[] signature;
        try {
            sign.update(input);
            signature = sign.sign();
        } catch (SignatureException e) {
            e.printStackTrace();
            return null;
        }

        return signature;
    }

}
