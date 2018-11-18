package com.company;

public class Utils {
    public static byte[] inttobytes(int i){
        byte[] dword = new byte[4];
        dword[0] = (byte) ((i >> 24) & 0x000000FF);
        dword[1] = (byte) ((i >> 16) & 0x000000FF);
        dword[2] = (byte) ((i >> 8) & 0x000000FF);
        dword[3] = (byte) (i & 0x00FF);
        return dword;
    }

    public static int bytestoint(byte[] data) {
        return data[0] << 24 | data[1] << 16 | data[2] << 8 | data[3];
    }

    public static byte[] shorttobytes(short i){
        byte[] b = new byte[2];
        b[0] = (byte) ((i >> 8) & 0x000000FF);
        b[1] = (byte) (i & 0x00FF);
        return b;
    }

    public static short bytestoshort(byte[] data) {
        return (short) (data[0] << 8 | data[1]);
    }
}
