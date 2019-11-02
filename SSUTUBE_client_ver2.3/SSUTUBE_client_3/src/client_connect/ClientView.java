package client_connect;

import javax.swing.JFrame;

import protocol.MediaProtocol;
import vlcj.*;
import Login.*;

public class ClientView {
	private Client user;
	
	// 로그인 form
	private LoginFrame login;
	
	// 등록 form
	private RegisterFrame register;
	
	// 재생 list form
	private VlcPlayer vlc;
	
	// 재생 list 제공 form
	
	
	public ClientView() {
		// 등록 화면 생성
		register = new RegisterFrame(user);
		
		// 로그인 화면 생성 및 보이기
		login = new LoginFrame(user, register, vlc);
		login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		login.setVisible(true);
		login.setLocationRelativeTo(null);
		// 크기 수정 불가 
		login.setResizable(false);
		
		//System.out.println(MediaProtocol.UPLOAD_INFO_REQ.)
		
//		vlc = new VlcPlayer(user);
//		vlc.setVisible(true);
	}
	
	public static void main(String[] args) {
		ClientView Client = new ClientView(); 
	}
}
