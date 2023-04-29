package use.openvpn.serverui.component.FTPCredentials;

public interface IConstantProgramFTPCredentialsOVPN {	
	public static String sINI_SECTION_ALIASNAME = "ProgFTPCredentials";
	
	public static String sLABEL_TEXTFIELD_USERNAME = "Benutzername:";
	public static String sCOMPONENT_TEXTFIELD_USERNAME = "textFTPUsername";
	public static String sVALUE_TEXTFIELD_USERNAME_INITIAL = "Enter username";
	
	public static String sLABEL_TEXTFIELD_PASSWORD_DECRYPTED = "Kennwort:";
	public static String sCOMPONENT_TEXTFIELD_PASSWORD_DECRYPTED = "textFTPPassword";
	public static String sVALUE_TEXTFIELD_PASSWORD_DECRYPTED_INITIAL = "Enter password";
	
	public static String sLABEL_LABEL_PASSWORD_ENCRYPTED = "Verschluesselt:";
	public static String sCOMPONENT_LABEL_PASSWORD_ENCRYPTED = "labelFTPPassword";
	public static String sVALUE_LABEL_PASSWORD_ENCRYPTED_INITIAL = "";
	
	public static String sLABEL_BUTTON_TO_INI = "to ini";
	
	public static String sLABEL_LABEL_MESSAGE = "";
	public static String sCOMPONENT_LABEL_MESSAGE = "textCredentialsLocal";
	public static String sVALUE_LABEL_MESSAGE_INITIAL = "xxxxxxx";
	
	public static String sINI_PROPERTY_USERNAME = "FTPUsername";
	public static String sINI_PROPERTY_PASSWORD = "FTPPassword";
	public static String sINI_PROPERTY_CREDENTIALDATE = "CredentialDate";
	public static String sINI_PROPERTY_CREDENTIALTIME = "CredentialTime";
	
	
}
