
//package com.ehelpy.brihaspati4.authenticate ;

package Authy;


import GC.Config;

import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

//last update 16 Feb 22
//author: anupamv20

// this class will compute time offset and adds it up to user current time to get node's updated time
// this will help in uniformity of time in all nodes and
// prevent forging of Digital certs

class UpdatedDateTime
{
    private static final Logger log = Logger.getLogger(String.valueOf(UpdatedDateTime.class));

    static long getTimeOffset() throws Exception {

        log.info("Starting time synchronization check "); ;
        //String serverUrl = "http://172.20.82.6:8080/b4server"; // B4 server address.
        String serverUrl = "https://www.google.co.in"; // testing server address

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault());//to fetch date in local (zone)date time format
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        Date date1 = null; //server time
        Date date2; // system time
        try
        {
            String sdate=getServerDate(serverUrl); // getting server date-time

            if (sdate==null) {
                date1=sdf2.parse(getLastLogoutTime()); // fetch server time from config file
            }
            else{
                date1 = sdf.parse(sdate); // fetches date of the server
            }

            log.info("Server Date-Time is : "+date1);
        }
        catch (ParseException e)
        {
            log.severe(" error in getting time from server");
            e.printStackTrace();
        }

        long server_in_mills = date1.getTime(); // converting server time in millisecs

        date2 = sdf2.parse(getCurrentDateTime()); // getting system time
        log.info("System Date-Time is : "+getCurrentDateTime());
        long system_in_mills = date2.getTime(); // converting user time in millisecs

        long offset = server_in_mills - system_in_mills ; // calculating offset
        log.info("time offset is: "+offset);

        return offset;

    }

    private static String getServerDate(String serverUrl) {
        try{
            URL url = new URL(serverUrl);
            URLConnection connection = url.openConnection();
            Map<String, List<String>> httpHeaders = connection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
                String headerName = entry.getKey();
                if (headerName != null && headerName.equalsIgnoreCase("date")) {
                    return entry.getValue().get(0);
                }
            }
        }
        catch(Exception ex){
            System.out.println("The error is "+ex.toString());
            return null; // returns null if connection error occurs
        }
        return null;
    }

    // fetches current system date and time
    static String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private static String getLastLogoutTime()
    {
        String l_datetime;
        try{
            //Read the last usage time from B4conf.properties file.
            l_datetime = Config.getConfigObject().getLastLogoutTime();
        }
        catch(Exception e){
            l_datetime=null;
        }
        return  l_datetime;
    }

    // update last logout time thru GC / webui / webserver in config file

//    private static void setLastLogoutTime() // to be updated as per webUI log out time
//    {
//        log.info("......setting updated time as Last Date-Time check in properties file..........");
//
//        //Set the updated time in B4conf.properties
//        Config.getConfigObject().saveLastLogoutTime(updated_user_time);
//    }

}

