package Login;

import javax.swing.*;

import vlcj.VlcPlayer;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Scanner;

import client_connect.*;
import Media.*;

public class LoginFrame extends JFrame {
	private static final int FRAME_WIDTH = 400;
	private static final int FRAME_HEIGHT = 250;
	static JTextField idField;
	static JPasswordField passwordField;
	private Client user;
	private RegisterFrame regFrame;
	private VlcPlayer vlc;
	
	// Login 화면 띄우기
	public LoginFrame(Client user, RegisterFrame regFrame, VlcPlayer vlc) {
		createComponents();
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		
		// user 값 공유 위해
		this.user = user;
		
		// regFrame 상태 공유를 위해
		this.regFrame = regFrame;
		
		// vlc 상태 공유를 위해
		this.vlc = vlc;
	}
	
	// Login 창 닫기
	public void closeFrame() {
		this.dispose();
	}

	//  항목 추가 
	private void createComponents() {
		
		// 사용자가 입력할 ID, PW
		idField = new JTextField(10);
		passwordField = new JPasswordField(10);
		
		// 입력글자 10개로 제한
		idField.setDocument((new JTextFieldLimit(10)));
		passwordField.setDocument((new JTextFieldLimit(10)));
		
		// image icon 객체 생성
		ImageIcon raw_ssutube_icon = new ImageIcon("img/ssu_tube_small.png");
		
		// image 리사이징
		Image origin_ssutube_img = raw_ssutube_icon.getImage();
		Image resize_ssutube_img = origin_ssutube_img.getScaledInstance(400,100, Image.SCALE_DEFAULT);
		ImageIcon modified_ssutube_icon = new ImageIcon(resize_ssutube_img);
		JLabel ssutube_icon = new JLabel(modified_ssutube_icon);
		
		// textfield 이름 지정 
		JLabel idLabel = new JLabel("ID");
		JLabel passwordLabel = new JLabel("Password");

		// login, register, exit button
		JButton loginButton = new JButton("Login");
		JButton registerButton = new JButton("Register");
		JButton exitButton = new JButton("Exit");

		// first glue
		JPanel buttonArea = new JPanel(new GridLayout(1,3, 30, 0));
		buttonArea.add(loginButton);
		buttonArea.add(registerButton);
		buttonArea.add(exitButton);
		
		JPanel panel = new JPanel();
//		panel.setLayout(new BorderLayout());
		// 패널에 아이콘 추가 
		panel.add(ssutube_icon);
		
		// id : __ password: __ 식으로 화면에 표현
		panel.add(idLabel);
		panel.add(idField);
		
		panel.add(passwordLabel);
		panel.add(passwordField);

		// 패널에 로그인, 등록, 종료 버튼 추가
		panel.add(buttonArea);
		add(panel);

		// ActionListener 등록 
		ActionListener exitListener = new ExitListener();
		ActionListener registerListener = new RegisterListener();
		ActionListener loginListener = new LoginListener();
		exitButton.addActionListener(exitListener);
		registerButton.addActionListener(registerListener);
		loginButton.addActionListener(loginListener);
	}

	// Exit 버튼 눌렸을 때.
	public class ExitListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			System.exit(0);
		}
	}

	public class RegisterListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			// String userNameInput = userNameField.getText();
			// String passwordInput = passwordField.getText();
			//
			// System.out.println(userNameInput);
			// System.out.println(passwordInput);
			
			// register page 띄우기 
			regFrame.setVisible(true);
			regFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			regFrame.setLocationRelativeTo(null);
			//크기 수정 불가
			regFrame.setResizable(false);
			
		}
	}

	public class LoginListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {

			// 입력받은 id, pw 저장
			String userIdInput = idField.getText();
			char rawPasswordInput[] = passwordField.getPassword();
			String passwordInput = String.valueOf(rawPasswordInput);
			
			// 클라이언트 생성
			user = new Client(userIdInput, passwordInput);
			
			try {
				
				// 로그인 성공할 떄까지 반복
				while (true) {
					// 컨트롤 서버와 연결 (재연결 포함)
					user.connectControlServer();
					
					// login 성공시
					if (user.sendLoginMsg()) {
						
						//login 페이지 닫기
						closeFrame();
						
						break;
					}
					
					else {
						// login 실패 메시지 띄움
						JOptionPane.showMessageDialog(null, "Login Failed");
						
						// idField, pwField 비우기
						idField.setText("");
						passwordField.setText("");
						
						// 컨트롤 서버와 연결 해제 
						user.disconnectControlServer();			
					}
				}

				user.connectControlServer();
				// player 화면에 들어 갈 LIST정보 획득
				user.sendListReqMsg();
				user.disconnectControlServer();	
				
				// 로그인 성공 후에는 player 화면 띄움
				vlc = new VlcPlayer(user);
				vlc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				vlc.setVisible(true);
				vlc.setLocationRelativeTo(null);
				
			}catch (UnknownHostException e) {
				// TODO Auto-generated catch block
	            e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
	            e.printStackTrace();
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
	}
}