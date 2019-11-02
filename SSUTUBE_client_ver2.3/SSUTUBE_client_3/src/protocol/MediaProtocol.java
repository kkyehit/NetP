package protocol;

// used as media protocol
public class MediaProtocol {
	
	public static byte UPLOAD_INFO_REQ = (byte)4;
	public static byte PLAY_REQ = (byte)5;
	public static byte[] SEARCH_BY_ID = {(byte)6, (byte)1};
	public static byte[] SEARCH_BY_TITLE = { (byte)6, (byte)2};	

}
