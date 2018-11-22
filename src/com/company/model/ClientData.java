package com.company.model;

import com.company.Utils;

import java.io.Serializable;

public class ClientData implements Serializable {
    byte[] macAddress;
    String hostName;
    byte[] ipAddress;
    int leaseTimeRemain; //in second

    public ClientData(byte[] macAddress, String hostName, byte[] ipAddress, int leaseTimeRemain) {
        this.macAddress = macAddress.clone();
        this.hostName = hostName;
        this.ipAddress = ipAddress.clone();
        this.leaseTimeRemain = leaseTimeRemain;
    }

    public byte[] getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(byte[] macAddress) {
        this.macAddress = macAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public byte[] getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(byte[] ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getLeaseTimeRemain() {
        return leaseTimeRemain;
    }

    public void setLeaseTimeRemain(int leaseTimeRemain) {
        this.leaseTimeRemain = leaseTimeRemain;
    }

    @Override
    public String toString() {
        return  Utils.macToString(macAddress) + '\n'
                + '\t' + hostName + '\n'
                + '\t' + Utils.ipToString(ipAddress) + '\n'
                + '\t' + leaseTimeRemain + '\n';
    }
}
