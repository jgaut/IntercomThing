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
 */

public class MyGpio {

    private static boolean delay = true;
    private String TAG = this.getClass().toString();
    private Gpio mGpio;
    private Gpio gpio18;
    private Gpio gpio23;

    MyGpio(){
        //Listing des ports GPIO
        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> portList = manager.getGpioList();
        if (portList.isEmpty()) {
            Log.i(TAG, "No GPIO port available on this device.");
        } else {
            Log.i(TAG, "List of available ports: " + portList);
        }

        //Gestion du callback
        GpioCallback mGpio18Callback = new GpioCallback() {
            @Override
            public boolean onGpioEdge(Gpio gpio) {
                // Read the active low pin state
                try {
                    //Log.i("GPIO", "BCM18="+gpio.getValue());
                    if (gpio.getValue() && delay) {
                        delay=false;
                        MyLog.logEvent("BCM18=" + gpio.getValue());
                        MyLog.logEvent("Security delay=" + delay);
                        //MyLog.i(TAG, gpio.toString() + new Boolean(gpio.getValue()).toString());

                        new Timer().schedule(new TimerTask(){
                            public void run(){
                                delay=true;
                                MyLog.logEvent("Security delay=" + delay);
                            }
                        },5000);

                        //new IftttHttpRequest().execute("ring");
                        MyLog.logEvent("Ring");
                        new MyEmail().execute("Ring", "Someone ring the bell !");
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
        //BCM18 -> IN
        try {
            gpio18 = manager.openGpio("BCM18");
            // Initialize the pin as an input
            gpio18.setDirection(Gpio.DIRECTION_IN);
            gpio18.setEdgeTriggerType(Gpio.EDGE_BOTH);
            MyLog.logEvent("BCM18=" + gpio18.getValue());
            //Attache du callback
            gpio18.registerGpioCallback(mGpio18Callback);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Initialisation des GPIO pour l'ouverture de la porte
        //BCM23 -> OUT to open the door
        try {
            gpio23 = manager.openGpio("BCM23");
            // Initialize the pin as an input
            gpio23.setDirection(Gpio.DIRECTION_IN);
            MyLog.logEvent("BCM23=" + gpio23.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openDoor(){
        MyLog.logEvent("Open Door");
        try {
            gpio23.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            MyLog.logEvent("BCM23:" + gpio23.getValue());
            new Timer().schedule(new TimerTask(){
                public void run(){
                    try {
                        gpio23.setDirection(Gpio.DIRECTION_IN);
                        MyLog.logEvent("BCM23:" + gpio23.getValue());
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
