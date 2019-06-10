import java.io.*;
import java.net.Socket;

public class Client {
    //Constructor
    public Client(Socket socket, Server parentServer){
        this.socket = socket;
        this.parentServer = parentServer;

        try {
            this.output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.input = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Variables
    private Server parentServer;
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
    private String sendMessageBuffer;
    private long lastDataSubmitTime = 0;

    private boolean isMessageWaiting(){
        try {
            return input.available() > 0;
        } catch (IOException e) {
            this.parentServer.removeClient(this);
            e.printStackTrace();
        }

        return false;
    }

    //Functions
    public String getMessage(){
        if(isMessageWaiting()) {
            byte[] buffer = new byte[0];
            try {
                buffer = new byte[input.available()];
                input.read(buffer);
            } catch (IOException e) {
                this.parentServer.removeClient(this);
            }

            return new String(buffer);
        }

        return null;
    }

    public void setMessage(String message){
        this.sendMessageBuffer = message;
    }

    public void sendMessage(){
        if(sendMessageBuffer.length() < 1){
            return;
        }

        try {
            output.write(sendMessageBuffer.getBytes());
            sendMessageBuffer = "";
        } catch (IOException e) {
            this.parentServer.removeClient(this);
        }
    }

    public void closeConnection(){
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getLastDataSubmitTime(){
        return this.lastDataSubmitTime;
    }

    public void setLastDataSubmitTime(){
        this.lastDataSubmitTime = System.currentTimeMillis();
    }
}
