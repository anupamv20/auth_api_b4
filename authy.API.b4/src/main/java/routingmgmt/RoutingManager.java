package routingmgmt;

//import com.ehelpy.brihaspati4.GC.GlueCode;
import GC.GlueCode;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

//import Authy.U;


/**
 * Created by S/L Umesh U Nair
 * <p>
 * <br> Aim is to create a Routing Manager API for Brihaspati-4
 * <br> This is the main class.
 * <br> The functions associated with the routing manager API can be accessed using this class.
 * <br> Few  layers were implemented as default:-
 * <br> 1. BaseRoutingTable - LayerID = 0
 * <br> 2. StorageRoutingTable - LayerID = 1
 * <br> Rest of the layer will be created as per the requirement of the user.
 * <br> Access to various layers can be controlled through the config.properties file.
 * <br> New Layer can be added by calling createNewLayer() method in the RoutingManager.
 */
public class RoutingManager {
    private static final Logger log = Logger.getLogger(String.valueOf(RoutingManager.class));
    private static RoutingManager routingManager;
    private static String resp;

    private  int max_limit_of_buffer=1024;
    private ArrayList process_var_for_RM;
    private LinkedList rm_buffer_gc= new LinkedList();


    private RoutingManager() throws Exception {

        System.out.println(" inside Routing Manager ");
        System.out.println("starting ProcessingThread_for_RM");
        ProcessingThread_for_RM(); // for continuous reading+processing

    }

    /**
     * @return RoutingManger Object
     * <br>This method is required to create an instance of RoutingManager.
     * <br>Instance of RoutingManager will be obtained by calling this function.
     * <br>Only one Instance will be created for RoutingManager class.
     */
    public static synchronized RoutingManager getInstance() throws Exception {
        if (routingManager == null) {
            routingManager = new RoutingManager();
        }
        return routingManager;
    }


