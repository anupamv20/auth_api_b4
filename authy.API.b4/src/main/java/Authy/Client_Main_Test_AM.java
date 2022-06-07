package Authy;
//package com.ehelpy.brihaspati4.authenticate ;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class Client_Main_Test_AM extends Thread {

    private static String updated_time="";
    private static X509Certificate client_cert = null;
    private static X509Certificate server_cert = null;
    private static PublicKey client_pubkey = null;
    private static boolean flagset = false;


    public static void main(String args[]) throws Exception {

        Authenticator auth = Authenticator.getInstance();

     //   if( auth.isAlive() != true) {
//            auth.start();
//        } else {
//            System.out.println("ClientMainNew: authenticator thread already running.");
//        }

//        updated_time = auth.getUpdatedTime();
//        System.out.println("node's updated time is: "+updated_time);
//
//        client_cert = auth.getClientCert();
//        System.out.println("client certificate is:"+client_cert);
//
//        client_pubkey = auth.getClientPubKey();
//        System.out.println("client public key is:"+client_pubkey);

        flagset = auth.FlagCheck();
        System.out.println("client AM flag is:"+flagset);

//        System.out.println("sample string is: 123");
//        String strhash = auth.getStringHash("123");
//        System.out.println("its hash is:"+strhash);





    }
}
