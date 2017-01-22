package com.example.jeremy.intercomthing;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by jeremy on 15/01/2017.
 */

public class IftttHttpRequest extends AsyncTask<String, Void, String> {

    private static String key = "cyvx1rRXFohARJaAe1TdW_";
    private String TAG = this.getClass().toString();
    protected String doInBackground(String... events){

        for (String event : events) {
            String url = "https://maker.ifttt.com/trigger/"+event+"/with/key/";
            String charset = java.nio.charset.StandardCharsets.UTF_8.name();  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
            URLConnection connection = null;
            try {
                MyLog.i(TAG, url);
                connection = new URL(url + key).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connection.setRequestProperty("Accept-Charset", charset);
            try {
                InputStream response = connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
