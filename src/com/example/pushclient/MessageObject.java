package com.example.pushclient;

public class MessageObject {
	
	public String topic;
	public int action;
	
	public MessageObject(){
		
	}
	
	public MessageObject(int action, String topic){
		this.action = action;
		this.topic = topic;
	}
	
	public MessageObject(int action){
		this.action = action;
		this.topic = null;
	}

}
