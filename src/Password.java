import java.io.Serializable;

public class Password implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private String password;
	private boolean masterKey;
	private boolean base32Secret;
	
	public Password(String name, String password) {
		this.setName(name);
		this.setPassword(password);
		this.setMasterKey(false);
		this.setBase32Secret(false);
	}
	
	public Password(String name, String password, boolean masterkey, boolean base32Secret) {
		this.setName(name);
		this.setPassword(password);
		this.setMasterKey(masterkey);
		this.setBase32Secret(base32Secret);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setMasterKey(boolean b) {
		this.masterKey = b;
	}
	
	public boolean isMasterKey() {
		return this.masterKey;
	}
	
	public void setBase32Secret(boolean b) {
		this.base32Secret = b;
	}
	
	public boolean Base32Secret() {
		return this.base32Secret;
	}
	
	public boolean isBase32Secret() {
		return this.base32Secret;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
