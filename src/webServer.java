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
    InputStream is;
    InputStreamReader isr;
    BufferedReader br;
    final static String folderName = "index";
    // to socket
    OutputStream os;


    static final String BASE_DIR = "/Users/prabhath/IdeaProjects/SimpleWebServer/src/";

    // constructor
    public ClientHelper(Socket s){
        this.cSocket = s;
        try {
            this.is = s.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.isr = new InputStreamReader(is);
        this.br = new BufferedReader(isr);

        // to socket
        try {
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
            String request = br.readLine();
            System.out.println(request);

            // handle request
            String fileName = "";
            StringTokenizer st = new StringTokenizer(request);

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

            sendFile(f, os);

            // close
            os.close();
            br.close();
            cSocket.close();

        } catch (IOException e) {
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
        }

    }
}
