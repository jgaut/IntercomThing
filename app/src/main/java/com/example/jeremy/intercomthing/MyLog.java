package com.example.jeremy.intercomthing;

import android.util.Log;

import com.splunk.mint.Mint;

/**
 * Created by jeremy on 19/01/2017.
 */

public class MyLog {

    public static void logEvent(String s) {
        Mint.logEvent(s);
        Log.i("Splunk", s);
    }

    public static void logException(String s1, Exception e1) {
        Log.i(s1, e1.toString());
    }
}
