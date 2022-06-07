package Authy;
//package com.ehelpy.brihaspati4.authenticate ;

//import java.io.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;


class KeystoreUtilities {

    private static final Logger log = Logger.getLogger(String.valueOf(KeystoreUtilities.class));

// this function will take two certs and prepare KS
    static boolean prepareKS(X509Certificate[] Certs) throws Exception {
        boolean prepareKS_flag = false;
        //log.info("in prepareKS");
        X509Certificate[] CertChain = new X509Certificate[2];
        CertChain = Certs;

//        log.info("servr cert iss : "+CertChain[0]);
//        log.info("client cert iss : "+CertChain[1]);

// to use webUI from getting inputs from user

       // final String alias1 = Gui.getaliasname(); // to be changed
        final String alias1 = Test_webUI_Inputs.getAlias();

//            final String password = Gui.getkeystorepass();
        final String password = Test_webUI_Inputs.getCreationPassword(); // KS pswd is string

        //------getting private key ----------------//
        //final PrivateKey priv_client = GenerateCertificate.priv();

        final String pkey = new String(Files.readAllBytes(Paths.get("key.txt", new String[0])));
        final byte[] keybytes = Base64.getDecoder().decode(pkey);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keybytes);
        final KeyFactory fact = KeyFactory.getInstance("RSA");
        final PrivateKey priv_client = fact.generatePrivate(keySpec);

        KeyStore keyStore = null;
        keyStore = KeyStore.getInstance("JKS"); // creating KS object

//        try {
        keyStore.load(null, password.toCharArray()); // KS loaded in null file with password
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        }

        try {
            keyStore.setKeyEntry(alias1, priv_client, password.toCharArray(), CertChain);

        } catch (KeyStoreException e) {
            e.printStackTrace();
            return false;
        }


        final FileOutputStream fos = new FileOutputStream("SignedClientKeyStore.JKS");

        try {
            keyStore.store(fos, password.toCharArray());
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (CertificateException e) {
//            e.printStackTrace();
        } catch (Exception e){
            log.info("error in writing/storing in file .jks");
            return false;
        }

        fos.close();

            //////sending KS to auth server for KS recovery//////

        String certstring=Certs.toString(); // converts to string
        final BufferedReader reader = new BufferedReader(new FileReader("SignedClientKeyStore.JKS"));
//        final BufferedReader reader = new BufferedReader(new FileReader("Test_SignedClientKeyStore.PKCS12"));

        final StringBuilder stringBuilder = new StringBuilder();
        for (char[] buffer = new char[10]; reader.read(buffer) != -1; buffer = new char[10]) {
               stringBuilder.append(new String(buffer));
            }
        reader.close();
        final String content = stringBuilder.toString();
//            System.out.println("content is: "+content);
        final CreateHttpConnection http_3 = new CreateHttpConnection();
        http_3.sendJKSPost("http://ictwiki.iitk.ac.in:8080/b4server/ProcessRequest", content, certstring);

            ////////end//////

        prepareKS_flag = true;

        log.info("Keystore prepared and stored with keys & certs");
        return prepareKS_flag;


    }
    //Before a key store can be accessed, it must be loaded.
     static KeyStore loadKeyStore(String keystorealias, String password) throws Exception // to be changed private::
    {
        KeyStore keystore = null;
        System.out.println("keystore loaded");

        try {
            keystore = KeyStore.getInstance("JKS");
//            keystore = KeyStore.getInstance("PKCS12");

        } catch (KeyStoreException e1) {
            log.severe("Unsupported instance type : Check instance of Keystore");
            //e1.printStackTrace();
            System.exit(0);
        }

            FileInputStream is = null;
        try {
            is = new FileInputStream("SignedClientKeyStore.JKS");
//            is = new FileInputStream("Test_SignedClientKeyStore.PKCS12"); // for testing & debugging

        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            log.severe("error in reading keystore file. check file extension");
            System.exit(0);
        }

        try {
            keystore.load(is,password.toCharArray());

            log.info("Password correct : KEYSTORE LOADED SUCCESSFULLY");

        } catch (IOException e) {
                log.info("incorrect password");
        //        e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                log.info("incorrect algorithm");
      //          e.printStackTrace();

        } catch (CertificateException e) {
                log.info("error : couldn't load certificates");
    //            e.printStackTrace();

        }

        return keystore;
    }

    // takes certs chain as parameter: return true is KS is prepared and certs & key added to it
    static boolean recoverKS (X509Certificate[] Certs) throws Exception {

        String keyStorepass = Test_webUI_Inputs.getCreationPassword();
        String keystorealias = Test_webUI_Inputs.getAlias();

      //  log.info("inside recoverKS");
        boolean recover_flag = false;

        X509Certificate[] CertChain = new X509Certificate[2];
        CertChain = Certs;
//        log.info("servr cert iss : "+CertChain[0]);
//        log.info("client cert iss : "+CertChain[1]);
        final KeyStore keystore = KeyStore.getInstance("JKS");
//        final KeyStore keystore = KeyStore.getInstance("PKCS12");

        keystore.load(null, keyStorepass.toCharArray());
        log.info("null KS prepred");

        try
        { final String pkey = new String(Files.readAllBytes(Paths.get("key.txt", new String[0])));
            final byte[] keybytes = Base64.getDecoder().decode(pkey);
            final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keybytes);
            final KeyFactory fact = KeyFactory.getInstance("RSA");
            final PrivateKey priv_client = fact.generatePrivate(keySpec);
            try
            {
                keystore.setKeyEntry(keystorealias, priv_client, keyStorepass.toCharArray(), CertChain);
            } catch (KeyStoreException e) {
                log.info("error in KS recovery: setKeyEntry");
                e.printStackTrace();
            }

        }
        catch (Exception e)
        {
            //e.printStackTrace();
            System.out.println("couldn't locate key.txt");
            System.out.println("kindly contact nw admin to remove your previous records from server and then login as new user");

        }

        final FileOutputStream fos = new FileOutputStream("SignedClientKeyStore.JKS");
//        final FileOutputStream fos = new FileOutputStream("Test_SignedClientKeyStore.PKCS12");

        try {
            keystore.store(fos, keyStorepass.toCharArray());
            recover_flag = true;
            log.info("KS successfully restored");
            } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            log.info("any of the cert could not be stored");
            e.printStackTrace();
        }
        return recover_flag;
    }

}
