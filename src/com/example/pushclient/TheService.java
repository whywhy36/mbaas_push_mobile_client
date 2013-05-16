package com.example.pushclient;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.codebutler.android_websockets.WebSocketClient;
import com.example.pushclient.protocol.AckownledgeMessage;
import com.example.pushclient.protocol.PushMessage;
import com.google.gson.Gson;



public class TheService extends Service {

	private static final String TAG = "TheService";
	
	private WebSocketClient mConnection = null;
	
	private String regId = null;
	private String subscriberId = null;
	
	private Gson gson = null;
	
	NotificationCompat.Builder mNotificationBuilder;
	
	private WSHandler mHandler;
	private Looper mWsLooper;
	
	Context that = this;
	
	private class WSHandler extends Handler {
		public WSHandler(Looper looper){
			super(looper);
		}
		
		public void handleMessage(Message msg){
			switch(msg.what){
			case 1:
				connect();
				break;
			case -1:
				disconnect();
				break;
			}
		}
		
		public void connect(){
			List<BasicNameValuePair> extraHeaders = Arrays.asList(
				    new BasicNameValuePair("Sec-WebSocket-Protocol", "msg-json"),
				    new BasicNameValuePair("Upgrade", "websocket"),
				    new BasicNameValuePair("Connection", "Upgrade")
			);
			
			mConnection = new WebSocketClient(URI.create(webSocketUrl()), new WebSocketClient.Listener() {
				
				@Override
				public void onMessage(byte[] data) {
				}
				
				@Override
				public void onMessage(String message) {
					Log.d(TAG, "Message: " + message);
					try{
						processMessage(message);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				
				@Override
				public void onError(Exception error) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Exception");
					error.printStackTrace();
				}
				
				@Override
				public void onDisconnect(int code, String reason) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Status: Connection lost.");
				}
				
				@Override
				public void onConnect() {
					// TODO Auto-generated method stub
					Log.d(TAG, "Status: Connected to " + webSocketUrl());

					JSONObject jo = new JSONObject();
					try {
						jo.put("event", "addRegId");
						jo.put("seq", "12345");
						JSONArray ja = new JSONArray();
						ja.put(regId);
						jo.put("regIds", ja );
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d(TAG, "send back to server message :" + jo.toString());
					mConnection.send(jo.toString());
				}
			}, extraHeaders);
			mConnection.connect();
		}
		
		public void disconnect(){
			mConnection.disconnect();
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate(){
		showToast("The service created.");
		that = this;
		HandlerThread thread = new HandlerThread("ServiceBgArgument", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        
        mWsLooper = thread.getLooper();
		mHandler = new WSHandler(mWsLooper); 
		Log.d(TAG, "onCreate");
	}
	
	public void onDestroy(){
		showToast("Disconnecting the server.");
		mHandler.sendEmptyMessage(-1);
		showToast("The service destroyed.");
		Log.d(TAG, "onDestroy");
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		if(gson == null){
			gson = new Gson();
		}
		showToast("The service starting.");
		regId = intent.getStringExtra("regId");
		subscriberId = intent.getStringExtra("subscriberId");
		Log.d(TAG, "regId is "+ regId + ", and the subscriberId is " + subscriberId);
		start();
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void showToast(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	public void showNotification(String content){
		if(mNotificationBuilder == null){
			mNotificationBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Notified Notification")
					.setContentText("Notification test")
					.setAutoCancel(true);
		}
		Intent resultIntent = new Intent(this, InfoActivity.class);
		resultIntent.putExtra("content", content);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(InfoActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
				0,
				PendingIntent.FLAG_UPDATE_CURRENT
			);
		mNotificationBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationBuilder.setContentText(content);
		
		mNotificationManager.notify(1, mNotificationBuilder.build());	
	}
	
	private String webSocketUrl(){
		SharedPreferences settings = getSharedPreferences( Constants.PREFS_NAME, 0);
		return settings.getString(Constants.WEB_SOCKET, Constants.DEFAULT_WS_URL);
	}
	
	private void start(){
		mHandler.sendEmptyMessage(1);
	}
	
	private void processMessage(String message) throws Exception{
		PushMessage pushMessage = gson.fromJson(message, PushMessage.class);
		if (pushMessage.event.equals("push")){
			popUpNotification(pushMessage);
			
			AckownledgeMessage ackMessage = new AckownledgeMessage();
			ackMessage.event = "pushAck";
			ackMessage.seq = 12345;
			ackMessage.info = new AckownledgeMessage.Info[1];
			
			AckownledgeMessage.Info theInfo = new AckownledgeMessage.Info();
			ackMessage.info[0] = theInfo;
			theInfo.regId = regId;
			
			PushMessage.Info.Message[] messages = pushMessage.info[0].messages;
			theInfo.messageIds = new String[messages.length];
			
			for(int i=0; i < messages.length; i++){
				ackMessage.info[0].messageIds[i] = messages[i].id;
			}
			
			String response = gson.toJson(ackMessage);
			Log.d(TAG, "the response is :" + response);
			mConnection.send(response);
		}else{
			// do nothing
		}
	}
	
	private void popUpNotification(PushMessage msgObject) throws JSONException{
		PushMessage.Info[] info = msgObject.info;
		PushMessage.Info theInfo = info[0];
		PushMessage.Info.Message[] messages = theInfo.messages;
		for(int i=0; i< messages.length; i++){
			JSONObject message = new JSONObject(messages[i].content);
			String content = (String) ((JSONObject)message.get("data")).get("message");
			Log.d(TAG, "The message is " + content );
			showNotification(content);
		}
	}
	
}
