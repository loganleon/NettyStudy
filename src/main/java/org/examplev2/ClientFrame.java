package org.examplev2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientFrame extends Frame {
    public static final ClientFrame INSTANCE = new ClientFrame();
    TextArea textArea = new TextArea();
    TextField tf = new TextField();
    Client client;
    private ClientFrame() {
        this.setSize(600, 400);
        this.setLocation(100, 20);
        this.add(this.textArea, BorderLayout.CENTER);
        this.add(tf, BorderLayout.SOUTH);
        this.tf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.send(tf.getText());
//                textArea.setText(textArea.getText() + tf.getText());
                // send text to netty server
                tf.setText("");
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.closeConnect();
                System.exit(0);
            }
        });
    }

    private void connectToServer() {
        client = new Client();
        client.connect();
    }
    public void updateText(String msg) {
        this.textArea.setText(textArea.getText() + System.getProperty("line.separator") + msg);
    }
    public static void main(String[] args) {
        ClientFrame frame = ClientFrame.INSTANCE;
        frame.setVisible(true);
        frame.connectToServer();
    }
}
