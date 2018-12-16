package com.company;

import java.nio.ByteBuffer;
import java.util.Locale;

public class Utils {
    public static byte[] intToBytes(int i){
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    public static int bytesToInt(byte[] data) {
        return ByteBuffer.wrap(data).getInt();
    }

    public static short bytesToShort(byte[] data) {
        return ByteBuffer.wrap(data).getShort();
    }

    public static byte[] shortToByte(short i) {
        return ByteBuffer.allocate(2).putShort(i).array();
    }

    public static String ipToString(byte[] data) {
        return String.format(Locale.US,
                "%d.%d.%d.%d", data[0] & 0xFF, data[1] & 0xFF, data[2] & 0xFF, data[3] & 0xFF);
    }

    public static String macToString(byte[] macAddress) {
        return String.format(Locale.US, "%02X:%02X:%02X:%02X:%02X:%02X",
                macAddress[0], macAddress[1],macAddress[2],macAddress[3],macAddress[4],macAddress[5]);
    }

    public static byte[] strToIp(String strIP) {
        String octet[] = strIP.split("\\.");
        byte[] ip = new byte[4];

        for(int i = 0; i < 4; i++)
            ip[i] = (byte) Integer.parseInt(octet[i]);
        return ip;
    }

    public static byte[] strToMAC(String strMAC) {
        String octet[]  = strMAC.split("\\:");
        byte[] MAC = new byte[6];

        for(int i = 0; i < 6; i++) {
            MAC[i] = (byte) Integer.parseInt(octet[i], 16);
        }

        return MAC;
    }
}
