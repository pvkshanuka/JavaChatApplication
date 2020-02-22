import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaChatClient {

    public static JavaChatClient javaChatClient;
    public static Socket socket;
    public static String serverIP, serverPort;

    Scanner scanner;
    String txt, recive, send, name, line;
    PrintWriter printWriter;
    BufferedReader bufferedReader;
    Pattern pattern;
    Matcher matcher;
    int port;
    boolean b;

    public static void main(String[] args) {

        javaChatClient = new JavaChatClient();

        try {
            javaChatClient.connectToServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void connectToServer() throws IOException {

        scanner = new Scanner(System.in);

        b = true;

        while (b) {

            System.out.println("Please connect to serve using below command\n'connect 000.000.000.000:0000 as YourName'");

            txt = scanner.nextLine();

            pattern = Pattern.compile("(^connect)\\s(\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b):(\\b\\d{4}\\b)\\s(\\bas\\b)\\s(\\b[A-Za-z]{3,15})$");
            matcher = pattern.matcher(txt);

            if (matcher.find()) {
                try {


                    if (socket == null) {

                        socket = new Socket(matcher.group(2), Integer.parseInt(matcher.group(3)));
                        name = matcher.group(5);

                        send = "name=" + name + " ";

                        printWriter = new PrintWriter(socket.getOutputStream(), true);
//                    printWriter.println(send + "message=connect  to Server");
                        printWriter.println(send);
                        printWriter.flush();

                        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        recive = bufferedReader.readLine();

                        System.out.println(recive + "\n");

                        if (recive.equals("Server:Connected to server successfully.")) {

                            Thread threadLisener = new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        line = "";
                                        while ((line = bufferedReader.readLine()) != null) {
                                            System.out.println(line);
                                        }


                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            threadLisener.start();

                            while (!(txt = scanner.nextLine()).equals("exit")) {
                                printWriter.println(txt);
                                printWriter.flush();

//                            recive = bufferedReader.readLine().replace("&","\n");

//                            recive = "";
//                            line = "";
//                            while ((line=bufferedReader.readLine()) != null){
//                                recive+=line;
//                            }

//                            System.out.println(recive);
                            }

                            printWriter.println("exit");
                            printWriter.flush();
                            bufferedReader.close();
                            bufferedReader = null;
                            printWriter.close();
                            System.out.println("Bye");

                            b = false;
                            socket.close();
                        } else {
                            socket = null;
                        }
                    } else {
                        System.out.println("You are already connected as " + name + "\n");
                    }
                } catch (ConnectException e) {
                    System.out.println("Invalid Address.\n");
                }
            } else {
                System.out.println("Invalid command\n");
            }


        }


//        printWriter = new PrintWriter(socket.getOutputStream(), true);
//        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//        printWriter.println("From Client");
//        System.out.println(bufferedReader.readLine());

    }

}
