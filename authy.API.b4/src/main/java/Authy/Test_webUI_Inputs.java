package Authy;
//package com.ehelpy.brihaspati4.authenticate ;

import javax.swing.*;

public class Test_webUI_Inputs {
    public static String data[] = new String[6];
    public static String pwd;
    private static String response;

    public static String getEmail() throws Exception {


        JLabel frame = new JLabel("EmailID");
        return JOptionPane.showInputDialog(frame, "Please enter your Email-Id");


    }

    public static String getAlias() throws Exception {

        //response = getFromWebServer("getAliasPage","");
        //Authenticator.resetResponse(); // it will reset the response value for next use
        //return response;

        JLabel frame = new JLabel("Alias");
        return JOptionPane.showInputDialog(frame, "Enter Alias name");


    }

    public static String[] getData() throws Exception { // club together in a form format
        String label[] = new String[6];
        label[0] = "Email Id";
        label[1] = "Organization Unit";
        label[2] = "Organization";
        label[3] = "City";
        label[4] = "State";
        label[5] = "Country";
        for (int i = 0; i <= 5; i++) {
//            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//            System.out.println("Enter ur "+label[i] + ":");
            //data[i] = br.readLine();

            JLabel frame = new JLabel(label[i]);
            data[i] = JOptionPane.showInputDialog(frame, "Enter your " + label[i]);

        }
        return data;
    }

    public static String getOTP() throws Exception {
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        System.out.println("Enter the otp: ");
//        otp = br.readLine();

        JLabel frame = new JLabel("otp");
        return JOptionPane.showInputDialog(frame, "Enter otp");
    }

    public static String getCreationPassword() throws Exception {
        String pwd1;
        String pwd2;

        //  System.out.println("ALERT: Remember KEYSTORE PASSWORD");

//        BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
//        System.out.println("Enter new keystore password: ");
//        pwd1 = br1.readLine();
        JLabel frame = new JLabel("pwd");
        pwd1 = JOptionPane.showInputDialog(frame, "Enter new keystore password");

//        BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
//        System.out.println("Re-Enter your keystore password: ");
//        pwd2 = br2.readLine();
        JLabel frame2 = new JLabel("pwd2");
        pwd2 = JOptionPane.showInputDialog(frame, "Re-Enter your keystore password");

        if (pwd1.equals(pwd2)) {
            pwd = pwd1;
        } else {
            System.out.println("password does not match: kindly enter password again");
            getCreationPassword();
        }
        // System.out.println("pwsd is:"+pwd);
        return pwd;
    }

    public static String getVerificationPassword() throws Exception {
//        response = getFromWebServer("getPasswordPage","");
//        Authenticator.resetResponse(); // it will reset the response value
//        return response;

        JPanel panel = new JPanel();
        JLabel jPassword = new JLabel("PASSWORD");
        JPasswordField password = new JPasswordField(20);
        panel.add(jPassword);
        panel.add(password);
        Object[] options = {"OK", "Recover Keystore", "Certificate Revocation", "Generate New Certificate"};
        int result = JOptionPane.showOptionDialog(null, panel, "KEYSTORE PASSWORD ", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        String passwordValue = null;
        //System.out.println("getkeystorepass RESULT :"+result);
        if (result == 0) { // ok
            char[] passwordValues = password.getPassword();
            passwordValue = String.valueOf(passwordValues);
        }

        if (result == 1) { // regeneration
            passwordValue = "1";
        }
        if (result == 2) {
            passwordValue = "2"; // certifi revocation
        }
        if (result == 3) { // new
            passwordValue = "3";
        }
        return passwordValue;
    }

    public static String getRevocationReason() throws Exception {
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        System.out.println("Enter the Revocation Reason: ");
//        String reason = br.readLine();
        JLabel frame = new JLabel("Revocation");
        return JOptionPane.showInputDialog(frame, "Enter the Revocation Reason:");

    }



    public static String getKSalias() throws Exception {
        JLabel frame = new JLabel("KSalias");
        return JOptionPane.showInputDialog(frame, "Enter your Keystore alias:");
    }

    public static String getKSpwd() throws Exception {
        JLabel frame = new JLabel("KSpwd");
        return JOptionPane.showInputDialog(frame, "Enter your Keystore password:");
    }

    // this func gets response from webserver

    public static String getFromWebServer(String func, String arg) throws Exception {

        System.out.println("AM needs" + func + "from webserver");
        Authenticator.sendQuery_gc_buffer_am("que", "am", "ws",func, arg);

        while (true) {
            System.out.println("AM waiting for:" + func);
            if (Authenticator.getResponse() != null) {
                break;
            }

            Thread.sleep(500);

        }
        return Authenticator.getResponse();

    }
    // called by CertificateSignature
    public static String getDevice_Id() throws Exception { // from RM

        System.out.println("AM needs DeviceId from RM");
        Authenticator.sendQuery_gc_buffer_am("que", "am", "rm", "getDeviceID", null);

        while (true) {
            System.out.println("AM waiting for RM to get DeviceID");
            if (Authenticator.getResponse() != null) {
                break;
            }

            Thread.sleep(100);
        }
        System.out.println("response got in test webui inputs for getDeviceID is" + Authenticator.getResponse());
        response = Authenticator.getResponse();
        Authenticator.resetResponse(); // it will reset the response value
        return response;
    }

    // called by CertificateSignature
    public static String getNode_Id() throws Exception {  // from RM

        System.out.println("AM needs NodeID from RM");
        Authenticator.sendQuery_gc_buffer_am("que", "am", "rm", "getNodeID", null);

        while (true) {
            System.out.println("AM waiting for RM to get NodeID");
            if (Authenticator.getResponse() != null) {
                break;
            }

            Thread.sleep(100);

        }
        System.out.println("response got in test webui inputs for getNodeID is" + Authenticator.getResponse());
        response = Authenticator.getResponse();
        Authenticator.resetResponse(); // it will reset the response value for next use
        return response;
    }

}
