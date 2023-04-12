import java.awt.Component;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class PasswordRenderer extends DefaultListCellRenderer {
	
	private static final long serialVersionUID = 1L;
	private Cryptox cryptox;
	
	public PasswordRenderer(Cryptox cryptox) {
		this.cryptox = cryptox;
	}
	
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // Call the superclass method to get the default renderer
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        // Cast the value to a Password object
        Password password = (Password) value;
        
        // Decrypt the name and set it as the text of the component
        String decryptedName = null;
		try {
			decryptedName = cryptox.getAESDecrypt(password.getName());
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        setText(decryptedName);
        
        return this;
    }
}