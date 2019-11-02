package client_connect;

public class Connect {
    private String USERNAME = "java";
    private String PASSWORD = "java";
    private int PORT = 3000;
    private int PORT2 = 3001;
    private String HOSTNAME = "10.27.24.57";
    // 10.27.0.117 5001 : control server
    // 10.27.24.58 3000 : streaming server

    public String getUsername(){
        return this.USERNAME;
    }

    public String getPassword(){

        return this.PASSWORD;
    }

    public int getPort(){
        return this.PORT;
    }
    public int getPort2(){
        return this.PORT2;
    }

    public String gethostName(){
        return this.HOSTNAME;
    }
}