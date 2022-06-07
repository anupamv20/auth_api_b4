package GC;

//package com.ehelpy.brihaspati4.GC;

import Authy.Authenticator;
import routingmgmt.RoutingManager;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class ClientMainNew extends Thread
{
    private static X509Certificate client_cert = null;
    private static X509Certificate server_cert = null;
    private static boolean flagset = false;
    public static int CtrlConsoleOut=0;
    public static String Device_Id="";
    public static String Node_Id="";
    public static String Node_Ip="";
    public static int port = 9000;
    public static String updated_time = "";
    private static PublicKey client_pubkey = null;

    public static void main(String args[]) throws Exception
    {

        GlobalObject globj= GlobalObject.getGlobalObject();
        globj.setRunStatus(true);
//

//
        RoutingManager rMgr = RoutingManager.getInstance();
//        if( rMgr.isAlive() != true) {
//            rMgr.start();
//        } else {
//            printLog("ClientMainNew: RoutingManager thread already running.");
//        }
        globj.updateObjRef("RoutingManager", rMgr);

        Authenticator auth = Authenticator.getInstance();
         if( auth.isAlive() != true) {
           auth.start();
         } else {
        printLog("ClientMainNew: authenticator thread already running.");
         }


//        // Create a file object
//        File f = new File("java/");
//
//        // Get the absolute path of file f
//        String StaticFilePath = f.getAbsolutePath();
//
//        WebUIServer WebUIS = WebUIServer.getInstance();
//        WebUIS.start(port,StaticFilePath);
//        globj.updateObjRef("WebUI global obj", WebUIS);



        GlueCode gc = GlueCode.getInstance();
        System.out.println("Now I am entering in GC.");
        gc.mainMethod();
        System.out.println("GC Started.");
        globj.updateObjRef("gluecode", gc);

        flagset = auth.FlagCheck();
        globj.updateObjRef("Authenticator", auth);


//        System.out.println("time sync checks....");
//
//        System.out.println(auth.getUpdatedTime());
//        System.out.println("time sync checks ends....");


        // RM asking from AM test:: outputs
       // rMgr.RM_AM_Test();

        String s = "hello123";
//        System.out.println("raw data is : "+s);
//        System.out.println("its hash value is : "+ auth.getStringHash(s));

        byte a [] = s.getBytes();
        System.out.println("plain data is:"+ s);

//        byte sign[] = auth.getSignature(a);
//        System.out.println(new String (sign));

        //byte e_data [] = auth.getEncryptedMsgWithSelfPvtKey(a);

//        String outMessage = Base64.getEncoder().encodeToString(e_data);
//
//        System.out.println("encrypted msg is:"+outMessage);
//
//        String d_data = new String (auth.getDecryptedDataWithSelfPvtKey(e_data));
//
//        System.out.println("decrypted data is:+"+d_data);




        if(flagset) {
   
            // start other services


            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
	
        // Loop created so that object reference of Global object is maintained and it is not garbage collected.
        // It need to exist for all the modules to run.
        while(globj.getRunStatus()) {
            Thread.sleep(30000);
        }
    }

    public static void printLog(String name) {

        System.out.println("Log = " + name);
    }


}
