package com.company.dhcpserver;

import com.company.NetworkUtils;
import com.company.Utils;
import com.company.common.DHCPMessage;
import com.company.common.DHCPOptions;
import com.company.dhcpclient.DHCPClient;
import com.company.model.ClientData;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DHCPServer {
    private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
    public static final int SERVER_PORT = 1667;//67;
    DatagramSocket socket;

    //client data
    List<ClientData> clientDataList = new ArrayList<>();

    public DHCPServer() throws IOException {
        DatagramSocket socket = new DatagramSocket(SERVER_PORT);
        byte[] buffer = new byte[MAX_BUFFER_SIZE];
        boolean isContinue = true;
        while (isContinue) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            DHCPMessage dhcpMessage = new DHCPMessage(packet.getData());
            int messageType = dhcpMessage.getOptions().getOption(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE)[0];
            switch (messageType) {
                case DHCPOptions.DHCPDISCOVER:
                    processDiscoverMessage(dhcpMessage, socket);
                    break;
                case DHCPOptions.DHCPREQUEST:
                    byte[] targetServerIp = dhcpMessage.getOptions().getOption(DHCPOptions.DHCP_OPTION_SERVER_ID);
                    System.out.println("Request Message:\n" + dhcpMessage.toString());
                    // TODO: 11/22/18 Check targetServerIp, in this example, it always true

                    byte[] subnetMask = new byte[] {(byte) 255, (byte) 255, (byte) 255, (byte) 255};
                    byte[] router = new byte[] {(byte) 192, (byte) 168, 1, 1};
                    byte[] dns = new byte[] {8, 8, 8, 8, 4, 4, 4, 4};
                    DHCPMessage ackMessage = dhcpMessage.createACKMsg(subnetMask, router, dns);
                    System.out.println("ACK Message:\n" + ackMessage.toString());
                    byte[] ack_buffer = ackMessage.externalize();
                    broadcastMessage(ack_buffer, socket, NetworkUtils.listAllBroadcastAddresses(), DHCPClient.CLIENT_PORT);
                    break;
                    default:
                        isContinue = false;
                        break;
            }

        }

        System.out.println("End Time!!");
//            socket = new DatagramSocket(SERVER_PORT);
//
//            byte[] payload = new byte[MAX_BUFFER_SIZE];
//            DatagramPacket p = new DatagramPacket(payload, MAX_BUFFER_SIZE);
//            System.out.println("DHCP Server Listening on port " + SERVER_PORT + "...");
//
//            //server is always listening
//            boolean listening = true;
//            while (listening) {
//                socket.receive(p); //throws i/o exception
//                byte[] msg = p.getData();
//                DHCPMessage dhcpMessage = new DHCPMessage(msg);
//                System.out.println("Data Received: \n" + dhcpMessage.toString());
//            }
//            socket.close();
        try {
            testRWObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void processDiscoverMessage(DHCPMessage dhcpMessage, DatagramSocket socket) throws IOException {
        System.out.println("Discover Message:\n" + dhcpMessage.toString());

        byte[] offerYIAddr = new byte[] {(byte) 192, (byte) 168, 1, 4};
        byte[] serverId = new byte[] {(byte) 192, (byte) 168, 1, 1};
        byte[] timeLease = Utils.intToBytes(123456);
        byte[] subnetMask = new byte[] {(byte) 255, (byte) 255, (byte) 255, (byte) 255};
        byte[] router = new byte[] {(byte) 192, (byte) 168, 1, 1};
        byte[] dns = new byte[] {8, 8, 8, 8, 4, 4, 4, 4};
        byte[] CLIENT_HARDWARD_ADDRESS = new byte[] {0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC};
        String HOST_NAME = "MY PC1";
        // TODO: 11/22/18 send offer messsage
        DHCPMessage offerMessage = dhcpMessage.createOfferMsg(offerYIAddr, serverId, timeLease, subnetMask, router, dns);
        byte[] buffer = offerMessage.externalize();
        broadcastMessage(buffer, socket, NetworkUtils.listAllBroadcastAddresses(), DHCPClient.CLIENT_PORT);
    }

    private void broadcastMessage(byte[] message, DatagramSocket socket, List<InetAddress> broadcastList, int port) throws IOException {
        socket.setBroadcast(true);
        for(InetAddress address: broadcastList) {
            DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
            socket.send(packet);
        }
    }

    public void testRWObject() throws IOException, ClassNotFoundException {




//        ClientData client1 = new ClientData(CLIENT_HARDWARD_ADDRESS, HOST_NAME, offerYIAddr, 123456);
//        ClientData client2 = new ClientData(CLIENT_HARDWARD_ADDRESS, HOST_NAME, serverId, 654321);
//        System.out.println("Client 1 data:\n" + client1.toString());
//        System.out.println("Client 2 data:\n" + client2.toString());
//
//        clientDataList.add(client1);
//        clientDataList.add(client2);
//
//        saveListClientToFile(clientDataList, "data.txt");
        List<ClientData> list = loadClientDataList("data.txt");

        for(ClientData data: list) {
            ClientData clientData = (ClientData) data;
            System.out.println("Client data:\n" + clientData.toString());
        }
    }

    private void broadcast(
            String broadcastMessage, InetAddress address, int clientPort) throws IOException {
        socket.setBroadcast(true);

        byte[] buffer = broadcastMessage.getBytes();

        DatagramPacket packet
                = new DatagramPacket(buffer, buffer.length, address, clientPort);
        socket.send(packet);
    }

    public boolean isClientExist(byte[] macAddress) {
        for(ClientData client : clientDataList) {
            if (Arrays.equals(client.getMacAddress(), macAddress))
                return true;
        }
        return false;
    }

    public static void saveListClientToFile(List<ClientData> clientDataList, String path) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(path);
        ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
        for(ClientData object: clientDataList) {
            objectOut.writeObject(object);
        }
        objectOut.close();
    }

    public static List<ClientData> loadClientDataList(String path) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(path);
        List<ClientData> clientDataList = new ArrayList<>();
        ObjectInputStream objectIn = new ObjectInputStream(fileInputStream);

        try {
            while (true) {
                ClientData clientData = (ClientData) objectIn.readObject();
                clientDataList.add(clientData);
            }
        } catch (EOFException e) {

        }
        finally {
            objectIn.close();
        }


        return clientDataList;
    }

    public static void main(String[] args) {
        try {
            DHCPServer server = new DHCPServer();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error when init DHCPServer");
        }

    }
}
