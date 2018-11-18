package com.company.dhcpclient;

import com.company.NetworkUtils;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;

public class DHCPClient {

    private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
    private int listenPort =  1338;//1338;
    private int serverPort =  1337;//1337;
    private DatagramSocket socket;

    /*
     * public DHCPClient(int servePort) { listenPort = servePort; new
     * DHCPServer(); }
     */

    public DHCPClient() throws SocketException {

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        DHCPClient client;
        /*
         * if (args.length >= 1) { server = new
         * DHCPClient(Integer.parseInt(args[0])); } else {
         */
        try {
            client = new DHCPClient();
            List<InetAddress> brList = NetworkUtils.listAllBroadcastAddresses();
            for(InetAddress inetAddress: brList) {
                client.broadcast("Hello", inetAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(
            String broadcastMessage, InetAddress address) throws IOException {
        socket = new DatagramSocket();
        socket.setBroadcast(true);

        byte[] buffer = broadcastMessage.getBytes();

        DatagramPacket packet
                = new DatagramPacket(buffer, buffer.length, address, serverPort);
        socket.send(packet);
        socket.close();
    }

    public static byte[] getMacAddress() {
        byte[] mac = null;
        try {
            InetAddress address = InetAddress.getLocalHost();

            /*
             * Get NetworkInterface for the current host and then read the
             * hardware address.
             */
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            mac = ni.getHardwareAddress();


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        assert(mac != null);
        return mac;
    }

    public static void printMacAddress() {
        try {
            InetAddress address = InetAddress.getLocalHost();

            /*
             * Get NetworkInterface for the current host and then read the
             * hardware address.
             */
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            byte[] mac = ni.getHardwareAddress();

            /*
             * Extract each array of mac address and convert it to hexa with the
             * . * following format 08-00-27-DC-4A-9E.
             */
            for (int i = 0; i < mac.length; i++) {
                System.out.format("%02X%s", mac[i], (i < mac.length - 1) ? ":"
                        : "");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
