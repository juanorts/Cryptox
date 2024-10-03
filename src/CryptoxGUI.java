import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.Font;

public class CryptoxGUI extends JFrame {

	private static final long serialVersionUID = 1L;

	//	Stored passwords
	ArrayList<Password> passwords = new ArrayList<Password>();

	//	Encryptor-decryptor
	private Cryptox cryptox;
	
	//	Window components
	@SuppressWarnings("unused")
	private JPanel pMain = new JPanel();
	private JTextField tPassword;
	private JComboBox<Password> comboBox;
	private File data;
	private File dir;
	private File logoPath;
	private File rightbgPath;
	private JPasswordField tPasswordHide;
	private JButton b2FA;
	private JButton bShow2FA;
	private JLabel lInfo;
	
	public CryptoxGUI() {
		//	Set initial configuration of the main frame: title, size, icon, layout(BorderLayout), centered
		setResizable(false);
		setTitle("Cryptox");
		setSize(550, 400);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		// Check if data directory exists, if not create it
		dir = new File(".data");
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		//	Load images
		logoPath = new File("resources/logo.png");
		rightbgPath = new File("resources/rightbg.png");
		setIconImage(new ImageIcon(logoPath.getAbsolutePath()).getImage());
		
		
		// Initialize the data file
		data = new File(".data/data.cryptox");
		
		// Create the main panel with an absolute layout
		JPanel pMain = new JPanel();
		pMain.setBackground(new Color(28, 28, 28));
		pMain.setForeground(new Color(0, 0, 0));
		getContentPane().add(pMain);
		pMain.setLayout(null);
		
		// Button to save a new password
		JButton bAddPassword = new JButton("Save");
		bAddPassword.setToolTipText("Store the new password into the Cryptox keychain.");
		bAddPassword.setFont(new Font("Roboto", Font.PLAIN, 13));
		bAddPassword.setBounds(68, 252, 75, 29);
		pMain.add(bAddPassword);
		
		bAddPassword.addActionListener(new ActionListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent e) {
				//	Ask user for a name for the password
				String name = "";
				while(true) {
					name = (String) JOptionPane.showInputDialog(null, "Write a name to identify the password\n\ne.g. Mail - john.doe@mail.com", "Cryptox - Create new password", JOptionPane.PLAIN_MESSAGE);
					if(name == null) {	// If CANCEL is pressed: stop the loop and do nothing
						break;
					} else if(name.isBlank()) {	//	Name is empty: tell the user to introduce it again
						JOptionPane.showInternalMessageDialog(null, "Name cannot be empty", "Password name is empty", JOptionPane.ERROR_MESSAGE);
					} else {
						break;
					}
				}
				
				if(name != null) {	// If OK was pressed and password is not empty: save the password
					Password p = new Password(name, tPasswordHide.getText());
					passwords.add(p);
					comboBox.addItem(p);
					try {
						storeAllPasswords();
					} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
							| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
							| IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					tPassword.setText("");
					tPasswordHide.setText("");
					Thread hilo = new Thread() {
						
						@Override
						public void run() {
							lInfo.setText("New password added");
							try {
								sleep(4000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							lInfo.setText("");
						}
					};
					hilo.start();
				}
			}
		});
		
		// Button to copy selected password to clipboard
		JButton bGetPassword = new JButton("Copy");
		bGetPassword.setToolTipText("Copy the currently selected password into the clipboard.");
		bGetPassword.setFont(new Font("Roboto", Font.PLAIN, 13));
		bGetPassword.setBackground(new Color(255, 255, 255));
		bGetPassword.setForeground(Color.BLACK);
		bGetPassword.setBounds(242, 63, 64, 29);
		bGetPassword.setVisible(false);
		pMain.add(bGetPassword);
		
		bGetPassword.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(comboBox.getItemCount() > 0) {
					Password p = (Password) comboBox.getSelectedItem();
					copyToClipboard(p.getPassword());
					
					Thread hilo = new Thread() {
						
						@Override
						public void run() {
							lInfo.setText("Password copied to clipboard");
							try {
								sleep(4000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							lInfo.setText("");
						}
					};
					hilo.start();
				}
			}
		});
		
		// Textfield to type in new passwords
		tPassword = new JTextField();
		tPassword.setFont(new Font("Roboto", Font.PLAIN, 13));
		tPassword.setToolTipText("Write new password");
		tPassword.setBounds(68, 214, 178, 26);
		tPassword.setVisible(false);
		tPassword.setEditable(false);
		pMain.add(tPassword);
		tPassword.setColumns(10);
		
