package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ClientAPI {


    InetAddress address;
    int port;
    JSONObject licence;
    JSONObject token;

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
    }

    public void setLicence(String userName, String licenceKey)
    {
        licence = new JSONObject();
        licence.put("LicenceUserName", userName);
        licence.put("LicenceKey", licenceKey);
    }

    private void sendData( Socket socket) throws IOException
    {
        String jsonString = licence.toString(); // Konwertuj obiekt JSON na String
        DataOutputStream send = new DataOutputStream(socket.getOutputStream());
        send.write(jsonString.getBytes(), 0, jsonString.length());
    }
    private JSONObject receiveToken(Socket socket) throws IOException, ParseException {

        DataInputStream dis = new DataInputStream(socket.getInputStream());
        byte[] buffer = new byte[1024]; // Załóżmy, że wiadomość JSON nie przekroczy 1024 bajtów
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

    public JSONObject getLicenceToken()
    {
        try {
            Socket clientSocket = new Socket(address,port);
            if(!isTokenValid())
            {
                sendData(clientSocket);
                token = receiveToken(clientSocket);
                if(isTokenValid())
                {
                    System.out.println("License is valid! Ex");
                }
                System.out.println(token.get("Description"));

            }
            System.out.println("XD");

        }catch (Exception e){}
        return null;
    }

    public static void main(String[] args) {
        try {
            ClientAPI api = new ClientAPI();
            InetAddress address = InetAddress.getByName("localhost");
            int port = 7;
            api.start(address,port);
            api.setLicence("Radek", "xd");
            api.getLicenceToken();

        }catch (Exception e)
        {

        }


    }

}
