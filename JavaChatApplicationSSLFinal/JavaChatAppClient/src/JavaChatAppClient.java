import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaChatAppClient {

    private Scanner scanner;
    private Pattern patternPort = Pattern.compile("(^port)\\s(\\d{4}$)");
    private Pattern patternParam = Pattern.compile("(^[a-zA-Z]*\\b)=(.*)");
    //    private static Map<String, ChatMessages> mapClients = new HashMap<>();
    private Integer port;
    private static BufferedReader bufferedReader;


    private final String USER_AGENT = "Mozilla/5.0";

    private String POST_URL = "";

    private String POST_PARAMS = "";

    private String txt, name,ipAddress;
    private Pattern pattern;
    private Pattern pattern2;
    private Matcher matcher;
    private Matcher matcher2;


    static {

        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier() {

                    public boolean verify(String hostname,
                                          javax.net.ssl.SSLSession sslSession) {


                        try {
                            if (hostname.equals(getIp())) {
                                return true;
                            }
                        } catch (IOException e) {
                            System.out.println("Unable to get IP");
                        }

                        System.out.println(hostname+" >>>>>");
                        return false;
                    }
                });

    }


    public static void main(String[] args) {
        try {

            char[] storepass = "123456".toCharArray();
            char[] keypass = "123456".toCharArray();


//            FileInputStream fIn = new FileInputStream("ChatKeyStore.jks");
            FileInputStream fIn = new FileInputStream("src/ChatKeyStore.jks");

            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(fIn, storepass);


            Certificate cert = keystore.getCertificate("chatkey");
            System.out.println(cert);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keystore, keypass);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keystore);

            JavaChatAppClient javaChatAppClient = new JavaChatAppClient();
            javaChatAppClient.startReciver(keyManagerFactory, trustManagerFactory);
            javaChatAppClient.startSender();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private void startSender() {

//        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
//                new javax.net.ssl.HostnameVerifier() {
//
//                    public boolean verify(String hostname,
//                                          javax.net.ssl.SSLSession sslSession) {
//                        if (hostname.equals(1212)) {
//                            return true;
//                        }
//                        return false;
//                    }
//                });


        //        Security.addProvider(new Provider());
        System.setProperty("javax.net.ssl.trustStore", "src/ChatTrustStore.jts");
//        System.setProperty("javax.net.ssl.trustStore", "ChatTrustStore.jts");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");
//        System.setProperty("javax.net.debug","all");

        try {

            boolean isConnected = false;

            scanner = new Scanner(System.in);

            while (!isConnected) {

                System.out.println("Please connect to serve using below command\n'connect 000.000.000.000:0000 as YourName'");

                txt = scanner.nextLine();

                pattern = Pattern.compile("(^connect)\\s(\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b):(\\b\\d{4}\\b)\\s(\\bas\\b)\\s(\\b[A-Za-z]{3,15})$");
//                pattern2 = Pattern.compile("(^connect)\\s(\\blocalhost\\b):(\\b\\d{4}\\b)\\s(\\bas\\b)\\s(\\b[A-Za-z]{3,15})$");
                matcher = pattern.matcher(txt);
//                matcher2 = pattern2.matcher(txt);

                if (matcher.find() || (matcher =(pattern = Pattern.compile("(^connect)\\s(\\blocalhost\\b):(\\b\\d{4}\\b)\\s(\\bas\\b)\\s(\\b[A-Za-z]{3,15})$")).matcher(txt)).find()) {
//                if (matcher.find()) {
//                if (true) {

                    name = matcher.group(5);

                    if (null != (ipAddress = getIp())) {

                        POST_PARAMS = "name=" + matcher.group(5) + ", ip="+ipAddress+",port="+port;

                        POST_URL = "https://" + matcher.group(2) + ":" + matcher.group(3);
                        System.out.println("link : "+POST_URL);
                        sendPOST(POST_PARAMS);

                        isConnected = true;
                    } else {
                        System.out.println("Unable to get IP Address.");
                    }

                } else {
                    System.out.println("Invalid command\n");
                }

            }

            startCommandListener();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void startCommandListener() throws IOException {

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

    private static String getIp() throws IOException {

        bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("ifconfig").getInputStream()));

        String txt = null;

        while ((txt = bufferedReader.readLine()) != null) {

            if (txt.contains("enp1s0: ")) {
                txt = bufferedReader.readLine();
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

        if (null == txt) {
            txt = "127.0.0.1";
        }



        return txt;

    }

    private void sendPOST(String postParams) throws IOException {

        URL obj = new URL(POST_URL);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
//        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(postParams.getBytes());
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
                    startSender();
                }
            }
        } else {
            System.out.println("POST request not worked");
        }

    }

    private void startReciver(KeyManagerFactory keyManagerFactory, TrustManagerFactory trustManagerFactory) throws IOException, NoSuchAlgorithmException, KeyManagementException {

        boolean isStarted = false;

        while (!isStarted) {

            scanner = new Scanner(System.in);

            System.out.println("Please enter port to start messaging app.\n'port xxxx'");

            String text = scanner.nextLine();

            Matcher matcher = patternPort.matcher(text);

            if (matcher.find()) {

                port = Integer.parseInt(matcher.group(2));

                HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
                SSLContext context = SSLContext.getInstance("TLS");

                context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);


                httpsServer.setHttpsConfigurator(new HttpsConfigurator(context) {
                    @Override
                    public void configure(HttpsParameters httpsParameters) {
                        try {

//                            SSLContext sslContext = SSLContext.getDefault();
                            SSLContext sslContext = getSSLContext();
                            SSLEngine sslEngine = sslContext.createSSLEngine();
                            httpsParameters.setNeedClientAuth(false);
                            httpsParameters.setCipherSuites(sslEngine.getEnabledCipherSuites());
                            httpsParameters.setProtocols(sslEngine.getEnabledProtocols());

                            SSLParameters defaultSSLParameters = sslContext.getSupportedSSLParameters();
                            httpsParameters.setSSLParameters(defaultSSLParameters);

                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Https server creation failed.");
                        }
                    }
                });

                httpsServer.createContext("/", JavaChatAppClient::handler);
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
                httpsServer.setExecutor(threadPoolExecutor);
                httpsServer.start();

                System.out.println("Application Started.");

                isStarted = true;

            } else {
                System.out.println("Invalid command.\n");
            }

        }
    }

    private static synchronized void handler(HttpExchange httpExchange) throws IOException {
        System.out.println("awa");
        BufferedReader in = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
        String inputLine;
        String reqbody = "";

        while ((inputLine = in.readLine()) != null) {
            reqbody += inputLine;
        }
        in.close();

        System.out.println(reqbody + "as");

        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        httpExchange.sendResponseHeaders(200, 1);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write("1".getBytes());
        outputStream.flush();
        outputStream.close();

    }
}

