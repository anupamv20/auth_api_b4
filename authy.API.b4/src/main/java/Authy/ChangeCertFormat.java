package Authy;

//package com.ehelpy.brihaspati4.authenticate;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import java.util.logging.Logger;

//23 jan 22
//This function convert String formatted X.509 Certificate into its original form
// It is able to convert the same either into a Certificate chain or individual certificate
public class ChangeCertFormat {
    private static final Logger log = Logger.getLogger(String.valueOf(ChangeCertFormat.class));


    public static X509Certificate convertToX509Cert(String certEntry) throws IOException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        InputStream in = null;
        X509Certificate cert = null;
        try {
            byte[] certEntryBytes = Base64.getDecoder().decode(certEntry);
            in = new ByteArrayInputStream(certEntryBytes);
          //  CertificateFactory certFactory = CertificateFactory.getInstance("X.509", new BouncyCastleProvider());
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");

            cert = (X509Certificate) certFactory.generateCertificate(in);
        } catch (CertificateException | NoSuchProviderException ex) {
            ex.getMessage();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return cert;
    }
}

