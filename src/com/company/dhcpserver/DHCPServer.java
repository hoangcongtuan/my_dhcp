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
//                System.out.println("Connection established from " + p.getAddress());
                byte[] msg = p.getData();
                DHCPMessage dhcpMessage = new DHCPMessage(msg);

                byte hardwareLenght = dhcpMessage.getHLen();
                byte[] macAdress = new byte[hardwareLenght];

                for(int i = 0; i < hardwareLenght; i++) {
                    macAdress[i] = msg[28 + i];
                }

                System.out.print("Mac Address: ");
                for (int i = 0; i < macAdress.length; i++) {
                    System.out.format("%02X%s", macAdress[i], (i < macAdress.length - 1) ? ":"
                            : "");
                }
                System.out.println();

                int clientPort = p.getPort();

                String sendStr = "Server Reply";
//                DatagramPacket packet = new DatagramPacket(sendStr.getBytes(), sendStr.getBytes().length, )
                List<InetAddress> inetAddressList = NetworkUtils.listAllBroadcastAddresses();
                for(InetAddress inetAddress : inetAddressList) {
                    broadcast(sendStr, inetAddress, clientPort);
                }

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
//        socket.close();
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
