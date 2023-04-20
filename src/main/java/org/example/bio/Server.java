package org.example.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress("127.0.0.1", 8888));
            while (true) {
                Socket s = ss.accept(); // blocking method, blocking IO

                // always need a new thread
                new Thread(() -> {
                    handle(s);
                }).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    static void handle(Socket s) {
        byte[] bytes = new byte[1024];
        try {
            // read and write will all block
            int len = s.getInputStream().read(bytes);
            System.out.println(new String(bytes, 0, len));

            s.getOutputStream().write(bytes, 0, len);
            s.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
