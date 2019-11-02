package Media;

import javax.swing.*;

import vlcj.AVPanel;
import client_connect.*;

import java.awt.*;
import java.awt.event.*;

import Login.JTextFieldLimit;

public class MediaFrame extends JFrame {

	private static final int FRAME_WIDTH = 1400;
	private static final int FRAME_HEIGHT = 800;

	private static final int VIDEO_WIDTH = 1280;
	private static final int VIDEO_HEIGHT = 720;

	private static final int GREETING_HEIGHT = 20;

	// private vlc vlcPlayer;
	private PlatListPane playList;

	private Client user;
	private JFileChooser uploader;

	public MediaFrame(String username, Client user) {

		createComponents(username);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		this.user = user;
	}

	private void createComponents(String name) {

		setTitle("SSUTUBE");

		// media play 패널 추가
		JPanel mediaPlayPanel = new JPanel();
		mediaPlayPanel.setLayout(new BorderLayout()); // BorderLayout 으로 변경

		// user greetings
		JLabel userGreetings = new JLabel("Welcome, " + name + "!");
		userGreetings.setFont(new Font("Helvetica", Font.BOLD, 14));

		// Greeting Area 패널에 userGreetings 레이블 추가
		JPanel greetingArea = new JPanel(new BorderLayout());
		greetingArea.setSize(VIDEO_WIDTH, GREETING_HEIGHT);
		greetingArea.add(userGreetings, "Center");

		// play, stop, upload button
		JButton playButton = new JButton("play");
		JButton stopButton = new JButton("stop");
		JButton uploadButton = new JButton("upload");

		// Button Area 패널에 submit, cancel 버튼 추가
		JPanel buttonArea = new JPanel(new GridLayout(1, 3, 50, 20));
		buttonArea.add(playButton);
		buttonArea.add(stopButton);
		buttonArea.add(uploadButton);
		buttonArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// playList 추가
		playList = new PlatListPane(name);

		// greetingArea는 North, vlcplayer는 Center,
		// buttonArea는 South, playList는 West에 추가
		mediaPlayPanel.add(greetingArea, "North");
		// mediaPlayPanel.add(vlcPlayer, "Center");
		mediaPlayPanel.add(buttonArea, "South");
		mediaPlayPanel.add(playList, "East");

		// media play 패널을 JFrame에 붙임
		add(mediaPlayPanel);

		ActionListener playListener = new PlayListener();
		ActionListener stopListener = new StopListener();
		ActionListener uploadListener = new UploadListener();

		playButton.addActionListener(playListener);
		stopButton.addActionListener(stopListener);
		uploadButton.addActionListener(uploadListener);
		
	}

	// play 버튼 눌렀을 때
	public class PlayListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			//
			System.out.println("Play");
			// vlcPlayer.play("downloaded/BTS_Performs_Boys_With_Luv.mp4");
		}
	}

	// stop 버튼 눌렸을 때.
	public class StopListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			
			System.out.println("Stop");
		}
	}

	// Upload 버튼 눌렸을 때.
	public class UploadListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			
			uploader.showOpenDialog(null);
			
			
			System.out.println("Upload");
		}
	}
}
