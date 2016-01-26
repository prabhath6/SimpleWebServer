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

    static final String FILE_NOT_FOUND = "<H1> HTTP/1.0 404 File Not Found </H!>";

    static final String FILE_PERMISSIONS = "<H1> Permission Restricted <H1>";

    static boolean check_for_file = true;

    static FileInputStream f;


    // constructor
    public ClientHelper(Socket s) throws Exception{

        this.cSocket = s;

        // from socket
        this.is = s.getInputStream();
        this.isr = new InputStreamReader(is);
        this.br = new BufferedReader(isr);

        // to socket
        this.os = s.getOutputStream();

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
            String request = br.readLine();
            System.out.println(request);

            // handle request
            String fileName = "";
            StringTokenizer st = new StringTokenizer(request);

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
            if (fileName.endsWith(".html") ||  fileName.endsWith(".htm")){
                fileType = "text/html";
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")){
                fileType = "image/jpeg";
            } else if (fileName.endsWith(".gif")) {
                fileType = "image/gif";
            }

            System.out.println(fileType);

            File[] dirs = new File (BASE_DIR + folderName).listFiles();
            assert dirs != null;
            for (File a: dirs) {
                if (!a.canRead()) {
                    // permission check
                    check(os, FILE_PERMISSIONS);
                    return;
                }
            }

            try {
                f = new FileInputStream(BASE_DIR + folderName + "/" + fileName);
            } catch (FileNotFoundException e) {
                check_for_file = false;
            }

            if (check_for_file) {
                sendFile(f, os);
            } else {
                check(os, FILE_NOT_FOUND);
            }

            // close
            os.close();
            br.close();
            cSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void check(OutputStream os, String Error) throws Exception{

        String Container = HTML_START + Error +  HTML_END;
        os.write(Container.getBytes());

        os.close();
        br.close();
        cSocket.close();
    }

}

public class webServer {

    public static ServerSocket serverSocket;
    public static Socket dataSocket;
    public static final int PORT_NUMBER = 8889;

    public static void main(String[] args) {

        // create a server socket
        try {
            System.out.println("Ip: " + InetAddress.getLocalHost());
            serverSocket = new ServerSocket(PORT_NUMBER);

            // loop for clients
            while (true) {

                // accept
                dataSocket = serverSocket.accept();

                // handle client
                ClientHelper client = new ClientHelper(dataSocket);
                client.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
