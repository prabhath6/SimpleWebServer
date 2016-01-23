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

    // constructor
    public ClientHelper(Socket s){
        this.cSocket = s;
    }

    // run
    public void run() {
        try {

            // open socket for data transfer
            // from socket
            InputStream is = cSocket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            // to socket
            OutputStream os = cSocket.getOutputStream();
            PrintStream out = new PrintStream(os);

            // read the incoming request in the for GET /index.html
            String request = br.readLine();
            System.out.println(request);

            // handle request
            String fileName = "";
            StringTokenizer st = new StringTokenizer(request);

            // TODO
            // add another st.hasMoreElements() in if statement it doesn't work.
            if (st.hasMoreElements() && st.nextToken().equalsIgnoreCase("GET")) {
                fileName = st.nextToken();

            } else if (fileName.length() == 0){
                // GET request empty then it should return index.html as file name default.
                fileName = "index.html";
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
                out.println("HTTP/1.0 301 Moved Permanently\\r\\n\"+\n\"Location: /\"+filename+\"/\\r\\n\\r\\n");
                out.close();
            }

            // open file may throw exception
            // to read file
            FileInputStream f = new FileInputStream("/Users/prabhath/IdeaProjects/SimpleWebServer/src/" + fileName);

            // determine the stream of file we are sending
            String fileType = "text/plain";
            if (fileName.endsWith("html") ||  fileName.endsWith("htm")){
                fileType = "text/html";
            } else if (fileName.endsWith("jpg") || fileName.endsWith("jpeg")){
                fileType = "image/jpeg";
            } else if (fileName.endsWith("gig")) {
                fileType = "image/gif";
            }

            // print success status
            out.println("HTTP/1.0 200 OK\\r\\n\"+\n\"Content-type: \"+fileType+\"\\r\\n\\r\\n");

            // send the file contents
            byte[] buffer = new byte[(int) fileName.length()];
            int n;

            while((n = f.read(buffer)) > 0){
                out.write(buffer, 0, n);
            }
            // close
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

public class webServer {

    public static ServerSocket serverSocket;
    public static final int PORT_NUMBER = 8888;

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
