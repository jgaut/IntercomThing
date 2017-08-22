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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static MyWebSocketServer myWebSocketServer;
    private MyGpio myGpio;
    private String TAG = this.getClass().toString();

    private static final String ADAPTER_FRIENDLY_NAME = "My Android Things device";
    private static final int DISCOVERABLE_TIMEOUT_MS = 3000;
    private static final int REQUEST_CODE_ENABLE_DISCOVERABLE = 100;

    private static final String UTTERANCE_ID = "com.example.androidthings.bluetooth.audio.UTTERANCE_ID";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothProfile mA2DPSinkProxy;

    private MyTts myTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain all app properties
        MyAppProperties.init(this.getApplicationContext());

        //Mint
            // Set the application environment
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.enableDebugLog();
        Mint.setUserOptOut(false);
        Mint.initAndStartSessionHEC(this.getApplication(), MyAppProperties.getProperty("Splunk.hec.url"), MyAppProperties.getProperty("Splunk.hec.token"));
            //Set some form of userIdentifier for this session
        Mint.setUserIdentifier("jgautier");

        // Text-to-Speech
        myTts = new MyTts(this);

        // GPIO
        myGpio = new MyGpio(myTts);

        //WebSocketServer
        WebSocketImpl.DEBUG = Boolean.getBoolean(MyAppProperties.getProperty("MyWebSocketServer.debug"));
        try {
            InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(MyAppProperties.getProperty("MyWebSocketServer.hostname")),
                    Integer.parseInt(MyAppProperties.getProperty("MyWebSocketServer.port")));
            //socketServer = new MyWebSocketServer(address);
            myWebSocketServer = new MyWebSocketServer(Integer.parseInt(MyAppProperties.getProperty("MyWebSocketServer.port")), myGpio, myTts);
            myWebSocketServer.start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "No default Bluetooth adapter. Device likely does not support bluetooth.");
            return;
        }

        registerReceiver(mAdapterStateChangeReceiver, new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(mSinkProfileStateChangeReceiver, new IntentFilter(
                A2dpSinkHelper.ACTION_CONNECTION_STATE_CHANGED));
        registerReceiver(mSinkProfilePlaybackChangeReceiver, new IntentFilter(
                A2dpSinkHelper.ACTION_PLAYING_STATE_CHANGED));

        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth Adapter is already enabled.");
            initA2DPSink();
        } else {
            Log.d(TAG, "Bluetooth adapter not enabled. Enabling.");
            mBluetoothAdapter.enable();
        }

        setContentView(R.layout.activity_main);

        new Timer().schedule(new TimerTask() {
            public void run() {
                MyLog.logEvent("Initialisation completed");
                myTts.speak("Initialisation completed");
            }
        }, 10000);
    }

    /**
     * Handle an intent that is broadcast by the Bluetooth adapter whenever it changes its
     * state (after calling enable(), for example).
     * Action is {@link BluetoothAdapter#ACTION_STATE_CHANGED} and extras describe the old
     * and the new states. You can use this intent to indicate that the device is ready to go.
     */
    private final BroadcastReceiver mAdapterStateChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int oldState = A2dpSinkHelper.getPreviousAdapterState(intent);
            int newState = A2dpSinkHelper.getCurrentAdapterState(intent);
            Log.d(TAG, "Bluetooth Adapter changing state from " + oldState + " to " + newState);
            if (newState == BluetoothAdapter.STATE_ON) {
                Log.i(TAG, "Bluetooth Adapter is ready");
                initA2DPSink();
            }
        }
    };

    /**
     * Handle an intent that is broadcast by the Bluetooth A2DP sink profile whenever a device
     * connects or disconnects to it.
     * Action is {@link A2dpSinkHelper#ACTION_CONNECTION_STATE_CHANGED} and
     * extras describe the old and the new connection states. You can use it to indicate that
     * there's a device connected.
     */
    private final BroadcastReceiver mSinkProfileStateChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(A2dpSinkHelper.ACTION_CONNECTION_STATE_CHANGED)) {
                int oldState = A2dpSinkHelper.getPreviousProfileState(intent);
                int newState = A2dpSinkHelper.getCurrentProfileState(intent);
                BluetoothDevice device = A2dpSinkHelper.getDevice(intent);
                Log.d(TAG, "Bluetooth A2DP sink changing connection state from " + oldState +
                        " to " + newState + " device " + device);
                if (device != null) {
                    String deviceName = Objects.toString(device.getName(), "a device");
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        myTts.speak("Connected to " + deviceName);
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        myTts.speak("Disconnected from " + deviceName);
                    }
                }
            }
        }
    };

    /**
     * Handle an intent that is broadcast by the Bluetooth A2DP sink profile whenever a device
     * starts or stops playing through the A2DP sink.
     * Action is {@link A2dpSinkHelper#ACTION_PLAYING_STATE_CHANGED} and
     * extras describe the old and the new playback states. You can use it to indicate that
     * there's something playing. You don't need to handle the stream playback by yourself.
     */
    private final BroadcastReceiver mSinkProfilePlaybackChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(A2dpSinkHelper.ACTION_PLAYING_STATE_CHANGED)) {
                int oldState = A2dpSinkHelper.getPreviousProfileState(intent);
                int newState = A2dpSinkHelper.getCurrentProfileState(intent);
                BluetoothDevice device = A2dpSinkHelper.getDevice(intent);
                Log.d(TAG, "Bluetooth A2DP sink changing playback state from " + oldState +
                        " to " + newState + " device " + device);
                if (device != null) {
                    if (newState == A2dpSinkHelper.STATE_PLAYING) {
                        Log.i(TAG, "Playing audio from device " + device.getAddress());
                    } else if (newState == A2dpSinkHelper.STATE_NOT_PLAYING) {
                        Log.i(TAG, "Stopped playing audio from " + device.getAddress());
                    }
                }
            }
        }
    };

    /**
     * Initiate the A2DP sink.
     */
    private void initA2DPSink() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth adapter not available or not enabled.");
            return;
        }
        Log.d(TAG, "Set up Bluetooth Adapter name and profile");
        mBluetoothAdapter.setName(ADAPTER_FRIENDLY_NAME);
        mBluetoothAdapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                mA2DPSinkProxy = proxy;
                enableDiscoverable();
            }
            @Override
            public void onServiceDisconnected(int profile) {
            }
        }, A2dpSinkHelper.A2DP_SINK_PROFILE);

        //configureButton();
    }

    /**
     * Enable the current {@link BluetoothAdapter} to be discovered (available for pairing) for
     * the next {@link #DISCOVERABLE_TIMEOUT_MS} ms.
     */
    private void enableDiscoverable() {
        Log.d(TAG, "Registering for discovery.");
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                DISCOVERABLE_TIMEOUT_MS);
        startActivityForResult(discoverableIntent, REQUEST_CODE_ENABLE_DISCOVERABLE);
    }



    private void disconnectConnectedDevices() {
        if (mA2DPSinkProxy == null || mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            return;
        }
        myTts.speak("Disconnecting devices");
        for (BluetoothDevice device: mA2DPSinkProxy.getConnectedDevices()) {
            Log.i(TAG, "Disconnecting device " + device);
            A2dpSinkHelper.disconnect(mA2DPSinkProxy, device);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_DISCOVERABLE) {
            Log.d(TAG, "Enable discoverable returned with result " + resultCode);

            // ResultCode, as described in BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE, is either
            // RESULT_CANCELED or the number of milliseconds that the device will stay in
            // discoverable mode. In a regular Android device, the user will see a popup requesting
            // authorization, and if they cancel, RESULT_CANCELED is returned. In Android Things,
            // on the other hand, the authorization for pairing is always given without user
            // interference, so RESULT_CANCELED should never be returned.
            if (resultCode == RESULT_CANCELED) {
                Log.e(TAG, "Enable discoverable has been cancelled by the user. " +
                        "This should never happen in an Android Things device.");
                return;
            }
            Log.i(TAG, "Bluetooth adapter successfully set to discoverable mode. " +
                    "Any A2DP source can find it with the name " + ADAPTER_FRIENDLY_NAME +
                    " and pair for the next " + DISCOVERABLE_TIMEOUT_MS + " ms. " +
                    "Try looking for it on your phone, for example.");

            // There is nothing else required here, since Android framework automatically handles
            // A2DP Sink. Most relevant Bluetooth events, like connection/disconnection, will
            // generate corresponding broadcast intents or profile proxy events that you can
            // listen to and react appropriately.

            myTts.speak("Bluetooth audio sink is discoverable for " + DISCOVERABLE_TIMEOUT_MS +
                    " milliseconds. Look for a device named " + ADAPTER_FRIENDLY_NAME);

        }
    }
}
