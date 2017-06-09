package com.example.jeremy.intercomthing;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by jeremy on 09/06/2017.
 */

/*https://github.com/TooTallNate/Java-WebSocket/blob/master/src/main/example/ChatServer.java*/

public class MyWebSocketServer extends WebSocketServer {

    public MyWebSocketServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public MyWebSocketServer(InetSocketAddress address) {
        super(address);
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
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        MyLog.logEvent("WebSocketServer error : " + ex.getMessage());
    }

    @Override
    public void onStart() {
        MyLog.logEvent("WebSocketServer started=" + this.getAddress().toString());
    }
}
