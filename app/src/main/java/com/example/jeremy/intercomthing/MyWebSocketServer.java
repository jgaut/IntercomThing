package com.example.jeremy.intercomthing;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

/**
 * Created by jeremy on 09/06/2017.
 */

/*
https://github.com/TooTallNate/Java-WebSocket/blob/master/src/main/example/ChatServer.java
*/

public class MyWebSocketServer extends WebSocketServer {

    private MyGpio myGpio;

    public MyWebSocketServer(int port, MyGpio myGpio) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.myGpio = myGpio;
    }

    public MyWebSocketServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public MyWebSocketServer(InetSocketAddress address) {
        super(address);
    }

    public MyWebSocketServer(InetSocketAddress address, MyGpio myGpio) {
        super(address);
        this.myGpio = myGpio;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        MyLog.logEvent("WebSocketServer opened=" + handshake.getResourceDescriptor());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        MyLog.logEvent("WebSocketServer closed;code=" + code + ";reason=" + reason + ";remote=" + remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        MyLog.logEvent("WebSocketServer message=" + message);
        if (message != null) {
            if (message.equals("open door")) {
                myGpio.openDoor();
            } else if (message.equals("echo")) {
                this.sendToAll("echo");
            } else if (message.equals("ring")) {
                myGpio.getmGpio24Callback().onGpioEdge(myGpio.getGpio18());
            }
        } else {

        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        MyLog.logEvent("WebSocketServer error : " + ex.getMessage());
    }

    @Override
    public void onStart() {
        MyLog.logEvent("WebSocketServer started=" + this.getAddress().toString());
        Log.i("socket", this.getDraft().toString());
    }

    public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
                MyLog.logEvent("echo sent to " + c.getRemoteSocketAddress());
            }
        }
    }
}
