//package com.ehelpy.brihaspati4.authenticate;
package Authy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;


/* 06 Feb 22
 * -- This class is used to generate Hash of String  .
 * -- To check if keystore is available in local machine
 * 
 */

 class IntegrityChecks {

     private static final Logger log = Logger.getLogger(String.valueOf(IntegrityChecks.class));

     // This method is used to create a hash of a String
     static String stringHash(String data) throws NoSuchAlgorithmException     {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashInBytes = md.digest(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes)
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    static boolean checkKeyStoreExists() {

        boolean flag=false;

        File f = new File("SignedClientKeyStore.JKS");
       // File f = new File("SignedClientKeyStore.PKCS12"); //pkcs12

        if(f.exists() && !f.isDirectory())
        {
            flag=true;
        }
        return flag;
    }
}