    /**
     * @return System IP address.
     */
    public String getSystemIP() {
        NetworkInterface networkInterface;
        String ethernet;
        String selfIPAddress = "";
        String regex = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
 //	String regex = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        try {
            String OSName = System.getProperty("os.name");
            if (OSName.contains("Windows")) {
                selfIPAddress = InetAddress.getLocalHost().getHostAddress();
            } else {
                try {
                    for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                        networkInterface = interfaces.nextElement();
                        ethernet = networkInterface.getDisplayName();
                        if (!(ethernet.equals("lo"))) {
                            if (!(ethernet.contains("br"))) {
                            if (!(ethernet.contains("docker"))) {
                                InetAddress inetAddress = null;
                                for (Enumeration<InetAddress> ips = networkInterface.getInetAddresses(); ips.hasMoreElements(); ) {
                                    inetAddress = ips.nextElement();
                                    if (Pattern.matches(regex, inetAddress.getCanonicalHostName())) {
                                        selfIPAddress = inetAddress.getCanonicalHostName();
                                        return selfIPAddress;
                                    }
                                }
                                assert inetAddress != null;
                                String pip = inetAddress.toString();
                                int mark = pip.indexOf("/");
                                int cutAt = mark + 1;
                                selfIPAddress = pip.substring(cutAt);
                                return selfIPAddress;
                            }
			    }
                        }
                    }
                } catch (SocketException e) {
                    log.info("Exception Occurred"+ e);
                }
            }
            return selfIPAddress;
        } catch (Exception e) {
            log.info("Exception Occurred"+ e);
            return null;
        }
    }

    /**
     * @return System MAC Address in String.
     */
    public String getSystemMACAddress() {

      //  String macaddr = "";
//        try {
//            String ipAddress = getSystemIP();
//            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getByName(ipAddress));
//            byte[] mac = network.getHardwareAddress();
//            StringBuilder sb = new StringBuilder();
//            for (int i = 0; i < mac.length; i++) {
//                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
//            }
//            log.info("Current MAC address : " + sb.toString());
//            macaddr = sb.toString();
//        } catch (Exception e) {
//            log.info("Exception Occurred"+ e);
//        }
        return "111111111";
    }


    /**
     * @return IP Address of the local Node in String.
     */
    public String getIPAddress() {
        return "12345";
    }

    /**
     * @return NodeID of the local Node in String.
     */
    public String getNodeID() {
        return "123456789";
    }

    /**
     * @return DeviceID of the local Node in String.
     */
    public String getDeviceID() {
        return "987654321";
    }

    // this method will return entries in AM's input buffer
    public LinkedList display_rm_buffer_gc(){
        return (rm_buffer_gc);
    }

    // GC will call this method to write req / que in the AM's input buffer
    public void addMessage_rm_buffer_gc(List gcMessage) 	{
        if ((rm_buffer_gc.size()) < max_limit_of_buffer)
        {
            rm_buffer_gc.add(gcMessage);
            System.out.println("List received by RM’s input buffer.");
        }
        else
        { System.out.println("RM’s input buffer is Full.");
        }
    }

    // RM will call this method to write query in the GC's input buffer
    // query: 0que, 1src, 2des, 3query_function, 4arguments

    public void sendQuery_gc_buffer_rm(String type, String src, String des, String func, String arg)
            throws Exception {
        GlueCode gc = GlueCode.getInstance();
        List abc = new ArrayList();
        abc.add(type); // 0:que
        abc.add(src); // 1:source
        abc.add(des); // 2:destination
        abc.add(func); // 3: function
        abc.add(arg); // 4: arguments in tlv format
        System.out.println("temp list abc of RM:" +abc);
        System.out.println(" Query being sent to gc ");
        gc.addMessage_gc_buffer_rm(abc);
    }

    // response: 0res, 1src, 2des, 3query_function, 4arguments, 5response
    // RM will call this method to write response in the GC's input buffer
    public void sendResponse_gc_buffer_rm(String response) throws Exception {
        GlueCode gc = GlueCode.getInstance();
        ArrayList temp1 ,temp2; //
        System.out.println("in RM sendResponse function");
        temp1 =  process_var_for_RM;
        temp2 = process_var_for_RM;

        rm_buffer_gc.pollFirst(); //remove first element from list
        temp1.set(0, "res"); // request type
        temp1.set(2, temp2.get(1)); // destination
        temp1.set(1, "rm"); // source
        temp1.set(3, temp2.get(3)); // que function
        temp1.set(4, temp2.get(4)); // que argument

        temp1.add(response); // adding response as the 5th field // tlv format

        System.out.println("\nTemp Var: " + temp1 + "\n");
        System.out.println("sending response to gc.");
        gc.addMessage_gc_buffer_rm(temp1);
    }

    // AM runs this thread to process resposne or query
    public void ProcessingThread_for_RM()  {
        Thread processingThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true)
                {
                    //System.out.println("RM processing thread running");


                    if (rm_buffer_gc.size() > 0)
                    {
                        process_var_for_RM = (ArrayList) rm_buffer_gc.get(0);
                        rm_buffer_gc.pollFirst(); // retrieve & removes first element
                        if (process_var_for_RM.get(0) == "res") // get value out of response
                        {
                            //
                            //System.out.println("process_var_for_AM started with Res");
                            resp = (String) process_var_for_RM.get(5);
                            System.out.println("response rxd by RM is:"+resp);

                        }
                        else if (process_var_for_RM.get(0) == "que") // send response for the query
                        {
                            String temp_func = (String) process_var_for_RM.get(3); // get function field
                            // taking single argument as on date // to be changed for multiple arg
                            String temp_arg = (String) process_var_for_RM.get(4); // get argument field
                            String temp_response = null;

                            if(temp_func == "getNodeID") {
                                System.out.println("getNodeID function is called");
                                try {
                                    temp_response = getNodeID();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    sendResponse_gc_buffer_rm(temp_response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }else if(temp_func == "getDeviceID"){
                                System.out.println("getDeviceID function is called");
                                try {
                                    temp_response = getSystemMACAddress();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    sendResponse_gc_buffer_rm(temp_response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }else {
                                System.out.println("this function is not present in RM API");
                                System.out.println("returning null as a response");
                                try {
                                    sendResponse_gc_buffer_rm(null);
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
    public String RM_AM_Test () throws Exception {
//    System.out.println("RM needs self Pub Key from  AM:");
//
//    sendQuery_gc_buffer_rm("que","rm","am","getSelfPubKey",null);
//
//    System.out.println("pub key is:"+res);
//
//    return res;

       System.out.println("RM needs certificate from AM:");
       //String input = " hello";
       sendQuery_gc_buffer_rm("que","rm","am","getSelfCert","");
        while (true) {
            System.out.println("RM waiting for AM response");
            if (resp != null) {
                break;
            }

            Thread.sleep(100);

        }
        System.out.println(" RM got resposne from AM");
        System.out.println("Received data is:"+resp);
        return resp;


    }

    public static String getResponse() throws Exception
    {
        //System.out.println("getResponse method called");
        return resp;
    }
    public void setResponse(String res) throws Exception
    {
        System.out.println("setResponse method called");
        System.out.println("setting response:"+res);
        resp = res;
    }
    // makes response variable null, for reuse
    public static void resetResponse()
    {
        resp = null;
    }




}
