
package Authy;
//package com.ehelpy.brihaspati4.authenticate;

import java.security.cert.X509Certificate;
import java.util.logging.Logger;


// this class is called when local KS is to be prepared:
// it takes email id and check with IS: if its a new userid or it an already existing userid
// if its a new user: 15 : this call will create new KS for it
// it its an old id: 14: it will do recovery of certs and prepare KS

class KeystoreGeneration {

    private static final Logger log = Logger.getLogger(String.valueOf(KeystoreGeneration.class));

     static boolean generateKeystore (String email) throws Exception {

        boolean KS_generate_flag = false;

        String email_id = email;
//        log.info("inside generate KS");

        String mserverurl ="http://ictwiki.iitk.ac.in:8080/b4server";
        String MSrequrl = mserverurl +"/ProcessRequest?req=checkkeystore&emailid="+email_id;

        CreateHttpConnection http_1 = new CreateHttpConnection();

        if (http_1.sendGet(MSrequrl))    // if IS status is true
        {
            int val=http_1.sendPost(MSrequrl,"checkkeystore");
            //log.info("response from is: "+val);

            if(val==15) // new userid
            {
                // create new self signed cert
                // sent cert to Identity Server and receives two signed certs
                // prepare KS

                //X509Certificate newcert = GenerateCertificate.createSelfSignedCert();
                X509Certificate newcert = GenerateSelfSignedCert.createSelfSignedCert();

                //log.info("new certificate is:  "+newcert);

                X509Certificate[] Certs = new X509Certificate[2];
                Certs = CertificateSignature.getCertChain(newcert);

                if (KeystoreUtilities.prepareKS(Certs)) // .jks ks
                {
                    KS_generate_flag = true; // KS is prepared and stored with key & certs
                }

            }
            else if(val==14) // email id ava with IS
            {
                MSrequrl = mserverurl +"/ProcessRequest?req=keystorecheckotpgen&emailid="+email_id;
                if ( http_1.sendPost(MSrequrl,"keystorecheckotpgen") == 1 ) // server sends otp
                {
                   // String otp=Gui.getotp();    :::::    to be changed with webUI
                    String otp = Test_webUI_Inputs.getOTP();

                   // to be changed as per Glue code query request ::::::::::
                    MSrequrl = mserverurl +"/ProcessRequest?req=keystorecheckotpverify&emailid="+email_id+"&otp="+otp
                            +"&deviceid="+ Test_webUI_Inputs.getDevice_Id();

                    val=http_1.sendPost(MSrequrl,"keystorecheckotpverify"); // server verifies otp
                    log.info("keystorecheckotpverify val is:"+val); // response : 16 or 17

                    if(val==16) // :: when KS is restored : true
                    {
                        log.info("inside val==16");
                        log.info("KS generated: proceeding towards : validity check");
                        KS_generate_flag = true;
                        return KS_generate_flag;
                    }
                    else if(val==17) // email id ava with IS, but not certs: so make new cert & kS
                    {
                        log.info("inside val==17");
                        log.info("no certs with IS as well : generating new certificate");

                        //X509Certificate newcert = GenerateCertificate.createSelfSignedCert();
                        X509Certificate newcert = GenerateSelfSignedCert.createSelfSignedCert();

                        log.info("new certificate is:  "+newcert);
                        X509Certificate[] Certs = new X509Certificate[2];
                        Certs = CertificateSignature.getCertChain(newcert);
                        if (KeystoreUtilities.prepareKS(Certs))
                        {
                            KS_generate_flag = true;
                        }

                    }
                    else
                    {
                        log.info("neither 16 nor 17 as for req type: keystorecheckotpverify");
                        KS_generate_flag = false;

                    }

                }

            }
        }
        else // cant connect to server
        {
            log.info(" Check internet connection or");
            log.info(" IS not responding: contact network Adm");
            System.exit(0);
        }
    return KS_generate_flag;
    }

}