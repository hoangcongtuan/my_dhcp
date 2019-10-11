package com.company;

import com.company.common.DHCPOptions;

import java.util.HashMap;

public class Constants {
    public static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes

    public final static HashMap<Integer, String> OPTION_TABLE = new HashMap<Integer, String>(){{
        put(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE, "DHCP Message Type");
        put(DHCPOptions.DHCP_OPTION_SERVER_ID, "DHCP Server Id");
        put(DHCPOptions.DHCP_OPTION_TIMELEASE, "IP Address Lease Time");
        put(DHCPOptions.DHCP_OPTION_SUBNET_MASK, "Subnet Mask");
        put(DHCPOptions.DHCP_OPTION_ROUTER, "Router");
        put(DHCPOptions.DHCP_OPTION_DNS, "DNS");
        put(DHCPOptions.DHCP_OPTIONS_HOST_NAME, "Host Name");
        put(DHCPOptions.DHCP_OPTION_REQUEST_IP, "Request IP");
    }};

    public final static HashMap<Integer, String> MESSAGE_TYPE = new HashMap<Integer, String>(){{
        put(DHCPOptions.DHCPDISCOVER, "DHCPDISCOVER");
        put(DHCPOptions.DHCPOFFER, "DHCPOFFER");
        put(DHCPOptions.DHCPREQUEST, "DHCPREQUEST");
        put(DHCPOptions.DHCPDECLINE, "DHCPDECLINE");
        put(DHCPOptions.DHCPACK, "DHCPACK");
        put(DHCPOptions.DHCPNAK, "DHCPNAK");
        put(DHCPOptions.DHCPRELEASE, "DHCPRELEASE");
    }};
}
