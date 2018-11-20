package com.company;

import java.nio.ByteBuffer;
import java.util.Locale;

public class Utils {
    public static byte[] inttobytes(int i){
        return ByteBuffer.allocate(4).putInt(i).array();
//        byte[] dword = new byte[4];
//        dword[0] = (byte) ((i >> 24) & 0x000000FF);
//        dword[1] = (byte) ((i >> 16) & 0x000000FF);
//        dword[2] = (byte) ((i >> 8) & 0x000000FF);
//        dword[3] = (byte) (i & 0x00FF);
//        return dword;
    }

    public static int bytestoint(byte[] data) {
        return ByteBuffer.wrap(data).getInt();
    }8

    public static byte[] shorttobytes(short i){
        byte[] b = new byte[2];
        b[0] = (byte) ((i >> 8) & 0x000000FF);
        b[1] = (byte) (i & 0x00FF);
        return b;

    }

    public static short bytestoshort(byte[] data) {
        return (short) (data[0] << 8 | data[1]);
    }

    public static String ipToString(byte[] data) {
        return String.format(Locale.US,
                "%d.%d.%d.%d", data[0] & 0xFF, data[1] & 0xFF, data[2] & 0xFF, data[3] & 0xFF);
    }

    public static String macToString(byte[] macAddress) {
        return String.format(Locale.US, "%02X:%02X:%02X:%02X:%02X:%02X",
                macAddress[0], macAddress[1],macAddress[2],macAddress[3],macAddress[4],macAddress[5]);
    }

}
