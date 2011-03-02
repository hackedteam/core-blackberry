package blackberry.agent.im;

public class User {
	public String name;
	public String pin;
	public String email;
	
	public User(String name, String pin, String email){
		this.name = name;
		this.pin = pin;
		this.email = email;
	}
	
	public User(String user) {
        this(user,"","");
    }

    public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(Object obj) {
		return name.equals(obj);
	}
}
