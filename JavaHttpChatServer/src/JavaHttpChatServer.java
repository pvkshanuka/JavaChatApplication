import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class JavaHttpChatServer {

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(1234);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Request Recived");
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                printWriter.println("Awa");
                printWriter.flush();
                printWriter.close();
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
