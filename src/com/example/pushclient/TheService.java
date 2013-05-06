package com.example.pushclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
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
	
	NotificationCompat.Builder mNotificationBuilder;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate(){
		Toast.makeText(this, "The Service Created", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onCreate");
	}
	
	public void onDestroy(){
		Toast.makeText(this, "The service destroyed", Toast.LENGTH_SHORT).show();
		mConnection.disconnect();
		Log.d(TAG, "onDestroy");
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		regId = intent.getStringExtra("regId");
		subscriberId = intent.getStringExtra("subscriberId");
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
		//mNotificationBuilder.setNumber(++mNotificationCount);
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
					mConnection.sendTextMessage(jo.toString());
				}
				
				public void onTextMessage(String message){
					Log.d(TAG, "Message: " + message);
					processMessage(message);
				}
				
				public void onClose(){
					Log.d(TAG, "Status: Connection lost.");
				}
			}, new WebSocketOptions());
		}catch(WebSocketException e){
			Log.d(TAG, e.toString());
		}
	}
	
	private void processMessage(String message){
		try {
			JSONObject jo = new JSONObject(message);
			if (jo.get("event").equals("push")){
				
				popUpNotification(jo);
				
				JSONObject response = new JSONObject();
				response.put("event", "pushAck");
				response.put("seq", "12345");
				response.put("info", null);
				mConnection.sendTextMessage(response.toString());
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void popUpNotification(JSONObject msgObject) throws JSONException{
		JSONObject one = (JSONObject)((JSONArray)msgObject.get("info")).get(0);
		JSONArray messages = (JSONArray)(one.get("messages"));
		String messageStr = (String) ((JSONObject)(messages.get(0))).get("content");
		JSONObject theObject = new JSONObject(messageStr);
		String content = (String) ((JSONObject) theObject.get("data")).get("message");
		Log.d(TAG, "The message is " + content );
		showNotification(content);
	}
	
}
