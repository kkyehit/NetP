package Login;

import javax.swing.*;

import client_connect.Client;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.UnknownHostException;

public class RegisterFrame extends JFrame {

	private static final int FRAME_WIDTH = 400;
	private static final int FRAME_HEIGHT = 250;
	JTextField idField;
	JTextField passwordField;
	JTextField nameField;
	private Client user;
		
	public RegisterFrame(Client user) {
		createComponents();
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		
	}
	
	// register 창 닫기
	public void closeFrame() {
		this.dispose();
	}

	// 항목 추가
	private void createComponents() {
		
		setTitle("SSUTUBE_Register");
		
		// 사용자가 등록시 입력할 ID, PW, Name
		idField = new JTextField(10);
		passwordField = new JTextField(10);
		nameField = new JTextField(10);
		
		// 입력글자 제한 (id, pw = 10개 name = 8개)
		idField.setDocument((new JTextFieldLimit(10)));
		passwordField.setDocument((new JTextFieldLimit(10)));
		nameField.setDocument((new JTextFieldLimit(8)));

		// textfield 이름 지정
		JLabel idLabel = new JLabel("ID");
		JLabel passwordLabel = new JLabel("Password");
		JLabel nameLabel = new JLabel("name");

		// 패널 추가
		JPanel registerPanel = new JPanel();
		registerPanel.setLayout(new BorderLayout()); // BorderLayout 으로 변경

		// submit, cancel 버튼
		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");

		// Button Area 패널에 submit, cancel 버튼 추가
		JPanel buttonArea = new JPanel(new GridLayout(1,2, 30, 10));
		buttonArea.add(submitButton);
		buttonArea.add(cancelButton);
		buttonArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// Input Area 패널에 id, pw, name Label 및 Field 추가 
		JPanel inputArea = new JPanel(new GridLayout(3,2, 30, 20));
		inputArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// id : __ 
		// password: __ 
		// name:__ 식으로 화면에 표현
		inputArea.add(idLabel);
		inputArea.add(idField);
		inputArea.add(passwordLabel);
		inputArea.add(passwordField);
		inputArea.add(nameLabel);
		inputArea.add(nameField);
		
		// button, input area를 registerPanel에 붙이기
		// button은 South, input은 Center
		registerPanel.add(buttonArea, "South");
		registerPanel.add(inputArea, "Center");
		
		// Panel을 JFrame에 붙임
		add(registerPanel);

		ActionListener submitListener = new SubmitListener();
		ActionListener cancelListener = new CancelListener();
		submitButton.addActionListener(submitListener);
		cancelButton.addActionListener(cancelListener);
	
	}

	public class SubmitListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			// 입력받은 id, pw, name 저장
			String userNameInput = idField.getText();
			String passwordInput = passwordField.getText();
			String nameInput = nameField.getText();

			// debug
			System.out.println("id :" + userNameInput);
			System.out.println("pw :" + passwordInput);
			System.out.println("name :" + nameInput);
			
			try {
				// user 객체 생성
				user = new Client(userNameInput, passwordInput, nameInput);
				
				// 컨트롤 서버와 연결
				user.connectControlServer();
				
				// register 성공시
				if (user.sendRegisterMsg()) {
					
					// register 성공 메시지 띄움
					JOptionPane.showMessageDialog(null, "Register successed. Please Login!");

					//register 페이지 닫기
					closeFrame();
					
				}
				else {
					// register 실패 메시지 띄움
					JOptionPane.showMessageDialog(null, "Register Failed");
					
					// idField, pwField, nameField 비우기
					idField.setText("");
					passwordField.setText("");
					nameField.setText("");
					
				}
				
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

	// Exit 버튼 눌렸을 때.
	public class CancelListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			dispose();
		}
	}
	
}

