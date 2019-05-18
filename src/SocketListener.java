import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketListener implements Runnable {
    //Constructor
    SocketListener(Server server){
        this.server = server;
    }

    //Variables
    private Server server;
    private ServerSocket getServerSocket(){
        return this.server.getServerSocket();
    }

    //Functions
    @Override
    public void run() {
            try {
                acceptClient();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.run();
    }


    private void acceptClient() throws IOException {
        System.out.println("Accepting socket connections.....");
        Socket socket = getServerSocket().accept();
        System.out.println(".....Socket accepted");

        this.server.createClient(socket);
    }
}
