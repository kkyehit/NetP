package client_connect;

import javax.swing.JFrame;

import protocol.MediaProtocol;
import vlcj.*;
import Login.*;

public class ClientView {
	private Client user;
	
	// �α��� form
	private LoginFrame login;
	
	// ��� form
	private RegisterFrame register;
	
	// ��� list form
	private VlcPlayer vlc;
	
	// ��� list ���� form
	
	
	public ClientView() {
		// ��� ȭ�� ����
		register = new RegisterFrame(user);
		
		// �α��� ȭ�� ���� �� ���̱�
		login = new LoginFrame(user, register, vlc);
		login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		login.setVisible(true);
		login.setLocationRelativeTo(null);
		// ũ�� ���� �Ұ� 
		login.setResizable(false);
		
		//System.out.println(MediaProtocol.UPLOAD_INFO_REQ.)
		
//		vlc = new VlcPlayer(user);
//		vlc.setVisible(true);
	}
	
	public static void main(String[] args) {
		ClientView Client = new ClientView(); 
	}
}
