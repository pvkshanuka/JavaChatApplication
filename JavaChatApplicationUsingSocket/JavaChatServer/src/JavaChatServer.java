import com.sun.net.ssl.internal.ssl.Provider;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaChatServer {

    public static Map<String, SocketThread> clientMap;
    public static Scanner scanner;
    public static JavaChatServer javaChatServer;
    public static String serverIP;

    PrintWriter printWriter;
    BufferedReader bufferedReader;
    SSLServerSocket serverSocket;
    String receive, send, txt, line;
    Pattern pattern;
    Matcher matcher;
    int port;
    boolean b;


    public static void main(String[] args) {

        Security.addProvider(new Provider());
        System.setProperty("javax.net.ssl.keyStore", "ChatKeyStore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
//        System.setProperty("javax.net.debug","all");

        scanner = new Scanner(System.in);
        clientMap = new HashMap<String, SocketThread>();
        javaChatServer = new JavaChatServer();


        try {

            javaChatServer.createServer();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createServer() throws IOException {

        b = true;

        while (b) {


            System.out.println("Please enter port to start server.\n'port xxxx'");

            txt = scanner.nextLine();

            pattern = Pattern.compile("(^port)\\s(\\d{4}$)");
            matcher = pattern.matcher(txt);


            if (matcher.find()) {

                port = Integer.parseInt(matcher.group(2));
                try {
                    serverSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);

                    bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("ifconfig").getInputStream()));

                    txt = "";

                    while ((txt = bufferedReader.readLine()) != null) {

                        if (txt.contains("inet ")) {
//                        System.out.println(txt);
                            pattern = Pattern.compile("(inet).(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b)");
                            matcher = pattern.matcher(txt);
                            if (matcher.find()) {
                                serverIP = matcher.group(2);

                                break;

                            } else {
                                System.out.println("Failed to get server IP address.\n");
                            }

                        }
                    }

                    System.out.println("\nServer started.\nClients can connect from below command\n'connect " + serverIP + ":" + port + " as ClientName'\n");

                    b = false;
                } catch (BindException e) {
                    System.out.println("Please select another port\n");
                }
            } else {
                System.out.println("Invalid command.\n");
            }

        }

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New Request Recived.");
            startThread(socket);

        }

    }

    private void startThread(Socket socket) {

        SocketThread socketThread = new SocketThread(socket);
        socketThread.start();

    }


}

class SocketThread extends Thread {

    public String clientName;
    private String receive, send, line;
    private Socket socket;
    private BufferedReader bufferedReader;
    public PrintWriter printWriter;

    SocketThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {


        try {


            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            receive = bufferedReader.readLine();

            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);


//                    Pattern pattern = Pattern.compile("(^name)=(\\b[A-Za-z]{3,15}\\b)\\s{1}(\\bmessage)=(\\b.{0,}\\b)");
            Pattern pattern = Pattern.compile("(^name)=(\\b[A-Za-z]{3,15}\\b)");

            Matcher matcher = pattern.matcher(receive);

            if (matcher.find()) {

                if (null == JavaChatServer.clientMap.get(matcher.group(2).toLowerCase())) {

                    System.out.println("New clinet " + matcher.group(2) + " connected\n");
                    printWriter.println("Server:Connected to server successfully.");

                    clientName = matcher.group(2);

                    JavaChatServer.clientMap.put(matcher.group(2).toLowerCase(), this);

                    receive = "";
                    while ((receive = bufferedReader.readLine()) != null) {

//                        pattern = Pattern.compile("(^message)=(\\b.{0,}\\b)\\s(\\bto)\\s(\\b[A-Za-z]{3,15}\\b)");
                        pattern = Pattern.compile("(^.{0,}\\b)\\s(\\bto)\\s(\\b[A-Za-z]{3,15}\\b)");
                        matcher = pattern.matcher(receive);

                        if (matcher.find()) {

                            SocketThread sendSocket = JavaChatServer.clientMap.get(matcher.group(3).toLowerCase());

                            if (null != sendSocket) {
                                if (sendSocket.isAlive()) {

//                                    send = "/";
//                                    printWriter.println(send);
//                                    printWriter.flush();
                                    System.out.println(clientName + " to " + sendSocket.clientName);
                                    if (sendSocket.printWriter == null) {
                                        sendSocket.printWriter = new PrintWriter(sendSocket.socket.getOutputStream(), true);
                                        sendSocket.printWriter.println(clientName + ": " + matcher.group(1));
                                        sendSocket.printWriter.flush();
                                    } else {
                                        sendSocket.printWriter.println(clientName + ": " + matcher.group(1));
                                        sendSocket.printWriter.flush();
                                    }
//                                    send = "/\n";
//                                    printWriter.println(send);
//                                    printWriter.flush();

                                } else {
                                    send = "Server: " + matcher.group(3) + " left.";
                                    JavaChatServer.clientMap.remove(matcher.group(3).toLowerCase());
                                    printWriter.println(send);
                                    printWriter.flush();
                                }
                            } else {
                                send = "Server: " + matcher.group(3) + " Invalid client name.";
                                printWriter.println(send);
                                printWriter.flush();
                            }

                        } else if (receive.equals("list")) {

                            send = "";
                            JavaChatServer.clientMap.forEach((k, v) -> send += ((clientName.toLowerCase().equals(k)) ? k + " *" : k) + "\n");
                            printWriter.println(send);
                            printWriter.flush();

                        } else if (receive.equals("exit")) {

                            bufferedReader.close();
                            printWriter.close();
                            socket.close();
                            JavaChatServer.clientMap.remove(clientName.toLowerCase());
                            break;


                        } else {
                            send = "Server: Invalid command";
                            printWriter.println(send);
                            printWriter.flush();
                        }

                    }

                } else {
                    System.out.println("Already in");
                    printWriter.println("Server: Please select another ClinetName");
                }


            } else {
                printWriter.println("Server: Invalid command\n");
                System.out.println("Invalid command recived.\n");
            }
            printWriter.flush();
            printWriter.close();
            System.out.println(((clientName == null) ? "" : clientName + "'s ") + "Thread End\n");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Something went wrong.");
        }


    }
}
