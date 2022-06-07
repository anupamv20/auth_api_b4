package Authy;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class Test_pkcs {
    private static KeyPair keys = null;
    private static PrivateKey prv = null;
    private static PublicKey pub = null;
    private static X509Certificate x509cert = null;

    private static X509Certificate x509clientcert = null;

    public static void main(String[] args) throws Exception {

        // send cert to IS for signature

//        x509cert = GenerateSelfSignedCert.createSelfSignedCert();
//        System.out.println(x509cert);
//
//        X509Certificate[] Certs = new X509Certificate[2];
//        Certs = CertificateSignature.certsign(x509cert);
//
//        System.out.println(Certs[0].getType());
//        System.out.println(Certs[1].getType());


        String emailid=Test_webUI_Inputs.getEmail(); // to be changed as per webui
        String mserverurl ="http://ictwiki.iitk.ac.in:8080/b4server";
        String MSrequrl = mserverurl +"/ProcessRequest?req=certificaterevocationotpgen&emailid="+emailid;

        CreateHttpConnection http_1 = new CreateHttpConnection ();
        if(http_1.sendPost(MSrequrl,"certificaterevocationotpgen")==1)
        {
            String otp = Test_webUI_Inputs.getOTP(); // to be changed as per webui
            MSrequrl = mserverurl + "/ProcessRequest?req=certificaterevocationotpverify&emailid=" + emailid + "&otp=" + otp;
            int val = http_1.sendPost(MSrequrl, "certificaterevocationotpverify");
            System.out.println(val);

            String revocationreason=Test_webUI_Inputs.getRevocationReason();
            MSrequrl = mserverurl +"/ProcessRequest?req=certificaterevocationreason&emailid="+emailid+"&reason="+revocationreason;
            int val2=http_1.sendPost(MSrequrl,"certificaterevocationreason");
        }






    }
}
