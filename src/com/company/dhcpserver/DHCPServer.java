package com.company.dhcpserver;

import com.company.NetworkUtils;
import com.company.common.DHCPMessage;
import sun.nio.ch.Net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class DHCPServer {
    private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
    public static final int SERVER_PORT = 1667;//67;
    DatagramSocket socket;

    public DHCPServer() throws IOException {
            socket = new DatagramSocket(SERVER_PORT);

            byte[] payload = new byte[MAX_BUFFER_SIZE];
            DatagramPacket p = new DatagramPacket(payload, MAX_BUFFER_SIZE);
            System.out.println("DHCP Server Listening on port " + SERVER_PORT + "...");

            //server is always listening
            boolean listening = true;
            while (listening) {
                socket.receive(p); //throws i/o exception
                byte[] msg = p.getData();
                DHCPMessage dhcpMessage = new DHCPMessage(msg);
                System.out.println("Data Received: " + new String(p.getData()).trim());
            }
            socket.close();
    }

    private void broadcast(
            String broadcastMessage, InetAddress address, int clientPort) throws IOException {
        socket.setBroadcast(true);

        byte[] buffer = broadcastMessage.getBytes();

        DatagramPacket packet
                = new DatagramPacket(buffer, buffer.length, address, clientPort);
        socket.send(packet);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            DHCPServer server = new DHCPServer();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error when init DHCPServer");
        }

    }
}
