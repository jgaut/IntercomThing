package com.example.jeremy.intercomthing;

import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by jeremy on 21/01/2017.
 */

public class MySocketIo {

    private String TAG = this.getClass().toString();
    private Socket soc;

    MySocketIo(){
        Log.i(TAG, "Connexion au serveur...");
        try{
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            soc = IO.socket("http://88.166.207.71:5000");
        } catch (URISyntaxException e) {
            MyLog.logException(TAG,e);
        }
        soc.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i(TAG, "connect");
            }

        }).on("test", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i(TAG, "test");
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {}

        }).on("ring", new Emitter.Listener() {

            @Override
            public void call(Object... args) {

            }

        });
        soc.connect();
        soc.emit("message", "New connection");
    }
}
