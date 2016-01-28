Name : A. Prabhath Kiran

Assignment : Assignment-1 Functional Web Server

Date : 28-01-2016

Description : The waits for clients to connect to the server. Once connected the server transfers
 contents server based on the request sent by the client. It throws corresponding errors like file not found, if
 requested file not found. The web server handles multiple clients by assigning a thread to process each client request.
 The sockets assigned to a specific client is terminated after 10 seconds and client can request resource from server
  by making new connection.

list of submitted files : webServer.java, readme.txt, script.docs

Instructions for running your program : webServer portNumber path folderName
(Example: java webServer 8889 /User/desktop/site/ www.scu.edu)

Any other information you want us to know : When the first connection is made and if the server did not receive any
request from clients then after 10 seconds it throws error and terminates, if even one connection is made it transfer
files and then terminate the socket but not the connection.