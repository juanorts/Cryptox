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
- Additional security layer using **2-FA** (Two-Factor Authentication): *can be configured using QR code or Base32 secret.*

### Additional notes
* Data is stored in directory *'Cryptox/.data'* inside a file named 'data.cryptox' *(these do not exist by default)*
* Data directory is meant to be hidden
* Directory *'Cryptox/resources'* contains graphics for the application *(do not make any changes to it)*
