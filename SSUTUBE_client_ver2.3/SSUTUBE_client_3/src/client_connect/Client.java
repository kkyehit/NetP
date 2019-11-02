package client_connect;

import java.io.*;
import java.net.*;
import java.nio.*;

import protocol.ControlServerProtocol;
import protocol.MediaProtocol;
import protocol.UserProtocol;

public class Client {

	// client->server buffer 크기 설정
	public static int loginReqSize = 21;
	public static int signupReqSize = 29;
	public static int listupReqSize = 1;
	public static int uploadInfoReqSize = 65;
	public static int contentsReqSize = 16834;
	public static int playReqSize = 5;
	public static int searchByIdReqSize = 12;
	public static int searchByTitleReqSize = 62;

	// server->client buffer 크기 설정
	public static int loginSuccessRespSize = 10;
	public static int loginFailureRespSize = 2;
	public static int signupSuccessRespSize = 10;
	public static int signupFailureRespSize = 2;
	public static int listupRespSize = 6;
	public static int listInfoRespSize = 84;
	public static int PlayRespSize = 4;
	public static int contentsSendRespSize = 16834;

	public static String downloadFile = "downloaded/tmp.mp4";

	Socket controlSocket; // 컨트롤 서버와 통신할 소켓
	Socket streamSocket; // 스트리밍 서버와 통신할 소켓

	BufferedReader msgReader;

	OutputStream sendReq; // 클라이언트->서버로 보낼 msg의 stream
	InputStream reqResult; // 서버->클라이언트로 받을 msg의 stream

	String[] registerInfo;
	String[] loginInfo;
	String[][] mediaList;

	String loginUserName;

	public PlayList[] playList; // 읽어 올 재생 목록 저장 변수

	int listCnt = 0; // 읽어 올 재생 목록 갯수 저장용

	PrintWriter msgWriter; // 클라이언트->서버로 msg를 보낼 때 쓰는 buffer
	DataInputStream resultData; // 서버->클라이언트로 받을 메시지를 저장하는 stream
	

	public boolean sendRegisterMsg() throws UnknownHostException, IOException,
			Exception {
		// 등록 메시지 전송 (protocol number(byte):2, id, pw, name)

		// Create OutputStream for sending login info to server
		sendReq = controlSocket.getOutputStream();
		reqResult = controlSocket.getInputStream();

		// 메시지 보낼 곳, 받을 곳 설정
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		msgReader = new BufferedReader(new InputStreamReader(reqResult));

		// msg 보내기
		msgWriter.println(UserProtocol.SIGNUP_REQ
				+ "0000000000".substring(registerInfo[0].length())
				+ registerInfo[0]
				+ "0000000000".substring(registerInfo[1].length())
				+ registerInfo[1]
				+ "00000000".substring(registerInfo[2].length())
				+ registerInfo[2]);

		// msg 버퍼 비우기
		msgWriter.flush();

		// msg 읽기
		String registerResult = msgReader.readLine();

		// register 성공, 실패 확인
		if (registerResult.substring(0, 2).equals("21")) {

			return true;
		} else if (registerResult.substring(0, 2).equals("22")) {
			// 실패시 서버와의 소켓 연결 끊기
			disconnectControlServer();
			return false;
		} else {
			// register protocol이 아닌 경우
			// 서버와의 소켓 연결 끊기
			disconnectControlServer();
			throw new Exception();
		}
	}

	public boolean sendLoginMsg() throws UnknownHostException, IOException,
			Exception {
		// 로그인 메시지 전송 (protocol number(byte):1, id, pw)
		// Create socket connection

		// Create OutputStream for sending login info to server
		sendReq = controlSocket.getOutputStream();
		reqResult = controlSocket.getInputStream();

		// 메시지 보낼 곳, 받을 곳 설정
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		msgReader = new BufferedReader(new InputStreamReader(reqResult));

		// msg 보내기
		msgWriter.println(UserProtocol.LOGIN_REQ
				+ "0000000000".substring(loginInfo[0].length()) + loginInfo[0]
				+ "0000000000".substring(loginInfo[0].length()) + loginInfo[1]);

		// msg 버퍼 비우기
		msgWriter.flush();

		// msg 읽기
		String loginResult = msgReader.readLine();

		// debug
		System.out.println(loginResult);

		// login 성공, 실패 확인
		if (loginResult.substring(0, 2).equals("11")) {
			loginUserName = new String(loginResult.substring(2));

			return true;
		} else if (loginResult.substring(0, 2).equals(
				ControlServerProtocol.LOGIN_FAILURE.toString())) {
			// 실패시 서버와의 소켓 연결 끊기
			disconnectControlServer();
			return false;
		} else {
			// login protocol이 아닌 경우
			// 서버와의 소켓 연결 끊기
			disconnectControlServer();
			throw new Exception();
		}
	}

