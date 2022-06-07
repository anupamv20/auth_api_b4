package Authy;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

class GenerateSelfSignedCert {

    private static String data [] = new String[6];
    private static  String E = null;
    private static String OU = null;
    private static String O = null;
    private static String L = null;
    private static  String ST = null;
    private static  String C = null;
    private static PrivateKey privKey = null;
    private static PublicKey pubKey = null;
    private static KeyPair keypair = null;


    private static final Logger log = Logger.getLogger(String.valueOf(GenerateSelfSignedCert.class));

    static X509Certificate createSelfSignedCert() throws Exception

    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        data = Test_webUI_Inputs.getData();

        E = data[0];
        OU = data[1];
        O = data[2];
        L = data[3];
        ST = data[4];
        C = data[5];

        System.out.println("user details are: " +E+ " " + OU + " "+ O + " " +L +" "+ST +" "+C+" ");
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA","BC");
            keyGen.initialize(2048,new SecureRandom());
            keypair = keyGen.generateKeyPair();
        } catch (Exception e) {
            log.info("error in generating keypairs");
            e.printStackTrace();
        }

        pubKey = keypair.getPublic();
        privKey = keypair.getPrivate();
        System.out.println("key pairs generated");
        // System.out.println("in gene cert, got priv key: "+privKey);

        // keeping priv key in key.txt in encoded form for future use: recovery of KS
        final byte[] bprivkey = privKey.getEncoded();
        final String sprivate = new String(Base64.getEncoder().encode(bprivkey));
        try {
            final FileWriter myWriter = new FileWriter("key.txt");
            myWriter.write(sprivate);
            myWriter.close();
        }
        catch (Exception e2) {
            e2.printStackTrace();
        }

        X500Name issuer = new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.CN, E)
                .addRDN(BCStyle.OU,OU).addRDN(BCStyle.O,O) .addRDN(BCStyle.L,L)
                .addRDN(BCStyle.ST,ST).addRDN(BCStyle.C,C).build();

        X500Name subject = issuer; // subject n issuer are same

        Calendar cal = Calendar.getInstance();
//        Date startDate = cal.getTime();
        Date updatedDate = Authenticator.getUpdatedTime(); // cert start date will be the "updated date"
        cal.setTime(updatedDate);
        Date startDate = cal.getTime();

        cal.add(Calendar.YEAR,1);
        Date endDate = cal.getTime();

        BigInteger certSerialNumber = BigInteger.valueOf(System.currentTimeMillis());
        String signatureAlgorithm = "SHA256WithRSA";

        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuer, certSerialNumber, startDate, endDate, subject, pubKey);
        ContentSigner sigGen = new JcaContentSignerBuilder(signatureAlgorithm).setProvider("BC").build(privKey);
        X509CertificateHolder certificateHolder = builder.build(sigGen);

        X509Certificate mycert = null;
        try {
            mycert = new JcaX509CertificateConverter().setProvider("BC")
                    .getCertificate(certificateHolder);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        System.out.println("user self signed cert is : "+mycert);
        return mycert;

    }

    // this function is created to get private key for encryption & decryption purposes
    // only AM can call this function.

    static PrivateKey getMyPK(){
        return privKey;
    }

}
