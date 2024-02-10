package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private  boolean done;
    private UUID myClientId;



    @Override
    public void run() {
        try {
            client = new Socket("localhost",5000);
            out = new PrintWriter(client.getOutputStream(),true);
            //send this client unique identifier
            in=new BufferedReader(new InputStreamReader(client.getInputStream()));
            myClientId= UUID.fromString(in.readLine());
            InputHandler handler=new InputHandler();
            Thread t=new Thread(handler);
            t.start();
            String inMessage;
            while((inMessage =in.readLine()) != null){
                System.out.println(inMessage);
            }

        } catch (IOException e) {
            shutDown();
        }

    }
    public  void shutDown(){
        done=true;

        try {
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    class InputHandler  implements Runnable{

        @Override
        public void run() {

            try {
                BufferedReader inReader =new BufferedReader(new InputStreamReader(System.in));
                while(!done) {
                    String message = inReader.readLine();

                    if(message.equals("/quit")) {
                        out.println(message);
                        inReader.close();
                        shutDown();
                    }else{
                        //System.out.println("about to send a message");
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                shutDown();
            }
        }

    }

    public static void main(String[] args) {
        Client client1=new Client();
        client1.run();
    }

}