	public void sendListReqMsg() throws IOException, Exception {
		// 리스트 목록 요청 메시지 전송 (protocol number(byte):3)

		// delay 없게
		controlSocket.setSoTimeout(5000);

		// Create OutputStream for sending login info to server
		sendReq = controlSocket.getOutputStream();
		reqResult = controlSocket.getInputStream();

		// 메시지 보낼 곳, 받을 곳 설정
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		msgReader = new BufferedReader(new InputStreamReader(reqResult));

		// msg 보내기
		msgWriter.println("3");

		// msg 버퍼 비우기
		msgWriter.flush();

		try {
			// msg 읽기
			String listInfo = msgReader.readLine();
		
			// debug
			System.out.println(listInfo);
		
			// list up 메시지 확인
			if (listInfo.substring(0, 2).equals("31")) {
		
				// 2부터 4byte 까지 받기
				String echoListSize = listInfo.substring(2);
		
				// debug
				System.out.println("msg length" + echoListSize.length());
		
				// debug
				System.out.println(echoListSize);
		
				// // 4byte 배열로 변환
				// byte[] listSizeArray = echoListSize.getBytes();
				//
				// // 변환
				// int echoSize = Convert.byteArrayToInt(listSizeArray);
		
				// 변환
				int echoSize = Integer.parseInt(echoListSize, 16);
		
				// debug
				System.out.println("echoSize : " + echoSize);
		
				playList = new PlayList[echoSize];
		
				// byte[] byteData = new byte[10];
				// int nread;
		
				// 갯수만큼 반복
				for (int i = 0; i < echoSize; i++) {
		
					// debug
					System.out.println("aaa");
		
					// 목록에 들어갈 정보 메시지 읽기
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
		
					// listRecord 메시지 확인 및 변환
					if (listRecord.substring(0, 2).equals("32")) {
		
						// key(4byte), title(60byte), id(10byte),
						// size(4byte), views(4bytes) string으로 초기화
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
		
						// int로 변환해야하는 key, size, views 는 4byte 배열로 변환
						// key = Integer.parseInt(keyStr, 16);
						size = Integer.parseInt(sizeStr, 16);
						System.out.println("size : "+size);
						views = Integer.parseInt(viewsStr, 16);
						System.out.println("view : "+views);
					} else {
						// listSize protocol이 아닌경우
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
				// listResp protocol이 아닌 경우
				throw new Exception();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendPlayReqMeg(String title) throws UnknownHostException,
			IOException, Exception {
		// 영상재생 메시지 전송 (protocol number(byte):5, key)

		// Create OutputStream for sending login info to server
		sendReq = streamSocket.getOutputStream();
		reqResult = streamSocket.getInputStream();

		// playlist 내 레코드 순회하면서 title 일치하는지 확인 (일치하면 index 확인)
		int index = findPlayList(title);

		// 목록에 없을 경우
		if (index < 0) {
			throw new Exception("NOT in list!");
		}

		// key 값 추출
		// int key = playList[index].getKey();

		// // key 값 byteArray로 변환
		// byte[] keyArray = new byte[4];
		// keyArray = Convert.intToByteArray(key, 4);
		//
		//
		// String keyStr = new String(keyArray.toString());
		// // 대문자로 수정
		// keyStr = keyStr.toUpperCase();
		//
		// // 메시지 보낼 곳, 받을 곳 설정
		// msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		// msgReader = new BufferedReader(new InputStreamReader(reqResult));
		//
		// // msg 보내기
		// msgWriter.println(MediaProtocol.SEARCH_BY_ID + keyStr);
		// // msg 버퍼 비우기
		// msgWriter.flush();
		//
		// msg 받기위해 내부 클래스 생성
		class downloadRunnable implements Runnable {

			FileOutputStream fileWrite; // 파일 쓸 stream
			// sendReq, reqResult
			DataOutputStream sendDataStream; // 파일을 실제로 보낼 stream
			DataInputStream recvDataStream; // 파일을 실제로 받을 stream

			int totalSize; // 파일 최종 크기
			int nread; // 파일 읽은 크기 확인 용

			byte[] flag = new byte[1];
			byte[] size = new byte[4];
			byte[] msg = new byte[5];
			byte[] contentsBuffer = new byte[contentsSendRespSize];

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					class myThread extends Thread {
						boolean flag; // 전송 완료를 알려주는 용도 (false면 전송완료)
						int sread, total = 0, totalRead = 0;

						public void run() {
							super.run();
							flag = true;
							sread = 0;
							while (flag) {
								try {
									// 1초간 sleep
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

					// downloaded/tmp.mp4 경로, 확장자로 다운로드
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
		// 업로드 영상정보 메시지 전송 (protocol number(byte):4, title, id, size)
		sendReq = streamSocket.getOutputStream();
		// reqResult = controlSocket.getInputStream();

		// 메시지 보낼 곳 설정
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));

		// titleBuffer 설정 "0" * 60개 (60byte)
		StringBuilder titleBuffer = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			titleBuffer.append("0000000000");
		}
		String title = titleBuffer.toString();

		// file 크기 -> byte Array[] -> String으로 변환 (대문자로)
		byte[] sizeArray = new byte[4];
		sizeArray = Convert.intToByteArray(fileSize, 4);
		String fileSizeStr = sizeArray.toString().toUpperCase();

		// msg 보내기
		msgWriter.println(MediaProtocol.UPLOAD_INFO_REQ
				+ title.substring(fileName.length()) + fileName
				+ "0000000000".substring(getUserID().length()) + getUserID()
				+ fileSizeStr);

		// msg 버퍼 비우기
		msgWriter.flush();

	}

	public void sendUploadMediaMsg(File file) throws IOException {
		// 업로드 영상정보 전송 (data size : 16834)

		// file 절대경로, 크기 추출
		final String filePath = file.getAbsolutePath();
		int fileSize = (int) file.length();

		class uploadRunnable implements Runnable {

			FileInputStream readFile; // 파일 읽어들일 stream
			int nread; // 파일 읽은 크기 확인 용
			DataOutputStream sendDataStream; // 파일을 실제로 보낼 stream
			byte[] contentsBuffer = new byte[contentsReqSize];

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					sendReq = streamSocket.getOutputStream();
					sendDataStream = new DataOutputStream(sendReq); // 파일을 실제로
																	// 보낼 stream
					readFile = new FileInputStream(filePath);

					// 파일 다 읽을 때까지 보내기
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

		// 내부 클래스 생성
		uploadRunnable uploadThread = new uploadRunnable();
		uploadThread.run();

	}

	public boolean sendSearchByIdMsg(String inputId)
			throws UnknownHostException, IOException, Exception {
		// 입력한 ID에 해당하는 영상이 있는지 id만 전송 (protocol number(byte):6,1, id)
		// 로그인 메시지 전송 (protocol number(byte):1, id, pw)

		// Create OutputStream for sending login info to server
		sendReq = controlSocket.getOutputStream();
		reqResult = controlSocket.getInputStream();

		// 메시지 보낼 곳, 받을 곳 설정
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		msgReader = new BufferedReader(new InputStreamReader(reqResult));

		// msg 보내기
		msgWriter.println(MediaProtocol.SEARCH_BY_ID
				+ "0000000000".substring(inputId.length()) + inputId);
		// msg 버퍼 비우기
		msgWriter.flush();

		// msg 읽기
		String listInfo = msgReader.readLine();

		// list up 메시지 확인
		if (listInfo.substring(0, 2).equals(
				MediaProtocol.SEARCH_BY_ID.toString())) {

			String echoListSize = listInfo.substring(2);

			// debug
			System.out.println(echoListSize);

			// 4byte 배열로 변환
			byte[] listSizeArray = echoListSize.getBytes();

			// 변환
			int echoSize = Convert.byteArrayToInt(listSizeArray);

			// 일치하는 재생 목록이 없을 경우
			if (echoSize < 1) {
				return false;
			}

			playList = new PlayList[echoSize];
			

			// 갯수만큼 반복
			for (int i = 0; i < echoSize; i++) {

				// 목록에 들어갈 정보 메시지 읽기
				String listRecord = msgReader.readLine();
				String keyStr;
				String titleStr;
				String idStr;
				String sizeStr;
				String viewsStr;
				int key;
				int size;
				int views;

				// listRecord 메시지 확인 및 변환
				if (listRecord.substring(0, 2).equals(
						ControlServerProtocol.LISTUP_DATA.toString())) {

					// key(4byte), title(60byte), id(10byte),
					// size(4byte), views(4bytes) string으로 초기화
					keyStr = listRecord.substring(2, 6);
					titleStr = listRecord.substring(6, 66);
					idStr = listRecord.substring(66, 76);
					sizeStr = listRecord.substring(76, 84);
					viewsStr = listRecord.substring(84, 88);

					// byte 배열인 key, size를 int로 변환
					// key = Integer.parseInt(keyStr, 16);
					size = Integer.parseInt(sizeStr, 16);
					views = Integer.parseInt(viewsStr, 16);
				} else {
					// listSize protocol이 아닌경우
					throw new Exception();
				}

				playList[i] = new PlayList(keyStr, titleStr, idStr, size, views);
			}

			// 원하는 목록 데이터가 있음.
			return true;

		} else {
			// listResp protocol이 아닌 경우
			throw new Exception();
		}

	}

	public boolean sendSearchByTitleMsg(String inputTitle) throws IOException,
			Exception {
		// 입력한 제목에 해당하는 영상이 있는지 Title만 전송 (protocol number(byte):6,2, title)
		// Create OutputStream for sending login info to server
		sendReq = controlSocket.getOutputStream();
		reqResult = controlSocket.getInputStream();

		// 메시지 보낼 곳, 받을 곳 설정
		msgWriter = new PrintWriter(new OutputStreamWriter(sendReq));
		msgReader = new BufferedReader(new InputStreamReader(reqResult));

		// titleBuffer 설정 "0" * 60개 (60byte)
		StringBuilder titleBuffer = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			titleBuffer.append("0000000000");
		}
		String title = titleBuffer.toString();

		// msg 보내기
		msgWriter.println(MediaProtocol.SEARCH_BY_TITLE
				+ title.substring(inputTitle.length()) + inputTitle);
		// msg 버퍼 비우기
		msgWriter.flush();

		// msg 읽기
		String listInfo = msgReader.readLine();

		// list up 메시지 확인
		if (listInfo.substring(0, 2).equals("62")) {

			String echoListSize = listInfo.substring(2);

			// debug
			System.out.println(echoListSize);

			// 4byte 배열로 변환
			byte[] listSizeArray = echoListSize.getBytes();

			// 변환
			int echoSize = Convert.byteArrayToInt(listSizeArray);

			// 일치하는 재생 목록이 없을 경우
			if (echoSize < 1) {
				return false;
			}

			playList = new PlayList[echoSize];

			// 갯수만큼 반복
			for (int i = 0; i < echoSize; i++) {

				// 목록에 들어갈 정보 메시지 읽기
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

				// listRecord 메시지 확인 및 변환
				if (listRecord.substring(0, 2).equals(
						ControlServerProtocol.LISTUP_DATA.toString())) {

					// key(4byte), title(60byte), id(10byte),
					// size(4byte), views(4bytes) string으로 초기화
					keyStr = listRecord.substring(2, 6);
					titleStr = listRecord.substring(6, 66);
					idStr = listRecord.substring(66, 76);
					sizeStr = listRecord.substring(76, 80);
					viewsStr = listRecord.substring(80, 84);

					// int로 변환해야하는 key, size, views 는 4byte 배열로 변환
					keyArray = new byte[keyStr.length()];
					keyArray = keyStr.getBytes();

					sizeArray = new byte[sizeStr.length()];
					sizeArray = sizeStr.getBytes();

					viewsArray = new byte[viewsStr.length()];
					viewsArray = viewsStr.getBytes();

					// byte 배열인 key, size를 int로 변환
					// key = Convert.byteArrayToInt(keyArray);
					size = Convert.byteArrayToInt(sizeArray);
					views = Convert.byteArrayToInt(viewsArray);
				} else {
					// listSize protocol이 아닌경우
					throw new Exception();
				}

				// playList[i] = new PlayList(key, titleStr, idStr, size,
				// views);
			}

			// 원하는 목록 데이터가 있음.
			return true;

		} else {
			// listResp protocol이 아닌 경우
			throw new Exception();
		}
	}

	// 컨트롤서버와 연결
	public void connectControlServer() throws IOException {
		Connect connectInfo = new Connect();
		controlSocket = new Socket(connectInfo.gethostName(),
				connectInfo.getPort());
	}

	// 스트리밍서버와 연결
	public void connectStreamServer() throws IOException {
		Connect connectInfo = new Connect();
		streamSocket = new Socket(connectInfo.gethostName(),
				connectInfo.getPort());
	}

	// control 서버와 연결 끊기
	public void disconnectControlServer() throws IOException {
		controlSocket.close();
	}

	// stream 서버와 연결 끊기
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

	// user 이름 가져오기
	public String getUserName() {
		return loginUserName;
	}

	// user ID 가져오기
	public String getUserID() {
		return loginInfo[0];
	}

	// 리스트 값 가져오기
	public int getListCnt() {
		return listCnt;
	}

	// 리스트 값 설정
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
		// 없으면 -1 반환
		return -1;
	}
}
