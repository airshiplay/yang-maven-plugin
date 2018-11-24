package demo;

import com.tailf.jnc.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NetconfCallhomeServerTest {

    public static void main(String args[]) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8022);
        } catch (Exception ex) {
            System.out.println("ServerSocket() failed: " + ex);
        }
        while (true) {

            try {
                System.out.println("\n\nWaiting to accept connection...");
                final Socket socket = serverSocket.accept();

                new Thread() {
                    @Override
                    public void run() {
                        InputStream inputStream = null;
                        try {
                            socket.getRemoteSocketAddress();
                            inputStream = socket.getInputStream();
                            byte[] bytes = new byte[20];
                            int read = inputStream.read(bytes, 0, 20);
                            System.out.println(new String(bytes));
                            Device device = new Device("admin", new DeviceUser("admin", "admin", "admin"), socket);
                            device.connect("admin");
                            device.newSession("admin");
                            NetconfSession netconfSession = device.getSession("admin");
                            netconfSession.get("/interfaces");
                            System.out.println("Accepted connection from: " +
                                    socket.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (YangException e) {
                            e.printStackTrace();
                        } catch (JNCException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("accept() failed: " + ex);
                System.exit(-1);
            }

        }
    }
}
