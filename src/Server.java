import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
    //Constructor
    public Server(int port){
        this.port = port;
        this.connectedClients = new LinkedList<Client>();

        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Variables
    private int port;
    private ServerSocket serverSocket;
    private LinkedList<Client> connectedClients;
    private int retrieveDataTimePeriod = 1000 * 60 * 60;  //In milliseconds

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    //Functions
    public void start(){
        System.out.println("Starting TCP Server.....");

        this.listenForClients();

        //Schedule data requests
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendClientMessages();
            }
        },0, 5000);

        while(true){
            this.listenForMessages();
        }

//        System.out.println(".....TCP Server started");
    }


    private void listenForClients() {
        new Thread(new SocketListener(this)).start();
    }

    private void listenForMessages(){
        int i = connectedClients.size();
        for (int clientCount = 0; clientCount < connectedClients.size(); clientCount++) {
            System.out.println("Client count: " + i);
            String myString = connectedClients.get(clientCount).getMessage();
            if (myString != null) {
                System.out.println("My message: " + myString);
                if(isClientSensorData(myString)){
                    SensorInfo sensorInfo = new SensorInfo(myString);

                    sensorInfo.submitSensorData();
                }
            }
        }
    }

    private void sendClientMessages(){
        System.out.println("Sending client message...");
        for (int clientCount = 0; clientCount < connectedClients.size(); clientCount++) {
            connectedClients.get(clientCount).setMessage("[DataRequest]");
            connectedClients.get(clientCount).sendMessage();

            while((String clientMessage = connectedClients.get(clientCount).getMessage()) == null){

            }
        }
    }

    public void createClient(Socket socket){
        System.out.println("Creating client......");

        Client client = new Client(socket, this);
        connectedClients.add(client);

        System.out.println(".....Client created");
    }

    public void removeClient(Client client){
        client.closeConnection();
        System.out.println("....Removed client");
        connectedClients.remove(client);
    }

    private boolean isClientSensorData(String dataToCompare){
        if (Server.isBalancedBrackets(dataToCompare)
                & dataToCompare.indexOf("mac") > 0
                & dataToCompare.indexOf("data") > 0) {

            return true;
        }

        return false;
    }


    public static boolean isBalancedBrackets(String str)
    {
        if (str.isEmpty() || str.indexOf('{') < 0)
            return false;

        Stack<Character> stack = new Stack<Character>();
        for (int i = 0; i < str.length(); i++)
        {
            char current = str.charAt(i);
            if (current == '{' || current == '(' || current == '[')
            {
                stack.push(current);
            }


            if (current == '}' || current == ')' || current == ']')
            {
                if (stack.isEmpty())
                    return false;

                char last = stack.peek();
                if (current == '}' && last == '{' || current == ')' && last == '(' || current == ']' && last == '[')
                    stack.pop();
                else
                    return false;
            }
        }

        return stack.isEmpty();
    }
}
