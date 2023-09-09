package org.example;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientAPI {
    InetAddress address;
    int port;
    JSONObject licence;
    JSONObject token;
    Thread renewalThread;

    public void start(InetAddress address, int port)
    {
        this.address =address;
        this.port = port;
    }

    public void stop()
    {
        address = null;
        port = 0;
        licence = null;
        token = null;
        if(renewalThread != null && renewalThread.isAlive())
        {
            renewalThread.interrupt();
            try {
                renewalThread.join();
            }catch (Exception e)
            {
                System.out.println("Could not join thread");
            }
        }
    }

    public void setLicence(String userName, String licenceKey)
    {
        licence = new JSONObject();
        licence.put("LicenceUserName", userName);
        licence.put("LicenceKey", licenceKey);
    }

    private void sendData( Socket socket) throws IOException
    {
        String jsonString = licence.toString();
        DataOutputStream send = new DataOutputStream(socket.getOutputStream());
        send.write(jsonString.getBytes(), 0, jsonString.length());
    }
    private JSONObject receiveToken(Socket socket) throws IOException, ParseException {

        DataInputStream dis = new DataInputStream(socket.getInputStream());
        byte[] buffer = new byte[1024]; // json size is 1024 bytes or less
        int length = dis.read(buffer);
        String jsonMessage = new String(buffer, 0, length);
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonMessage);
        return jsonObject;
    }

    private boolean isTokenValid()
    {
        if(token == null) return false;
        else return (boolean) token.get("Licence");
    }

    private void renewLicence()
    {
        while (isTokenValid())
        {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                LocalDateTime expiredTime = LocalDateTime.parse((String)token.get("Expired"), formatter);
                LocalDateTime currentDateTime = LocalDateTime.now();
                long timeDiff = Duration.between(currentDateTime,expiredTime).getSeconds();
                System.out.println(timeDiff);
                Thread.sleep((timeDiff+1) *1000);

                Socket clientSocket = new Socket(address,port);
                sendData(clientSocket);
                token = receiveToken(clientSocket);
                if(!isTokenValid()) {
                    System.out.println(token.get("Description"));
                }
                clientSocket.close();
            }catch (InterruptedException ignored) {}
            catch (SocketException socketException)
            {
                token = null;
                System.out.println("Could not connect to server");
            }
            catch (Exception e) {
                System.out.println("renew Licence Thread exception: " + e.getMessage());
            }
        }
    }

    public JSONObject getLicenceToken()
    {
        if(!isTokenValid())
        {
            try {
                Socket clientSocket = new Socket(address,port);
                sendData(clientSocket);
                token = receiveToken(clientSocket);
                if(isTokenValid()) {
                    System.out.println("License is valid! Licence expired time " + token.get("Expired"));
                    renewalThread = new Thread(this::renewLicence);
                    renewalThread.start();
                }
                else {
                    System.out.println(token.get("Description"));
                }
                clientSocket.close();

            }catch (SocketException socketException) {
                token = null;
                System.out.println("Could not connect to server");
            }
            catch (Exception e){
                System.out.println("getLicecne: " + e.getMessage());
            }
        }
        else{
            System.out.println("License is valid! Licence expired time " + token.get("Expired"));
        }
        return token;
    }



    public static void main(String[] args) {
        try {
            ClientAPI api = new ClientAPI();
            InetAddress address = InetAddress.getByName("localhost");
            int port = 7;
            api.start(address,port);
            api.setLicence("Radek", "9f3a08745c23449a53fc05d68eda1e1b");
            api.getLicenceToken();
        }catch (Exception e)
        {
            System.out.println("main error: " + e.getMessage());
        }


    }

}
