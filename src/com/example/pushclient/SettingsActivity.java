package com.example.pushclient;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity implements OnClickListener{
	
	EditText mEditTextWebsocket;
	EditText mEditTextPushEngine;
	EditText mEditTextRegisterServer;
	Button mButtonSave;
	
	String TAG = "SettingActivity";
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		mEditTextWebsocket = (EditText) findViewById(R.id.editTextWSURL);
		mEditTextPushEngine = (EditText) findViewById(R.id.editTextPushEngine);
		mEditTextRegisterServer = (EditText) findViewById(R.id.editTextRegisterServer);
		
		mButtonSave = (Button) findViewById(R.id.buttonSettingSave);
		mButtonSave.setOnClickListener(this);
	}
	
	protected void onStart(){
		super.onStart();
		SharedPreferences settings = getSharedPreferences( Constants.PREFS_NAME, 0);
		mEditTextWebsocket.setText(settings.getString(Constants.WEB_SOCKET, Constants.DEFAULT_WS_URL));
		mEditTextPushEngine.setText(settings.getString(Constants.PUSH_ENGINE, Constants.DEFAULT_PE_URL));
		mEditTextRegisterServer.setText(settings.getString(Constants.REGISTER_SERVER, Constants.DEFAULT_RS_URL));
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if(view.getId() == R.id.buttonSettingSave){
			Log.d(TAG, "Saving...");
			SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putString(Constants.WEB_SOCKET, mEditTextWebsocket.getText().toString());
		    editor.putString(Constants.PUSH_ENGINE, mEditTextPushEngine.getText().toString());
		    editor.putString(Constants.REGISTER_SERVER, mEditTextRegisterServer.getText().toString());
		    if(editor.commit()){
		    	Log.d(TAG, "saved done");
		    	Toast.makeText(this, "Save Successfully", Toast.LENGTH_SHORT).show();
		    }
		}
	}
}
