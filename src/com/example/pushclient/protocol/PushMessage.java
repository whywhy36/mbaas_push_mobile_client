package com.example.pushclient.protocol;

public class PushMessage {
	public String event;
	public Info[] info;

	public PushMessage(){
		
	}
	
	public static class Info{
		public String regId;
		public Message[] messages;
		
		public Info(){
			
		}
		
		public static class Message{
			public String id;
			public String content;
			public Message(){
				
			}
		}
	}
}
