# **Cryptox**
**A simple local password key chain with AES encryption in Java**

### Main features
- **Store** new passwords
- **Edit** stored passwords
- **Delete** passwords
- **Copy** stored passwords **to clypboard**
- **Generate secure passwords** (up to 100 characters long with special characters and numbers)
- **Delete all data**
- Secure the passwords using **AES encryption**
- Secure access to Cryptox key chain using a **Master Key**
- Additional security layer using **2-FA** (Two-Factor Authentication)

### Additional notes
* Data is stored in directory *'Cryptox/.data'* inside a file named 'data.cryptox' *(these do not exist by default)*
* Data directory is meant to be hidden
* Directory *'Cryptox/resources'* contains graphics for the application *(do not make any changes to it)*

### What's new? - *update 2.0.1*
- If 2-FA is set up, the user will be asked for the 6 digits code even if the Master Key is wrong.
- 2-FA can now be set up using the base 32 secret code instead of scanning a QR code.
- Master Key can no longer be copied to clipboard, it is only editable.
