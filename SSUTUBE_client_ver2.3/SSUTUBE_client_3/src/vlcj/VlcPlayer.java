package vlcj;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import client_connect.Client;
import client_connect.Connect;
import client_connect.Convert;
import sun.management.snmp.util.MibLogger;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

import org.apache.commons.io.*; // fileFilter ��

import Media.PlatListPane;

public class VlcPlayer extends JFrame {

	boolean check = false;
	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
	private Client user;
	private JPanel contentPane;
	private JPanel controlsPane;
	private JFileChooser uploader;
	static Connect con;

	// public static void main(final String[] args) {
	// new NativeDiscovery().discover();
	// SwingUtilities.invokeLater(new Runnable() {
	// @Override
	// public void run() {
	// new VlcPlayer();
	// }
	// });
	// }

	public VlcPlayer(Client user) {

		new NativeDiscovery().discover();
		
		// user �� ���� ����
		this.user = user;
		
		// null �� ���
		if (user == null) {
			this.user = new Client("jihyun", "1234");
		}

		con = new Connect();
		// ���� Ž���� ���� (.mp4 ���ϸ� ����)
		uploader = new JFileChooser();
		uploader.addChoosableFileFilter(new FileNameExtensionFilter("mp4",
				"mp4"));

		setBounds(500, 500, 1000, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

		setContentPane(mediaPlayerComponent);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());

		contentPane.add(mediaPlayerComponent, BorderLayout.CENTER);

		controlsPane = new JPanel();

		JButton rewindButton = new JButton("Rewind");
		controlsPane.add(rewindButton);
		final JButton playPauseButton = new JButton("play");
		controlsPane.add(playPauseButton);
		JButton skipButton = new JButton("Skip");
		controlsPane.add(skipButton);

		// upload ��ư �߰� (��ư Ŭ���� ���� Ž���� ����)
		JButton uploadButton = new JButton("upload");
		uploadButton.addActionListener(new FileUploadListener());
		controlsPane.add(uploadButton);

		PlatListPane playList = new PlatListPane("test");
		
		contentPane.add(controlsPane, BorderLayout.SOUTH);
		contentPane.add(playList, BorderLayout.EAST);

		playPauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton pressed = (JButton) e.getSource();

				// debug
				System.out.println(pressed.getText());

				// play ��ư�� ���� ���
				if (pressed.getText().equals("play")) {

					// play -> pause �� ��ư ���� ����
					playPauseButton.setText("pause");

					// ó�� ����ϴ� ���
					if (check == false) {
						// path�� ��ġ�� ���� ���
						String path = "./temp.mp4";
						mediaPlayerComponent.getMediaPlayer().playMedia(path);

						// ó�� ����� �ƴ�.
						check = true;
					} else {
						// �ٽ� ���
						mediaPlayerComponent.getMediaPlayer().pause();
					}
				} else {
					// pause -> play �� ��ư ���� ����
					playPauseButton.setText("play");
					mediaPlayerComponent.getMediaPlayer().pause();
				}

			}
		});

		rewindButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().skip(-10000);
			}
		});

		skipButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().skip(10000);
			}
		});

		mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(
				new MediaPlayerEventAdapter() {
					@Override
					public void playing(MediaPlayer mediaPlayer) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setTitle(String.format(
										"My First Media Player - %s",
										mediaPlayerComponent.getMediaPlayer()
												.getMediaMeta().getTitle()));
							}
						});
					}

					@Override
					public void finished(MediaPlayer mediaPlayer) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {

							}
						});
					}

					@Override
					public void error(MediaPlayer mediaPlayer) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								JOptionPane.showMessageDialog(null,
										"Failed to play media", "Error",
										JOptionPane.ERROR_MESSAGE);
								closeWindow();
							}
						});
					}
				});

		setContentPane(contentPane);
		// setVisible(true);
	}

	// upload ��ư ������ ��
	public class FileUploadListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {

			int ret = uploader.showOpenDialog(null);

			File uploadFile; // ���ε� �� ����
			String uploadFileName; // ���ε� �� ���� ��
			int uploadFileSize; // ����ũ�� �����

			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = uploader.getSelectedFile();
				String fileStr = file.getName();

				// ���� Ȯ���� Ȯ��
				String extension = FilenameUtils.getExtension(fileStr);

				// ���� Ȯ���ڰ� "mp4" �� ���
				if (extension.equals("mp4")) {

					// ���ϸ� ����
					uploadFileName = fileStr.replaceFirst("[.][^.]+$", "");

					// ���� ũ�� ����
					uploadFileSize = (int) file.length();

					// debug
					System.out.println("upload file : " + uploadFileName);
					System.out.println("upload file size : " + uploadFileSize);

					try {
						Thread t1 = new Thread(new uploadRunnable(uploadFileName, file.getPath()));
						t1.start();
					} catch (Exception e) {
						e.printStackTrace();

					}

				} else {
					JOptionPane.showMessageDialog(null,
							"file upload failed! mp4 file only!!!");
				}
			}

		}

	}

    static class uploadRunnable implements Runnable{
		OutputStream sendStream;
		DataOutputStream dsendStream;
		FileInputStream readFile;
		Socket socket;
		String id, source,mediaName;
		
		int size, nread;
		byte[] flag = new byte[1];
		byte[] msg = new byte[75];
		byte[] contentsBuffer = new byte[16834];
		File fileObj;
		uploadRunnable(String _medianame,String _source) {
			source = new String(_source);
			mediaName = new String(_medianame);
			fileObj = new File(source);
		}
		@Override
		public void run() {
			try {
				socket = new Socket(con.gethostName(),con.getPort2());
				sendStream = socket.getOutputStream();
				dsendStream = new DataOutputStream(sendStream);

				id = "test_id";
				flag[0] = 4;
				
				size = (int) fileObj.length();
				byte[] sizeByte = Convert.intToByteArray(size,4);
				System.out.println("size : "+size +" length : "+sizeByte.length);
				
				System.arraycopy(flag, 0, msg, 0, 1);
				System.arraycopy(mediaName.getBytes(), 0, msg, 1, mediaName.length());
				System.arraycopy(id.getBytes(), 0, msg, 61, id.length());
				System.arraycopy(sizeByte, 0, msg, 71, sizeByte.length);
				System.out.println(msg);
				dsendStream.write(msg);
				
				readFile = new FileInputStream(source);
				while((nread = readFile.read(contentsBuffer)) != -1) {
						dsendStream.write(contentsBuffer);
				}

				System.out.println("done");
			}catch (Exception e) {
				System.out.println(e);
			}finally {
				try {
					readFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	
	}
	private void closeWindow() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
}