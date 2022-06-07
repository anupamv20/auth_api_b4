
package Authy;
//package com.ehelpy.brihaspati4.authenticate ;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.FileWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;



//20 jan 22
//this class : generates private & public key,
// create x509 certificate with self signature
//returns self signed certificate

public class GenerateCertificate // to be changed to private
{
    private static final Logger log = Logger.getLogger(String.valueOf(GenerateCertificate.class));

    private static String data [] = new String[6];

    private static  String E = null;
    private static String OU = null;
    private static String O = null;
    private static String L = null;
    private static  String ST = null;
    private static  String C = null;
    private static PrivateKey privKey = null;
    private static PublicKey pubKey = null;
    static KeyPair keypair = null;

    public static X509Certificate createSelfSignedCert() throws Exception
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        data = Test_webUI_Inputs.getData(); // to be changed as per GC and webUI

        E = data[0];
        OU = data[1];
        O = data[2];
        L = data[3];
        ST = data[4];
        C = data[5];


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

        @Deprecated
        X509V3CertificateGenerator x500Name = new X509V3CertificateGenerator();
        //   X509v3CertificateBuilder x500Name = new X509v3CertificateBuilder();

        Vector<ASN1ObjectIdentifier> order = new Vector<>();
        Hashtable<ASN1ObjectIdentifier, String> attributeMap = new Hashtable<>();
        if (E != null) {
            attributeMap.put(X509Name.CN, E);
            order.add(X509Name.CN);
        }
        if (OU != null) {
            attributeMap.put(X509Name.OU, OU);
            order.add(X509Name.OU);
        }
        if (O != null) {
            attributeMap.put(X509Name.O, O);
            order.add(X509Name.O);
        }
        if (L != null) {
            attributeMap.put(X509Name.L, L);
            order.add(X509Name.L);
        }
        if (ST != null) {
            attributeMap.put(X509Name.ST, ST);
            order.add(X509Name.ST);
        }
        if (C != null) {
            attributeMap.put(X509Name.C, C);
            order.add(X509Name.C);
        }
        X509Name issuerDN = new X509Name(order, attributeMap);
        Calendar c = Calendar.getInstance();
        x500Name.setNotBefore(c.getTime());
        c.add(Calendar.YEAR, 1);
        x500Name.setNotAfter(c.getTime());
        x500Name.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        x500Name.setSignatureAlgorithm("SHA256WithRSAEncryption");
        x500Name.setIssuerDN(issuerDN);
        x500Name.setSubjectDN(issuerDN);
        x500Name.setPublicKey(pubKey);

        X509Certificate[] chain = new X509Certificate[1];
        try {
            chain[0] = x500Name.generate(privKey, "BC");
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
        } //catch (CertificateException e1) {
        // TODO Auto-generated catch block
        //e1.printStackTrace();
        catch (SignatureException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            // TODO Auto-generated catch block
            // e1.printStackTrace();
        } catch (NoSuchProviderException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return chain[0];

    }

//    public static PrivateKey priv() {
//        if(keypair!=null)
//            privKey = keypair.getPrivate();
//        return (PrivateKey) privKey;
//    }

    public static PublicKey getPublicKey()
    {
        return pubKey;
    }
}