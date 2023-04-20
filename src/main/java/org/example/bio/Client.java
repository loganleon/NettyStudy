package org.example.bio;

import java.io.IOException;
import java.net.Socket;

//Half duplex Communication
public class Client {
    public static void main(String[] args) {
        // need to handle exception, and close properly
        try {
            Socket s = new Socket("127.0.0.1", 8888);
            s.getOutputStream().write("HelloServer".getBytes());
            s.getOutputStream().flush();
            System.out.println("write over, waiting for msg back...");
            byte[] bytes = new byte[1024];
            int len = s.getInputStream().read(bytes);
            System.out.println(new String(bytes, 0 , len));
            s.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
