package com.example.jeremy.intercomthing;

import android.util.Log;

import com.splunk.mint.Mint;
import com.splunk.mint.MintLogLevel;

import java.util.HashMap;

/**
 * Created by jeremy on 19/01/2017.
 */

public class MyLog {

    public static void i(String s1, String s2){
        Log.i(s1, s2);
        Mint.logEvent(s2, MintLogLevel.Info);
        Mint.flush();
    }

    public static void logException(String s1, Exception s2){
        Log.e(s1, s2.toString());
        Mint.logException(s2);
        Mint.flush();
    }

    public static void transaction(String s){
        String txID = Mint.transactionStart(s);
        HashMap<String, Object> mydata = new HashMap<String, Object>();
        mydata.put("data1", "value1");
        mydata.put("data2", "value2");
        Mint.transactionStop(txID, mydata);
        Mint.flush();
    }
}
