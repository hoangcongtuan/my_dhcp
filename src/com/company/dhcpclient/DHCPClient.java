package com.company.dhcpclient;

import com.company.NetworkUtils;
import com.company.Utils;
import com.company.common.DHCPMessage;
import com.company.dhcpserver.DHCPServer;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;

public class DHCPClient {

    private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
    public final static int CLIENT_PORT =  1668;//68;
    private DatagramSocket socket;


    public DHCPClient() throws SocketException {
        socket = new DatagramSocket();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        DHCPClient client;
        try {
            DHCPMessage discoveryMsg = new DHCPMessage();
            byte[] offerYIAddr = new byte[] {(byte) 192, (byte) 168, 1, 4};
            byte[] serverId = new byte[] {(byte) 192, (byte) 168, 1, 1};
            byte[] timeLease = Utils.inttobytes(123456);
            byte[] subnetMask = new byte[] {(byte) 255, (byte) 255, (byte) 255, (byte) 255};
            byte[] router = new byte[] {(byte) 192, (byte) 168, 1, 1};
            byte[] dns = new byte[] {8, 8, 8, 8, 4, 4, 4, 4};

//            byte[] data = discoveryMsg.discoverMsg(NetworkUtils.getMacAddress());
            byte[] data = discoveryMsg.offerMsg(offerYIAddr, serverId, timeLease, subnetMask, router, dns);
            client = new DHCPClient();
            List<InetAddress> brList = NetworkUtils.listAllBroadcastAddresses();
            for(InetAddress inetAddress: brList) {
                client.broadcast(data, inetAddress);
            }

//            byte[] reData = new byte[MAX_BUFFER_SIZE];
//            Arrays.fill(reData, (byte) 0);
//            DatagramPacket p = new DatagramPacket(reData, MAX_BUFFER_SIZE);
//            client.socket.receive(p);
//            System.out.println("Server: " + new String(p.getData()).trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(
            String broadcastMessage, InetAddress address) throws IOException {
        socket.setBroadcast(true);

        byte[] buffer = broadcastMessage.getBytes();

        DatagramPacket packet
                = new DatagramPacket(buffer, buffer.length, address, DHCPServer.SERVER_PORT);
        socket.send(packet);
    }

    private void broadcast(
            byte[] buffer, InetAddress address) throws IOException {
        socket.setBroadcast(true);

        DatagramPacket packet
                = new DatagramPacket(buffer, buffer.length, address, DHCPServer.SERVER_PORT);
        socket.send(packet);
    }
}
