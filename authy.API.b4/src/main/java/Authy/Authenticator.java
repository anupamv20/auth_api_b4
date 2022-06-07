package Authy;
//package com.ehelpy.brihaspati4.authenticate ;


import GC.GlueCode;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/* last update: 05/06/22
author: anupamv20
Authenticator API class
it is called by ClientMainNew class
 *
 * */
public class Authenticator extends Thread {

    private static Authenticator auth;

    private  X509Certificate self_cert;
    private  static long time_offset=0;
    private  PublicKey self_pubkey;
    private X509Certificate server_cert;
    private boolean auth_verify_flag=false;
    private  static String response_at_auth=null;
    private  int max_limit_of_buffer=1024;
    private  ArrayList process_var_for_AM;
    private  LinkedList am_buffer_gc= new LinkedList();

    /*
    * returns the object of Authenticator.
    * Only one Instance will be created.
    * */
    public static synchronized Authenticator getInstance() throws Exception {
        System.out.println("i am in AM instance");

        if (auth == null) {
            auth = new Authenticator();
        }
        return auth;
    }
/*
* Constructor of authenticator
* - it starts processing thread to read query or request coming from GC
* - it calculates and stores the time-offset by comparing B4 server time and system current time,
*   this offset will later be used to get user's correct time
* */
    Authenticator() throws Exception {

        System.out.println("i am in AM");

        System.out.println("starting ProcessingThread_for_AM");
        ProcessingThread_for_AM(); // for continuous reading+processing

        time_offset = UpdatedDateTime.getTimeOffset(); // calculates time-offset

    }

/*
    this function is called by GC to get status of AM's input buffer
     returns:  entries in AM's input buffer
*/
    public LinkedList display_am_buffer_gc(){
        return (am_buffer_gc);
    }

/*
    GC will call this method to write req / que in the AM's input buffer
*/
    public void addMessage_am_buffer_gc(List gcMessage) 	{
        if ((am_buffer_gc.size()) < max_limit_of_buffer)
        {
            am_buffer_gc.add(gcMessage);
            System.out.println("List received by AM’s input buffer.");
        }
        else
        { System.out.println("AM’s input buffer is Full.");
        }
    }

/*
    AM and its classes will call this method to send query in the GC's input buffer
*/
    static void sendQuery_gc_buffer_am(String type, String src, String des, String func, String arg) throws Exception {
        GlueCode gc = GlueCode.getInstance();
        List abc = new ArrayList();
        abc.add(type); // 0:que
        abc.add(src); // 1:source
        abc.add(des); // 2:destination
        abc.add(func); // 3: function
        abc.add(arg); // 4: arguments in tlv format
        System.out.println("temp list abc of AM:" +abc);
        System.out.println(" Query being sent to gc ");
        gc.addMessage_gc_buffer_am(abc);
    }

/*
    AM will call this method to send response in the GC's input buffer
*/
    private void sendResponse_gc_buffer_am(String response) throws Exception {

        GlueCode gc = GlueCode.getInstance();
        ArrayList temp1 ,temp2; //
        System.out.println("in AM sendResponse function");
        temp1 =  process_var_for_AM;
        temp2 = process_var_for_AM;

        am_buffer_gc.pollFirst(); //remove first element from list
        temp1.set(0, "res"); // request type
        temp1.set(2, temp2.get(1)); // destination
        temp1.set(1, "am"); // source
        temp1.set(3, temp2.get(3)); // que function
        temp1.set(4, temp2.get(4)); // que argument

        temp1.add(response); // adding response as the 5th field // tlv format

        System.out.println("\nTemp Var: " + temp1 + "\n");
        System.out.println("sending response to gc.");
        gc.addMessage_gc_buffer_am(temp1);
    }

/*
    AM runs this thread to process response or query
    if thread detects "response' : it takes out response value from 5th field and setResponse
    if thread detects "query" : it calls relevant function, gets response from function and sendResponse to GC
*/
    private void ProcessingThread_for_AM()  {
        Thread processingThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true)
                {
//                    System.out.println("AM processing thread running");

                    if (am_buffer_gc.size() > 0)
                    {
                        process_var_for_AM = (ArrayList) am_buffer_gc.get(0);
                        am_buffer_gc.pollFirst(); // removes first element
                        System.out.println("entry in AM proc var is:"+process_var_for_AM);

                        if (process_var_for_AM.get(0) == "res") // get value out of response
                        {
                            System.out.println("process_var_for_AM started with Res");
                            String res = (String) process_var_for_AM.get(5);
                            System.out.println("response rcxd by AM processing thread is:"+res);
                            try {
                                setResponse(res);
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("error in setting the response in Authenticator");

                            }

                        }
                        else if (process_var_for_AM.get(0) == "que") // send response for the query
                        {
                            String temp_func = (String) process_var_for_AM.get(3); // get function field
                            // taking single argument as on date // to be changed for multiple arg
                            String temp_arg = (String) process_var_for_AM.get(4); // get argument field
                            String temp_response = null;

                            if(temp_func == "getSelfCert") {
                                System.out.println("getSelfCert function is called");
                                try {
                                     temp_response = getSelfCert().toString();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    sendResponse_gc_buffer_am(temp_response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }else if(temp_func == "getUpdatedTime"){
                                System.out.println("getUpdatedTime function is called");
                                try {
                                    temp_response = getUpdatedTime().toString();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    sendResponse_gc_buffer_am(temp_response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }else if(temp_func == "getSelfPubKey"){
                                System.out.println("getSelfPubKey function is called");
                                try {
                                    temp_response = getSelfPubKey().toString();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    sendResponse_gc_buffer_am(temp_response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }else if(temp_func == "getStringHash"){
                                System.out.println("getStringHash function is called");
                                try {
                                    temp_response = getStringHash(temp_arg);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    sendResponse_gc_buffer_am(temp_response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }else if(temp_func == "getPubKeyFromCert"){
                                System.out.println("getPubKeyFromCert function is called");
                                try {
                                    temp_response = getPubKeyFromCert(temp_arg).toString();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    sendResponse_gc_buffer_am(temp_response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                            else if(temp_func == "getDecryptedDataWithSelfPvtKey"){
                                System.out.println("getDecryptedDataWithSelfPvtKey function is called");
                                try {
                                    temp_response = getDecryptedDataWithSelfPvtKey(temp_arg.getBytes(StandardCharsets.UTF_8)).toString();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    sendResponse_gc_buffer_am(temp_response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                            else if(temp_func == "getEncryptedMsgWithSelfPvtKey"){
                                System.out.println("getEncryptedMsgWithSelfPvtKey function called");
                                try {
                                    temp_response = getEncryptedMsgWithSelfPvtKey(temp_arg.getBytes(StandardCharsets.UTF_8)).toString();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    sendResponse_gc_buffer_am(temp_response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }else if(temp_func == "getSignature"){
                                System.out.println("getSignature function called");
                                try {
                                    temp_response = getSignature(temp_arg.getBytes(StandardCharsets.UTF_8)).toString();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    sendResponse_gc_buffer_am(temp_response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                            else {
                                System.out.println("this function not present in Auth API");
                                System.out.println("returning null as a response");
                                try {
                                    sendResponse_gc_buffer_am(null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                    }
                    try {
                        //System.out.println("Processing Thread going to sleep");
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        processingThread.start();

    }
    /*
    * -this function is called by ClientMainNew: to check the status of Valid Certificate
    * -it will check the availability of keystore
    * -if available: it will call VerifyCerts class to get alias and password from user and checks validity of user certificate
    * -if not available: it will call KeystoreGeneration class to either retrieve keystore from IA (registered user)
    *  or generate new keystore
    *  -after validation this function will return true
    * */

    public boolean FlagCheck() throws Exception {
        if (!IntegrityChecks.checkKeyStoreExists()) // keystore does not exist, we create new keystore
        {
            System.out.println("keystore not detected in local storage");
            String email_id = Test_webUI_Inputs.getEmail();

            // KeystoreGeneration : will create keystore
            if (KeystoreGeneration.generateKeystore(email_id)) // if KS successfully generated and stored
            {
                System.out.println("KS generated ");
                System.out.println("now will do verification check");
                while(!auth_verify_flag) // runs until flag becomes true
                {
                    auth_verify_flag = VerifyCerts.verifyCert(); // re-run option page :::
                }
            }
        }
        else // when keystore  exist: we can directly do verification check
        {
            System.out.println("found keystore in local storage: continuing with verification check");
            while(!auth_verify_flag) // runs until flag becomes true
            {
                auth_verify_flag = VerifyCerts.verifyCert(); // re-run option page :::
            }
        }

        System.out.println("your cert is valid");
        server_cert = VerifyCerts.returnServerCert();
        self_cert = VerifyCerts.returnClientCert();
        self_pubkey = self_cert.getPublicKey();

        System.out.println("your pubic key is::"+self_pubkey);
        System.out.println("cert period is : ");
        System.out.println(self_cert.getNotBefore());

        System.out.println(self_cert.getNotAfter());


        System.out.println("....flag for Am is true....other services can start");

        //testing
//        String s = Test_webUI_Inputs.getDevice_Id();
//        String ss = Test_webUI_Inputs.getNode_Id();
//
//
//        System.out.println("got response from test webui for getDevice_Id : "+s);
//        System.out.println("got response from test webui for getNode_Id : "+ss);
//        System.out.println("###################################################################");
//        System.out.println(getUpdatedTime());
//        System.out.println(getSelfCert());
//        System.out.println(getSelfPubKey());
            //testing ends

        return auth_verify_flag;

    }

    /*  role:    - this function returns user's own certificate,
       arg : nothing
       return : X509Certificate
   */
    private X509Certificate getSelfCert() {
        return self_cert;
    }

    /*  role:    - this function calculates user's corrected time based on the offset,
        arg : nothing
        return : latest corrected date-time in Date format
    */
    static Date getUpdatedTime() throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date date;
        date = sdf.parse(UpdatedDateTime.getCurrentDateTime());
        long system_in_mills = date.getTime();
        String updated_user_time = sdf.format(system_in_mills + time_offset);
        date = sdf.parse(updated_user_time);
        return date;
    }

  /* role:    - this function returns user's public key,
     arg : nothing
     return : public key as PublicKey */

    private PublicKey getSelfPubKey() {
        return self_pubkey;
    }

    /* role:    - this function calculates hash of the data,
              - this function uses IntegrityChecks class
              - Algo used : SHA-256
     arg : input data as String
     return : hash value as string */
    private String getStringHash (String data) throws NoSuchAlgorithmException {
        return IntegrityChecks.stringHash(data);
    }

   /*  role:    - this function extracts the public key attached to the cert,
              - this function first converts string format to X509 format
              - the function will carry out two checks before extracting the public key:
              - Identity server's signature verification check and certificate expiry date.
              - once both checks are true, it will return the public key, else returns null
     arg : certificate as string
     return : Public Key */
    private PublicKey getPubKeyFromCert(String cert) throws IOException {

        X509Certificate othercert = ChangeCertFormat.convertToX509Cert(cert);
        try{
            othercert.verify(server_cert.getPublicKey()); // signature check
            othercert.checkValidity(Authenticator.getUpdatedTime()); // expiry check
        }
        catch (Exception e1)
        {
            System.out.println(" submitted certificate signature doesn't matches or expired");
            return null;
        }

        return othercert.getPublicKey();

    }


 /*  role: - this function will decrypt the msg with its own private key, which was encrypted by its public key,
           - used for one-to-one encryption-decryption process,
     arg: encrypted data as byte array
     returns: plain data as byte[], if exception occurs: it will return null. */
    private byte[] getDecryptedDataWithSelfPvtKey (byte[] encrypted_input)
    {
        Cipher cipher = null;
        byte[] decrypt_output = null;

        // this will call my priv key for encryption purpose
        PrivateKey self_privkey = VerifyCerts.returnMyPK();

        try {
            System.out.println("The length of input stream is" + encrypted_input.length);
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            System.out.println("starting decryption");
            cipher.init(Cipher.DECRYPT_MODE, self_privkey);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {

            decrypt_output = cipher.doFinal(encrypted_input);
            System.out.println("Decryption completed");

        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return decrypt_output;

    }


/*   role: - this function will encrypt the msg with its own private key,
           - so that others peers can decrypt using public key : for broadcast/multicast,
           - one to many encryption process.
     arg: data as byte array
     returns: encrypted output as byte[], if exception occurs: it will return null. */
    private byte[] getEncryptedMsgWithSelfPvtKey(byte[] input)
    {
        byte[] output = null;
        Cipher cipher = null;

        // this will call my priv key for encryption purpose
        PrivateKey self_privkey = VerifyCerts.returnMyPK();
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            System.out.println("starting encyption");
            cipher.init(Cipher.ENCRYPT_MODE, self_privkey);
           // cipher.init(Cipher.ENCRYPT_MODE, self_pubkey); // for checking encryption with pub key


            output = cipher.doFinal(input);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return null;

        } catch (NoSuchPaddingException e2) {
            e2.printStackTrace();
            return null;

        }
        catch (InvalidKeyException e3) {
            e3.printStackTrace();
            return null;

        }

        catch (IllegalBlockSizeException e4) {
            e4.printStackTrace();
            return null;

        } catch (BadPaddingException e5) {
            e5.printStackTrace();
            return null;

        }
        System.out.println("  msg encrpted using own private key msg completed");

        return output;
    }

 /*    role: this function calculates the digital signature
     arg: data as byte array
     returns: signature as byte[], if exception occurs: it will return null. */
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


/*     role: the response AM Processing Thread receives, will get stored here
     arg: String
     returns: nothing */
    private void setResponse(String res) {
        System.out.println("setResponse method called");
        System.out.println("setting response:"+res);
        response_at_auth = res;
    }

/*     role: the response AM Processing Thread receives, will get stored in "response_at_auth"
           this function will return the value stored in the class variable
     arg: nothing
     returns: String*/
    static String getResponse() {
        //System.out.println("getResponse method called");
        return response_at_auth;
    }

/*     role: it will make the response class variable as "null", for future reuse
     arg: nothing
     returns: nothing*/
    static void resetResponse()
    {
        response_at_auth = null;
    }


}