		// Button to delete a selected password
		JButton bDelete = new JButton("Delete password");
		bDelete.setToolTipText("Delete the currently selected password.");
		bDelete.setForeground(new Color(255, 0, 0));
		bDelete.setFont(new Font("Roboto", Font.PLAIN, 13));
		bDelete.setBounds(186, 104, 120, 29);
		bDelete.setVisible(true);
		pMain.add(bDelete);
		
		bDelete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!((Password) comboBox.getSelectedItem()).isMasterKey()) {
					int option = JOptionPane.showConfirmDialog(null, "The password " + ((Password) comboBox.getSelectedItem()).getName() + " will be deleted forever.\nAre you sure?", "Delete password", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
					if(option == JOptionPane.YES_OPTION) {
						passwords.remove(comboBox.getSelectedItem());
						comboBox.removeItemAt(comboBox.getSelectedIndex());
						try {
							try {
								storeAllPasswords();
							} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
									| InvalidAlgorithmParameterException | IllegalBlockSizeException
									| BadPaddingException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						Thread hilo = new Thread() {
							
							@Override
							public void run() {
								lInfo.setText("Password deleted");
								try {
									sleep(4000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								lInfo.setText("");
							}
						};
						hilo.start();
					}
				} else {
					JOptionPane.showInternalMessageDialog(null, "Cannot delete Master Key", "Delete Master Key", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		// Combobox to select a password between all stored passwords
        comboBox = new JComboBox<Password>();
        comboBox.setFont(new Font("Roboto", Font.PLAIN, 13));
        comboBox.setBackground(new Color(255, 255, 255));
        comboBox.setForeground(new Color(0, 0, 0));
		comboBox.setToolTipText("Select one password stored in the Cryptox keychain.");
		comboBox.setBounds(68, 63, 178, 29);
		pMain.add(comboBox);
		
		comboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Password p = (Password) comboBox.getSelectedItem();
				if(p.isMasterKey()) {
					bGetPassword.setVisible(false);
					bDelete.setVisible(false);
				} else {
					bGetPassword.setVisible(true);
					bDelete.setVisible(true);
				}
			}
		});
		
		// Label on the right that contains the logo of Cryptox
		JLabel rightbg = new JLabel("");
		rightbg.setHorizontalAlignment(SwingConstants.CENTER);
		rightbg.setIcon(new ImageIcon(rightbgPath.getAbsolutePath()));
		rightbg.setBounds(373, 0, 177, 347);
		pMain.add(rightbg);
		
		rightbg.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showInternalMessageDialog(null, "Cryptox 2.0.2\n\nDeveloped by Juan Orts\n\n2024", "About Cryptox", JOptionPane.PLAIN_MESSAGE);
			}
		});
		
		// Button to edit a selected password
		JButton bEdit = new JButton("Edit password");
		bEdit.setToolTipText("Edit the currently selected password.");
		bEdit.setFont(new Font("Roboto", Font.PLAIN, 13));
		bEdit.setBounds(68, 104, 106, 29);
		pMain.add(bEdit);
		
		bEdit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//	Check that the password selected is not the Master Key
				if(!((Password) comboBox.getSelectedItem()).isMasterKey() ) {	// If it isn't: ask for a new name for the password
					
					String name = (String) JOptionPane.showInputDialog(null, "Write a name to identify the password\n\ne.g. Mail - john.doe@mail.com", "Cryptox - Edit password name", JOptionPane.PLAIN_MESSAGE, null, null, ((Password) comboBox.getSelectedItem()).getName());
					
					if(name != null) {	// If name is not null: OK was pressed
						
						String password = (String) JOptionPane.showInputDialog(null, "Type in new password", "Cryptox - Edit password", JOptionPane.PLAIN_MESSAGE, null, null, ((Password) comboBox.getSelectedItem()).getPassword());
						
						if(password != null) {	// If password is not null: OK was pressed
							if(!password.isBlank()) {	// If it isn't empty: modify the password instance and delete that instance from the arraylist
								Password p = (Password) comboBox.getSelectedItem();
								passwords.remove(p);
								p.setName(name);
								p.setPassword(password);
								
								try {
									storeAllPasswords();
								} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
										| InvalidAlgorithmParameterException | IllegalBlockSizeException
										| BadPaddingException | IOException e2) {
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}
								
								Thread hilo = new Thread() {
									
									@Override
									public void run() {
										
										lInfo.setText("Password " + p.getName() + " edited");
										
										try {
											sleep(4000);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										lInfo.setText("");
									}
								};
								hilo.start();
							} else {	// If password is empty: tell user it must not be empty
								JOptionPane.showInternalMessageDialog(null, "Password cannot be empty", "Password is empty", JOptionPane.ERROR_MESSAGE);				
							}
						}
					}
				} else { // If it is the Master Key: only ask for the new Master Key
					String password = JOptionPane.showInputDialog(null, "Type in the new Master Key\n\n WARNING: Store this password in a safe place, if not you could lose access to your Cryptox keychain!", "Change Master Key", JOptionPane.PLAIN_MESSAGE);
					if(password != null) {	// If the password is not null: OK was pressed
						if(!password.isBlank()) {	// If the password is not empty: modify the Password instance and remove it from the ArrayList
							Password p = (Password) comboBox.getSelectedItem();
							p.setPassword(password);	// Set the new Master Key
							changeMasterKey(password);	// Create a new Cryptox instance
							
							try {
								storeAllPasswords();
							} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
									| InvalidAlgorithmParameterException | IllegalBlockSizeException
									| BadPaddingException | IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							Thread hilo = new Thread() {
								
								@Override
								public void run() {
									lInfo.setText("Cryptox Master Key changed succesfully");
									try {
										sleep(4000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									lInfo.setText("");
								}
							};
							hilo.start();
						} else { // If it is empty: tell user it must not be empty
							JOptionPane.showInternalMessageDialog(null, "Password cannot be empty", "Password is empty", JOptionPane.ERROR_MESSAGE);				
						}
					}
				}
				
			}
		});
		
		// Label at the bottom to display actions performed
		lInfo = new JLabel("");
		lInfo.setBackground(new Color(50, 50, 50));
		lInfo.setHorizontalAlignment(SwingConstants.CENTER);
		lInfo.setForeground(new Color(255, 255, 255));
		lInfo.setFont(new Font("Roboto", Font.BOLD, 16));
		lInfo.setBounds(0, 347, 550, 26);
		lInfo.setOpaque(true);
		pMain.add(lInfo);
		
		// Password field to type in new passwords
		tPasswordHide = new JPasswordField();
		tPasswordHide.setBounds(68, 214, 178, 26);
		pMain.add(tPasswordHide);
		
		// Button to show the typed in password
		JButton bShowPass = new JButton("Show");
		bShowPass.setFont(new Font("Roboto", Font.PLAIN, 13));
		bShowPass.setBounds(242, 214, 64, 29);
		pMain.add(bShowPass);
		
		bShowPass.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showHide();
			}
		});
		
		// Label for the combobox
		JLabel lSelectPass = new JLabel("Select a stored password");
		lSelectPass.setForeground(new Color(255, 255, 255));
		lSelectPass.setFont(new Font("Roboto", Font.PLAIN, 18));
		lSelectPass.setHorizontalAlignment(SwingConstants.CENTER);
		lSelectPass.setBounds(68, 35, 238, 16);
		pMain.add(lSelectPass);
		
		// Label for the new password
		JLabel lCreateNew = new JLabel("Create a new password");
		lCreateNew.setHorizontalAlignment(SwingConstants.CENTER);
		lCreateNew.setForeground(Color.WHITE);
		lCreateNew.setFont(new Font("Roboto", Font.PLAIN, 18));
		lCreateNew.setBounds(68, 186, 238, 16);
		pMain.add(lCreateNew);
		
		// Button to generate random new passwords
		JButton bGenerate = new JButton("Generate random");
		bGenerate.setToolTipText("Generate a new password randomly.");
		bGenerate.setFont(new Font("Roboto", Font.PLAIN, 13));
		bGenerate.setBounds(171, 252, 135, 29);
		pMain.add(bGenerate);
		
		bGenerate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
		        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 10);
		        slider.setMajorTickSpacing(20);
		        slider.setMinorTickSpacing(5);
		        slider.setPaintTicks(true);
		        slider.setPaintLabels(true);
		        JLabel label = new JLabel("Number of characters: " + slider.getValue());

		        slider.addChangeListener(new ChangeListener() {
		            @Override
		            public void stateChanged(ChangeEvent e) {
		                label.setText("Number of characters: " + slider.getValue());
		            }
		        });

		        JCheckBox includeSymbolsCheckBox = new JCheckBox("Include symbols");
		        JCheckBox includeNumbersCheckBox = new JCheckBox("Include numbers");

		        Object[] options = {label, slider, includeSymbolsCheckBox, includeNumbersCheckBox};
		        int result = JOptionPane.showConfirmDialog(null, options, "Generate random password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		        if (result == JOptionPane.OK_OPTION) {
		        	String password = cryptox.generateRandomPassword(slider.getValue(), includeSymbolsCheckBox.isSelected(), includeNumbersCheckBox.isSelected());
					tPasswordHide.setText(password);
					showHide();
		        }
			}
		});
		
		// Button to delete all stored data
		JButton bReset = new JButton("Delete all data");
		bReset.setToolTipText("Delete all passwords in the Cryptox keychain including the Master Key.");
		bReset.setForeground(Color.RED);
		bReset.setFont(new Font("Roboto", Font.PLAIN, 13));
		bReset.setBounds(68, 318, 120, 29);
		pMain.add(bReset);
		
		bReset.addActionListener(new ActionListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent e) {
				String masterKey = null;
				do {
					JLabel lWarning = new JLabel("WARNING: ALL PASSWORDS WILL BE DELETED FOREVER!");
					JLabel lMessage = new JLabel("Confirm Master Key to delete all data");
					JPasswordField tPass = new JPasswordField();
					Object[] options = {lWarning, lMessage, tPass};
					int result = JOptionPane.showConfirmDialog(null, options, "Cryptox", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
					if (result == JOptionPane.OK_OPTION) {
						masterKey = tPass.getText();
					} else {
						masterKey = null;
					}
					if(masterKey == null){
						break;
					}
					if(!getMasterKey().getPassword().equals(masterKey)) {
						JOptionPane.showInternalMessageDialog(null, "Incorrect Master Key", "Error", JOptionPane.ERROR_MESSAGE);
					}
						
				} while(!getMasterKey().getPassword().equals(masterKey));
				if(getMasterKey().getPassword().equals(masterKey)) {
					String twoFA = null;
					if(has2FA()) {	// If 2FA is set up: ask user for lastest code
						lInfo = new JLabel("Type in Two-Factor Authentication (2FA) code");
						JTextField tCode = new JTextField();
						Object[] options2 = {lInfo, tCode};
							
						int result = JOptionPane.showConfirmDialog(null, options2, "Cryptox", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
						if (result == JOptionPane.OK_OPTION) {	// If OK pressed: check 2FA code is valid
							twoFA = tCode.getText();
							String code = null;
							try {
								code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(get2FA().getPassword());
							} catch (GeneralSecurityException e1) {
								e1.printStackTrace();
							}
							if(twoFA.equals(code) && !twoFA.isBlank()) {	//	Check if 2FA is valid
								data.delete();
								dir.delete();
								JOptionPane.showInternalMessageDialog(null, "All data was deleted successfully", "Reset", JOptionPane.PLAIN_MESSAGE);
								System.exit(0);
									
							} else {
								// 2FA code is not valid
								JOptionPane.showInternalMessageDialog(null, "Incorrect Two-Factor Authentication code", "2FA Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					} else {
						data.delete();
						dir.delete();
						JOptionPane.showInternalMessageDialog(null, "All data was deleted successfully", "Reset", JOptionPane.PLAIN_MESSAGE);
						System.exit(0);
					}
				}
			}
		});
		
		// Button to set/edit two-factor authentication
		b2FA = new JButton("");
		b2FA.setForeground(new Color(0, 119, 255));
		b2FA.setFont(new Font("Roboto", Font.BOLD, 13));
		b2FA.setBounds(200, 318, 106, 29);
		pMain.add(b2FA);
		
		b2FA.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(has2FA()) {
					delete2FA();
				} else {
					try {
						set2FA();
					} catch (GeneralSecurityException | IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		// Button to show Base32 secret and QR code to set 2FA in a new authentication app
		bShow2FA = new JButton("Show 2FA");
		bShow2FA.setToolTipText("Show the Base32 secret and QR code to set 2FA in a new authentication app.");
		bShow2FA.setForeground(new Color(0, 0, 0));
		bShow2FA.setFont(new Font("Roboto", Font.BOLD, 13));
		bShow2FA.setBounds(200, 293, 106, 29);
		pMain.add(bShow2FA);
		
		bShow2FA.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					show2FA();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
		});
		
		//	Stop the execution when closing the frame
		this.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
				
			}
		});
	}
	
	// Load the combobox with the stored passwords in the ArrayList
	public void loadCombobox() {
		for(Password p : this.passwords) {
			if(!p.isBase32Secret()) {
				comboBox.addItem(p);
			}
		}
	}
	
	// Delete the already existing two-factor authentication method
	public void delete2FA() {
		int option = JOptionPane.showConfirmDialog(null, "Two-Factor Authentication will be removed. Are you sure?", "Remove 2FA", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		if(option == JOptionPane.YES_OPTION) {
			passwords.remove(this.get2FA());
			try {
				try {
					storeAllPasswords();
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			this.b2FA.setText("Set 2FA");
			Thread hilo = new Thread() {
				
				@Override
				public void run() {
					lInfo.setText("Two-Factor Authentication removed");
					try {
						sleep(4000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					lInfo.setText("");
				}
			};
			hilo.start();
		}
	}
	
	// Set a two-factor authentication method for extra security by generating a QR code to scan it
	public void set2FA() throws GeneralSecurityException, IOException {
		String base32Secret = TimeBasedOneTimePasswordUtil.generateBase32Secret();

		String keyId = "Cryptox%202.0";
		JLabel lInfo = new JLabel("Scan the QR code or copy the code displayed above and type in the current code");
		JTextField tCode = new JTextField();
		URL qrCodeUrl = new URL(TimeBasedOneTimePasswordUtil.qrImageUrl(keyId, base32Secret));
		ImageIcon qr = new ImageIcon(qrCodeUrl);
		
		JLabel lQrCode = new JLabel(base32Secret);
		lQrCode.setFont((new Font("Roboto", Font.PLAIN, 25)));
		lQrCode.setHorizontalAlignment(JLabel.CENTER);
		Object[] options = {qr, lQrCode, lInfo, tCode};
		int result = JOptionPane.showConfirmDialog(null, options, "Cryptox", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(base32Secret);
			if(code.equals(tCode.getText())) {
				// Save 2FA code
				Password p = new Password("2FA Base32Secret", base32Secret, false, true);
				this.passwords.add(p);
				this.storeAllPasswords();
				this.b2FA.setText("Delete 2FA");
				Thread hilo = new Thread() {
					
					@Override
					public void run() {
						lInfo.setText("Two-Factor Authentication (2FA) added");
						try {
							sleep(4000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						lInfo.setText("");
					}
				};
				hilo.start();
			}
			else {
				// Display password mismatch
				JOptionPane.showInternalMessageDialog(null, "Incorrect Two-Factor Authentication code, make sure you type in the latest code before it changes", "2FA Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	// Show Base32 secret and QR code for configuring 2FA in a new authenticator
	public void show2FA() throws IOException {
		String base32Secret = get2FA().getPassword();
		
		String keyId = "Cryptox%202.0";
		JLabel lInfo = new JLabel("Scan the QR code or copy the code displayed above");
		URL qrCodeUrl = new URL(TimeBasedOneTimePasswordUtil.qrImageUrl(keyId, base32Secret));
		ImageIcon qr = new ImageIcon(qrCodeUrl);
		
		JLabel lQrCode = new JLabel(base32Secret);
		lQrCode.setFont((new Font("Roboto", Font.PLAIN, 25)));
		lQrCode.setHorizontalAlignment(JLabel.CENTER);
		Object[] options = {qr, lQrCode, lInfo};
		JOptionPane.showMessageDialog(null, options, "Cryptox - Showing 2FA", JOptionPane.PLAIN_MESSAGE);
	}
	
	// Load all the passwords from the encrypted file
	@SuppressWarnings("unchecked")
	public void loadStoredPasswords() throws ClassNotFoundException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException { 
		File data = new File(".data/data.cryptox");

        FileInputStream fis = new FileInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(fis);
        
        byte[] readData = (byte[]) ois.readObject();
        
        byte[] decryptedData = null;
		decryptedData = cryptox.getAESDecrypt(readData);
        
        //    	Deserialize
    	ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
    	ObjectInputStream oisb = new ObjectInputStream(bais);
    	ArrayList<Password> passwords = (ArrayList<Password>) oisb.readObject();

    	oisb.close();
    	bais.close();
        
        ois.close();
        fis.close();
	    
	    this.passwords = passwords;
	}
	
	// Store all the passwords that are inside the ArrayList of passwords in the encrypted file
	public void storeAllPasswords() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		File data = new File(".data/data.cryptox");
		FileOutputStream fos = new FileOutputStream(data);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        
        //	Serialize the ArrayList
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oosb = new ObjectOutputStream(baos);
		oosb.writeObject(this.passwords);
		oos.flush();

		byte[] serializedData = baos.toByteArray();
		byte[] encryptedData = cryptox.getAES(serializedData);

        // Write the updated list of passwords to the file
        oos.writeObject(encryptedData);

        // Close the output stream
        oosb.close();
		baos.close();
        oos.close();
        fos.close();
	}
	
	// Check if there is a Master Key in the stored passwords
	public boolean hasMasterKey() {
		for(Password p : this.passwords) {
			if(p.isMasterKey()) {
				return true;
			}
		}
		return false;
	}
	
	// Get Cryptox's Master Key from the stored passwords
	public Password getMasterKey() {
		for(Password p : this.passwords) {
			if(p.isMasterKey()) {
				return p;
			}
		}
		return null;
	}
	
	// Check if there is a base32 secret password for 2FA in the stored passwords
	public boolean has2FA() {
		for(Password p : this.passwords) {
			if(p.isBase32Secret()) {
				return true;
			}
		}
		return false;
	}
	
	// Get the base32 secret password for 2FA from the stored passwords
	public Password get2FA() {
		for(Password p : this.passwords) {
			if(p.isBase32Secret()) {
				return p;
			}
		}
		return null;
	}
	
	// Create an instance of Cryptox with the masterkey
	public void loadCryptox(String masterKey) {
		this.cryptox = new Cryptox(masterKey);
	}
	
	// Copy the string passed as a parameter to clipboard
	public void copyToClipboard(String s) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		StringSelection strSel = new StringSelection(s);
		clipboard.setContents(strSel, null);
	}
	
	//	Hide the password field and show a textfield to display the password for 2 seconds
	public void showHide() {
		Thread hilo = new Thread() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				tPassword.setText(tPasswordHide.getText());
				tPassword.setVisible(true);
				tPasswordHide.setVisible(false);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {	}
				tPassword.setVisible(false);
				tPasswordHide.setVisible(true);
			}
		};
		hilo.start();
	}
	
	public void changeMasterKey(String newKey) {
		//	Create new instance of Cryptox with new key
		this.cryptox = new Cryptox(newKey);
	}
	
	// Load the data and show the window
	public void showWindow() throws ClassNotFoundException, IOException {
		
		// Load all stored passwords
		try {
			this.loadStoredPasswords();
		} catch (InvalidKeyException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
				| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("All passwords were retrieved succesfully (" + this.passwords.size() + ")");
		
		// Make combobox display decrypted passwords
		this.loadCombobox();
		
		// Write text of 2FA button
		if(this.has2FA()) {
			b2FA.setText("Delete 2FA");
		} else {
			b2FA.setText("Set 2FA");
		}
		
		// Set visibility of bShow2FA button
		if(this.has2FA()) {
			bShow2FA.setVisible(true);
		} else {
			bShow2FA.setVisible(false);
		}
		
		//	Make frame visible
		this.setVisible(true);
	}
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		//	Initialize the frame, masterkey and two-factor authentication
		CryptoxGUI gui = new CryptoxGUI();
		String masterKey = null;
		String twoFA = null;
		
		// Check if data file exists
		if(!gui.data.exists()) {	// If it doesn't: create a new Master Key
			JLabel lInfo = new JLabel("Create a Master Key");
			JLabel lWarning = new JLabel("WARNING: Store this key in a safe place, if not you could lose access to your Cryptox keychain!");
			JPanel pPassword = new JPanel(new FlowLayout());
			JPasswordField tPass = new JPasswordField(30);
			pPassword.add(tPass);
			JLabel lShowPassword = new JLabel("");
			lShowPassword.setHorizontalAlignment(JLabel.CENTER);
			JButton bShow = new JButton("Show");
			pPassword.add(bShow);
			
			bShow.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Thread hilo = new Thread() {
						
						@Override
						public void run() {
							lShowPassword.setText("Password: " + tPass.getText());
							try {
								sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							lShowPassword.setText("");
						}
					};
					hilo.start();
				}
			});
			
			Object[] options = {lInfo, lWarning, pPassword, lShowPassword};
			// Ask user for new Master Key
			
			while(true) {
				int result = JOptionPane.showConfirmDialog(null, options, "Cryptox", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				
				if(result == JOptionPane.OK_OPTION) {	// If OK is pressed: check that the password field is not empty
					if(!tPass.getText().isBlank()) {	// If it isn't empty: create a Password of type Master Key and store it
						masterKey = tPass.getText();
						gui.cryptox = new Cryptox(masterKey);
						Password p = new Password("Cryptox Master Key", tPass.getText(), true, false);
						gui.passwords.add(p);
						try {
							gui.storeAllPasswords();
						} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
								| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
								| IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							gui.showWindow();
						} catch (ClassNotFoundException | IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						break;	// Exit loop
					} else {	// If it is empty: inform the user that it cannot be empty and reask
						JOptionPane.showInternalMessageDialog(null, "Master Key cannot be empty", "Empty Master Key", JOptionPane.ERROR_MESSAGE);
					}
				} else if(masterKey == null) {	// If CANCEL is pressed: stop the execution
					System.exit(0);
				}
			}
			
		} else {	// If it does exist: ask user for the Master Key
			// Ask user for Master Key
			JLabel lInfo = new JLabel("Type in Master Key");
			JPasswordField tPass = new JPasswordField();
			Object[] options = {lInfo, tPass};
			while(true) {
				int result = JOptionPane.showConfirmDialog(null, options, "Cryptox", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if(result == JOptionPane.OK_OPTION) {	// If OK is pressed: check Master Key is valid
					masterKey = tPass.getText();
					gui.cryptox = new Cryptox(masterKey);	// Initialize Cryptox
					
					// Load all passwords to check if Master Key is correct
					
					boolean incorrectMasterKey = false;
					try {
						gui.loadStoredPasswords();
					} catch (InvalidKeyException | ClassNotFoundException | NoSuchAlgorithmException
							| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
							| BadPaddingException | IOException e1) {
						incorrectMasterKey = true;
						
						// Show 2FA input panel to trick intruders
						lInfo = new JLabel("Type in Two-Factor Authentication (2FA) code");
						JTextField tCode = new JTextField();
						Object[] options2 = {lInfo, tCode};
						
						result = JOptionPane.showConfirmDialog(null, options2, "Cryptox", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
						JOptionPane.showInternalMessageDialog(null, "Incorrect credentials", "Login Error", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}
					
					if(!incorrectMasterKey) {
						Password p = gui.getMasterKey();
						String readMasterKey = p.getPassword();
						
						if(masterKey.equals(readMasterKey) && !masterKey.isBlank()) {	// Check if Master Key is valid
							if(gui.has2FA()) {	// If 2FA is set up: ask user for lastest code
								lInfo = new JLabel("Type in Two-Factor Authentication (2FA) code");
								JTextField tCode = new JTextField();
								Object[] options2 = {lInfo, tCode};
								
								result = JOptionPane.showConfirmDialog(null, options2, "Cryptox", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
								if (result == JOptionPane.OK_OPTION) {	// If OK pressed: check 2FA code is valid
									twoFA = tCode.getText();
									String code = null;
									try {
										code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(gui.get2FA().getPassword());
									} catch (GeneralSecurityException e) {
										e.printStackTrace();
									}
									if(twoFA.equals(code) && !twoFA.isBlank()) {	//	Check if 2FA is valid
										try {
											gui.showWindow();
										} catch (ClassNotFoundException | IOException e) {
											e.printStackTrace();
										}
									} else {
										// 2FA code is not valid
										JOptionPane.showInternalMessageDialog(null, "Incorrect credentials", "Login Error", JOptionPane.ERROR_MESSAGE);
										System.exit(0);
									}
								} else if(twoFA == null) { // If CANCEL pressed: stop execution
									System.exit(0);
								}
							} else {
								try {
									gui.showWindow();
								} catch (ClassNotFoundException | IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							break;	// Stop loop;
						}
					}
				} else {	// If CANCEL is pressed: stop the execution
					System.exit(0);
				}
			}
		}
	}
}