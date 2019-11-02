package Media;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import Login.JTextFieldLimit;
import client_connect.Client;
import client_connect.Connect;
import client_connect.Convert;

public class PlatListPane extends JPanel {
	
	private static final int TABLE_WIDTH = 120;
	private static final int TABLE_HEIGHT = 720;
	
	private static final int PLAYLIST_HEIGHT = 800;
	
	private static final int LIST_CNT = 20;
	
	private JTable tableView;
	
	private JTextField searchField; // �˻�â
	private JComboBox searchBy; // �˻��� ���� ���ÿ�
	
	private JButton searchButton; // �˻���ư
	static int keyValue;
	static Connect con;
	static DefaultTableModel tableModel;
	static Client use ;
	
	public PlatListPane(String name) {
		this.setLayout(new BorderLayout());
		this.setSize(TABLE_WIDTH, PLAYLIST_HEIGHT);
		createComponents(name);
	}
	
	
	private void createComponents(String name) {
		
		// �޺��ڽ��� ID, Title �׸� �߰� 
		searchBy = new JComboBox();
		searchBy.addItem("ID");
		searchBy.addItem("Title");
		
		// ����ڰ� �Է��� �˻�â
		searchField = new JTextField(30);
	
		// �Է±��� ���� (�ִ� 60�� �Է°���)
		searchField.setDocument((new JTextFieldLimit(60)));
		
		// search button �� search �̹��� �߰�
		ImageIcon searchImg = new ImageIcon("img/search.png");
		searchButton = new JButton(searchImg);
		try {
		
			use = new Client("", "");
			use.connectControlServer();
			use.sendListReqMsg();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		searchButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		// search �г� �߰�
		JToolBar toolBar = new JToolBar("search Menu");
		
		// search �гο� �˻� �׸� �޺� �ڽ�, �˻� �Է�â, ��ư �߰�
		toolBar.add(searchBy);
		toolBar.add(searchField);
		toolBar.add(searchButton);
		
		// JTable �׸� (���� ����, ���� ���ε�id, ���� ��ȸ ��)
		String[] attribute = {"Title", "ID", "Views" };
		
		// Title, ID, Views�� �׸����� ���� JTable �߰� 
		tableModel = new DefaultTableModel(attribute, LIST_CNT) {
			public boolean isCellEditable(int i, int c){ return false; }
		};
		tableView = new JTable(tableModel);
		tableView.addMouseListener(new MouseEventListener());
		con = new Connect();
		
		
		// �гο� searchPanel, tableView �߰� 
		add(toolBar, "North");
		add(tableView, "Center");

		refreash_model();
	}
	
	// ���콺 Ŭ������ ���
	public class MouseEventListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent event) {
			// ����Ŭ�� ���� ���
			if (event.getClickCount() == 2) {
				// ���⼭���� �ؾ� ��.
				int select = tableView.getSelectedRow();
				keyValue = Integer.parseInt(new String(new StringBuilder(use.playList[select].getKey()).reverse()), 16);

				System.out.println("video req "+keyValue);
				Thread t1 = new Thread(new downloadRunnable());
				t1.start();
			}
		}
	}
	/*�ٿ�ε� Runnable*/
	static class downloadRunnable implements Runnable{
		OutputStream sendStream;
		DataOutputStream dsendStream;
		InputStream recvSream;
		DataInputStream drecvStream;
		FileOutputStream fileWrite;
		Socket socket;
		int totalsize;
		int nread;
		byte[] flag = new byte[1];
		byte[] size = new byte[4];
		byte[] msg = new byte[5];
		byte[] contentsBuffer = new byte[16834];
		@Override
		public void run() {
			try {
				class myTread extends Thread{
					boolean flag;
					int sread, total = 0, totalread = 0;
					@Override
					public void run() {
						super.run();
						flag = true;
						sread = 0;
						while(flag) {
							try{
								Thread.sleep(1000);
								System.out.println("download : "+(sread)/10+"bytes/s ["+((double)totalread/total)*100+"%]");
								sread = 0;
							}catch (Exception e) {
								// TODO: handle exception
							}
						}
					}
				}
				socket = new Socket(con.gethostName(),con.getPort2());
				sendStream = socket.getOutputStream();
				dsendStream = new DataOutputStream(sendStream);
				recvSream = socket.getInputStream();
				drecvStream = new DataInputStream(recvSream);
				fileWrite = new FileOutputStream("./temp.mp4");
				
				byte[] key = Convert.intToByteArray(keyValue,4);
				flag[0] = 5;
				System.arraycopy(flag, 0, msg, 0, 1);
				System.arraycopy(key, 0, msg, 1, key.length);
				dsendStream.write(msg);
				System.out.println(msg);
				drecvStream.read(size);
				totalsize = Convert.byteArrayToInt(size);
				System.out.println("size : "+totalsize);
				myTread t = new myTread();
				t.total = totalsize;
				t.start();
				while(totalsize > 0) {
					if((nread = drecvStream.read(contentsBuffer))<0) {
						System.out.println("read error");
						break;
					}
					t.sread += nread;
					t.totalread += nread;
					fileWrite.write(contentsBuffer,0,nread);
					totalsize -= nread;
				}
				t.flag = false;
				System.out.println("done");
			}catch (Exception e) {
				System.out.println("download thread :"+e);
			}finally {
				try {
					socket.close();
					sendStream.close();
					dsendStream.close();
					recvSream.close();
					drecvStream.close();
					fileWrite.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	
	}
	
	public static void refreash_model() {
		if(use.playList == null) {
			System.out.println("null");
			return;
		}
		init_model();
		for(int i = 0; i < use.playList.length; i++) {
			add_to_model(use.playList[i].getTitle(),use.playList[i].getId(), use.playList[i].getViews());
		}
		
	}
	
    public static void init_model() {
		while(tableModel.getRowCount()>0){
			tableModel.removeRow(0);
		}
		//model.setRowCount(0);
	}
	public synchronized static void add_to_model(String _name, String _id, int _view){
		tableModel.addRow(new String[] {_name,_id,Integer.toString(_view)});
	}
	 public static int StringtoInt(String s) {
		 int res = 0;
		 for(int i = 0; i < 4; i++ ) {
			 res *=16;
			 res += (s.charAt(i) > '9')? s.charAt(i) - 'A' : s.charAt(i) - '0';
		 }
		 return res;
	    } 
	 
	    
}
