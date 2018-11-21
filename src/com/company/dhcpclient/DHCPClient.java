package com.company.dhcpclient;

import com.company.NetworkUtils;
import com.company.Utils;
import com.company.common.DHCPMessage;
import com.company.dhcpserver.DHCPServer;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class DHCPClient {

    private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
    public final static int CLIENT_PORT =  1668;//68;
    private DatagramSocket socket;
    public final static byte[] CLIENT_HARDWARD_ADDRESS = new byte[] {0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC};
    private final static String HOST_NAME = "MY PC1";

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
            byte[] timeLease = Utils.intToBytes(123456);
            byte[] subnetMask = new byte[] {(byte) 255, (byte) 255, (byte) 255, (byte) 255};
            byte[] router = new byte[] {(byte) 192, (byte) 168, 1, 1};
            byte[] dns = new byte[] {8, 8, 8, 8, 4, 4, 4, 4};

            discoveryMsg.discoverMsg(CLIENT_HARDWARD_ADDRESS, HOST_NAME.getBytes());
            System.out.println(discoveryMsg.toString());
            byte[] data = discoveryMsg.externalize();
            DHCPMessage receivedDHCPMessage = new DHCPMessage(data);
            System.out.println(receivedDHCPMessage.toString());

//            byte[] data = discoveryMsg.offerMsg(offerYIAddr, serverId, timeLease, subnetMask, router, dns);
//            System.out.println(discoveryMsg.toString());
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
