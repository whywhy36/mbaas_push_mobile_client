package com.example.pushclient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.pushclient.protocol.AckownledgeMessage;
import com.example.pushclient.protocol.AckownledgeMessage.Info;
import com.example.pushclient.protocol.PushMessage;
import com.google.gson.Gson;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import de.tavendo.autobahn.WebSocketOptions;

public class TheService extends Service {

	private static final String TAG = "TheService";
	private static final String URL = "ws://10.110.185.92:10280";
	private static final String REG_SERVER = "http://10.110.185.92:10080";
	private static final String PUSH_ENGINE = "http://10.110.185.90";
	
	private final WebSocketConnection mConnection = new WebSocketConnection();
	
	private String regId = null;
	private String subscriberId = null;
	
	private Gson gson = null;
	
	NotificationCompat.Builder mNotificationBuilder;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate(){
		Toast.makeText(this, "The service created", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onCreate");
	}
	
	public void onDestroy(){
		Toast.makeText(this, "The service destroyed", Toast.LENGTH_SHORT).show();
		mConnection.disconnect();
		Log.d(TAG, "onDestroy");
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		if(gson == null){
			gson = new Gson();
		}
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		regId = intent.getStringExtra("regId");
		subscriberId = intent.getStringExtra("subscriberId");
		Log.d(TAG, "regId is "+ regId + ", and the subscriberId is " + subscriberId);
		start();
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void showNotification(String content){
		if(mNotificationBuilder == null){
			mNotificationBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Notified Notification")
					.setContentText("Notification test")
					.setAutoCancel(true);
		}
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
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
	
	private void start(){
		try{
			String[] sp = new String[1];
			sp[0] = "msg-json";
			
			mConnection.connect(URL, sp, new WebSocketHandler(){
				public void onOpen(){
					Log.d(TAG, "Status: Connected to " + URL);

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
					mConnection.sendTextMessage(jo.toString());
				}
				
				public void onTextMessage(String message){
					Log.d(TAG, "Message: " + message);
					try{
						processMessage(message);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				
				public void onClose(){
					Log.d(TAG, "Status: Connection lost.");
				}
			}, new WebSocketOptions());
		}catch(WebSocketException e){
			Log.d(TAG, e.toString());
		}
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
			mConnection.sendTextMessage(response);
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
