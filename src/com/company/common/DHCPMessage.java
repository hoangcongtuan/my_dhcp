package com.company.common;

import com.company.Utils;

import java.util.Arrays;

public class DHCPMessage {
    private static final int BOOTREQUEST = 1;
    private static final int BOOTREPLY = 2;
    private static final int DHCPREQUEST = 1;
    private static final int DHCPREPLY = 2;
    private static final int ETHERNET10MB = 1;

    //Operation Code:
    //Specifies the general type of message
    private byte op;

    //Hardware Type:
    //Specifies the type of hardware used for the local network
    private byte hType;

    //Hardware Address Length:
    //Specifies how long hardware addresses are in this message.
    private byte hLen;

    //Hops:
    private byte hops;

    //Transaction Identifier: (32-bit)
    //Identification field generated by client
    //private byte[] xid = new byte[3];
    private int xid;

    //Seconds: (16-bit)
    //Number of seconds elapsed since a client began an attempt to acquire or renew a lease.
    //private byte[] secs = new byte[1];
    private short secs;

    //Flags: (16-bit)
    //1bit broadcast flag (0-1)
    //15 bit reserverd
    //private byte[] flags = new byte[1];
    private short flags;

    //Client IP Address: (32-bit)
    private byte[] cIAddr;
    //private InetAddress cIAddr = new Inet4Address();

    //"Your" IP Address: (32-bit)
    private byte[] yIAddr;
    //Server IP Address: (32-bit)
    private byte[] sIAddr;
    //Gateway IP Address: (32-bit)
    private byte[] gIAddr;

    //Client Hardware Address: (128-bit : 16 bytes)
    private byte[] cHAddr;

    //Server Name: (512-bit : 64 bytes)
    private byte[] sName;

    //Boot Filename: (1024-bit : 128 bytes)
    private byte[] file;

    //Options: (variable)
    private DHCPOptions options;

    public DHCPMessage() {
        cIAddr = new byte[4];
        yIAddr = new byte[4];
        sIAddr = new byte[4];
        gIAddr = new byte[4];
        cHAddr = new byte[16];
        sName = new byte[64];
        file = new byte[128];
        options = new DHCPOptions();
    }

    public DHCPMessage(DHCPMessage msg) {
        this.op = msg.op;
        this.hType = msg.hType;
        this.hLen = msg.hLen;
        this.hops = msg.hops;
        this.xid = msg.xid;
        this.secs = msg.secs;
        this.flags = msg.flags;

        this.cIAddr = msg.cIAddr.clone();
        this.yIAddr = msg.yIAddr.clone();
        this.sIAddr = msg.sIAddr.clone();
        this.gIAddr = msg.gIAddr.clone();
        this.cHAddr = msg.cHAddr.clone();
        this.sName = msg.sName.clone();
        this.file = msg.file.clone();
        this.options = new DHCPOptions(msg.options);
    }


    public DHCPMessage(byte[] data) {
        cIAddr = new byte[4];
        yIAddr = new byte[4];
        sIAddr = new byte[4];
        gIAddr = new byte[4];
        cHAddr = new byte[16];
        sName = new byte[64];
        file = new byte[128];
        options = new DHCPOptions();

        this.op = data[0];
        this.hType = data[1];
        this.hLen = data[2];
        this.hops = data[3];

        this.xid = Utils.bytesToInt(Arrays.copyOfRange(data, 4, 8));
        this.secs = Utils.bytesToShort(Arrays.copyOfRange(data, 8, 10));
        this.flags = Utils.bytesToShort(Arrays.copyOfRange(data, 10, 12));

        this.cIAddr = Arrays.copyOfRange(data, 12, 16);
        this.yIAddr = Arrays.copyOfRange(data, 16, 20);
        this.sIAddr = Arrays.copyOfRange(data, 20, 24);
        this.gIAddr = Arrays.copyOfRange(data, 24, 28);
        this.cHAddr = Arrays.copyOfRange(data, 28, 44);
        this.sName = Arrays.copyOfRange(data, 44, 108);
        this.file = Arrays.copyOfRange(data, 108, 236);

        this.options = new DHCPOptions(Arrays.copyOfRange(data, 236, data.length));
    }

