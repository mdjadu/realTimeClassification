package com.example.realtimeeyegazeclassification;

import static android.content.ContentValues.TAG;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class EchoWebSocketListener extends WebSocketListener {
    private static final int CLOSE_STATUS = 1000;
    public String coordies = "";
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Log.d("webSocket", "webSocket got connected");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        String tmp = "";
        Log.d(TAG,"Receiving: " + text);
        try {
            JSONObject js = new JSONObject(text);
            tmp = js.getString("payload");

            JSONObject c = new JSONObject(tmp);
            String coordi = c.getString("coordinates");



            Log.d("rrmm",coordi);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        coordies = coordies + text + "\n";
//        MainActivity.coordi.setText(coordies);

    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        webSocket.close(CLOSE_STATUS,null);
        Log.d(TAG,"connection closed");
    }

    @Override
    public void onFailure(WebSocket webSocket,Throwable t,Response response){
        Log.d("webSocket", t.toString());
    }
}
