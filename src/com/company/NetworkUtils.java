package com.company;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class NetworkUtils {
    public static List<InetAddress> listAllBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces
                = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(a -> a.getBroadcast())
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
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