    public DHCPMessage createDiscoverMsg(byte[] cMacAddress, byte[] hostName) {
        DHCPMessage discoverMsg = new DHCPMessage(this);
        discoverMsg.op = DHCPREQUEST;
        discoverMsg.hType = ETHERNET10MB; // (0x1) 10Mb Ethernet
        discoverMsg.hLen = 6; // (0x6)
        discoverMsg.hops = 0; // (0x0)
        discoverMsg.xid = 556223005; // (0x21274A1D)
        discoverMsg.secs = 0;  // (0x0)
        discoverMsg.flags = 0; // (0x0)
        // DHCP: 0............... = No Broadcast

        Arrays.fill(discoverMsg.cIAddr, (byte) 0);
        Arrays.fill(discoverMsg.yIAddr, (byte) 0);
        Arrays.fill(discoverMsg.sIAddr, (byte) 0);
        Arrays.fill(discoverMsg.gIAddr, (byte) 0);
        Arrays.fill(discoverMsg.cHAddr, (byte) 0);
        System.arraycopy(cMacAddress, 0, discoverMsg.cHAddr, 0, cMacAddress.length);

        byte[] dhcpOptions_msgType = new byte[1];
        dhcpOptions_msgType[0] = DHCPOptions.DHCPDISCOVER;
        discoverMsg.options.putOptionData(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE, dhcpOptions_msgType);
        discoverMsg.options.putOptionData(DHCPOptions.DHCP_OPTIONS_HOST_NAME, hostName);

        return discoverMsg;
    }

    public DHCPMessage createOfferMsg(byte[] offerYIAddr, byte[] serverId, byte[] timeLease,
                                      byte[] subnetMask, byte[] router, byte[] dns) {
        DHCPMessage offerMsg = new DHCPMessage(this);
        offerMsg.op = DHCPREPLY;
        System.arraycopy(offerYIAddr, 0, offerMsg.yIAddr, 0, offerYIAddr.length);

        offerMsg.options.reset();
        byte[] dhcpOptions_msgType = new byte[1];
        dhcpOptions_msgType[0] = DHCPOptions.DHCPOFFER;
        offerMsg.options.putOptionData(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE, dhcpOptions_msgType);
        offerMsg.options.putOptionData(DHCPOptions.DHCP_OPTION_SERVER_ID, serverId);
        offerMsg.options.putOptionData(DHCPOptions.DHCP_OPTION_TIMELEASE, timeLease);
        offerMsg.options.putOptionData(DHCPOptions.DHCP_OPTION_SUBNET_MASK, subnetMask);
        offerMsg.options.putOptionData(DHCPOptions.DHCP_OPTION_ROUTER, router);
        offerMsg.options.putOptionData(DHCPOptions.DHCP_OPTION_DNS, dns);

        return offerMsg;
    }

    public DHCPMessage createRequestMsg(byte[] requestIPAddr, byte[] serverId, byte[] timeLease, byte[] hostName) {
        DHCPMessage requestMsg = new DHCPMessage(this);
        requestMsg.op = DHCPREQUEST;

        requestMsg.options.reset();
        byte[] dhcpOptions_msgType = new byte[1];
        dhcpOptions_msgType[0] = DHCPOptions.DHCPREQUEST;
        requestMsg.options.putOptionData(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE, dhcpOptions_msgType);
        requestMsg.options.putOptionData(DHCPOptions.DHCP_OPTION_REQUEST_IP, requestIPAddr);
        requestMsg.options.putOptionData(DHCPOptions.DHCP_OPTION_SERVER_ID, serverId);
        requestMsg.options.putOptionData(DHCPOptions.DHCP_OPTION_TIMELEASE, timeLease);
        requestMsg.options.putOptionData(DHCPOptions.DHCP_OPTIONS_HOST_NAME, hostName);

        return requestMsg;
    }

    public DHCPMessage createACKMsg(byte[] subnetMask, byte[] router, byte[] dns) {
        DHCPMessage ackMsg = new DHCPMessage(this);
        ackMsg.op = DHCPREPLY;

        byte[] dhcpOptions_msgType = new byte[1];
        dhcpOptions_msgType[0] = DHCPOptions.DHCPACK;
        ackMsg.options.putOptionData(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE, dhcpOptions_msgType);
        ackMsg.options.putOptionData(DHCPOptions.DHCP_OPTION_SUBNET_MASK, subnetMask);
        ackMsg.options.putOptionData(DHCPOptions.DHCP_OPTION_ROUTER, router);
        ackMsg.options.putOptionData(DHCPOptions.DHCP_OPTION_DNS, dns);

        return ackMsg;
    }

    public DHCPMessage createNAKMsg(byte[] subnetMask, byte[] router, byte[] dns) {
        DHCPMessage nakMsg = new DHCPMessage(this);
        nakMsg.op = DHCPREPLY;

        byte[] dhcpOptions_msgType = new byte[1];
        dhcpOptions_msgType[0] = DHCPOptions.DHCPNAK;
        nakMsg.options.putOptionData(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE, dhcpOptions_msgType);
        return nakMsg;
    }

    public DHCPMessage createDECLINEMsg(byte[] subnetMask, byte[] router, byte[] dns) {
        DHCPMessage declineMsg = new DHCPMessage(this);
        declineMsg.op = DHCPREQUEST;

        byte[] dhcpOptions_msgType = new byte[1];
        dhcpOptions_msgType[0] = DHCPOptions.DHCPDECLINE;
        declineMsg.options.putOptionData(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE, dhcpOptions_msgType);
        return declineMsg;
    }

