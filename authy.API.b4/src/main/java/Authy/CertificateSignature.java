package Authy;

//package com.ehelpy.brihaspati4.authenticate ;

import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Logger;

// 23 jan 22

// creates certs chain
//
//This code forward the newly generated self signed certificate to the identity server for Authentication
// It carries out the OTP verification for verification of Email Id
// takes response from IS : two certs : server and clients both signed
// returns cert chain

 class CertificateSignature {
     private static final Logger log = Logger.getLogger(String.valueOf(CertificateSignature.class));


    @SuppressWarnings("static-access")
     static X509Certificate[] getCertChain(X509Certificate cert) throws Exception {

        log.info("Welcome to CertificateSignature .Sending new cert to Iden Server for sign");
        String certstring = cert.toString(); // converts cert data into string
        byte[] certbyte = cert.getEncoded(); // encode it
        String certstringbyte = new String(Base64.getEncoder().encode(certbyte));
        //String mserverurl ="http://localhost:8084/beans_b4server";
        String mserverurl = "http://ictwiki.iitk.ac.in:8080/b4server";
        String MSrequrl = mserverurl + "/ProcessRequest?req=sscccertsign&cert=" + URLEncoder.encode(certstring, "UTF-8");
        CreateHttpConnection http = new CreateHttpConnection();
        boolean server1 = http.sendGet(MSrequrl); // gets server status; true or false
        try {
            http.sendPost(MSrequrl); //  sends the request url to IS

        } catch (Exception e) {
            System.exit(0);
        }

        X509Certificate[] CertChain = new X509Certificate[2];
        if (server1) {

            final String OTP = Test_webUI_Inputs.getOTP();


            String deviceid = Test_webUI_Inputs.getDevice_Id();
            log.info("device id rxd Certificate signature  is:"+deviceid);
            String nodeid = Test_webUI_Inputs.getNode_Id();
            log.info("node id rcxd in Certificate signature is:"+nodeid);

            final String MSrequrl2 = mserverurl + "/otp_verification?req=otpverify&OTP=" +
                    URLEncoder.encode(OTP, "UTF-8") + "&cert=" + URLEncoder.encode(certstring, "UTF-8") +
                    "&certstringbyte=" + URLEncoder.encode(certstringbyte, "UTF-8")
                    + "&deviceid=" + deviceid + "&nodeid=" +nodeid ;

            log.info("sending url with certifictae + otp + device id + node id to I-server");

            try {
                CertChain = GetCertsFromServer.sendPost(MSrequrl2); // returns client's and server's certificate qty:02

            } catch (Exception e) {
                System.out.println(e);
                System.out.println("error in getting signed certs from Auth Server, Kindly re-try");
                System.exit(0);

            }

            log.info("server certi is: " + CertChain[0]); // server certi
            log.info("client certi is " + CertChain[1]); // client certi

        }
        return CertChain;
    }

}

