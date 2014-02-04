package com.rim.samples.device.bbminjectdemo;

public class User {
	public String name;
	public String pin;
	public String email;
	
	public User(String name, String pin, String email){
		this.name = name;
		this.pin = pin;
		this.email = email;
	}
	
	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(Object obj) {
		return name.equals(obj);
	}
}
