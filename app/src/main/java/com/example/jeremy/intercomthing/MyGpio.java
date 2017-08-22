package com.example.jeremy.intercomthing;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jeremy on 18/01/2017.
 * https://developer.android.com/things/hardware/raspberrypi-io.html
 */

public class MyGpio {

    private static boolean delay = true;
    private String TAG = this.getClass().toString();
    private Gpio gpio24;
    private Gpio gpio23;
    private Gpio gpio18;
    private GpioCallback mGpio24Callback;

    MyGpio() {
        //Listing des ports GPIO
        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> portList = manager.getGpioList();
        if (portList.isEmpty()) {
            Log.i(TAG, "No GPIO port available on this device.");
        } else {
            Log.i(TAG, "List of available ports: " + portList);
        }

        //Gestion du callback
        mGpio24Callback = new GpioCallback() {
            @Override
            public boolean onGpioEdge(Gpio gpio) {
                // Read the active low pin state
                try {
                    //Log.i("GPIO", "BCM24="+gpio.getValue());
                    if (gpio.getValue() && delay) {
                        delay=false;
                        Log.i(TAG, "BCM24=" + gpio.getValue());
                        Log.i(TAG, "Security delay=" + delay);
                        //MyLog.i(TAG, gpio.toString() + new Boolean(gpio.getValue()).toString());

                        new Timer().schedule(new TimerTask(){
                            public void run(){
                                delay=true;
                                Log.i(TAG, "Security delay=" + delay);
                                initGpio24();
                            }
                        },5000);

                        //new IftttHttpRequest().execute("ring");
                        MyLog.logEvent("Ring");
                        if (MainActivity.myWebSocketServer != null) {
                            MainActivity.myWebSocketServer.sendToAll("Ring");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Continue listening for more interrupts
                return true;
            }

            @Override
            public void onGpioError(Gpio gpio, int error) {
                Log.w(TAG, gpio + ": Error event " + error);
            }
        };

        //Initialisation des GPIO pour la détection de la sonnerie
        //BCM24 -> IN
        try {
            gpio24 = manager.openGpio("BCM24");

            // Initialize the pin as an input
            this.initGpio24();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Initialisation des GPIO pour la détection de la sonnerie -> Simulation
        //BCM18 -> OUT
        try {
            gpio18 = manager.openGpio("BCM18");
            gpio18.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            Log.i(TAG, "First time BCM18=" + gpio18.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Initialisation des GPIO pour l'ouverture de la porte
        //BCM23 -> OUT to open the door
        try {
            gpio23 = manager.openGpio("BCM23");
            // Initialize the pin as an input
            gpio23.setDirection(Gpio.DIRECTION_IN);
            Log.i(TAG, "First time BCM23=" + gpio23.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Gpio getGpio18() {
        return gpio18;
    }

    public GpioCallback getmGpio24Callback() {
        return mGpio24Callback;
    }


    private void initGpio24() {
        try {
            gpio24.setDirection(Gpio.DIRECTION_IN);
            //gpio24.setActiveType(Gpio.ACTIVE_HIGH);
            //gpio24.setEdgeTriggerType(Gpio.EDGE_RISING);
            //Attache du callback
            gpio24.registerGpioCallback(mGpio24Callback);
            Log.i(TAG, "First time BCM24=" + gpio24.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void openDoor(){
        MyLog.logEvent("Open Door");
        if (MainActivity.myWebSocketServer != null) {
            MainActivity.myWebSocketServer.sendToAll("Open Door");
        }
        try {
            gpio23.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            Log.i(TAG, "BCM23:" + gpio23.getValue());
            new Timer().schedule(new TimerTask(){
                public void run(){
                    try {
                        gpio23.setDirection(Gpio.DIRECTION_IN);
                        Log.i(TAG, "BCM23:" + gpio23.getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            },1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
