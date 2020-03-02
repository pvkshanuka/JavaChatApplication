import com.sun.net.ssl.internal.ssl.Provider;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.security.Security;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JavaHttpChatClient {

    private static final String USER_AGENT = "Mozilla/5.0";

    private static String POST_URL = "";

    private static String POST_PARAMS = "";

    private static Scanner scanner;

    private static String txt, name;
    private static Pattern pattern;
    private static Matcher matcher;


    static {
        Scanner scanner1 = new Scanner(System.in);
        System.out.println("Please enter IP address of server.");
        Pattern pattern1 = Pattern.compile("(^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$)");
        Matcher matcher1 = pattern1.matcher("");
        while (true) {
            String nextLine = scanner1.nextLine();
            matcher1.reset(nextLine);
            if (matcher1.find()) {
                javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                        new javax.net.ssl.HostnameVerifier() {

                            public boolean verify(String hostname,
                                                  javax.net.ssl.SSLSession sslSession) {
                                if (hostname.equals(nextLine)) {
                                    return true;
                                }
                                return false;
                            }
                        });
                break;
            } else {
                System.out.println("Invalid command.");
            }
        }
    }


    public static void main(String[] args) {

//        Security.addProvider(new Provider());
        System.setProperty("javax.net.ssl.trustStore", "src/ChatTrustStore.jts");
//        System.setProperty("javax.net.ssl.trustStore", "ChatTrustStore.jts");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");
//        System.setProperty("javax.net.debug","all");

        try {

            connectToServer();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void connectToServer() throws IOException {

        boolean isConnected = false;

        scanner = new Scanner(System.in);

        while (!isConnected) {

            System.out.println("Please connect to serve using below command\n'connect 000.000.000.000:0000 as YourName'");

            txt = scanner.nextLine();

            pattern = Pattern.compile("(^connect)\\s(\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b):(\\b\\d{4}\\b)\\s(\\bas\\b)\\s(\\b[A-Za-z]{3,15})$");
            matcher = pattern.matcher(txt);

            if (matcher.find()) {

                name = matcher.group(5);

                POST_PARAMS = "name=" + matcher.group(5);

                POST_URL = "https://" + matcher.group(2) + ":" + matcher.group(3);

                sendPOST(POST_PARAMS);

                isConnected = true;

            } else {
                System.out.println("Invalid command\n");
            }

        }


        startResponseListener();

        startCommandListener();

    }

    private static void startCommandListener() throws IOException {
        boolean isContinue = true;

        while (isContinue) {

            txt = scanner.nextLine();

            if ("list".equals(txt)) {

                POST_PARAMS = "name=" + name + ",cmd=list";
                sendPOST(POST_PARAMS);

            } else if ("history".equals(txt)) {

                POST_PARAMS = "name=" + name + ",cmd=history";
                sendPOST(POST_PARAMS);

            } else if ("exit".equals(txt)) {

                POST_PARAMS = "name=" + name + ",cmd=exit";
                sendPOST(POST_PARAMS);
                isContinue = false;
                System.exit(0);
            } else {

                pattern = Pattern.compile("(^.{0,}\\b)\\s(\\bto)\\s(\\b[A-Za-z]{3,15}\\b)");
                matcher = pattern.matcher(txt);
                if (matcher.find()) {

                    POST_PARAMS = "name=" + name + ",msg=" + matcher.group(1) + ",to=" + matcher.group(3);
                    sendPOST(POST_PARAMS);

                } else {
                    System.out.println("Invalid command.\n");
                }
            }
        }
    }

    private static void startResponseListener() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    while (true) {

                        Thread.sleep(2000);

                        sendPOST("name=" + name + ",cmd=check");

                    }
                } catch (ConnectException e) {
                    System.out.println("Server Disconnected.");
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    private static void sendPOST(String params) throws IOException {
        URL obj = new URL(POST_URL);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);

        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(params.getBytes());
        os.flush();
        os.close();

        int responseCode = con.getResponseCode();

        if (responseCode == HttpsURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (response.toString().length() > 0) {
                System.out.println(response.toString().replace(",", "\n"));
                if ("[Server] : Invalid client name".equals(response.toString())) {
                    connectToServer();
                }
            }
        } else {
            System.out.println("POST request not worked");
        }
    }
}