    public DHCPMessage createRELEASEMsg(byte[] subnetMask, byte[] router, byte[] dns) {
        DHCPMessage releaseMsg = new DHCPMessage(this);
        releaseMsg.op = DHCPREQUEST;

        byte[] dhcpOptions_msgType = new byte[1];
        dhcpOptions_msgType[0] = DHCPOptions.DHCPRELEASE;
        releaseMsg.options.putOptionData(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE, dhcpOptions_msgType);
        return releaseMsg;
    }

    /**
     * Converts a DHCPMessage object to a byte array.
     *
     * @return a byte array with information from DHCPMessage object.
     */
    public byte[] externalize() {
        int staticSize = 236;
        byte[] options = this.options.externalize();
        int size = staticSize + options.length;
        byte[] msg = new byte[size];

        //add each field to the msg array
        //single bytes
        msg[0] = this.op;
        msg[1] = this.hType;
        msg[2] = this.hLen;
        msg[3] = this.hops;

        //add multibytes
        for (int i = 0; i < 4; i++) msg[4 + i] = Utils.intToBytes(xid)[i];
        for (int i = 0; i < 2; i++) msg[8 + i] = Utils.shortToByte(secs)[i];
        for (int i = 0; i < 2; i++) msg[10 + i] = Utils.shortToByte(flags)[i];
        for (int i = 0; i < 4; i++) msg[12 + i] = cIAddr[i];
        for (int i = 0; i < 4; i++) msg[16 + i] = yIAddr[i];
        for (int i = 0; i < 4; i++) msg[20 + i] = sIAddr[i];
        for (int i = 0; i < 4; i++) msg[24 + i] = gIAddr[i];
        for (int i = 0; i < cHAddr.length; i++) msg[28 + i] = cHAddr[i];
        for (int i = 0; i < 64; i++) msg[44 + i] = sName[i];
        for (int i = 0; i < 128; i++) msg[108 + i] = file[i];

        //add options
        for (int i = 0; i < options.length; i++) msg[staticSize + i] = options[i];

        return msg;
    }

    public byte getOp() {
        return op;
    }

    public void setOp(byte op) {
        this.op = op;
    }

    public byte getHType() {
        return hType;
    }

    public void setHType(byte type) {
        hType = type;
    }

    public byte getHLen() {
        return hLen;
    }

    public void setHLen(byte len) {
        hLen = len;
    }

    public byte getHops() {
        return hops;
    }

    public void setHops(byte hops) {
        this.hops = hops;
    }

    public int getXid() {
        return xid;
    }

    public void setXid(int xid) {
        this.xid = xid;
    }

    public short getSecs() {
        return secs;
    }

    public void setSecs(short secs) {
        this.secs = secs;
    }

    public short getFlags() {
        return flags;
    }

    public void setFlags(short flags) {
        this.flags = flags;
    }

    public byte[] getCIAddr() {
        return cIAddr;
    }

    public void setCIAddr(byte[] addr) {
        cIAddr = addr;
    }

    public byte[] getYIAddr() {
        return yIAddr;
    }

    public void setYIAddr(byte[] addr) {
        yIAddr = addr;
    }

    public byte[] getSIAddr() {
        return sIAddr;
    }

    public void setSIAddr(byte[] addr) {
        sIAddr = addr;
    }

    public byte[] getGIAddr() {
        return gIAddr;
    }

    public void setGIAddr(byte[] addr) {
        gIAddr = addr;
    }

    public byte[] getCHAddr() {
        return cHAddr;
    }

    public void setCHAddr(byte[] addr) {
        cHAddr = addr;
    }

    public byte[] getSName() {
        return sName;
    }

    public void setSName(byte[] name) {
        sName = name;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public byte[] getOptions() {
        return options.externalize();
    }

    //no set options yet...
	/*public void setOptions(byte[] options) {
		this.options = options;
	}*/

    public void printMessage() {
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        String msg = new String();

        msg += "Operation Code: " + (this.op == 1 ? "Request(1)" : "Reply(2)") + "\n";
        msg += "Hardware Type: " + this.hType + "\n";
        msg += "Hardware Length: " + this.hLen + "\n";
        msg += "Hops: " + this.hops + "\n";

        msg += "xID: " + Integer.toString(xid) + "\n";
        msg += "Secs: " + Short.toString(secs) + "\n";
        msg += "Flag: " + Short.toString(flags) + "\n";
        msg += "Client IP Adress: " + Utils.ipToString(cIAddr) + "\n";
        msg += "Your Ip Adress: " + Utils.ipToString(yIAddr) + "\n";
        msg += "Server IP Adress: " + Utils.ipToString(sIAddr) + "\n";
        msg += "Gateway IP Adress: " + Utils.ipToString(gIAddr) + "\n";
        msg += "Client Hardward Adress: " + Utils.macToString(cHAddr) + "\n";
        msg += "Server Name: " + new String(sName).trim() + "\n";
        msg += "Boot File Path: " + new String(file).trim() + "\n";

        msg += options.toString() + "\n";

        return msg;
    }
}
