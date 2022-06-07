
//package com.ehelpy.brihaspati4.authenticate ;
package Authy;

import java.io.*;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;


// 19 jan 22
//function of this class is to (i) get server status
// (ii)est connection with Identity server and check node's keystore availability
//(iii)send keystore content to IS

class CreateHttpConnection
{
    private static final Logger log = Logger.getLogger(String.valueOf(CreateHttpConnection.class));

    private final static String USER_AGENT = "Chrome";

    // HTTP GET request // this returns the status of server, we want to connect
    static boolean sendGet(String urlmaster) throws Exception
    {
        boolean serverstat = false;
        URL obj = new URL(urlmaster);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        StringBuffer response =null;
        int responseCode =0 ;
        log.info("\nChecking auth server status ");
        try {

            responseCode= con.getResponseCode();
        } catch (Exception e)
        {
            log.info("\nconnection error with server is : " +e);
           // server_down.id_exist();
        }

        log.info("Response Code : " + responseCode);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                serverstat = true;
            }
            in.close();
        } catch (Exception e) {
        }
        //log.info("Auth server status is: "+serverstat);
        return serverstat ;
    }
//    // HTTP POST request
    void sendPost(String urlmaster)  {
        try {
            URL obj = new URL(urlmaster);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            //System.out.println("\nSending 'POST' request to URL : " + urlmaster);
            log.info("\nSending 'POST' request to server " );
           // log.info("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine = null;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

        }
        catch(Exception e) {
            return;
        }
    }
//
//    this function sends keystore content to Identity server
    void sendJKSPost(String urlmaster,String data,String certificate)  {
        try {
            URL obj = new URL(urlmaster);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345&req=storejks&cert="+ URLEncoder.encode(certificate, "UTF-8")+"&jks="+URLEncoder.encode(data, "UTF-8");
            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            log.info("\nSending 'POST' request to server URL :sendJKSPost " );
            log.info("Send JKS Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine = null;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            log.info("response from auth server is: "+response);
        }
        catch(Exception e) {
            return;
        }
    }

    static int sendPost(String urlmaster,String reqtype) throws Exception {
        @SuppressWarnings("unused")
        boolean flag = false;
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
        //add request header
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
            //e.printStackTrace();
            log.severe("check internet connection");
            System.out.println("restart the application");
            System.exit(0);

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
        log.info("response code is: " + responseCode);
        log.info("\nSending 'POST' request to server URL  ");
        log.info("Response Code : " + responseCode);
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("the server encountered an unexpected condition that prevented it from fulfilling the request");
            System.out.println("restart the application");
            System.exit(0);
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

        if (reqtype == "forgotpasswordotpverify")
        {
            return Integer.parseInt(recdmessage);
        } else if (reqtype == "certificaterevocationotpverify")
        {
            return Integer.parseInt(recdmessage);
        } else if (reqtype == "certificaterevocationreason")
        {
            return Integer.parseInt(recdmessage);
        } else if (reqtype == "checkkeystore")
        {
            return Integer.parseInt(recdmessage);
        } else if (reqtype == "checkcrl")
        {
            return Integer.parseInt(recdmessage);
        } else if (reqtype == "keystorecheckotpverify")
        {
            if ( !recdmessage.equals("17") )  // otp is correct // IS has certs
            {
                log.info("Recd Message : " + recdmessage);
                final String[] ServerCert = recdmessage.split("ClientCert");
                String[] ClientCert = null;

                ClientCert = ServerCert[1].split("ClientCert");

                X509Certificate server_certificate = null;
                server_certificate = ChangeCertFormat.convertToX509Cert(ServerCert[0]);

                X509Certificate Client_certificate = null;
                Client_certificate = ChangeCertFormat.convertToX509Cert(ClientCert[0]);

                log.info("ServerCert is .." + server_certificate.toString());
                log.info("ClientCert is..." + Client_certificate.toString());

                final X509Certificate[] Certs = {server_certificate, Client_certificate};

                if (KeystoreUtilities.recoverKS(Certs)) // creates and stores Keystore
                    return 16; // when KS is restored : true
            }
                return Integer.parseInt(recdmessage);
            }
        else { // return 1 for req type: keystorecheckotpgen: means server has sent the otp
                return 1;
            }
        }


    // HTTP POST request
//    static	X509Certificate[] sendRevokePost(String urlmaster)  {
//
//        // not used as f now
//        return null;
//    }
}


