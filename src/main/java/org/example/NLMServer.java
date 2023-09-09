package org.example;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NLMServer extends Thread {
    private static final int defaultPortNumber = 7;
    private ServerGUI gui2;
    private NLMServer server;
    private ServerSocket serverSocket;
    private ArrayList<Thread> activeThreads;
    private static int port = defaultPortNumber;


    ArrayList<License> licenses;
    final List<LicenseInUse> activeLicenses = Collections.synchronizedList(new ArrayList<>());



    public ServerSocket getServerSocket() {
        return serverSocket;
    }


    public NLMServer(ServerGUI gui2)
    {
        try {
            this.serverSocket = new ServerSocket();
            this.activeThreads = new ArrayList<>();
            this.gui2 = gui2;
            licenses = getLicencesArray("payload");
        }
        catch (Exception e)
        {
            System.out.println("Failed to create server");
            gui2.addNewLog("Failed to create server");
        }
    }


    public void startServer()
    {
        try {
            server = new NLMServer(gui2);
            port = gui2.getPort();
            InetAddress address =  InetAddress.getByName("0.0.0.0");
            server.getServerSocket().bind(new InetSocketAddress(address, port));
            gui2.addNewLog("Server start working at port: " + port );
            start();
        }
        catch (UnknownHostException u)
        {
            System.out.println("Incorrect host name - Should never happened");
            gui2.addNewLog("Incorrect host name - Should never happened");
        }
        catch (IOException e)
        {
            System.out.println("Bind failed");
            gui2.addNewLog("Bind failed");
            closeServer();
        }
    }


    public  void closeServer()
    {
        try {
            server.getServerSocket().close();
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            System.out.println("Could not close server socket: ");
            gui2.addNewLog("Could not close server socket: ");
        }
        port = defaultPortNumber;
    }



    void sendMsg(Socket socket, JSONObject json)  throws IOException
    {
        String jsonString = json.toString(); // Konwertuj obiekt JSON na String
        DataOutputStream send = new DataOutputStream(socket.getOutputStream());
        send.write(jsonString.getBytes(), 0, jsonString.length());
    }

    public String generateLicenseKey(String licenseUserName) {
        return MD5.getMd5Hash(licenseUserName);
    }


    public Boolean validateLicenceKey(String licenseUserName, String license)
    {
        String key = generateLicenseKey(licenseUserName);
        return key.equals(license);
    }

    void NMLThread(Socket socket) throws IOException, ParseException
    {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        byte[] buffer = new byte[1024]; //json has max 1024 bytes
        int length = dis.read(buffer);
        String jsonMessage = new String(buffer, 0, length);
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonMessage);


        String userName = jsonObject.get("LicenceUserName").toString();
        String licenceKey = jsonObject.get("LicenceKey").toString();

        JSONObject serverRespondJson = new JSONObject();
        serverRespondJson.put("LicenceUserName", userName);

        if(!validateLicenceKey(userName,licenceKey)) {
            serverRespondJson.put("Licence", false);
            serverRespondJson.put("Description", "Not valid licence key");
        }
        else {
            if(userHaveLicense(userName)) {
                License userLicense = getUserLicense(userName);
                List<LicenseInUse> list = getUserActiveLicenses(userName);
                if(list.isEmpty())
                {
                    String expiredTime = createExpiredTime(userLicense.getValidationTime());
                    synchronized (activeLicenses) {
                        activeLicenses.add(new LicenseInUse(userLicense.getLicenseUserName(),expiredTime));
                    }

                    serverRespondJson.put("Licence", true);
                    serverRespondJson.put("Expired", expiredTime);
                }
                else {
                    serverRespondJson.put("Licence", false);
                    serverRespondJson.put("Description", "All licenses are already taken");
                }
            }
            // There's no license for user
            else{
                serverRespondJson.put("Licence", false);
                serverRespondJson.put("Description", "There is not valid licence for user " + userName);
            }

        }
        sendMsg(socket, serverRespondJson);
        socket.close();
    }


    public synchronized void removeAllExpiredLicense()
    {
        List<LicenseInUse> validLicenses = new ArrayList<>();
        for (LicenseInUse license : activeLicenses) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime expiredTime = LocalDateTime.parse(license.getExpiredTime(), formatter);
            LocalDateTime currentDateTime = LocalDateTime.now();
            if (currentDateTime.isBefore(expiredTime)) {
                validLicenses.add(license);
            }
        }
        activeLicenses.clear();
        activeLicenses.addAll(validLicenses);
    }

    public void updateLicenses()
    {
        while (!server.getServerSocket().isClosed()) {
            removeAllExpiredLicense();
            try {
                sleep(10);
            }catch (Exception ignored){}

        }
    }


    public void showActiveLicenses()
    {
        if(activeLicenses.isEmpty()) {
            gui2.addNewLog("There is no active licenses");
        }

        for(LicenseInUse license:activeLicenses)
        {
            gui2.addNewLog(license.licenseUserName);

            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime expiredTime = LocalDateTime.parse(license.getExpiredTime(), formatter);
            LocalDateTime currentDateTime = LocalDateTime.now();
            long timeDiff = Duration.between(currentDateTime,expiredTime).getSeconds();
            gui2.addNewLog(" - " + timeDiff);
        }
    }


    @Override
    public void run() {
        //gui2.addNewLog("Waiting for client");
        Runnable updateTask = this::updateLicenses;
        Thread updateListThread = new Thread(updateTask);
        updateListThread.start();
        while (!server.getServerSocket().isClosed()) {
            try {
                Socket socket = server.getServerSocket().accept();
                gui2.addNewLog("New client accepted: " + socket.getInetAddress().getHostAddress() + ":" +socket.getPort());
                Thread task = new Thread(() -> {
                    try {
                        NMLThread(socket);
                    }
                    catch (IOException| ParseException  e) {
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
                task.start();
                activeThreads.add(task);
            }
            catch (SocketException ignored) {}
            catch (IOException exception) {
                gui2.addNewLog("Client could not connect: " + exception.getMessage());
                gui2.addNewLog(exception.toString());
            }
        }


        try {
            updateListThread.join();
        }
        catch (Exception e) {
            System.out.println("Update Licenses Thread exception: " + e.getMessage());
        }


        for(Thread thread: activeThreads) {
            try {
                thread.join();
            }
            catch (Exception e) {
                System.out.println("Could not join thread");
            }
        }
        activeThreads.clear();
        gui2.addNewLog("Server closed");
    }










    private String createExpiredTime(Long offset){
        // Pobranie aktualnego czasu
        LocalDateTime currentTime = LocalDateTime.now();
        currentTime = currentTime.plusSeconds(offset);
        // Utworzenie formattera dla ISO 8601
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return  currentTime.format(formatter);
    }










    public static JSONArray readLicencesByLicenceName(String licenceName) throws IOException, ParseException {
        Object o  = new JSONParser().parse(new FileReader("licenses.json"));
        JSONObject licences = (JSONObject) o;
        return  (JSONArray) licences.get(licenceName);
    }
    public ArrayList<License> getLicencesArray(String licenceName) throws IOException, ParseException
    {
        JSONArray array = readLicencesByLicenceName(licenceName);
        ArrayList<License> licences = new ArrayList<>();
        for(Object o:array)
        {
            JSONObject json = (JSONObject) o;
            Long numberOfLicences = (Long) json.get("Licence");
            String userName = (String) json.get("LicenceUserName");
            Long validationTime = (Long) json.get("ValidationTime");
            JSONArray IPs = (JSONArray) json.get("IPadresses");
            List<String> ipAddresses = new ArrayList<>();
            for (Object ip : IPs) {
                ipAddresses.add((String) ip);
            }
            licences.add(new License(userName,numberOfLicences,ipAddresses,validationTime));
        }
        return licences;
    }


    boolean userHaveLicense(String userName)
    {
        for(License licence:licenses) {
            if(licence.getLicenseUserName().equals(userName)) return true;
        }
        return false;
    }

    public License getUserLicense(String userName) {

        List<License> list = licenses.stream()
                .filter(license -> license.getLicenseUserName().equals(userName))
                .toList();
        return list.isEmpty()? null : list.get(0);
    }

    public List<LicenseInUse> getUserActiveLicenses( String userName)
    {
        return  activeLicenses.stream().filter(license -> license.getLicenseUserName().equals(userName))
                .toList();
    }




}































/*
    public JSONArray getUserLicences(String name)
    {
        Set<String> keySet = licences.keySet();
        JSONArray filteredArray = new JSONArray();
        for(String key:keySet)
        {
            JSONArray jsonArray = (JSONArray) licences.get(key);
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                if (jsonObject.get("LicenceUserName").toString().equals(name)) {
                    filteredArray.add(jsonObject);
                }
            }
        }
        return filteredArray;
    }

    public JSONObject getUserLicence(String name)
    {
        JSONArray array = getUserLicences(name);
        if(array.size()!=1) return null;
        return (JSONObject) array.get(0);
    }
}

 */
