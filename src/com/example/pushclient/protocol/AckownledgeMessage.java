package com.example.pushclient.protocol;

public class AckownledgeMessage {
	public String event = "pushAck";
	public int seq;
	public Info[] info;
	
	public AckownledgeMessage(){
		
	}
	
	public static class Info{
		public String regId;
		public String[] messageIds;
		public Info(){
			
		}
	}

}
