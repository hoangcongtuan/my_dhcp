package com.company.dhcpserver;

import com.company.Constants;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DHCPServer {
    public static final int SERVER_PORT = 1667;//67;

    private static byte[] START_IP = new byte[] {(byte) 192, (byte) 168, 1, 2};
    private static byte[] END_IP = new byte[] {(byte) 192, (byte) 168, 1, (byte) 254};
    byte[] SUBNET_MASK = new byte[] {(byte) 255, (byte) 255, (byte) 255, (byte) 255};
    byte[] ROUTER = new byte[] {(byte) 192, (byte) 168, 1, 1};
    byte[] DNS = new byte[] {8, 8, 8, 8, 4, 4, 4, 4};
    byte[] SERVER_IP = new byte[] {(byte) 192, (byte) 168, 1, 1};

    DatagramSocket socket;

    //client data
    List<ClientData> clientDataList;

    public DHCPServer() throws IOException {
        DatagramSocket socket = new DatagramSocket(SERVER_PORT);
        byte[] buffer = new byte[Constants.MAX_BUFFER_SIZE];

        clientDataList = loadClientDataList("data.txt");

        boolean isContinue = true;
        while (isContinue) {
            Arrays.fill(buffer, (byte) 0);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            DHCPMessage dhcpMessage = new DHCPMessage(packet.getData());
            int msgType = dhcpMessage.getOptions().getOption(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE)[0];
            switch (msgType) {
                case DHCPOptions.DHCPDISCOVER:
                    processDiscoverMessage(dhcpMessage, socket);
                    break;

                case DHCPOptions.DHCPREQUEST:
                    byte[] targetServerIp = dhcpMessage.getOptions().getOption(DHCPOptions.DHCP_OPTION_SERVER_ID);

                    if (!Utils.ipToString(targetServerIp).equals(Utils.ipToString(SERVER_IP)))
                        break;

                    System.out.println("Request Message:\n" + dhcpMessage.toString());

                    DHCPMessage ackMessage = dhcpMessage.createACKMsg(SUBNET_MASK, ROUTER, DNS);
                    System.out.println("ACK Message:\n" + ackMessage.toString());
                    byte[] ack_buffer = ackMessage.externalize();
                    broadcastMessage(ack_buffer, socket, NetworkUtils.listAllBroadcastAddresses(), DHCPClient.CLIENT_PORT);

                    //update client table
                    updateClientTable(dhcpMessage.getCHAddr(),
                            dhcpMessage.getOptions().getOption(DHCPOptions.DHCP_OPTIONS_HOST_NAME),
                            dhcpMessage.getOptions().getOption(DHCPOptions.DHCP_OPTION_REQUEST_IP),
                            dhcpMessage.getOptions().getOption(DHCPOptions.DHCP_OPTION_TIMELEASE));
                    break;

                    default:
                        isContinue = false;
                        break;
            }
        }
        System.out.println("End Time!!");
    }

    private void updateClientTable(byte[] chAddr, byte[] hostName, byte[] requestIP, byte[] bufTimeLease) throws IOException {
        String strMac = Utils.macToString(chAddr);
        boolean isExist = false;
        for(ClientData data: clientDataList) {
            String str1 = Utils.macToString(data.getMacAddress());
            if (str1.equals(strMac)) {
                String strHostName = new String(hostName).trim();
                data.setHostName(strHostName);
                data.setIpAddress(requestIP.clone());
                int timeLease = Utils.bytesToInt(bufTimeLease);
                data.setLeaseTimeRemain(timeLease);
                isExist = true;
            }
        }

        if (!isExist) {
            ClientData newClient = new ClientData();
            newClient.setMacAddress(chAddr.clone());
            String strHostName = new String(hostName).trim();
            newClient.setHostName(strHostName);
            newClient.setIpAddress(requestIP.clone());
            int timeLease = Utils.bytesToInt(bufTimeLease);
            newClient.setLeaseTimeRemain(timeLease);

            clientDataList.add(clientDataList.size(), newClient);
        }

        //write to disk
        saveListClientToFile(clientDataList, "data.txt");
    }

    private byte[] generateIPAddress(byte[] MACAddress) {
        //check mac address
        for(ClientData data: clientDataList) {
            String str1 = Utils.macToString(MACAddress);
            String str2 = Utils.macToString(data.getMacAddress());

            if (str1.equals(str2)) {
                return data.getIpAddress();
            }
        }

        int start = START_IP[3] & 0xFF;
        int end = END_IP[3] & 0xFF;

        for(int i = start; i < end; i++) {
            byte[] genIP = new byte[]{0, 0, 0, 0};
            genIP[0] = START_IP[0];
            genIP[1] = START_IP[1];
            genIP[2] = START_IP[2];
            genIP[3] = (byte) i;
            boolean isFree = true;
            for(ClientData data: clientDataList) {
                if (Utils.ipToString(genIP)
                        .equals(Utils.ipToString(data.getIpAddress()))){
                    isFree = false;
                    break;
                }
            }

            if (isFree)
                return genIP;
        }

        return null;
    }

    private void processDiscoverMessage(DHCPMessage dhcpMessage, DatagramSocket socket) throws IOException {
        System.out.println("Discover Message:\n" + dhcpMessage.toString());

        byte[] MACAddress = dhcpMessage.getCHAddr();
        byte[] requestIP = dhcpMessage.getOptions().getOption(DHCPOptions.DHCP_OPTION_REQUEST_IP);
        byte[] offerYIAddr;
        if (IPisValidAndNotExits(requestIP)) {
            offerYIAddr = requestIP.clone();
            System.out.println("Accept request IP");
        }
        else {
            offerYIAddr = generateIPAddress(MACAddress);
            System.out.println("IP is Exist");
        }

        if (offerYIAddr == null)
            return;

        byte[] timeLease = Utils.intToBytes(123456);

        DHCPMessage offerMessage = dhcpMessage.createOfferMsg(offerYIAddr, SERVER_IP, timeLease, SUBNET_MASK, ROUTER, DNS);
        byte[] buffer = offerMessage.externalize();
        broadcastMessage(buffer, socket, NetworkUtils.listAllBroadcastAddresses(), DHCPClient.CLIENT_PORT);
    }



    private boolean IPisValidAndNotExits(byte[] requestIP) {
        //check in range
        if (requestIP[0] != START_IP[0])
            return false;
        if (requestIP[1] != START_IP[1])
            return false;
        if (requestIP[2] != START_IP[2])
            return false;

        int octetIP = requestIP[3] & 0xFF;
        int octetSTART= START_IP[3] & 0xFF;
        int octetEND = END_IP[3] & 0xFF;
        if (octetIP < octetSTART || octetIP > octetEND)
            return false;

        for(ClientData data: clientDataList) {
            String str1 = Utils.ipToString(requestIP);
            String str2 = Utils.ipToString(data.getIpAddress());
            if (str1.equals(str2))
                return false;
        }
        return true;
    }

    private void broadcastMessage(byte[] message, DatagramSocket socket, List<InetAddress> broadcastList, int port) throws IOException {
        socket.setBroadcast(true);
        for(InetAddress address: broadcastList) {
            DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
            socket.send(packet);
        }
    }

    public static void saveListClientToFile(List<ClientData> clientDataList, String path) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(path));
        for(ClientData data: clientDataList) {
            bw.write(data.toString());
        }
        bw.close();
    }

    public static List<ClientData> loadClientDataList(String path) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(path));
        List<ClientData> clientDataList = new ArrayList<>();

        String line = null;
        while((line = br.readLine()) != null) {
            byte[] MACAddress = Utils.strToMAC(line);
            String hostName = br.readLine();
            byte[] ipAddress = Utils.strToIp(br.readLine());
            int timeLeaseRemain = Integer.parseInt(br.readLine());

            ClientData clientData = new ClientData(MACAddress, hostName, ipAddress, timeLeaseRemain);
            clientDataList.add(clientData);
        }
        br.close();

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
