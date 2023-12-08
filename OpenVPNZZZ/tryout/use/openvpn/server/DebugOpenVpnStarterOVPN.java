package use.openvpn.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DebugOpenVpnStarterOVPN {

	public static void main(String[] args) {
		DebugOpenVpnStarterOVPN objStart = new DebugOpenVpnStarterOVPN();
		objStart.startServerProcessByProcessBuilder();
		
	}
	
	public boolean startServerProcessByProcessBuilder() {
		boolean bReturn= false;
		main:{
			Process objProcess =this.getProcess();
			if(objProcess==null) {
				bReturn = false;
				break main;
			}else {
				objProcess.destroy();
			}
			bReturn = true;
		}//end main:
		return bReturn;
	}
	
	public Process getProcess() {
		Process objReturn = null;
		main:{
			 try {
				//###########################################
				//### Suche nach der Alternative diese OVPN Konfiguration per Batch zu starten.
				//### Mit ProcessBuilder-Klasse
				//###########################################
				 
				//A) Starte den Server wie den Client....
				//Ohne Verwendung des LOG
			    //ProcessBuilder pb = new ProcessBuilder("C:\\Programme\\OpenVPN\\bin\\openvpn.exe", "--config", "C:\\Programme\\OpenVPN\\config\\server_TCP_4999.ovpn");				 
					
				//Leider gibt das ueberhaupt kein Log zurueck.
				//ProcessBuilder pb = new ProcessBuilder("C:\\Programme\\OpenVPN\\bin\\openvpn.exe", "--log", "c:\\fglkernel\\kernellog\\ovpn.log", "--config", "C:\\Programme\\OpenVPN\\config\\server_TCP_4999.ovpn");
								 
							
				//B) mit GUI
				//falsch alles in einem String:                  ProcessBuilder pb = new ProcessBuilder("C:\\Programme\\OpenVPN\\bin\\openvpn-gui.exe C:\\Programme\\OpenVPN\\config\\server_TCP_4999.ovpn");				 				 
				//richtig, jedes Argument in einem extra String: ProcessBuilder pb = new ProcessBuilder("C:\\Programme\\OpenVPN\\bin\\openvpn-gui.exe","--connect", "C:\\Programme\\OpenVPN\\config\\server_TCP_4999.ovpn");
				 
				//mit Angabe des Log Files
				//ist aber nur mit anderem log_dir Parameter möglich
				//ProcessBuilder pb = new ProcessBuilder("C:\\Programme\\OpenVPN\\bin\\openvpn-gui.exe","--connect", "C:\\Programme\\OpenVPN\\config\\server_TCP_4999.ovpn", "--log", "c:\\fglkernel\\kernellog\\ovpn.log");
				//ProcessBuilder pb = new ProcessBuilder("C:\\Programme\\OpenVPN\\bin\\openvpn-gui.exe","--connect", "C:\\Programme\\OpenVPN\\config\\server_TCP_4999.ovpn", "--help");
				
				//Merke: Die Log Datei muss immer weggegraumt werden sonst kann er sie nicht neu erstellen und liefert Fehler.
				//ProcessBuilder pb = new ProcessBuilder("C:\\Programme\\OpenVPN\\bin\\openvpn-gui.exe","--connect", "C:\\Programme\\OpenVPN\\config\\server_TCP_4999.ovpn", "--log_dir", "c:\\fglkernel\\kernellog\\ovpn.log");
			    
				//Merke: Das Konfigurationsverzeichnis muss hier auch wieder anders angegeben werden, ohne die angabe sucht er in einen auto-config Verzeichnis, oder so...
				ProcessBuilder pb = new ProcessBuilder("C:\\Programme\\OpenVPN\\bin\\openvpn-gui.exe","--config_dir", "C:\\Programme\\OpenVPN\\config", "--connect", "server_TCP_4999.ovpn", "--log_dir", "c:\\fglkernel\\kernellog\\ovpn.log"); 
				 
				//Das gibt ueberhaupt keine Zeile mehr aus... 
				//objReturn = pb.inheritIO().start();
				objReturn = pb.start();
				 
			     BufferedReader error = new BufferedReader(new InputStreamReader(objReturn.getErrorStream()));
				 String errorString = error.readLine();
				 while(error.readLine()!=null) {
					 System.out.println("Error: " + errorString);
					 errorString = error.readLine();
				 }
				 
				 
				 BufferedReader standard = new BufferedReader(new InputStreamReader(objReturn.getInputStream()));
				 String outputString = standard.readLine();
				 
				 while(standard.readLine()!=null) {
					 System.out.println("Standard: " + outputString);
					 outputString = standard.readLine();
				 }
				 
				 //### Weitere Entwicklung
				 //Mache einen Thread, der das Log beobachtet
				 //LogFileWatchRunnerZZZ
				 
				 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//end main:
		return objReturn;
	}

}
