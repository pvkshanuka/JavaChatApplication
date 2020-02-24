import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaHttpChatServer {

    private static Scanner scanner;
    private static Pattern patternPort = Pattern.compile("(^port)\\s(\\d{4}$)");
    private static Pattern patternParam = Pattern.compile("(^[a-zA-Z]*\\b)=(.*)");
    private static Map<String, ChatMessages> mapClients = new HashMap<>();
    private static Integer port;
    private static BufferedReader bufferedReader;

    public static void main(String[] args) {

        try {

            startServer();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void startServer() throws IOException {

        boolean isStarted = false;

        while (!isStarted) {

            scanner = new Scanner(System.in);

            System.out.println("Please enter port to start server.\n'port xxxx'");

            String text = scanner.nextLine();

            Matcher matcher = patternPort.matcher(text);

            if (matcher.find()) {

                port = Integer.parseInt(matcher.group(2));

                HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);

                httpServer.createContext("/", JavaHttpChatServer::handler);
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
                httpServer.setExecutor(threadPoolExecutor);
                httpServer.start();



                System.out.println("\nServer started.\nClients can connect from below command\n'connect " + getServerIP() + ":" + port + " as ClientName'\n");

                isStarted = true;

            } else {
                System.out.println("Invalid command.\n");
            }

        }

    }

    private static String getServerIP() throws IOException {

        bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("ifconfig").getInputStream()));

        String txt = "";

        while ((txt = bufferedReader.readLine()) != null) {

            if (txt.contains("inet ")) {
//                        System.out.println(txt);
                Pattern pattern = Pattern.compile("(inet).(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b)");
                Matcher matcher = pattern.matcher(txt);
                if (matcher.find()) {

                    txt = matcher.group(2);

                    break;

                } else {
                    System.out.println("Failed to get server IP address.\n");
                }

            }
        }
        return txt;
    }

    public static synchronized void handler(HttpExchange httpExchange) {

        try {

//            System.out.println("New Request");
            String resp = "";

//            GETTING PARAMS

            BufferedReader in = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
            String inputLine;
            String reqbody = "";

            while ((inputLine = in.readLine()) != null) {
                reqbody += inputLine;
            }
            in.close();

            String[] split = reqbody.split(",");
            Map<String, String> mapParams = new HashMap<>();

            Matcher matcher;

            for (String para : split) {
                matcher = patternParam.matcher(para);
                if (matcher.find()) {
                    mapParams.put(matcher.group(1), matcher.group(2));
                }
            }


//            SEND RESPONSE BY CONSIDORING PARAMS

            if (null == mapClients.get(mapParams.get("name"))) {
                mapClients.put(mapParams.get("name"),new ChatMessages());
                System.out.println("New client registered " + mapParams.get("name"));
                resp = "[Server] : Connected to server successfully.";
            } else {


                if ("list".equals(mapParams.get("cmd"))) {

                    //                    mapClients.forEach((s, chatMessages) -> resp+=(s.toLowerCase().equals(mapParams.get("name").toLowerCase())?s+" *":s)+"\n");

                    for (Map.Entry<String, ChatMessages> entry : mapClients.entrySet()) {
                        resp += (entry.getKey().toLowerCase().equals(mapParams.get("name").toLowerCase()) ? entry.getKey() + " *" : entry.getKey()) + ",";
                    }

                    System.out.println(resp);

                } else if (null != mapParams.get("msg") && null != mapParams.get("to") && null != mapParams.get("name")) {

                    System.out.println(mapClients);
                    System.out.println(mapParams.get("to"));
                    if (null == mapClients.get(mapParams.get("to"))) {
                        resp = "[Server] : Invalid no client from that name";
                    } else {
                        ChatMessages chatMessages;
                        if(null !=(chatMessages = mapClients.get(mapParams.get("to")))){

                            chatMessages.addMessage("[" + mapParams.get("name") + "] : " + mapParams.get("msg") + "\n");

                        }else{
                            resp = "[Server] : Invalid client name to send message.,";
                        }
                    }

                } else if ("exit".equals(mapParams.get("cmd")) && null != mapParams.get("name")) {

                    resp = "[Server] : Bye\n";
                    mapClients.remove(mapParams.get("name"));

                }else if(null != mapParams.get("name") && "check" != mapParams.get("cmd")){

                    ChatMessages messages;

                    if(null != (messages = mapClients.get(mapParams.get("name")))){
                        resp="";

                        //                        messages.getMessages().forEach(chatMessage -> resp+=((!chatMessage.isSend()) ? chatMessage.getMessage() + " *" : "") + ",");

                        for (ChatMessage chatMessage : messages.getMessages()) {
                            if (!chatMessage.isSend()){
                                chatMessage.setSend(true);
                                resp+=chatMessage.getMessage();
                            }
                        }

                    }else{
                        resp = "[Server] : Invalid client name.,Bye.,";
                    }

                } else {

                    resp = "[Server] : Invalid client name";

                }

            }

            sendResponse(resp,httpExchange);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void sendResponse(String resp, HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(200, resp.length());
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(resp.getBytes());
        outputStream.flush();
        outputStream.close();
    }

}
