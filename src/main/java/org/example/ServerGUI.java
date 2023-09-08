package org.example;

import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {

    private NLMServer server;

    private final JTextField portInput;
    private final JTextArea serverLogsArea;
    private final JTextField keyInput;


    private ServerGUI() {

        this.setTitle("Server");
        JLabel portLabel = new JLabel("Listening port");
        portInput = new JTextField("7");
        portInput.setForeground(Color.black);
        portInput.setFont(new Font("Calibri", Font.PLAIN,12));
        JButton startButton = new JButton("Start server");
        JButton stopButton = new JButton("Stop server");


        keyInput = new JTextField();
        JButton generateKeyButton = new JButton("Generate License Key ");
        JButton showDetails = new JButton("Show licenses");


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
            server = new NLMServer(this);
            server.startServer();
            portInput.setEnabled(false);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        });


        stopButton.addActionListener(e ->
        {
            System.out.println("Close server");
            server.closeServer();
            portInput.setEnabled(true);
            stopButton.setEnabled(false);
            startButton.setEnabled(true);

        });

        generateKeyButton.addActionListener(e ->
        {
            String key = server.generateLicenseKey(getKeyInput());
            addNewLog("Generated key for \""+ getKeyInput()+"\": " +key);
        });
        showDetails.addActionListener(e ->
        {
            server.showActiveLicenses();
        });





        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    server.closeServer();
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


    public void addNewLog(String str)
    {
        serverLogsArea.insert(str +'\n',0);
    }

    public int getPort()
    {
        String txt = portInput.getText();
        if(txt.length()==0) return 7;
        return Integer.parseInt(txt);
    }

    public String getKeyInput()
    {
        return  keyInput.getText();
    }



    public static void main(String[] args)  {

        new ServerGUI();

    }
}

