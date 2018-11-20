package com.company.common;

import com.company.Constants;
import com.company.Utils;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Locale;

public class DHCPOptions {
    //DHCP Message Types
    public static final int DHCPDISCOVER = 1;
    public static final int DHCPOFFER = 2;
    public static final int DHCPREQUEST = 3;
    public static final int DHCPDECLINE = 4;
    public static final int DHCPACK = 5;
    public static final int DHCPNAK = 6;
    public static final int DHCPRELEASE = 7;

    //DHCP Option Identifiers
    public static final int DHCP_OPTIONS_MESSAGE_TYPE = 53;
    public static final int DHCP_OPTION_SERVER_ID = 54;
    public static final int DHCP_OPTION_TIMELEASE = 51;
    public static final int DHCP_OPTION_SUBNET_MASK = 1;
    public static final int DHCP_OPTION_ROUTER = 3;
    public static final int DHCP_OPTION_DNS = 6;
    public static final int DHCP_OPTIONS_HOST_NAME = 12;
    public static final int DHCP_OPTION_REQUEST_IP = 50;

    //private LinkedList<byte[]> options = new LinkedList<byte[]>();
    private Hashtable<Integer,byte[]> options;

    public DHCPOptions() {
        options = new Hashtable<Integer, byte[]>();
    }

    public DHCPOptions(byte[] data) {
        options = new Hashtable<Integer, byte[]>();
        int i = 0;
        while (true) {
            int id = data[i];
            if (id == 0 || i  >= data.length)
                break;
            int lenght = data[i + 1];
            byte[] values = Arrays.copyOfRange(data, i + 2, i + 2 + lenght);
            options.put(id, values);
            i += 2 + lenght;
        }
    }

    public void reset() {
        options.clear();
    }

    public byte[] getOption(int optionID) {
        return options.get(optionID);
    }

    public void setOption(int optionID, byte[] option) {
        options.put(optionID, option);
    }

    public byte[] getOptionData(int optionID) {
        byte[] option = options.get(optionID);
        byte[] optionData = new byte[option.length-2];
        for (int i=0; i < optionData.length; i++)  optionData[i] = option[2+i];
        return optionData;
    }

    public void putOptionData(int optionID, byte[] optionData) {
        options.put(optionID, optionData);
//        byte[] option = new byte[2+optionData.length];
//        option[0] = (byte) optionID;
//        option[1] = (byte) optionData.length;
//        for (int i=0; i < optionData.length; i++) option[2+i] = optionData[i];
//        options.put(optionID, option);
    }

    public void printOption (int optionID) {
        String output = new String("");
        if (options.get(optionID) != null) {
            byte[] option = options.get(optionID);
            for (int i=0; i < option.length; i++) {
                output += option[i]  +
                        (i == option.length-1 ? "" : ",");
            }
        } else {
            output = "<Empty>";
        }
        System.out.println(output);
    }

    public void printOptions () {
        for (byte[] option : options.values()) {
            printOption(option[0]);
        }
    }



    public static void main (String[] args) {
        DHCPOptions test = new DHCPOptions();

        //test.printOptions();
    }

    public byte[] externalize() {
        //get size
        int totalBytes = 0;
        for (byte[] option : this.options.values()) {
            totalBytes += option.length + 2;
        }

        byte[] option_data = new byte[totalBytes];

//        //copy bytes
//        int index = 0;
//        for (byte[] option : this.options.values()) {
//            System.arraycopy(option, 0, option_data, index, option.length);
//            index += option.length;
//        }

        int index = 0;
        for(int id :this.options.keySet()) {
            //id:lenght:[data]
            byte[] value = this.options.get(id);
            byte[] option = new byte[2 + value.length];
            option[0] = (byte) id;
            option[1] = (byte) value.length;
            System.arraycopy(value, 0, option, 2, value.length);
            System.arraycopy(option, 0, option_data, index, option.length);
            index += option.length;

        }
        return option_data;
    }

    @Override
    public String toString() {
        String result = "";
        for(int key: options.keySet()) {
            byte[] value = options.get(key);
            switch (key) {
                case DHCP_OPTIONS_MESSAGE_TYPE:
                    int msg_type = Utils.bytestoint(value);
                    result += String.format(Locale.US, "Option(%d): %s(%s):\n", key, Constants.OPTION_TABLE.get(key),
                            Constants.MESSAGE_TYPE.get(msg_type));
                    result += String.format(Locale.US, "\tLength: %d\n", value.length);
                    result += String.format(Locale.US, "\tDHCP: %s (%d)\n", Constants.MESSAGE_TYPE.get(msg_type), msg_type);
                    break;

                case DHCP_OPTION_SERVER_ID: case DHCP_OPTION_SUBNET_MASK: case DHCP_OPTION_ROUTER: case DHCP_OPTION_DNS: case DHCP_OPTION_REQUEST_IP:
                    String strIp = Utils.ipToString(value);
                    result += String.format(Locale.US, "Option(%d): %s:\n", key, Constants.OPTION_TABLE.get(key));
                    result += String.format(Locale.US, "\tLength: %d\n", value.length);
                    result += String.format(Locale.US, "\t%s: %s\n", Constants.OPTION_TABLE.get(key), strIp);
                    break;

                case DHCP_OPTION_TIMELEASE:
                    int timeLease = Utils.bytestoint(value);
                    result += String.format(Locale.US, "Option(%d): %s:\n", key, Constants.OPTION_TABLE.get(key));
                    result += String.format(Locale.US, "\tLength: %d\n", value.length);
                    result += String.format(Locale.US, "\tIP Time Lease: %d\n", timeLease);
                    break;

                case DHCP_OPTIONS_HOST_NAME:
                    String hostName = new String(value);
                    result += String.format(Locale.US, "Option(%d): %s:\n", key, Constants.OPTION_TABLE.get(key));
                    result += String.format(Locale.US, "\tLength: %d\n", value.length);
                    result += String.format(Locale.US, "\tHost Name: %s\n", hostName);
                    break;

                    default:
                        result += "Uknown\n";
            }
        }
        return result;
    }
}
