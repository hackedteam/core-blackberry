package com.ht.rcs.blackberry.transfer;

public class Command {
	public int id;
	public byte[] payload;
	
	public int size()
	{
		if(payload != null)
			return payload.length;
		else
			return 0;
	}
	
	public Command(int commandId, byte[] payload)
	{
		this.id = commandId;
		this.payload = payload;
	}	
	
	public String toString()
	{
		return id + " len:" + size();
	}
}
