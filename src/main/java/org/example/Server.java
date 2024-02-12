package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements  Runnable{
     private List<ConnectionHandler> connections;
     private ServerSocket server;
     private boolean done;
     private ExecutorService pool;

     Server(){
         this.done=false;
         connections=new ArrayList<>();
     }




    @Override
    public void run(){
        try {
            //to connect to server this is the port number
            server = new ServerSocket(5000);
            /*Executors to manage our socket threads newCachedThread is preferred
               for performance
             */
            pool=Executors.newCachedThreadPool();

            while(!done){
                /* The serverSocket method accept gives a socket to the client
                 for communication so that the Serversocket can continue listening
                 to other requests
                 */
                Socket client=server.accept();
                /* new socket object created is  passed to a connection handler
                  which is a needs a socket object for constructor invocation
                  The connection handler handles the server socket object which is used
                  for communication with the client socket object
                 */
                ConnectionHandler connectionHandler=new ConnectionHandler(client);
                //A list of socket useful when broadcasting messages
                connections.add(connectionHandler);
                /*creation of a new thread for the socket object which
                 will be used for communication with the client
                 */
                pool.execute(connectionHandler);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch (Exception e){
            shutDown();
        }

    }
    private void shutDown() {
        try{
            done=true;
            pool.shutdown();
            if(!server.isClosed()){
                server.close();
            }
            for(ConnectionHandler x:connections){
                x.shutDown();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /* goes through a list of  socket  objects and sends them a message
    from one of  the clients
     */

    public void broadcast(String message,UUID clientid){
        for(ConnectionHandler ch:connections){
            if(ch != null){
                if(ch.clientId==clientid){
                    continue;
                }else{
                    ch.sendMessage(message);
                }
            }
        }

    }
     class  ConnectionHandler implements Runnable{
        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
         private UUID clientId;

        ConnectionHandler(Socket client){
            this.client=client;
            this.clientId=UUID.randomUUID();

        }



         @Override
        public void run() {
            try {
                out=new PrintWriter(client.getOutputStream(),true);
                in=new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println(clientId);
                out.println("Here is your id "+clientId);
                out.println("Please enter a nickname");
                 String name=in.readLine();
                
                broadcast(name + " joined the chat",clientId);
                String message;
                while((message = in.readLine()) != null){
                    if(message.startsWith("/denno")){
                        String[] messageSplit=message.split(" ",2);
                        if(messageSplit.length ==2){
                            broadcast(name + " renamed themselves to " + messageSplit[1],clientId);
                            System.out.println(name + " renamed to " + messageSplit[1]);
                            name=messageSplit[1];
                            out.println("Successfully changed name to " + name);
                        }else{
                            out.println("No nickname provided");
                        }
                    } else if (message.startsWith("/quit")) {
                        broadcast(name + " left the chat",clientId);
                        shutDown();
                    }else{
                        broadcast(name + ":" + message,clientId);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }
        public void sendMessage(String message){

            out.println(message);
        }
        public void shutDown(){
            try {
                in.close();
                out.close();
                if(!client.isClosed()){
                    client.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void main(String[] args) {
        Server server1=new Server();
       /*Server will always run on MAIN thread and the socket objects
       the server will create for communication  will hove their own threads
       which MAIN will maintain and ensure the communication
       you can uncomment the  print method to CONFIRM!
        System.out.println(Thread.currentThread().getName());
        */

        server1.run();

    }
}
