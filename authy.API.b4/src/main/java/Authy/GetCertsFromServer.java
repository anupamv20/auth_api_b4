package Authy;

//package com.ehelpy.brihaspati4.authenticate ;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;


//23 Jan 22
//This function send the url containing OTP, to the Identity server for Authentication of the peer.
// returns client's and server's certificate

 class GetCertsFromServer {
     private static final Logger log = Logger.getLogger(String.valueOf(GetCertsFromServer.class));

     private final static  String USER_AGENT = "Chrome";
    private static X509Certificate server_certificate =null;
    private static X509Certificate Client_certificate =null;

    // HTTP POST request // returns certificates

    static	X509Certificate[] sendPost(String urlmaster)  {
        @SuppressWarnings("unused")
        boolean flag = false ;
        URL obj = null;
        try {
            obj = new URL(urlmaster);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //add reuqest header
        try {
            con.setRequestMethod("POST");
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = null;
        try {
            wr = new DataOutputStream(con.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            wr.writeBytes(urlParameters);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            wr.flush();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            wr.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.info("\nSending submitted OTP to server URL " );
        //log.info("Response Code : " + responseCode);
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String inputLine;
        StringBuffer response = new StringBuffer();
        try {
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String recdmessage = response.toString();
        log.info("Recd Message : " + recdmessage);
        String[] ServerCert = recdmessage.split("ClientCert");
        String [] ClientCert = null;
        try {
            ClientCert = ServerCert[1].split("ClientCert");
        }
        catch(Exception e) {
            System.out.println(e);;
        }

        try {
            server_certificate = ChangeCertFormat.convertToX509Cert(ServerCert[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Client_certificate = ChangeCertFormat.convertToX509Cert(ClientCert[0]);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            System.out.println("otp is wrong or email is wrong");
            System.out.println("kindly restart the app and re-enter correct email/otp");
            System.exit(0);
        }

        X509Certificate[] Certs	= new  X509Certificate[2];
        Certs[0] = server_certificate;
        Certs[1] = Client_certificate;
        return Certs;
    }

    public static X509Certificate returnServerCert() {
        return server_certificate;
    }

    public static X509Certificate returnClientCert() {
        return Client_certificate;
    }
}

