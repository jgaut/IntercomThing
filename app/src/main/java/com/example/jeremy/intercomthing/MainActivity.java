package com.example.jeremy.intercomthing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;



public class MainActivity extends AppCompatActivity {

    private static FirebaseAnalytics mFirebaseAnalytics;
    private MyGpio myGpio;
    private String TAG = this.getClass().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        MyLog.setmFirebaseAnalytics(mFirebaseAnalytics);

        // Init GPIO
        myGpio = new MyGpio();

        new IftttHttpRequest().execute("init");

        //myGpio.openDoor();
        setContentView(R.layout.activity_main);

        MyLog.i(TAG, "init");
    }
}
