/**
 * Created by prabhath on 1/23/16.
 */

import java.net.*;
import java.io.*;
import java.util.StringTokenizer;


// client helper to handle all the client requests.
class ClientHelper extends Thread{

    // socket to be handled
    Socket cSocket;

    // from socket
    InputStream is;
    InputStreamReader isr;
    BufferedReader br;
    final static String CRLF = "\r\n";

    // to socket
    OutputStream os;

    // path specific
    final static String folderName = "index";
    static final String BASE_DIR = "/Users/prabhath/IdeaProjects/SimpleWebServer/src/";

    // html tags
    static final String HTML_START =
            "<html>" +
                    "<title>HTTP Server in java</title>" +
                    "<body>";

    static final String HTML_END =
            "</body>" +
                    "</html>";

    static final String FILE_NOT_FOUND = "HTTP/1.0 404 Not Found" + CRLF;

    static final String FILE_PERMISSIONS = "<H1> Permission Restricted <H1>";

    static boolean check_for_file = true;

    static FileInputStream f;

    String request;

    StringTokenizer st;

    String fileName;


    // constructor
    public ClientHelper(Socket s) {

        try {

            this.cSocket = s;

            // from socket
            this.is = s.getInputStream();
            this.isr = new InputStreamReader(is);
            this.br = new BufferedReader(isr);

            // to socket
            this.os = s.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // send file
    public void sendFile(FileInputStream fileName, OutputStream out) {
        try {

            // send the file contents
            byte[] buffer = new byte[4028];
            int n;

            while((n = fileName.read(buffer)) > 0){
                out.write(buffer, 0, n);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // run
    public void run() {
        try {


            // read the incoming request in the for GET /index.html
            request = br.readLine();
            System.out.println(request);


            try {
                // handle request
                fileName = "";
                st = new StringTokenizer(request);

                if (st.hasMoreElements() && st.nextToken().equalsIgnoreCase("GET") && st.hasMoreElements()) {
                    fileName = st.nextToken();
                }

                // file name fix
                if (fileName.equals("/")) {
                    fileName = "index.html";
                }

                // remove leading '/' in request
                if (fileName.indexOf("/") == 0) {
                    fileName = fileName.substring(1);
                }

                // check for illegal file requests
                if (fileName.contains("..") || fileName.contains(":") || fileName.contains("|"))
                    throw new FileNotFoundException();

                // determine the stream of file we are sending
                String fileType = "text/plain";
                if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                    fileType = "text/html";
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    fileType = "image/jpeg";
                } else if (fileName.endsWith(".gif")) {
                    fileType = "image/gif";
                }

                String serverLine = "Server: Simple Java Http Server";
                String statusLine = null;
                String contentTypeLine = null;
                String contentLengthLine = "error";

                try {
                    f = new FileInputStream(BASE_DIR + folderName + "/" + fileName);
                } catch (FileNotFoundException e) {
                    check_for_file = false;
                }

                if (check_for_file) {
                    statusLine = "HTTP/1.0 200 OK" + CRLF;
                    contentTypeLine = "Content-type: " + fileType
                            + CRLF;
                    contentLengthLine = "Content-Length: "
                            + (new Integer(f.available())).toString() + CRLF;
                } else {
                    statusLine = FILE_NOT_FOUND;
                    contentTypeLine = "text/html\n";
                    System.out.println("HTTP/1.0 404 Not Found");
                    check(os, statusLine);

                    // close connection
                    os.close();
                    br.close();
                    cSocket.close();
                }

                os.write(statusLine.getBytes());
                System.out.println(statusLine);

                // Send the server line.
                os.write(serverLine.getBytes());
                System.out.println(serverLine);

                // Send the content type line.
                os.write(contentTypeLine.getBytes());
                System.out.println(contentTypeLine);

                // Send the Content-Length
                os.write(contentLengthLine.getBytes());
                System.out.println(contentLengthLine);

                // Send a blank line to indicate the end of the header lines.
                os.write(CRLF.getBytes());
                System.out.println(CRLF);

                System.out.println(fileType);

                File[] dirs = new File(BASE_DIR + folderName).listFiles();
                assert dirs != null;
                for (File a : dirs) {
                    if (!a.canRead()) {
                        // permission check
                        check(os, FILE_PERMISSIONS);
                        return;
                    }
                }

                sendFile(f, os);

                // close
                os.close();
                br.close();
                cSocket.close();
            } catch (NullPointerException e) {
            e.printStackTrace();
        }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void check(OutputStream os, String Error) throws Exception{

        String Container = HTML_START + Error +  HTML_END;
        os.write(Container.getBytes());

    }

}

public class webServer {

    public static ServerSocket serverSocket;
    public static Socket dataSocket;
    public static final int PORT_NUMBER = 8889;
    static final int TIMEOUT = 50000;

    public static void main(String[] args) {

        // create a server socket
        try {
            System.out.println("Ip: " + InetAddress.getLocalHost());
            serverSocket = new ServerSocket(PORT_NUMBER);
            serverSocket.setSoTimeout(TIMEOUT);

            // loop for clients
            while (true) {
                try {

                    // accept
                    dataSocket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    System.out.println("New Time out");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // handle client
                ClientHelper client = new ClientHelper(dataSocket);
                client.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
