package com.company.dhcpclient;

import com.company.NetworkUtils;
import com.company.Utils;
import com.company.common.DHCPMessage;
import com.company.common.DHCPOptions;
import com.company.dhcpserver.DHCPServer;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;

public class DHCPClient {

    private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
    public final static int CLIENT_PORT =  1668;//68;
    private final static int TIME_OUT = 5000; //in millis
    public final static byte[] CLIENT_HARDWARD_ADDRESS = new byte[] {0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC};
    private final static String HOST_NAME = "MY PC1";

    public final static int STATE_INIT = 0;
    public final static int STATE_SELECTING = 1;
    public final static int STATE_REQUESTING = 2;
    public final static int STATE_BOUDING = 3;

    private int currentState;
    private boolean isContinue;
    private int myXid;


    public DHCPClient() throws IOException {
        DatagramSocket socket = new DatagramSocket(CLIENT_PORT);

        currentState = STATE_INIT;
        isContinue = true;

        while (isContinue) {
            switch (currentState) {
                case STATE_INIT:
                    DHCPMessage tempMsg = new DHCPMessage();
                    myXid = tempMsg.getXid();
                    DHCPMessage discoverMsg = tempMsg.createDiscoverMsg(CLIENT_HARDWARD_ADDRESS, HOST_NAME.getBytes());
                    System.out.println("Discover Message:\n" + discoverMsg.toString());
                    //send discovery message to server
                    byte[] message_buffer = discoverMsg.externalize();
                    broadcastMessage(message_buffer, socket, NetworkUtils.listAllBroadcastAddresses());
                    currentState = STATE_SELECTING;
                    break;

                case STATE_SELECTING:
                    //waitting for offer
                    socket.setSoTimeout(TIME_OUT);
                    byte[] buffer = new byte[MAX_BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException e) {
                        currentState = STATE_INIT;
                        break;
                    }

                    DHCPMessage offerMessage = new DHCPMessage(packet.getData());
                    System.out.println("OfferMessage:\n" + offerMessage.toString());
                    //get neccessary information
                    byte[] requestIpAddress = offerMessage.getYIAddr().clone();
                    byte[] serverId = offerMessage.getOptions().getOption(DHCPOptions.DHCP_OPTION_SERVER_ID);
                    byte[] timeLease = offerMessage.getOptions().getOption(DHCPOptions.DHCP_OPTION_TIMELEASE);

                    DHCPMessage requestMessage
                            = offerMessage.createRequestMsg(requestIpAddress, serverId, timeLease, HOST_NAME.getBytes());
                    byte[] request_buffer = requestMessage.externalize();
                    broadcastMessage(request_buffer, socket, NetworkUtils.listAllBroadcastAddresses());
                    currentState = STATE_REQUESTING;
                    break;

                case STATE_REQUESTING:
                    //wait ack, nak message
                    socket.setSoTimeout(TIME_OUT);
                    byte[] buffer_ack = new byte[MAX_BUFFER_SIZE];
                    DatagramPacket packet_ack = new DatagramPacket(buffer_ack, buffer_ack.length);
                    try {
                        socket.receive(packet_ack);
                    } catch (SocketTimeoutException e) {
                        currentState = STATE_INIT;
                        break;
                    }

                    DHCPMessage ackMessage = new DHCPMessage(packet_ack.getData());
                    System.out.println("ACKMessage:\n" + ackMessage.toString());
                    int messageType = ackMessage.getOptions().getOption(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE)[0];
                    if (messageType == DHCPOptions.DHCPNAK) {
                        currentState = STATE_INIT;
                        break;
                    } else if (messageType == DHCPOptions.DHCPACK) {
                        byte[] ip = ackMessage.getYIAddr();
                        // TODO: 11/22/18 check ip is can use, in this example, it always be can use
                        currentState = STATE_BOUDING;
                        break;
                    } else {
                        // TODO: 11/22/18 unExpected State, re lease again
                        currentState = STATE_INIT;
                    }
                    break;
                case STATE_BOUDING:
                    System.out.println("Finish Allocation IP Address, open browser and go to favorite website, enjoi!!");
                    isContinue = false;
                    break;
                default:
                        isContinue = false;
                        break;
            }
        }

        System.out.println("End time!!");

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            DHCPClient client = new DHCPClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            DHCPMessage dhcpMessage = new DHCPMessage();
//            byte[] offerYIAddr = new byte[] {(byte) 192, (byte) 168, 1, 4};
//            byte[] serverId = new byte[] {(byte) 192, (byte) 168, 1, 1};
//            byte[] timeLease = Utils.intToBytes(123456);
//            byte[] subnetMask = new byte[] {(byte) 255, (byte) 255, (byte) 255, (byte) 255};
//            byte[] router = new byte[] {(byte) 192, (byte) 168, 1, 1};
//            byte[] dns = new byte[] {8, 8, 8, 8, 4, 4, 4, 4};
//
//            DHCPMessage discoveryMsg = dhcpMessage.createDiscoverMsg(CLIENT_HARDWARD_ADDRESS, HOST_NAME.getBytes());
//            byte[] data = discoveryMsg.externalize();
//            System.out.println(discoveryMsg.toString());

//            client = new DHCPClient();
//            List<InetAddress> brList = NetworkUtils.listAllBroadcastAddresses();
//            for(InetAddress inetAddress: brList) {
//                client.broadcast(data, inetAddress);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void broadcastMessage(byte[] message, DatagramSocket socket, List<InetAddress> broadcastList) throws IOException {
        socket.setBroadcast(true);
        for(InetAddress address: broadcastList) {
            DatagramPacket packet = new DatagramPacket(message, message.length, address, DHCPServer.SERVER_PORT);
            socket.send(packet);
        }
    }


//    private void broadcast(
//            byte[] buffer, InetAddress address) throws IOException {
//        socket.setBroadcast(true);
//
//        DatagramPacket packet
//                = new DatagramPacket(buffer, buffer.length, address, DHCPServer.SERVER_PORT);
//        socket.send(packet);
//    }
}
