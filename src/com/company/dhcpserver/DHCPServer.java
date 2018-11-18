package com.company.dhcpserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class DHCPServer {
    private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
    private int listenPort = 1337;//1337;

    public DHCPServer(int servePort) {
        listenPort = servePort;
        new DHCPServer();
    }

    public DHCPServer() {
        //System.out.println("Opening UDP Socket On Port: " + listenPort);

        DatagramSocket socket = null;
        try {

            socket = new DatagramSocket(listenPort);  // ipaddress? throws socket exception

            byte[] payload = new byte[MAX_BUFFER_SIZE];
            int length = 6;
            DatagramPacket p = new DatagramPacket(payload, length);
            //System.out.println("Success! Now listening on port " + listenPort + "...");
            System.out.println("Listening on port " + listenPort + "...");

            //server is always listening
            boolean listening = true;
            while (listening) {
                socket.receive(p); //throws i/o exception

                System.out.println("Connection established from " + p.getAddress());

                System.out.println("Data Received: " + new String(p.getData()).trim());
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        DHCPServer server;
        if (args.length >= 1) {
            server = new DHCPServer(Integer.parseInt(args[0]));
        } else {
            server = new DHCPServer();
        }

    }
}
