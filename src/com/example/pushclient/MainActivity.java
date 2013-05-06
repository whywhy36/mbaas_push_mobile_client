package com.example.pushclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = "MainActivity";
	
	private static final String URL = "ws://10.110.185.92:10280";
	private static final String REG_SERVER = "http://10.110.185.92:10080";
	private static final String PUSH_ENGINE = "http://10.110.185.90";
	
	private static final int SUBSCRIBE_ACTION = 1;
	private static final int UNSUBSCRIBE_ACTION = 2;
	private static final int REGISTER_DEVICE = 3;
	private static final int UNREGISTER_DEVICE = 4;
	
	ToggleButton mToggleButtonApple, mToggleButtonAmazon, mToggleButtonIntel, mToggleButtonGoogle, mToggleButtonMicrosoft, mToggleButtonVMware;
	
	Switch mSwitchService;
	TextView mTextViewStatus;
	
	private HttpHandler mHttpHandler;
	private Looper mHttpLooper;
	
	private String regId;
	private String subscriberId;
	
	private Context that;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mToggleButtonApple = (ToggleButton) findViewById(R.id.toggleButtonApple);
        mToggleButtonAmazon = (ToggleButton) findViewById(R.id.ToggleButtonAmazon);
        mToggleButtonIntel = (ToggleButton) findViewById(R.id.ToggleButtonIntel);
        mToggleButtonGoogle = (ToggleButton) findViewById(R.id.ToggleButtonGoogle);
        mToggleButtonMicrosoft = (ToggleButton) findViewById(R.id.ToggleButtonMicrosoft);
        mToggleButtonVMware = (ToggleButton) findViewById(R.id.ToggleButtonVMware);
        
        mSwitchService = (Switch)findViewById(R.id.switchService);
        
        mTextViewStatus = (TextView)findViewById(R.id.textViewStatus);
        
        HandlerThread thread = new HandlerThread("ServiceStartArgument", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        
        mHttpLooper = thread.getLooper();
        mHttpHandler = new HttpHandler(mHttpLooper);
        
        that = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public void onClick(View src) {
		// TODO Auto-generated method stub
		
	}
	
	public void topicAction(View view, String topic ){
		boolean on = ((ToggleButton)view).isChecked();
		if(on){
			subscribeTopic(topic);
		}else{
			unsubscribeTopic(topic);
		}
	}
	
	public void registerAction(View view){
		boolean on = ((Switch)view).isChecked();
		registerDevice(on);
	}
	
	public void onToggleClicked(View view){
		switch(view.getId()){
		case R.id.toggleButtonApple:
			topicAction(view, "Apple");
			break;
		case R.id.ToggleButtonAmazon:
			topicAction(view, "Amazon");
			break;
		case R.id.ToggleButtonIntel:
			topicAction(view, "Intel");
			break;
		case R.id.ToggleButtonGoogle:
			topicAction(view, "Google");
			break;
		case R.id.ToggleButtonMicrosoft:
			topicAction(view, "Microsoft");
			break;
		case R.id.ToggleButtonVMware:
			topicAction(view, "VMware");
			break;
		case R.id.switchService:
			registerAction(view);
			break;
		}
	}
	
	private void updateStatus(String status){
		mTextViewStatus.setText(status);
	}
	
	private void subscribeTopic(String topic){
		Message message = mHttpHandler.obtainMessage();
		message.obj = new MessageObject(this.SUBSCRIBE_ACTION, topic);
		mHttpHandler.sendMessage(message);
	}
	
	private void unsubscribeTopic(String topic){
		Message message = mHttpHandler.obtainMessage();
		message.obj = new MessageObject(this.UNSUBSCRIBE_ACTION, topic);
		mHttpHandler.sendMessage(message);
	}
	
	private void registerDevice(boolean flag){
		Message message = mHttpHandler.obtainMessage();
		if(flag){
			message.obj = new MessageObject(this.REGISTER_DEVICE);
		}else{
			message.obj = new MessageObject(this.UNREGISTER_DEVICE);
		}
		mHttpHandler.sendMessage(message);
	}
	
	private final class HttpHandler extends Handler {
		public HttpHandler(Looper looper){
			super(looper);
		}
		
		public void handleMessage(Message msg){
			try{
				MessageObject message = (MessageObject) msg.obj;
				int action = message.action;
				String topic = message.topic;
				switch(action){
				case SUBSCRIBE_ACTION:
					subscribe(topic);
					break;
				case UNSUBSCRIBE_ACTION:
					//TODO:
					break;
				case REGISTER_DEVICE:
					register();
					connectPE();
					//startService(new Intent(that, TheService.class));
					break;
				case UNREGISTER_DEVICE:
					//TODO:
					stopService(new Intent(that, TheService.class));
					break;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		private void register() throws ClientProtocolException, IOException{
			ResponseHandler resHandler = new ResponseHandler(){
				public Object handleResponse(HttpResponse response){
					String responseString;
					try {
						responseString = EntityUtils.toString(response.getEntity());
						regId = (String)HttpUtils.responseGet(responseString, "regId");
						Log.d(TAG, "Response of register is " + responseString);
					}catch(Exception e){
						e.printStackTrace();
					}
					return null;
				}
			};
			String register_url = REG_SERVER + "/register";
			HttpUtils.restPost(register_url, "{\"appKey\": \"100\", \"deviceFingerPrint\": \"1111\"}", resHandler);
		}
		
		private void connectPE() throws ClientProtocolException, IOException, JSONException{
			ResponseHandler resHandler = new ResponseHandler(){
				public Object handleResponse(HttpResponse response){
					String responseString;
					try{
						responseString = EntityUtils.toString(response.getEntity());
						subscriberId = (String)HttpUtils.responseGet(responseString, "id");
						Log.d(TAG, "the response of connection to PE is " + responseString);
						Intent intent = new Intent(that, TheService.class);
						intent.putExtra("regId", regId);
						intent.putExtra("subscriberId", subscriberId);
						startService(intent);
					}catch(Exception e){
						e.printStackTrace();
					}	
					return null;
				}
			};
			String subscribers_url = PUSH_ENGINE + "/subscribers";
			JSONObject reqObj = new JSONObject();
			reqObj.put("proto", "vns");
			reqObj.put("token", regId);
			HttpUtils.restPost(subscribers_url, reqObj.toString(), resHandler);
		}
		
		private void subscribe(String topic) throws ClientProtocolException, IOException{
			ResponseHandler resHandler = new ResponseHandler(){
				public Object handleResponse(HttpResponse response){
					String responseString;
					try {
						responseString = EntityUtils.toString(response.getEntity());
						Log.d(TAG, "the response of subscription is " + responseString);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					return null;
				}
			};
			Log.d(TAG, "try to subscribe topic:" + topic);
			String subscribe_url = PUSH_ENGINE + "/subscriber/" + subscriberId + "/subscriptions/" + topic;
			HttpUtils.restPost(subscribe_url, "{}", resHandler);
		}
	}
	
	
}
