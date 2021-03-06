/**
 * Created by prabhath on 1/23/16.
 */

import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    // html tags
    static final String HTML_START =
            "<html>" +
                    "<title>HTTP Server in java</title>" +
                    "<body>";

    static final String HTML_END =
            "</body>" +
                    "</html>";

    static final String FILE_NOT_FOUND = "HTTP/1.0 404 Not Found" + CRLF;

    static final String FILE_PERMISSIONS = "<H1> HTTP/1.0 403 Permission Restricted <H1>";

    String request;
    String folderName;// = "www.scu.edu";
    String BASE_DIR;// = "/Users/prabhath/IdeaProjects/SimpleWebServer/src/";


    // constructor
    public ClientHelper(Socket s, String BASE_DIR, String folderName) {

        try {

            this.cSocket = s;

            // from socket
            this.is = cSocket.getInputStream();
            this.isr = new InputStreamReader(is);
            this.br = new BufferedReader(isr);

            // to socket
            this.os = cSocket.getOutputStream();

            // path specific
            this.folderName = folderName;
            this.BASE_DIR = BASE_DIR;

        } catch (NullPointerException e) {
            System.out.println();
        } catch (SocketException e) {
            System.out.println();
        } catch (IOException e) {
        e.printStackTrace();}

    }

    // send file
    public void sendFile(FileInputStream fileName, OutputStream out) {
        try {

            // send the file contents
            byte[] buffer = new byte[1024];
            int n;

            while((n = fileName.read(buffer)) != -1){
                out.write(buffer, 0, n);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // run
    public void run() {
        try {
            process();
        } catch (Exception e) {
            System.out.println("Socket closed");
        }
    }

    public void process() throws Exception{

            // read the incoming request in the for GET /index.html
            request = br.readLine();
            System.out.println(request);

            boolean check_for_file = true;

            // handle request
            String fileName = "";
            StringTokenizer st = new StringTokenizer(request);

                FileInputStream f = null;

                if (st.hasMoreElements() && st.nextToken().equalsIgnoreCase("GET") && st.hasMoreElements()) {
                    fileName = st.nextToken();
                }

                 //file name fix
                if (fileName.equals("/")) {
                    fileName = "index.html";
                }

                // remove leading '/' in request
                if (fileName.indexOf("/") == 0) {
                    fileName = fileName.substring(1);
                }

                // check for illegal file requests
                if (fileName.contains("..") || fileName.contains(":") || fileName.contains("|"))
                    check(os, FILE_NOT_FOUND);


                // determine the stream of file we are sending
                String fileType ;//= "text/plain";
                if (fileName.endsWith(".html") || fileName.endsWith(".htm") || fileName.endsWith(".txt")) {
                    fileType = "text/html";
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    fileType = "image/jpeg";
                } else if (fileName.endsWith(".gif")) {
                    fileType = "image/gif";
                } else {
                    fileType = "application/octet-stream";
                }

                String serverLine = "Server: Simple Java Http Server";
                String statusLine = null;
                String contentTypeLine = null;
                String contentLengthLine = "error";

                // date
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                String today = dateFormat.format(date) + CRLF;

                try {
                    f = new FileInputStream(BASE_DIR + folderName + "/" + fileName);
                } catch (FileNotFoundException e) {
                    check_for_file = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (check_for_file) {
                    statusLine = "HTTP/1.0 200 OK" + CRLF;
                    contentTypeLine = "Content-type: " + fileType + CRLF;
                    contentLengthLine = "Content-Length: " + (new Integer(f.available())).toString() + CRLF;
                } else {
                    statusLine = FILE_NOT_FOUND;
                    contentTypeLine = "text/html";
                    System.out.println("HTTP/1.0 404 Not Found");
                    check(os, statusLine);
                }

                os.write(statusLine.getBytes());
                System.out.println(statusLine);

                // date
                os.write(today.getBytes());
                System.out.println(today);

                // Send the server line.
                os.write(serverLine.getBytes());
                System.out.println(serverLine);

                // Send the content type line.
                os.write(contentTypeLine.getBytes());
                System.out.println(contentTypeLine);

                // Send the Content-Length
                os.write(contentLengthLine.getBytes());
                System.out.println(contentLengthLine);

                // today's date


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

        if (check_for_file) {
            sendFile(f, os);
        } else {
            check(os, statusLine);
        }


        try {
            os.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            cSocket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
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
    static final int TIMEOUT = 10000;

    public static void main(String[] args) {

        // create a server socket
        final int PORT_NUMBER = Integer.parseInt(args[0]);
        try {

            System.out.println("Ip: " + InetAddress.getLocalHost());
            System.out.println("Port Number" + PORT_NUMBER);
            serverSocket = new ServerSocket(PORT_NUMBER);
            serverSocket.setSoTimeout(TIMEOUT);

            // path specific
            String folderName = args[2];
            String BASE_DIR = args[1];

            // loop for clients
            while (true) {
                try {

                    // accept
                    dataSocket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    System.out.println("\nNew Time out");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // handle client
                ClientHelper client = new ClientHelper(dataSocket, BASE_DIR, folderName);
                client.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}