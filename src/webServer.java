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
    String folderName;
    InputStream is;
    InputStreamReader isr;
    BufferedReader br;
    final static String CRLF = "\r\n";

    // to socket
    OutputStream os;


    static final String BASE_DIR = "/Users/prabhath/IdeaProjects/SimpleWebServer/src/";

    // constructor
    public ClientHelper(Socket s) throws Exception{
        this.cSocket = s;
        this.is = s.getInputStream();
        this.isr = new InputStreamReader(is);
        this.br = new BufferedReader(isr);

        // to socket
        this.os = s.getOutputStream();

    }

    // send file
    public void sendFile(FileInputStream fileName, OutputStream os) {
        try {

            //System.out.println(BASE_DIR + folderName + "/" + fileName);
            //FileInputStream f = new FileInputStream(BASE_DIR + folderName + "/" + fileName);

            //System.out.println(fileType);

            // print success status
            //os.println("HTTP/1.0 200 OK\\r\\n\"+\n\"Content-type: \"+fileType+\"\\r\\n\\r\\n");

            // send the file contents
            byte[] buffer = new byte[1024];//(int) fileName.length()];
            int n;

            while((n = fileName.read(buffer)) != -1){
                os.write(buffer, 0, n);
            }
            // close
            //os.close();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // run
    public void run() {
        try {

            process();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void process() {
        try {

            // read the incoming request in the for GET /index.html
            String request = br.readLine();
            System.out.println(request);

            // handle request
            String fileName = "";
            StringTokenizer st = new StringTokenizer(request);

            // TODO
            // add another st.hasMoreElements() in if statement it doesn't work.
            if (st.hasMoreElements() && st.nextToken().equalsIgnoreCase("GET") && st.hasMoreElements()) {
                fileName = st.nextToken();

            } else if (fileName.length() == 0){
                // GET request empty then it should return index.html as file name default.
                fileName = "index/index.html";
            }

            // remove leading '/' in request
            if (fileName.indexOf("/") == 0) {
                fileName = fileName.substring(1);
            }

            // check for illegal file reqursts
            if (fileName.contains("..") || fileName.contains(":") || fileName.contains("|"))
                throw new FileNotFoundException();

            // if trailing / is missing error message
            if (new File(fileName).isDirectory()) {
                fileName.replace("\\", "/");
                //out.println("HTTP/1.0 301 Moved Permanently\\r\\n\"+\n\"Location: /\"+filename+\"/\\r\\n\\r\\n");
                //out.close();
                os.close();
            }

            // check for the directory
            //String delims = ".";
            //StringTokenizer folder = new StringTokenizer(fileName, delims);
            folderName = "index";

            // to check if directory is present or not
//            File[] dirs = new File (BASE_DIR).listFiles();
//            int count = 0;
//
//            assert dirs != null;
//            for (File a: dirs) {
//                if (a.getName().equals(folderName)) {
//                    count += 1;
//                }
//            }
//
//            if (count == 0) {
//                //out.println("No such directory");
//                //out.close();
//                os.close();
//                return;
//            }

            // open file may throw exception
            // to read file
            File[] files = new File(BASE_DIR + folderName).listFiles();

            assert files != null;
            //System.out.println("filename: " + fileName);
            boolean fileExists = true;
            String serverLine = "Server: Simple Java Http Server";
            String statusLine = null;
            String contentTypeLine = null;
            String entityBody = null;
            String contentLengthLine = "error";

            // determine the stream of file we are sending
            String fileType = "text/plain";
            if (fileName.endsWith(".html") ||  fileName.endsWith(".htm")){
                fileType = "text/html";
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")){
                fileType = "image/jpeg";
            } else if (fileName.endsWith(".gif")) {
                fileType = "image/gif";
            }

            FileInputStream f = new FileInputStream(BASE_DIR + folderName + "/" + fileName);

            if (fileExists) {
                statusLine = "HTTP/1.0 200 OK" + CRLF;
                contentTypeLine = "Content-type: " + fileType
                        + CRLF;
                contentLengthLine = "Content-Length: "
                        + (new Integer(f.available())).toString() + CRLF;
            } else {
                statusLine = "HTTP/1.0 404 Not Found" + CRLF;
                contentTypeLine = "text/html";
                entityBody = "<HTML>"
                        + "<HEAD><TITLE>404 Not Found</TITLE></HEAD>"
                        + "<BODY>404 Not Found"
                        + "<br>usage:http://yourHostName:port/"
                        + "fileName.html</BODY></HTML>";
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


            sendFile(f, os);

            // close
            os.close();
            br.close();
            cSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

public class webServer {

    public static ServerSocket serverSocket;
    public static final int PORT_NUMBER = 8889;

    public static void main(String[] args) {

        // create a server socket
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);

            // loop for clients
            while (true) {
                Socket dataSocket = serverSocket.accept();

                // handle client
                ClientHelper client = new ClientHelper(dataSocket);
                client.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
