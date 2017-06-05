package com.example.jeremy.intercomthing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.splunk.mint.Mint;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private MyGpio myGpio;
    private String TAG = this.getClass().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the application environment
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.enableDebugLog();

        // TODO: Update with your API key
        Mint.initAndStartSession(this.getApplication(), "60e4006b");

        // TODO: Update with your HEC token
        // Mint.initAndStartSessionHEC(this.getApplication(), "MINT_HEC_URL", "YOUR_HEC_TOKEN");

        // Obtain all app properties
        MyAppProperties.init(this.getApplicationContext());

        // Init GPIO
        myGpio = new MyGpio();

        //new IftttHttpRequest().execute("init");

        //myGpio.openDoor();
        setContentView(R.layout.activity_main);

        new Timer().schedule(new TimerTask() {
            public void run() {
                MyLog.logEvent("Initialisation");
            }
        }, 10000);

        new MyEmail().execute("Initialisation", "Initialisation completed");



    }
}
