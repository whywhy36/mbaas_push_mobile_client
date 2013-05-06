package com.example.pushclient;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONObject;

public class HttpUtils {
	public static void restPost(String endpoint, String jsonPayload, ResponseHandler resHandler) throws ClientProtocolException, IOException{
		HttpClient hc = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(endpoint);
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setEntity(new StringEntity(jsonPayload));
		hc.execute(httpPost, resHandler);
	}
	
	public static Object responseGet(String responseStr, String key) throws JSONException{
		JSONObject jsonObj = new JSONObject(responseStr);
		Object value = jsonObj.get(key);
		return value;
	}
}
