package client_connect;

public class Convert {
	/*bytearray to 이진수 변환*/
    public String byteArrayToBinaryString(byte[] b){
        StringBuilder sb=new StringBuilder();
        for(int i=0; i<b.length; ++i){
            sb.append(byteToBinaryString(b[i]));
        }
        return sb.toString();
    } 
 
 
     /*byte to 이진수 변환*/
    public String byteToBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
    } 
 
 
    /* long to 8bytearray 변환*/
    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    } 
 
 
    /* 8bytearray to long 변환*/
    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 7; i >= 0; i--) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
    
    /*int to 4or2 bytearray 변환*/
    public static byte[] intToByteArray(int value, int lengthDiv) {
        byte[] byteArray = new byte[lengthDiv];
        if (lengthDiv == 2){
            byteArray[1] = (byte) value;
            byteArray[0] = (byte) (value >>> 8);   
        }else if (lengthDiv == 4){
            byteArray[3] = (byte)(value >> 24);
            byteArray[2] = (byte)(value >> 16);
            byteArray[1] = (byte)(value >> 8);
            byteArray[0] = (byte)(value);           
        }
        return byteArray;
    } 
     
    
    public static int byteArrayToInt(byte bytes[]) {
    	return ((((int)bytes[3] & 0xff) << 24) |
    			(((int)bytes[2] & 0xff) << 16) |
    			(((int)bytes[1] & 0xff) << 8) |
    			(((int)bytes[0] & 0xff)));
    } 

}