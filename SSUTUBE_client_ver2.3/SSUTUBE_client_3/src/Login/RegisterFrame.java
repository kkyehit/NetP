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
	
	// register â �ݱ�
	public void closeFrame() {
		this.dispose();
	}

	// �׸� �߰�
	private void createComponents() {
		
		setTitle("SSUTUBE_Register");
		
		// ����ڰ� ��Ͻ� �Է��� ID, PW, Name
		idField = new JTextField(10);
		passwordField = new JTextField(10);
		nameField = new JTextField(10);
		
		// �Է±��� ���� (id, pw = 10�� name = 8��)
		idField.setDocument((new JTextFieldLimit(10)));
		passwordField.setDocument((new JTextFieldLimit(10)));
		nameField.setDocument((new JTextFieldLimit(8)));

		// textfield �̸� ����
		JLabel idLabel = new JLabel("ID");
		JLabel passwordLabel = new JLabel("Password");
		JLabel nameLabel = new JLabel("name");

		// �г� �߰�
		JPanel registerPanel = new JPanel();
		registerPanel.setLayout(new BorderLayout()); // BorderLayout ���� ����

		// submit, cancel ��ư
		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");

		// Button Area �гο� submit, cancel ��ư �߰�
		JPanel buttonArea = new JPanel(new GridLayout(1,2, 30, 10));
		buttonArea.add(submitButton);
		buttonArea.add(cancelButton);
		buttonArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// Input Area �гο� id, pw, name Label �� Field �߰� 
		JPanel inputArea = new JPanel(new GridLayout(3,2, 30, 20));
		inputArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// id : __ 
		// password: __ 
		// name:__ ������ ȭ�鿡 ǥ��
		inputArea.add(idLabel);
		inputArea.add(idField);
		inputArea.add(passwordLabel);
		inputArea.add(passwordField);
		inputArea.add(nameLabel);
		inputArea.add(nameField);
		
		// button, input area�� registerPanel�� ���̱�
		// button�� South, input�� Center
		registerPanel.add(buttonArea, "South");
		registerPanel.add(inputArea, "Center");
		
		// Panel�� JFrame�� ����
		add(registerPanel);

		ActionListener submitListener = new SubmitListener();
		ActionListener cancelListener = new CancelListener();
		submitButton.addActionListener(submitListener);
		cancelButton.addActionListener(cancelListener);
	
	}

	public class SubmitListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			// �Է¹��� id, pw, name ����
			String userNameInput = idField.getText();
			String passwordInput = passwordField.getText();
			String nameInput = nameField.getText();

			// debug
			System.out.println("id :" + userNameInput);
			System.out.println("pw :" + passwordInput);
			System.out.println("name :" + nameInput);
			
			try {
				// user ��ü ����
				user = new Client(userNameInput, passwordInput, nameInput);
				
				// ��Ʈ�� ������ ����
				user.connectControlServer();
				
				// register ������
				if (user.sendRegisterMsg()) {
					
					// register ���� �޽��� ���
					JOptionPane.showMessageDialog(null, "Register successed. Please Login!");

					//register ������ �ݱ�
					closeFrame();
					
				}
				else {
					// register ���� �޽��� ���
					JOptionPane.showMessageDialog(null, "Register Failed");
					
					// idField, pwField, nameField ����
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

	// Exit ��ư ������ ��.
	public class CancelListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			dispose();
		}
	}
	
}

