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
	
	private JTextField searchField; // 검색창
	private JComboBox searchBy; // 검색할 종류 선택용
	
	private JButton searchButton; // 검색버튼
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
		
		// 콤보박스에 ID, Title 항목 추가 
		searchBy = new JComboBox();
		searchBy.addItem("ID");
		searchBy.addItem("Title");
		
		// 사용자가 입력할 검색창
		searchField = new JTextField(30);
	
		// 입력글자 제한 (최대 60개 입력가능)
		searchField.setDocument((new JTextFieldLimit(60)));
		
		// search button 에 search 이미지 추가
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
		// search 패널 추가
		JToolBar toolBar = new JToolBar("search Menu");
		
		// search 패널에 검색 항목 콤보 박스, 검색 입력창, 버튼 추가
		toolBar.add(searchBy);
		toolBar.add(searchField);
		toolBar.add(searchButton);
		
		// JTable 항목 (영상 제목, 영상 업로드id, 영상 조회 수)
		String[] attribute = {"Title", "ID", "Views" };
		
		// Title, ID, Views를 항목으로 갖는 JTable 추가 
		tableModel = new DefaultTableModel(attribute, LIST_CNT) {
			public boolean isCellEditable(int i, int c){ return false; }
		};
		tableView = new JTable(tableModel);
		tableView.addMouseListener(new MouseEventListener());
		con = new Connect();
		
		
		// 패널에 searchPanel, tableView 추가 
		add(toolBar, "North");
		add(tableView, "Center");

		refreash_model();
	}
	
	// 마우스 클릭했을 경우
	public class MouseEventListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent event) {
			// 더블클릭 했을 경우
			if (event.getClickCount() == 2) {
				// 여기서부터 해야 됨.
				int select = tableView.getSelectedRow();
				keyValue = Integer.parseInt(new String(new StringBuilder(use.playList[select].getKey()).reverse()), 16);

				System.out.println("video req "+keyValue);
				Thread t1 = new Thread(new downloadRunnable());
				t1.start();
			}
		}
	}
	/*다운로드 Runnable*/
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
