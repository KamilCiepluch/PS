package org.example;

import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame {
    private static GUI gui;
    private Server server;



    private static JLabel portLabel;
    private static JTextField portInput;
    private static JButton startButton;
    private static JButton stopButton;
    private static JTextArea serverLogsArea;


    private static JTextField keyInput;
    private static JButton generateKeyButton;
    private static JButton showDetails;


    private GUI() {

        this.setTitle("Server");
        portLabel = new JLabel("Listening port");
        portInput = new JTextField("7");
        portInput.setForeground(Color.black);
        portInput.setFont(new Font("Calibri", Font.PLAIN,12));
        startButton = new JButton("Start server");
        stopButton = new JButton("Stop server");


        keyInput = new JTextField();
        generateKeyButton = new JButton("Generate License Key ");
        showDetails = new JButton("Show licenses");


        serverLogsArea = new JTextArea();
        serverLogsArea.setFont(new Font("Calibri", Font.PLAIN,12).deriveFont(Font.BOLD));

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);


        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);


        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(portLabel)
                                        .addComponent(portInput)
                                        .addComponent(startButton)
                                        .addComponent(stopButton))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(keyInput)
                                        .addComponent(generateKeyButton)
                                        .addComponent(showDetails))
                                .addComponent(serverLogsArea)
                        ));



        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(portLabel)
                                .addComponent(portInput)
                                .addComponent(startButton)
                                .addComponent(stopButton))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(keyInput)
                                .addComponent(generateKeyButton)
                                .addComponent(showDetails))
                        .addComponent(serverLogsArea)
        );

        this.setSize(600,400);
        this.setVisible(true);
        stopButton.setEnabled(false);

        startButton.addActionListener(e ->
        {
            System.out.println("Started server");
            server = Server.getServer();
            server.startServer();
            portInput.setEnabled(false);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        });


        stopButton.addActionListener(e ->
        {
            if(Server.isServerAlive())
            {
                System.out.println("Close server");
                Server.getServer().closeServer();
                portInput.setEnabled(true);
                stopButton.setEnabled(false);
                startButton.setEnabled(true);
            }
        });






        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    Server.getServer().closeServer();
                    dispose();
                }
                catch (Exception e1)
                {
                    System.out.println("Could not close socket");
                    dispose();
                }
            }
        });
    }
    public static GUI getGui() {

        synchronized (GUI.class)
        {
            if(gui == null)
            {
                gui = new GUI();
            }
        }
        return gui;
    }

    public static void addNewLog(String str)
    {
        synchronized (GUI.class)
        {
            serverLogsArea.insert(str +'\n',0);
        }
    }


    public int getPort()
    {
        String txt = portInput.getText();
        if(txt.length()==0) return 7;
        return Integer.parseInt(txt);
    }
    public static void main(String[] args)  {

        GUI.getGui();

    }
}

