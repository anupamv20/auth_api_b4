package Authy;
//
//package com.ehelpy.brihaspati4.authenticate;
//
//

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Logger;

////28 jan 22
////This function carries out verification of the existing certificate of the peer
////15 days prior to certificate expiry, the system will prompt the user for renewing the certificate
////In case the peer certificate is not valid, it prompts the peer to generate a new certificate
////It also forward the certificate for signature to the Identity server
////Entire package whenever either the client certificate or server certificate is required the functions are
//// defined in this class

 class VerifyCerts {

     private static final Logger log = Logger.getLogger(String.valueOf(VerifyCerts.class));

    private static X509Certificate x509servercert = null;
    private static X509Certificate x509clientcert = null;

    private static KeyStore keystore = null;
    private static String keystorepass ;
    private static String keystorealias;
    private static	Certificate[] cert = null;
     private static PrivateKey myprivKey = null;


     //
    @SuppressWarnings("static-access")
// This function loads keystore and checks the validity of the certificate

    static boolean verifyCert() throws Exception {
        boolean status = false; // flag for KS verification status

// ask user options : restoration(1), verify/ok ,  new(3), revocation(2)
        keystorepass = Test_webUI_Inputs.getVerificationPassword();
        if(keystorepass==null ){
            System.out.println("got no input..Re-try");
            keystorepass = Test_webUI_Inputs.getVerificationPassword();
            if(keystorepass==null){
                System.out.println("again no input...exiting the application");
                System.exit(0);
            }

        }

        if (keystorepass.equals("1")) // recovery of KS
        {
            if(KSrecoveryByUser()) // to recover KS
            {
                System.out.println("KS successfully restored");
                return false; //  return false, so that auth can again ask user for options
            }

        } else if (keystorepass.equals("2")) //  revocation process
        {

            String emailid=Test_webUI_Inputs.getEmail(); // to be changed as per webui
            String mserverurl ="http://ictwiki.iitk.ac.in:8080/b4server";
            String MSrequrl = mserverurl +"/ProcessRequest?req=certificaterevocationotpgen&emailid="+emailid;

            CreateHttpConnection http_1 = new CreateHttpConnection ();

            if(http_1.sendPost(MSrequrl,"certificaterevocationotpgen")==1)
            {
                String otp=Test_webUI_Inputs.getOTP(); // to be changed as per webui
                MSrequrl = mserverurl +"/ProcessRequest?req=certificaterevocationotpverify&emailid="+emailid+"&otp="+otp;
                int val=http_1.sendPost(MSrequrl,"certificaterevocationotpverify");
                System.out.println("revocation request value is:"+val);
                if(val==6)
                {

                    String revocationreason=Test_webUI_Inputs.getRevocationReason();
                    MSrequrl = mserverurl +"/ProcessRequest?req=certificaterevocationreason&emailid="+emailid+"&reason="+revocationreason;
                    int val2=http_1.sendPost(MSrequrl,"certificaterevocationreason");
                    if(val2==7)
                    {
                        //X509Certificate newcert = GenerateCertificate.createSelfSignedCert();
                        X509Certificate newcert = GenerateSelfSignedCert.createSelfSignedCert();

                        log.info("New certificate of node is created");
                        X509Certificate[] Certs = new X509Certificate[2];
                        Certs = CertificateSignature.getCertChain(newcert);

                        if (KeystoreUtilities.prepareKS(Certs)) // when KS is prepared
                        {
                            return false; // so that user can be shown option form again

                        }

                    }

                }
                else if(val==4)
                {
                    System.out.println("Authenticity of Email not confirmed");
                    System.out.println("restart the app and kindly enter correct email");
                    // correct otp !!!
                    System.exit(0);
                }
                else if(val==3)
                {
                    System.out.println("No user record available");
                    // redirect to new cert generation
                    //steps
                    System.out.println("Contact n/w admin");
                    System.exit(0);

                }
            }
        } else if (keystorepass.equals("3")) {   // new cert generation

            // new cert and new KS
//            X509Certificate newcert = GenerateCertificate.createSelfSignedCert();
            X509Certificate newcert = GenerateSelfSignedCert.createSelfSignedCert();

            log.info("new certificate is:  "+newcert);
            X509Certificate[] Certs = new X509Certificate[2];
            Certs = CertificateSignature.getCertChain(newcert);
            log.info("got cert chain from Auth Server certificate");

            if (KeystoreUtilities.prepareKS(Certs)) // true when Ks is prepared and stored // false when KS is not prepared
            {
                System.out.println("new valid Certs generated and store.");
                return true; // no need to verify cert validity as, these are created just now
            }

        } else //  OK // user enters password and hits OK
        {
            keystorealias = Test_webUI_Inputs.getAlias();


            try {
                keystore = KeystoreUtilities.loadKeyStore(keystorealias, keystorepass); // returns the keystore
            } catch (Exception e) {
               // e.printStackTrace();
                log.info("incorrect password ");
                log.info("Kindly enter correct password");
                return false; // this will end this verification process and authenticator will restart verification process

            }

            // enters only when loaded KS is empty
            if (keystore == null) // keystore is loaded , but its empty, ie no certs and priv key
            {
                log.severe("loaded KS is null");
                log.info("kindly Recover the Certs from Auth Server");
                return false; // this will end this verification process and authenticator will restart verification process

            }

            try{
                cert = keystore.getCertificateChain(keystorealias);// Returns the cert associated with given alias
                myprivKey = (PrivateKey) keystore.getKey(keystorealias,keystorepass.toCharArray());
            }
            catch(Exception e)
            {
                log.info("error occured in geting certs out of Keystore:"+e);
                return false;

            }

            //log.info("Cert with given alias in KEYSTORE is " + cert);
            if (cert == null) {
                System.out.println(" No certs available for given alias ");
                System.out.println(" Re-enter correct alias or generate new certificate or recover certs from Auth Server ");
                return false; // re-route the application for user inputs
            }

            x509servercert = (X509Certificate) cert[0]; //type casting
            x509clientcert = (X509Certificate) cert[1];

            if (x509clientcert instanceof X509Certificate) {
                try {
                    // checking whether the "updated date is withing the start-end date of cert"
                    x509clientcert.checkValidity(Authenticator.getUpdatedTime());
                    String srnum = x509clientcert.getSerialNumber().toString();
                    String mserverurl = "http://ictwiki.iitk.ac.in:8080/b4server";
                    //String mserverurl ="http://localhost:8084/beans_b4server";
                    String MSrequrl = mserverurl + "/ProcessRequest?req=checkcrl&certsrno=" + srnum;
                    CreateHttpConnection http_1 = new CreateHttpConnection();
                    int val = http_1.sendPost(MSrequrl, "checkcrl");
                    //log.info("CRL check response is: " + val);
                    if (val == 25)
                        {
                            log.info("Checked certificate validity. Your Certificate is VALID.");
                            status = true;
                        }
                    else if (val == 24)
                    {
                        log.info("Certificate compromised: Generate New certificates");
                        return false;
                    }
                    else
                    {
                        log.info("Something wrong with cert check: kindly log in again");
                        return false;

                    }


                } catch (Exception e) {
                    //e.printStackTrace();
                    log.info(e.getMessage());
                    log.info("Your certificate has EXPIRED (not valid). Select new Cert Acquisition");
                    status = false;
                    // move to new cert generation thru user Option window

                }
                Date certNotAfter = x509clientcert.getNotAfter();
                Date now = new Date();
                long timeLeft = certNotAfter.getTime() - now.getTime(); // Time left in ms
                long days = timeLeft / (24 * 3600 * 1000);
                log.info("Your Certificate is valid for only " + days + " days");
                if (days < 16) {
                    String msgg = "Acquire a new certificate immediately";
                    // Gui.showMessageDialogBox(msgg);
                    log.info("" + msgg); // webui alert msg

                }
            } else // certs not x509
            {
                log.info("Certificate is not X509 Type. Recover KS or Generate new Certs");
                return false; // return to option form
            }

        }

    return status;    // status- returns status of checkValidity method
    }


     static X509Certificate returnServerCert() throws Exception {
         if(x509servercert==null)
             // when user clicks generate new cert, verify function will deemed true and hence will not run
             // so we require certs from other class which had them
             x509servercert= GetCertsFromServer.returnServerCert();
         return x509servercert;
     }
     static X509Certificate returnClientCert() throws Exception {
         if(x509clientcert==null)
             x509clientcert= GetCertsFromServer.returnClientCert();
         return x509clientcert;
     }

     static PrivateKey returnMyPK(){
        if(myprivKey == null)
            myprivKey = GenerateSelfSignedCert.getMyPK();
        return myprivKey;
     }

     static boolean KSrecoveryByUser () throws Exception {
         boolean recover_flag = false;

         String emailid = Test_webUI_Inputs.getEmail();
         String mserverurl = "http://ictwiki.iitk.ac.in:8080/b4server";
         String MSrequrl = mserverurl + "/KeystoreRecovery?req=keystoreotpgen&emailid=" + emailid;

         RecoveryHttpConnection http_2 = new RecoveryHttpConnection();
         if (http_2.sendPost(MSrequrl, "keystoreotpgen") == 1) // otp sent
         {
             String otp = Test_webUI_Inputs.getOTP();
             MSrequrl = mserverurl + "/KeystoreRecovery?req=keystoreotpverify&emailid=" + emailid + "&otp=" + otp;

             X509Certificate[] Certs = new X509Certificate[2];
             Certs = http_2.sendPost(MSrequrl); // returns 2 certs
//                System.out.println("SERVER CERT :"+Certs[0]);
//                System.out.println("CLIENT CERT :"+Certs[1]);

             String keyStorepass = Test_webUI_Inputs.getCreationPassword();
             String keystorealias = Test_webUI_Inputs.getAlias();

         final KeyStore keystore = KeyStore.getInstance("JKS");
        //final KeyStore keystore = KeyStore.getInstance("PKCS12");

             keystore.load(null, keyStorepass.toCharArray());
//        log.info("null KS prepred");

             try
             {
                 final String pkey = new String(Files.readAllBytes(Paths.get("key.txt", new String[0])));
                 final byte[] keybytes = Base64.getDecoder().decode(pkey);
                 final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keybytes);
                 final KeyFactory fact = KeyFactory.getInstance("RSA");
                 final PrivateKey priv_client = fact.generatePrivate(keySpec);
                 try {
                     keystore.setKeyEntry(keystorealias, priv_client, keyStorepass.toCharArray(), Certs);
                 } catch (KeyStoreException e) {
                     System.out.println("error in KS recovery: setKeyEntry: "+e);
                     //e.printStackTrace();
                 }

             } catch (Exception e) {
                 //e.printStackTrace();
                 System.out.println("couldn't locate key.txt");
                 System.out.println("kindly contact nw admin to remove your previous records from server and then login as new user");
                 System.out.println("or");
                 System.out.println("Generate New Certfificates");
                 return false;
             }

             final FileOutputStream fos = new FileOutputStream("SignedClientKeyStore.JKS");
       // final FileOutputStream fos = new FileOutputStream("Test_SignedClientKeyStore.PKCS12");

             try {
                 keystore.store(fos, keyStorepass.toCharArray());
                 recover_flag = true;
                // System.out.println("KS successfully restored");
             } catch (KeyStoreException e) {
                 //e.printStackTrace();
                 System.out.println("error in keystore storing:"+e);
                 return false;
             } catch (IOException e) {
                // e.printStackTrace();
                 System.out.println("error in keystoring storing:"+e);
                 return false;
             } catch (NoSuchAlgorithmException e) {
                 e.printStackTrace();
                 System.out.println("error in keystoring storing:"+e);
                 return false;
             } catch (CertificateException e) {
                 System.out.println("error in keystore storing:"+e);
                 //e.printStackTrace();
                 return false;

             }

         }return recover_flag;

     }

}

