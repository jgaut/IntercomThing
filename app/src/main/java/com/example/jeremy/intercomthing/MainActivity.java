package com.example.jeremy.intercomthing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.splunk.mint.DataSaverResponse;
import com.splunk.mint.Mint;
import com.splunk.mint.MintCallback;
import com.splunk.mint.NetSenderResponse;

import org.java_websocket.WebSocketImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static MyWebSocketServer myWebSocketServer;
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
        Mint.setUserOptOut(false);
        Mint.setMintCallback(new MintCallback() {
            @Override
            public void netSenderResponse(NetSenderResponse netSenderResponse) {
                Log.i(TAG, netSenderResponse.toString());
            }

            @Override
            public void dataSaverResponse(DataSaverResponse dataSaverResponse) {
                Log.i(TAG, dataSaverResponse.getData());
                Log.i(TAG, dataSaverResponse.toString());
            }

            @Override
            public void lastBreath(Exception e) {
                Log.i(TAG, e.getMessage());
            }
        });

        // TODO: Update with your API key
        //Mint.initAndStartSession(this.getApplication(), MyAppProperties.getProperty("Splunk.api.key"));

        // TODO: Update with your HEC token
        Mint.initAndStartSessionHEC(this.getApplication(), MyAppProperties.getProperty("Splunk.hec.url"), MyAppProperties.getProperty("Splunk.hec.token"));

        //Set some form of userIdentifier for this session
        Mint.setUserIdentifier("jgautier");

        // Init GPIO
        myGpio = new MyGpio();

        //WebSocketServer
        WebSocketImpl.DEBUG = Boolean.getBoolean(MyAppProperties.getProperty("MyWebSocketServer.debug"));
        try {
            InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(MyAppProperties.getProperty("MyWebSocketServer.hostname")),
                    Integer.parseInt(MyAppProperties.getProperty("MyWebSocketServer.port")));
            //socketServer = new MyWebSocketServer(address);
            myWebSocketServer = new MyWebSocketServer(Integer.parseInt(MyAppProperties.getProperty("MyWebSocketServer.port")), myGpio);
            myWebSocketServer.start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        new Timer().schedule(new TimerTask() {
            public void run() {
                MyLog.logEvent("Initialisation");
            }
        }, 10000);

        //new MyEmail().execute("Initialisation", "Initialisation completed");



    }
}
