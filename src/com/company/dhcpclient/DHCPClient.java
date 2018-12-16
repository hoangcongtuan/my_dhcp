package com.company.dhcpclient;

import com.company.NetworkUtils;
import com.company.Utils;
import com.company.common.DHCPMessage;
import com.company.common.DHCPOptions;
import com.company.dhcpserver.DHCPServer;
import com.company.model.ClientData;

import java.io.*;
import java.net.*;
import java.util.List;

public class DHCPClient {

    private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
    public final static int CLIENT_PORT =  1668;//68;
    private final static int TIME_OUT = 5000; //in millis

    public final static int STATE_INIT = 0;
    public final static int STATE_SELECTING = 1;
    public final static int STATE_REQUESTING = 2;
    public final static int STATE_BOUDING = 3;

    private int currentState;
    private boolean isContinue;
    private int myXid;

    ClientData mConfig;


    public DHCPClient() throws IOException {
        DatagramSocket socket = new DatagramSocket(CLIENT_PORT);

        currentState = STATE_INIT;
        isContinue = true;

        mConfig = loadMyConfig("clientConfig.txt");

        while (isContinue) {
            switch (currentState) {
                case STATE_INIT:
                    DHCPMessage tempMsg = new DHCPMessage();
                    myXid = tempMsg.getXid();
                    DHCPMessage discoverMsg = tempMsg.createDiscoverMsg(
                            mConfig.getMacAddress(),
                            mConfig.getHostName().getBytes(),
                            mConfig.getIpAddress());
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
                            = offerMessage.createRequestMsg(requestIpAddress, serverId, timeLease, mConfig.getHostName().getBytes());
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
                        byte[] bufferIP = ackMessage.getYIAddr();
                        byte[] bufferTimeLease = ackMessage.getOptions().getOption(DHCPOptions.DHCP_OPTION_TIMELEASE);
                        // TODO: 11/22/18 check ip is can use, in this example, it always be can use
                        currentState = STATE_BOUDING;
                        mConfig.setIpAddress(bufferIP);
                        mConfig.setLeaseTimeRemain(Utils.bytesToInt(bufferTimeLease));
                        break;
                    } else {
                        // TODO: 11/22/18 unExpected State, re lease again
                        currentState = STATE_INIT;
                    }
                    break;

                case STATE_BOUDING:
                    saveMyConfig("clientConfig.txt", mConfig);
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

    private ClientData loadMyConfig(String path) throws IOException {
        ClientData myConfig = new ClientData();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = null;
        if ((line = br.readLine()) != null) {
            byte[] macAddress = Utils.strToMAC(line);
            String hostName = br.readLine();
            byte[] ipAddress = Utils.strToIp(br.readLine());
            int timeLeaseRemain = Integer.parseInt(br.readLine());

            myConfig.setMacAddress(macAddress);
            myConfig.setHostName(hostName);
            myConfig.setIpAddress(ipAddress);
            myConfig.setLeaseTimeRemain(timeLeaseRemain);
        }

        br.close();
        return myConfig;
    }

    private void saveMyConfig(String path, ClientData config) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(path));
        String line = null;
        String strMac = Utils.macToString(config.getMacAddress());
        String strHostName = config.getHostName();
        String strIP = Utils.ipToString(config.getIpAddress());
        String strTimeLease = String.valueOf(config.getLeaseTimeRemain());

        bw.write(strMac);
        bw.newLine();
        bw.write(strHostName);
        bw.newLine();
        bw.write(strIP);
        bw.newLine();
        bw.write(strTimeLease);

        bw.close();
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
    }

    private void broadcastMessage(byte[] message, DatagramSocket socket, List<InetAddress> broadcastList) throws IOException {
        socket.setBroadcast(true);
        for(InetAddress address: broadcastList) {
            DatagramPacket packet = new DatagramPacket(message, message.length, address, DHCPServer.SERVER_PORT);
            socket.send(packet);
        }
    }
}
