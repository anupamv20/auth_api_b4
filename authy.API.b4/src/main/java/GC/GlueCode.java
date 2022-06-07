package GC;

//package com.ehelpy.brihaspati4.GC;
//Glue Code Pseudo Code:

//  Glue Code = gc
//  Authentication Manager = am
//  Communication Manager = cm
//  Indexing Manager = im
//  Routing Manager = rm
//  Web Server Module = ws
//  Web Module = web


//import the required packages...

import Authy.Authenticator;
import routingmgmt.RoutingManager;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class GlueCode extends Thread
{

    //final_query = ModuleCheck.findModuleName(qname);
    //Call the final query...

    private  int status_skip_flag_am = 1;

    private  int status_skip_flag_rm = 1;

//    private  int status_skip_flag_ws = 1;


    //Initialize a current_skip_flag corresponding to each input buffer (one input buffer for each module) of the Glue Code with 1. (This current_skip_flag will contain the current value; ie., after how many rotations the input buffer corresponding to that respective module will be checked.) (The default value for the current_skip_flag for each module at starting will be 1.)

    private  int current_skip_flag_am = 1;

    private  int current_skip_flag_rm = 1;

  //  private  int current_skip_flag_ws = 1;

    private  int max_limit_of_skip_flags = 32;
    private  int max_limit_of_buffer = 1024;
    private  int count = 0;
    //private String[] module_acronyms = {"am", "cm", "im", "rm", "ws", "web", "voip", "dfs", "ufs", "mail", "sms"};

    // inputModule_buffer_outputModule
    // here, in the next line, "am" sent the data to "gc"
    private  LinkedList gc_buffer_am = new LinkedList();

    private  LinkedList gc_buffer_rm = new LinkedList();
    //private  LinkedList gc_buffer_ws = new LinkedList();



    private  LinkedList internal_process_queue = new LinkedList();
    private  List process_var = new ArrayList();
    private  String destination_module = new String();
    private static GlueCode gc;
    private Authenticator am = Authenticator.getInstance();
    private RoutingManager rm = RoutingManager.getInstance();
    //private WebUIServer ws = WebUIServer.getInstance();


    private Config conf;

    // gc.addMessage_gc_buffer_rm(list_variable)
    GlueCode() throws Exception {
        conf = new Config();
    }


    public static synchronized GlueCode getInstance() throws Exception {
        if (gc == null)
        {
            gc = new GlueCode();
        }
        return gc;
    }


    private synchronized void addMessage_gc_buffer_xx(LinkedList gc_buffer_xx, List xxMessage, String moduleName)
    {
        if ((gc_buffer_xx.size()) < max_limit_of_buffer)
        {
            gc_buffer_xx.add(xxMessage);
            System.out.println("List received by GC's input buffer connected with " + moduleName + ".");
        }
        else
        {
            System.out.println("GC's input buffer connected with " + moduleName + " is Full.");
        }
    }

    public void addMessage_gc_buffer_am(List amMessage)
    {
        addMessage_gc_buffer_xx(gc_buffer_am, amMessage, "AM");
    }

    public void addMessage_gc_buffer_rm(List rmMessage)
    {
        addMessage_gc_buffer_xx(gc_buffer_rm, rmMessage, "RM");
    }

    //public void addMessage_gc_buffer_ws(List wsMessage)
    //{
      //  addMessage_gc_buffer_xx(gc_buffer_ws, wsMessage, "WS");
    //}

    public synchronized void display_buffer_status()
    {
        try
        {
            System.out.println("\nSHOWING BUFFER STATUS:");
            System.out.println("List received by GC's input buffer connected with AM: " + gc_buffer_am);
            System.out.println("List received by GC's input buffer connected with RM: " + gc_buffer_rm);
           // System.out.println("List received by GC's input buffer connected with WS: " + gc_buffer_ws);

            //System.out.println("GC's internal process queue: " + internal_process_queue);
            //System.out.println("process_var: " + process_var + "\n");
            System.out.println("AM's input buffer: " + am.display_am_buffer_gc());
            //System.out.println("RM's input buffer: " + rm.display_rm_buffer_gc() + "\n");
            //System.out.println("WS's input buffer: " + ws.display_ws_buffer_gc() + "\n");

        }catch (IndexOutOfBoundsException | NullPointerException e1) {
            System.out.println("Exception Occurred: " + e1);
        }
    }

    //The input buffers of the Glue Code will be checked periodically (only if the current_skip_flag’s value is 1. More about this in the next point.) for all the modules in Round Robin fashion. Each input buffer will be checked for 5 seconds or until all the data/query are extracted, whichever time is less.

    //If the current_skip_flag value is 1, then only the corresponding module’s input buffer is checked. Otherwise, the current_skip_flag’s value is decremented by 1.

    private synchronized void readingMethod(int current_skip_flag_xx, int status_skip_flag_xx, LinkedList gc_buffer_xx)
    {
        if(current_skip_flag_xx == 1)
        {
            //check output buffer of the Module;
            //if (data found in the output buffer of the Module)
            if ((gc_buffer_xx.size())>0)
            {
                display_buffer_status();
                status_skip_flag_xx = 1;
                count=0;
                while ((gc_buffer_xx.size()>0) && (count<((max_limit_of_buffer)/2)))
                {
                    internal_process_queue.add(gc_buffer_xx.pollFirst());
                    count = count+1;
                }
                display_buffer_status();
            }
            //else if (no query found in the output buffer of the Module)
            //else if (gc_buffer_xx.size()==0)
            else
            {
                if (status_skip_flag_xx < max_limit_of_skip_flags)
                {
                    status_skip_flag_xx = status_skip_flag_xx * 2;
                }
                current_skip_flag_xx = status_skip_flag_xx;
            }
        }
        else
        {
            current_skip_flag_xx--;
        }
    }

    public void readingthread()
    {
        Thread readingThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    while (true)
                    {
                       // System.out.println("GC Reading Thread running");


                        readingMethod(current_skip_flag_am, status_skip_flag_am, gc_buffer_am);

                        readingMethod(current_skip_flag_rm, status_skip_flag_rm, gc_buffer_rm);
                        //readingMethod(current_skip_flag_ws, status_skip_flag_ws, gc_buffer_ws);

                        try {
                            //System.out.println("Reading Thread going to sleep");
                            Thread.sleep(60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }catch (IndexOutOfBoundsException | NullPointerException e2) {
                    System.out.println("Exception Occurred: " + e2);
                    //log.error("Exception Occurred",e2);
                }

            }
        });
        readingThread.start();
    }

    public void processingthread()
    {
        Thread processingThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                // This statement will request Routing manager to ascertain keys for which I am root node.
                try
                {
                    while (true)
                    {
                       // System.out.println("GC Proc Thread running");

                        if ((internal_process_queue.size())>0)
                        {

                            System.out.println("\nInternal Process Queue: " + internal_process_queue);
                            System.out.println("Process Var: " + process_var + "\n");

                            process_var = (ArrayList) internal_process_queue.get(0);
                            internal_process_queue.pollFirst();

                            System.out.println("\nInternal Process Queue: " + internal_process_queue);
                            System.out.println("Process Var: " + process_var + "\n");

                            //if (process_var != null)
                            //{
                            if ((process_var.get(0) == "que") || (process_var.get(0) == "res"))
                            {
                                display_buffer_status();
                                System.out.println("process_var starts with que or res");
                                destination_module = (String) process_var.get(2);
                                //display_buffer_status();
                                System.out.println("Destination Module = " + destination_module);


                                if (destination_module == "am")
                                {
                                    am.addMessage_am_buffer_gc(process_var);
                                }
                                else if (destination_module == "rm")
                                {
                                    rm.addMessage_rm_buffer_gc(process_var);
                                }
//                                else if (destination_module == "ws")
//                                {
//                                    ws.addMessage_ws_buffer_gc(process_var);
//                                }



                                process_var = null;
                                display_buffer_status();
                            }
                        }
                        try {
                            //System.out.println("Processing Thread going to sleep");
                            Thread.sleep(60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }catch (IndexOutOfBoundsException | NullPointerException e3) {
                    System.out.println("Exception Occurred: " + e3);
                    //log.error("Exception Occurred",e3);
                }
            }
        });
        processingThread.start();
    }

    public void mainMethod()
    {
        try(FileReader reader =  new FileReader("gc_config.txt"))
        {
            Properties properties = new Properties();
            properties.load(reader);
            max_limit_of_skip_flags = Integer.parseInt(properties.getProperty("max_limit_of_skip_flags"));
            max_limit_of_buffer = Integer.parseInt(properties.getProperty("max_limit_of_buffer"));

            System.out.println(max_limit_of_skip_flags);
            System.out.println(max_limit_of_buffer);
        }catch (Exception e)
        {
            ;
            e.printStackTrace();
        }

        readingthread();
        processingthread();
    }


} // GlueCode Class close
