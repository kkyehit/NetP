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
	
	// Login ȭ�� ����
	public LoginFrame(Client user, RegisterFrame regFrame, VlcPlayer vlc) {
		createComponents();
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		
		// user �� ���� ����
		this.user = user;
		
		// regFrame ���� ������ ����
		this.regFrame = regFrame;
		
		// vlc ���� ������ ����
		this.vlc = vlc;
	}
	
	// Login â �ݱ�
	public void closeFrame() {
		this.dispose();
	}

	//  �׸� �߰� 
	private void createComponents() {
		
		// ����ڰ� �Է��� ID, PW
		idField = new JTextField(10);
		passwordField = new JPasswordField(10);
		
		// �Է±��� 10���� ����
		idField.setDocument((new JTextFieldLimit(10)));
		passwordField.setDocument((new JTextFieldLimit(10)));
		
		// image icon ��ü ����
		ImageIcon raw_ssutube_icon = new ImageIcon("img/ssu_tube_small.png");
		
		// image ������¡
		Image origin_ssutube_img = raw_ssutube_icon.getImage();
		Image resize_ssutube_img = origin_ssutube_img.getScaledInstance(400,100, Image.SCALE_DEFAULT);
		ImageIcon modified_ssutube_icon = new ImageIcon(resize_ssutube_img);
		JLabel ssutube_icon = new JLabel(modified_ssutube_icon);
		
		// textfield �̸� ���� 
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
		// �гο� ������ �߰� 
		panel.add(ssutube_icon);
		
		// id : __ password: __ ������ ȭ�鿡 ǥ��
		panel.add(idLabel);
		panel.add(idField);
		
		panel.add(passwordLabel);
		panel.add(passwordField);

		// �гο� �α���, ���, ���� ��ư �߰�
		panel.add(buttonArea);
		add(panel);

		// ActionListener ��� 
		ActionListener exitListener = new ExitListener();
		ActionListener registerListener = new RegisterListener();
		ActionListener loginListener = new LoginListener();
		exitButton.addActionListener(exitListener);
		registerButton.addActionListener(registerListener);
		loginButton.addActionListener(loginListener);
	}

	// Exit ��ư ������ ��.
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
			
			// register page ���� 
			regFrame.setVisible(true);
			regFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			regFrame.setLocationRelativeTo(null);
			//ũ�� ���� �Ұ�
			regFrame.setResizable(false);
			
		}
	}

	public class LoginListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {

			// �Է¹��� id, pw ����
			String userIdInput = idField.getText();
			char rawPasswordInput[] = passwordField.getPassword();
			String passwordInput = String.valueOf(rawPasswordInput);
			
			// Ŭ���̾�Ʈ ����
			user = new Client(userIdInput, passwordInput);
			
			try {
				
				// �α��� ������ ������ �ݺ�
				while (true) {
					// ��Ʈ�� ������ ���� (�翬�� ����)
					user.connectControlServer();
					
					// login ������
					if (user.sendLoginMsg()) {
						
						//login ������ �ݱ�
						closeFrame();
						
						break;
					}
					
					else {
						// login ���� �޽��� ���
						JOptionPane.showMessageDialog(null, "Login Failed");
						
						// idField, pwField ����
						idField.setText("");
						passwordField.setText("");
						
						// ��Ʈ�� ������ ���� ���� 
						user.disconnectControlServer();			
					}
				}

				user.connectControlServer();
				// player ȭ�鿡 ��� �� LIST���� ȹ��
				user.sendListReqMsg();
				user.disconnectControlServer();	
				
				// �α��� ���� �Ŀ��� player ȭ�� ���
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