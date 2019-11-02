package client_connect;

import java.io.*;
import java.net.*;
import java.nio.*;

import protocol.ControlServerProtocol;
import protocol.MediaProtocol;
import protocol.UserProtocol;

public class Client {

	// client->server buffer ũ�� ����
	public static int loginReqSize = 21;
	public static int signupReqSize = 29;
	public static int listupReqSize = 1;
	public static int uploadInfoReqSize = 65;
	public static int contentsReqSize = 16834;
	public static int playReqSize = 5;
	public static int searchByIdReqSize = 12;
	public static int searchByTitleReqSize = 62;

	// server->client buffer ũ�� ����
	public static int loginSuccessRespSize = 10;
	public static int loginFailureRespSize = 2;
	public static int signupSuccessRespSize = 10;
	public static int signupFailureRespSize = 2;
	public static int listupRespSize = 6;
	public static int listInfoRespSize = 84;
	public static int PlayRespSize = 4;
	public static int contentsSendRespSize = 16834;

	public static String downloadFile = "downloaded/tmp.mp4";

	Socket controlSocket; // ��Ʈ�� ������ ����� ����
	Socket streamSocket; // ��Ʈ���� ������ ����� ����

	BufferedReader msgReader;

	OutputStream sendReq; // Ŭ���̾�Ʈ->������ ���� msg�� stream
	InputStream reqResult; // ����->Ŭ���̾�Ʈ�� ���� msg�� stream

	String[] registerInfo;
	String[] loginInfo;
	String[][] mediaList;

	String loginUserName;

	public PlayList[] playList; // �о� �� ��� ��� ���� ����

	int listCnt = 0; // �о� �� ��� ��� ���� �����

	PrintWriter msgWriter; // Ŭ���̾�Ʈ->������ msg�� ���� �� ���� buffer
	DataInputStream resultData; // ����->Ŭ���̾�Ʈ�� ���� �޽����� �����ϴ� stream
	

	public boolean sendRegisterMsg() throws UnknownHostException, IOException,
			Exception {
		// ��� �޽��� ���� (protocol number(byte):2, id, pw, name)

		// Create OutputStream for sending login info to server
		sendReq = controlSocket.getOutputStream();
		reqResult = controlSocket.getInputStream();

		// �޽��� ���� ��, ���� �� ����
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		msgReader = new BufferedReader(new InputStreamReader(reqResult));

		// msg ������
		msgWriter.println(UserProtocol.SIGNUP_REQ
				+ "0000000000".substring(registerInfo[0].length())
				+ registerInfo[0]
				+ "0000000000".substring(registerInfo[1].length())
				+ registerInfo[1]
				+ "00000000".substring(registerInfo[2].length())
				+ registerInfo[2]);

		// msg ���� ����
		msgWriter.flush();

		// msg �б�
		String registerResult = msgReader.readLine();

		// register ����, ���� Ȯ��
		if (registerResult.substring(0, 2).equals("21")) {

			return true;
		} else if (registerResult.substring(0, 2).equals("22")) {
			// ���н� �������� ���� ���� ����
			disconnectControlServer();
			return false;
		} else {
			// register protocol�� �ƴ� ���
			// �������� ���� ���� ����
			disconnectControlServer();
			throw new Exception();
		}
	}

	public boolean sendLoginMsg() throws UnknownHostException, IOException,
			Exception {
		// �α��� �޽��� ���� (protocol number(byte):1, id, pw)
		// Create socket connection

		// Create OutputStream for sending login info to server
		sendReq = controlSocket.getOutputStream();
		reqResult = controlSocket.getInputStream();

		// �޽��� ���� ��, ���� �� ����
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		msgReader = new BufferedReader(new InputStreamReader(reqResult));

		// msg ������
		msgWriter.println(UserProtocol.LOGIN_REQ
				+ "0000000000".substring(loginInfo[0].length()) + loginInfo[0]
				+ "0000000000".substring(loginInfo[0].length()) + loginInfo[1]);

		// msg ���� ����
		msgWriter.flush();

		// msg �б�
		String loginResult = msgReader.readLine();

		// debug
		System.out.println(loginResult);

		// login ����, ���� Ȯ��
		if (loginResult.substring(0, 2).equals("11")) {
			loginUserName = new String(loginResult.substring(2));

			return true;
		} else if (loginResult.substring(0, 2).equals(
				ControlServerProtocol.LOGIN_FAILURE.toString())) {
			// ���н� �������� ���� ���� ����
			disconnectControlServer();
			return false;
		} else {
			// login protocol�� �ƴ� ���
			// �������� ���� ���� ����
			disconnectControlServer();
			throw new Exception();
		}
	}

