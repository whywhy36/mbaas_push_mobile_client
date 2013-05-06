package com.example.pushclient.protocol;

public class SubscribeMessage {
	public String event = "addRegId";
	public int seq;
	public String[] regIds;
	
	public SubscribeMessage(){
		
	}
}
