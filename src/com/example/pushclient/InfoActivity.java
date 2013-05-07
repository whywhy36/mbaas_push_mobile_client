package com.example.pushclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class InfoActivity extends Activity implements OnClickListener{
	
	private TextView mTextViewInfo;
	private Button mButton;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		
		mTextViewInfo = (TextView) findViewById(R.id.textViewInfo);
		mButton = (Button)findViewById(R.id.buttonBackToConsole);
		mButton.setOnClickListener(this);
	}
	
	protected void onStart(){
		super.onStart();
		Intent intent = getIntent();
		if(intent != null){
			String content = intent.getStringExtra("content");
			mTextViewInfo.setText(content);
		}
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		if(viewId == R.id.buttonBackToConsole){
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
	}
}
