package com.example.jeremy.intercomthing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.splunk.mint.Mint;

import org.java_websocket.WebSocketImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private MyGpio myGpio;
    private String TAG = this.getClass().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain all app properties
        MyAppProperties.init(this.getApplicationContext());

        // Set the application environment
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.enableDebugLog();

        // TODO: Update with your API key
        Mint.initAndStartSession(this.getApplication(), MyAppProperties.getProperty("Splunk.api.key"));

        // TODO: Update with your HEC token
        // Mint.initAndStartSessionHEC(this.getApplication(), "MINT_HEC_URL", "YOUR_HEC_TOKEN");

        //WebSocketServer
        WebSocketImpl.DEBUG = Boolean.getBoolean(MyAppProperties.getProperty("MyWebSocketServer.debug"));
        try {
            InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(MyAppProperties.getProperty("MyWebSocketServer.hostname")),
                    Integer.parseInt(MyAppProperties.getProperty("MyWebSocketServer.port")));
            MyWebSocketServer socketServer = new MyWebSocketServer(address);
            socketServer.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

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
