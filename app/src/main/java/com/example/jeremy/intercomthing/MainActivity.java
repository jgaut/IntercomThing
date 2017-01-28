package com.example.jeremy.intercomthing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.splunk.mint.Mint;
import com.splunk.mint.MintLogLevel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private MyGpio myGpio;
    private String TAG = this.getClass().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Mint.setApplicationEnvironment(Mint.appEnvironmentTesting);
        Mint.setLogging(50);
        Mint.setSessionInterval(1);
        Mint.enableDebugLog();
        // Set the application environment
        Mint.initAndStartSession(this.getApplication(), "60e4006b");
        Mint.startSession(this.getApplication());

        Mint.logEvent("Initialisation", MintLogLevel.Info);
        myGpio = new MyGpio();

        new IftttHttpRequest().execute("init");

        //myGpio.openDoor();
        setContentView(R.layout.activity_main);

        MyLog.transaction("init");
        MyLog.i(TAG, "init");
    }
}
