package protocol;

public class ControlServerProtocol {
	public static byte[] LOGIN_SUCCESS = {(byte)1, (byte)1};
	public static byte[] LOGIN_FAILURE = {(byte)1, (byte)2};
	public static byte[] SIGNUP_SUCCESS = {(byte)2, (byte)1};
	public static byte[] SIGNUP_FAILURE = {(byte)2, (byte)2};
	public static byte[] LISTUP_SIZE = {(byte)3, (byte)1};
	public static byte[] LISTUP_DATA = {(byte)3, (byte)2};
}