	public void sendListReqMsg() throws IOException, Exception {
		// ����Ʈ ��� ��û �޽��� ���� (protocol number(byte):3)

		// delay ����
		controlSocket.setSoTimeout(5000);

		// Create OutputStream for sending login info to server
		sendReq = controlSocket.getOutputStream();
		reqResult = controlSocket.getInputStream();

		// �޽��� ���� ��, ���� �� ����
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		msgReader = new BufferedReader(new InputStreamReader(reqResult));

		// msg ������
		msgWriter.println("3");

		// msg ���� ����
		msgWriter.flush();

		try {
			// msg �б�
			String listInfo = msgReader.readLine();
		
			// debug
			System.out.println(listInfo);
		
			// list up �޽��� Ȯ��
			if (listInfo.substring(0, 2).equals("31")) {
		
				// 2���� 4byte ���� �ޱ�
				String echoListSize = listInfo.substring(2);
		
				// debug
				System.out.println("msg length" + echoListSize.length());
		
				// debug
				System.out.println(echoListSize);
		
				// // 4byte �迭�� ��ȯ
				// byte[] listSizeArray = echoListSize.getBytes();
				//
				// // ��ȯ
				// int echoSize = Convert.byteArrayToInt(listSizeArray);
		
				// ��ȯ
				int echoSize = Integer.parseInt(echoListSize, 16);
		
				// debug
				System.out.println("echoSize : " + echoSize);
		
				playList = new PlayList[echoSize];
		
				// byte[] byteData = new byte[10];
				// int nread;
		
				// ������ŭ �ݺ�
				for (int i = 0; i < echoSize; i++) {
		
					// debug
					System.out.println("aaa");
		
					// ��Ͽ� �� ���� �޽��� �б�
					String listRecord = msgReader.readLine();
		
					// nread = reqResult.read(byteData);
					//
					// String listRecord = byteData.toString();
					//
					// //debug
					// System.out.println("read byte"+ nread);
					//
					// //debug
					// System.out.println(listRecord);
					//
					String keyStr;
					String titleStr;
					String idStr;
					String sizeStr;
					String viewsStr;
		
					String title;
					String id;
					int size;
					int views;
		
					// debug
					System.out.println(i + "msg :" + listRecord);
		
					// listRecord �޽��� Ȯ�� �� ��ȯ
					if (listRecord.substring(0, 2).equals("32")) {
		
						// key(4byte), title(60byte), id(10byte),
						// size(4byte), views(4bytes) string���� �ʱ�ȭ
						keyStr = listRecord.substring(2, 6);
		
						titleStr = listRecord.substring(6, 66);
						idStr = listRecord.substring(66, 76);
						sizeStr = listRecord.substring(76, 80);
						viewsStr = listRecord.substring(80, 84);
		
						// replace "0" -> ""
						title = new String(titleStr.replaceAll("0", ""));
						System.out.println("title : "+title);
						id = new String(idStr.replaceAll("0", ""));
						System.out.println("id : "+id);
		
						// int�� ��ȯ�ؾ��ϴ� key, size, views �� 4byte �迭�� ��ȯ
						// key = Integer.parseInt(keyStr, 16);
						size = Integer.parseInt(sizeStr, 16);
						System.out.println("size : "+size);
						views = Integer.parseInt(viewsStr, 16);
						System.out.println("view : "+views);
					} else {
						// listSize protocol�� �ƴѰ��
						throw new Exception();
					}
		
					playList[i] = new PlayList(keyStr, title, id, size, views);
		
					// debug
					System.out
							.println("key: " + playList[i].getKey() + "title: "
									+ playList[i].getTitle() + "id: "
									+ playList[i].getId());
				}
		
			} else {
				// listResp protocol�� �ƴ� ���
				throw new Exception();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendPlayReqMeg(String title) throws UnknownHostException,
			IOException, Exception {
		// ������� �޽��� ���� (protocol number(byte):5, key)

		// Create OutputStream for sending login info to server
		sendReq = streamSocket.getOutputStream();
		reqResult = streamSocket.getInputStream();

		// playlist �� ���ڵ� ��ȸ�ϸ鼭 title ��ġ�ϴ��� Ȯ�� (��ġ�ϸ� index Ȯ��)
		int index = findPlayList(title);

		// ��Ͽ� ���� ���
		if (index < 0) {
			throw new Exception("NOT in list!");
		}

		// key �� ����
		// int key = playList[index].getKey();

		// // key �� byteArray�� ��ȯ
		// byte[] keyArray = new byte[4];
		// keyArray = Convert.intToByteArray(key, 4);
		//
		//
		// String keyStr = new String(keyArray.toString());
		// // �빮�ڷ� ����
		// keyStr = keyStr.toUpperCase();
		//
		// // �޽��� ���� ��, ���� �� ����
		// msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		// msgReader = new BufferedReader(new InputStreamReader(reqResult));
		//
		// // msg ������
		// msgWriter.println(MediaProtocol.SEARCH_BY_ID + keyStr);
		// // msg ���� ����
		// msgWriter.flush();
		//
		// msg �ޱ����� ���� Ŭ���� ����
		class downloadRunnable implements Runnable {

			FileOutputStream fileWrite; // ���� �� stream
			// sendReq, reqResult
			DataOutputStream sendDataStream; // ������ ������ ���� stream
			DataInputStream recvDataStream; // ������ ������ ���� stream

			int totalSize; // ���� ���� ũ��
			int nread; // ���� ���� ũ�� Ȯ�� ��

			byte[] flag = new byte[1];
			byte[] size = new byte[4];
			byte[] msg = new byte[5];
			byte[] contentsBuffer = new byte[contentsSendRespSize];

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					class myThread extends Thread {
						boolean flag; // ���� �ϷḦ �˷��ִ� �뵵 (false�� ���ۿϷ�)
						int sread, total = 0, totalRead = 0;

						public void run() {
							super.run();
							flag = true;
							sread = 0;
							while (flag) {
								try {
									// 1�ʰ� sleep
									Thread.sleep(1000);
									System.out.println("download : " + (sread)
											/ 10 + "bytes/s ["
											+ ((double) totalRead / total)
											* 100 + "%]");
									sread = 0;
								} catch (Exception e) {
									// TODO: handle exception
								}
							}
						}
					}
					sendReq = streamSocket.getOutputStream();
					sendDataStream = new DataOutputStream(sendReq);
					reqResult = streamSocket.getInputStream();
					recvDataStream = new DataInputStream(reqResult);

					// downloaded/tmp.mp4 ���, Ȯ���ڷ� �ٿ�ε�
					fileWrite = new FileOutputStream(downloadFile);

					// byte[] keyArray = Convert.intToByteArray(key);

				} catch (Exception e) {
					System.out.println("download thread :" + e);
				} finally {
					try {

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	public void sendUploadInfoMsg(String fileName, String uploadUser,
			int fileSize) throws IOException {
		// ���ε� �������� �޽��� ���� (protocol number(byte):4, title, id, size)
		sendReq = streamSocket.getOutputStream();
		// reqResult = controlSocket.getInputStream();

		// �޽��� ���� �� ����
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));

		// titleBuffer ���� "0" * 60�� (60byte)
		StringBuilder titleBuffer = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			titleBuffer.append("0000000000");
		}
		String title = titleBuffer.toString();

		// file ũ�� -> byte Array[] -> String���� ��ȯ (�빮�ڷ�)
		byte[] sizeArray = new byte[4];
		sizeArray = Convert.intToByteArray(fileSize, 4);
		String fileSizeStr = sizeArray.toString().toUpperCase();

		// msg ������
		msgWriter.println(MediaProtocol.UPLOAD_INFO_REQ
				+ title.substring(fileName.length()) + fileName
				+ "0000000000".substring(getUserID().length()) + getUserID()
				+ fileSizeStr);

		// msg ���� ����
		msgWriter.flush();

	}

	public void sendUploadMediaMsg(File file) throws IOException {
		// ���ε� �������� ���� (data size : 16834)

		// file ������, ũ�� ����
		final String filePath = file.getAbsolutePath();
		int fileSize = (int) file.length();

		class uploadRunnable implements Runnable {

			FileInputStream readFile; // ���� �о���� stream
			int nread; // ���� ���� ũ�� Ȯ�� ��
			DataOutputStream sendDataStream; // ������ ������ ���� stream
			byte[] contentsBuffer = new byte[contentsReqSize];

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					sendReq = streamSocket.getOutputStream();
					sendDataStream = new DataOutputStream(sendReq); // ������ ������
																	// ���� stream
					readFile = new FileInputStream(filePath);

					// ���� �� ���� ������ ������
					while ((nread = readFile.read(contentsBuffer)) != -1) {
						sendDataStream.write(contentsBuffer);
					}

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						readFile.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		// ���� Ŭ���� ����
		uploadRunnable uploadThread = new uploadRunnable();
		uploadThread.run();

	}

	public boolean sendSearchByIdMsg(String inputId)
			throws UnknownHostException, IOException, Exception {
		// �Է��� ID�� �ش��ϴ� ������ �ִ��� id�� ���� (protocol number(byte):6,1, id)
		// �α��� �޽��� ���� (protocol number(byte):1, id, pw)

		// Create OutputStream for sending login info to server
		sendReq = controlSocket.getOutputStream();
		reqResult = controlSocket.getInputStream();

		// �޽��� ���� ��, ���� �� ����
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		msgReader = new BufferedReader(new InputStreamReader(reqResult));

		// msg ������
		msgWriter.println(MediaProtocol.SEARCH_BY_ID
				+ "0000000000".substring(inputId.length()) + inputId);
		// msg ���� ����
		msgWriter.flush();

		// msg �б�
		String listInfo = msgReader.readLine();

		// list up �޽��� Ȯ��
		if (listInfo.substring(0, 2).equals(
				MediaProtocol.SEARCH_BY_ID.toString())) {

			String echoListSize = listInfo.substring(2);

			// debug
			System.out.println(echoListSize);

			// 4byte �迭�� ��ȯ
			byte[] listSizeArray = echoListSize.getBytes();

			// ��ȯ
			int echoSize = Convert.byteArrayToInt(listSizeArray);

			// ��ġ�ϴ� ��� ����� ���� ���
			if (echoSize < 1) {
				return false;
			}

			playList = new PlayList[echoSize];
			

			// ������ŭ �ݺ�
			for (int i = 0; i < echoSize; i++) {

				// ��Ͽ� �� ���� �޽��� �б�
				String listRecord = msgReader.readLine();
				String keyStr;
				String titleStr;
				String idStr;
				String sizeStr;
				String viewsStr;
				int key;
				int size;
				int views;

				// listRecord �޽��� Ȯ�� �� ��ȯ
				if (listRecord.substring(0, 2).equals(
						ControlServerProtocol.LISTUP_DATA.toString())) {

					// key(4byte), title(60byte), id(10byte),
					// size(4byte), views(4bytes) string���� �ʱ�ȭ
					keyStr = listRecord.substring(2, 6);
					titleStr = listRecord.substring(6, 66);
					idStr = listRecord.substring(66, 76);
					sizeStr = listRecord.substring(76, 84);
					viewsStr = listRecord.substring(84, 88);

					// byte �迭�� key, size�� int�� ��ȯ
					// key = Integer.parseInt(keyStr, 16);
					size = Integer.parseInt(sizeStr, 16);
					views = Integer.parseInt(viewsStr, 16);
				} else {
					// listSize protocol�� �ƴѰ��
					throw new Exception();
				}

				playList[i] = new PlayList(keyStr, titleStr, idStr, size, views);
			}

			// ���ϴ� ��� �����Ͱ� ����.
			return true;

		} else {
			// listResp protocol�� �ƴ� ���
			throw new Exception();
		}

	}

	public boolean sendSearchByTitleMsg(String inputTitle) throws IOException,
			Exception {
		// �Է��� ���� �ش��ϴ� ������ �ִ��� Title�� ���� (protocol number(byte):6,2, title)
		// Create OutputStream for sending login info to server
		sendReq = controlSocket.getOutputStream();
		reqResult = controlSocket.getInputStream();

		// �޽��� ���� ��, ���� �� ����
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		msgReader = new BufferedReader(new InputStreamReader(reqResult));

		// titleBuffer ���� "0" * 60�� (60byte)
		StringBuilder titleBuffer = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			titleBuffer.append("0000000000");
		}
		String title = titleBuffer.toString();

		// msg ������
		msgWriter.println(MediaProtocol.SEARCH_BY_TITLE
				+ title.substring(inputTitle.length()) + inputTitle);
		// msg ���� ����
		msgWriter.flush();

		// msg �б�
		String listInfo = msgReader.readLine();

		// list up �޽��� Ȯ��
		if (listInfo.substring(0, 2).equals("62")) {

			String echoListSize = listInfo.substring(2);

			// debug
			System.out.println(echoListSize);

			// 4byte �迭�� ��ȯ
			byte[] listSizeArray = echoListSize.getBytes();

			// ��ȯ
			int echoSize = Convert.byteArrayToInt(listSizeArray);

			// ��ġ�ϴ� ��� ����� ���� ���
			if (echoSize < 1) {
				return false;
			}

			playList = new PlayList[echoSize];

			// ������ŭ �ݺ�
			for (int i = 0; i < echoSize; i++) {

				// ��Ͽ� �� ���� �޽��� �б�
				String listRecord = msgReader.readLine();
				byte[] keyArray;
				byte[] sizeArray;
				byte[] viewsArray;
				String keyStr;
				String titleStr;
				String idStr;
				String sizeStr;
				String viewsStr;
				int key;
				int size;
				int views;

				// listRecord �޽��� Ȯ�� �� ��ȯ
				if (listRecord.substring(0, 2).equals(
						ControlServerProtocol.LISTUP_DATA.toString())) {

					// key(4byte), title(60byte), id(10byte),
					// size(4byte), views(4bytes) string���� �ʱ�ȭ
					keyStr = listRecord.substring(2, 6);
					titleStr = listRecord.substring(6, 66);
					idStr = listRecord.substring(66, 76);
					sizeStr = listRecord.substring(76, 80);
					viewsStr = listRecord.substring(80, 84);

					// int�� ��ȯ�ؾ��ϴ� key, size, views �� 4byte �迭�� ��ȯ
					keyArray = new byte[keyStr.length()];
					keyArray = keyStr.getBytes();

					sizeArray = new byte[sizeStr.length()];
					sizeArray = sizeStr.getBytes();

					viewsArray = new byte[viewsStr.length()];
					viewsArray = viewsStr.getBytes();

					// byte �迭�� key, size�� int�� ��ȯ
					// key = Convert.byteArrayToInt(keyArray);
					size = Convert.byteArrayToInt(sizeArray);
					views = Convert.byteArrayToInt(viewsArray);
				} else {
					// listSize protocol�� �ƴѰ��
					throw new Exception();
				}

				// playList[i] = new PlayList(key, titleStr, idStr, size,
				// views);
			}

			// ���ϴ� ��� �����Ͱ� ����.
			return true;

		} else {
			// listResp protocol�� �ƴ� ���
			throw new Exception();
		}
	}

	// ��Ʈ�Ѽ����� ����
	public void connectControlServer() throws IOException {
		Connect connectInfo = new Connect();
		controlSocket = new Socket(connectInfo.gethostName(),
				connectInfo.getPort());
	}

	// ��Ʈ���ּ����� ����
	public void connectStreamServer() throws IOException {
		Connect connectInfo = new Connect();
		streamSocket = new Socket(connectInfo.gethostName(),
				connectInfo.getPort());
	}

	// control ������ ���� ����
	public void disconnectControlServer() throws IOException {
		controlSocket.close();
	}

	// stream ������ ���� ����
	public void disconnectStreamServer() throws IOException {
		streamSocket.close();
	}

	// constructor (login)
	public Client(String id, String pw) {
		loginInfo = new String[2];
		loginInfo[0] = new String(id);
		loginInfo[1] = new String(pw);

	}

	// constructor (register)
	public Client(String id, String pw, String name) {
		registerInfo = new String[3];
		registerInfo[0] = new String(id);
		registerInfo[1] = new String(pw);
		registerInfo[2] = new String(name);
	}

	// user �̸� ��������
	public String getUserName() {
		return loginUserName;
	}

	// user ID ��������
	public String getUserID() {
		return loginInfo[0];
	}

	// ����Ʈ �� ��������
	public int getListCnt() {
		return listCnt;
	}

	// ����Ʈ �� ����
	public void setListCnt(int listCnt) {
		this.listCnt = listCnt;
	}

	public PlayList[] getPlayList() {
		return playList;
	}

	public int findPlayList(String title) {
		for (int i = 0; i < playList.length; i++) {
			if (playList[i].getTitle().equals(title)) {
				return i;
			}
		}
		// ������ -1 ��ȯ
		return -1;
	}
}
