package com.example.jeremy.intercomthing;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.util.HashMap;

/**
 * Created by jeremy on 19/01/2017.
 */

public class MyLog {


    private static FirebaseAnalytics mFirebaseAnalytics;
    private static Bundle bundle;

    public static void i(String s1, String s2){
        Log.i(s1, s2);
        bundle.clear();
        bundle.putString(FirebaseAnalytics.Param.ORIGIN, s1);
        bundle.putString(FirebaseAnalytics.Param.VALUE, s2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
    }

    public static void logException(String s1, Exception s2){
        FirebaseCrash.report(s2);
    }


    public static void setmFirebaseAnalytics(FirebaseAnalytics mFirebaseAnalytics) {
        MyLog.mFirebaseAnalytics = mFirebaseAnalytics;
        bundle = new Bundle();
    }
}
