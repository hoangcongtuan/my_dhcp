package com.company.dhcpclient;

import com.company.NetworkUtils;
import com.company.Utils;
import com.company.common.DHCPMessage;
import com.company.common.DHCPOptions;
import com.company.dhcpserver.DHCPServer;
import com.company.model.ClientData;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import javax.rmi.CORBA.Util;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DHCPClientController implements Initializable {

    private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
    public final static int CLIENT_PORT = 1668;//68;
    private final static int TIME_OUT = 10000; //in millis

    public final static int STATE_INIT = 0;
    public final static int STATE_SELECTING = 1;
    public final static int STATE_REQUESTING = 2;
    public final static int STATE_BOUDING = 3;
    @FXML
    public TextArea textConfig;
    @FXML
    public TextArea textLog;
    @FXML
    public TextArea textDHCPEvent;
    @FXML
    public Button btnRequestIP;
    @FXML
    public Button btnRequestAnotherIP;

    private int currentState;
    private boolean isContinue;
    private int myXid;

    byte[] specialIP;

    ClientData mConfig;
    DatagramSocket socket;

    private void startClient(boolean isRequestAnother) throws IOException {
        currentState = STATE_INIT;
        isContinue = true;
        textLog.clear();

        while (isContinue) {
            switch (currentState) {
                case STATE_INIT:
                    DHCPMessage tempMsg = new DHCPMessage();
                    myXid = tempMsg.getXid();
                    DHCPMessage discoverMsg;
                    if (!isRequestAnother)
                        discoverMsg = tempMsg.createDiscoverMsg(
                                mConfig.getMacAddress(),
                                mConfig.getHostName().getBytes(),
                                mConfig.getIpAddress());
                    else
                        discoverMsg = tempMsg.createDiscoverMsg(
                                mConfig.getMacAddress(),
                                mConfig.getHostName().getBytes(),
                                specialIP);
                    javafx.application.Platform.runLater( () -> {
                                textDHCPEvent.appendText("Client: DISCOVERY MESSAGE\n");
                                textLog.appendText("Client: Discover Message:\n" + discoverMsg.toString());
                    });

                    System.out.println("Client: Discover Message:\n" + discoverMsg.toString());
                    //send discovery message to server
                    byte[] message_buffer = discoverMsg.externalize();
                    broadcastMessage(message_buffer, socket, NetworkUtils.listAllBroadcastAddresses());
                    currentState = STATE_SELECTING;
                    break;

                case STATE_SELECTING:
                    //waitting for offer
                    socket.setSoTimeout(TIME_OUT);
                    byte[] buffer = new byte[MAX_BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException e) {
                        currentState = STATE_INIT;
                        break;
                    }

                    DHCPMessage offerMessage = new DHCPMessage(packet.getData());
                    javafx.application.Platform.runLater( () -> {
                        textDHCPEvent.appendText("Server: OFFER MESSAGE\n");
                        textLog.appendText("Server: Offer Message:\n" + offerMessage.toString());
                    });


                    System.out.println("Server: Offer Message:\n" + offerMessage.toString());
                    //get neccessary information
                    byte[] requestIpAddress = offerMessage.getYIAddr().clone();
                    byte[] serverId = offerMessage.getOptions().getOption(DHCPOptions.DHCP_OPTION_SERVER_ID);
                    byte[] timeLease = offerMessage.getOptions().getOption(DHCPOptions.DHCP_OPTION_TIMELEASE);

                    DHCPMessage requestMessage
                            = offerMessage.createRequestMsg(requestIpAddress, serverId, timeLease, mConfig.getHostName().getBytes());
                    byte[] request_buffer = requestMessage.externalize();
                    broadcastMessage(request_buffer, socket, NetworkUtils.listAllBroadcastAddresses());
                    javafx.application.Platform.runLater( () -> {
                        textDHCPEvent.appendText("Client: REQEUST MESSAGE\n");
                        textLog.appendText("Client: Request Message:\n" + offerMessage.toString());
                    });

                    System.out.println("Client: Request Message:\n" + offerMessage.toString());
                    currentState = STATE_REQUESTING;
                    break;

                case STATE_REQUESTING:
                    //wait ack, nak message
                    socket.setSoTimeout(TIME_OUT);
                    byte[] buffer_ack = new byte[MAX_BUFFER_SIZE];
                    DatagramPacket packet_ack = new DatagramPacket(buffer_ack, buffer_ack.length);
                    try {
                        socket.receive(packet_ack);
                    } catch (SocketTimeoutException e) {
                        currentState = STATE_INIT;
                        break;
                    }

                    DHCPMessage ackMessage = new DHCPMessage(packet_ack.getData());
                    javafx.application.Platform.runLater( () -> {
                        textDHCPEvent.appendText("Sever: ACK MESSAGE\n");
                        textLog.appendText("Server: ACK Message:\n" + ackMessage.toString());
                    });
                    System.out.println("Server: ACK Message:\n" + ackMessage.toString());
                    int messageType = ackMessage.getOptions().getOption(DHCPOptions.DHCP_OPTIONS_MESSAGE_TYPE)[0];
                    if (messageType == DHCPOptions.DHCPNAK) {
                        currentState = STATE_INIT;
                        break;
                    } else if (messageType == DHCPOptions.DHCPACK) {
                        byte[] bufferIP = ackMessage.getYIAddr();
                        byte[] bufferTimeLease = ackMessage.getOptions().getOption(DHCPOptions.DHCP_OPTION_TIMELEASE);
                        // TODO: 11/22/18 check ip is can use, in this example, it always be can use
                        currentState = STATE_BOUDING;
                        mConfig.setIpAddress(bufferIP);
                        mConfig.setLeaseTimeRemain(Utils.bytesToInt(bufferTimeLease));
                        break;
                    } else {
                        // TODO: 11/22/18 unExpected State, re lease again
                        currentState = STATE_INIT;
                    }
                    break;

                case STATE_BOUDING:
                    javafx.application.Platform.runLater( () -> {
                        textDHCPEvent.appendText("Allocation Finish\n");
                    });
                    saveMyConfig("clientConfig.txt", mConfig);
                    System.out.println("Finish Allocation IP Address, open browser and go to favorite website, enjoi!!");
                    displayConfig();
                    isContinue = false;
                    break;

                default:
                    isContinue = false;
                    break;
            }
        }

        System.out.println("End time!!");
    }

    private void displayConfig() {
        textConfig.clear();
        textConfig.appendText(mConfig.toString());
    }

    private ClientData loadMyConfig(String path) throws IOException {
        ClientData myConfig = new ClientData();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = null;
        if ((line = br.readLine()) != null) {
            byte[] macAddress = Utils.strToMAC(line);
            String hostName = br.readLine();
            byte[] ipAddress = Utils.strToIp(br.readLine());
            int timeLeaseRemain = Integer.parseInt(br.readLine());

            myConfig.setMacAddress(macAddress);
            myConfig.setHostName(hostName);
            myConfig.setIpAddress(ipAddress);
            myConfig.setLeaseTimeRemain(timeLeaseRemain);
        }

        br.close();
        return myConfig;
    }

    private void saveMyConfig(String path, ClientData config) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(path));
        String line = null;
        String strMac = Utils.macToString(config.getMacAddress());
        String strHostName = config.getHostName();
        String strIP = Utils.ipToString(config.getIpAddress());
        String strTimeLease = String.valueOf(config.getLeaseTimeRemain());

        bw.write(strMac);
        bw.newLine();
        bw.write(strHostName);
        bw.newLine();
        bw.write(strIP);
        bw.newLine();
        bw.write(strTimeLease);

        bw.close();
    }

    private void broadcastMessage(byte[] message, DatagramSocket socket, List<InetAddress> broadcastList) throws IOException {
        socket.setBroadcast(true);
        for (InetAddress address : broadcastList) {
            DatagramPacket packet = new DatagramPacket(message, message.length, address, DHCPServer.SERVER_PORT);
            socket.send(packet);
        }
    }

    @FXML
    public void onBtnRequestIPClick(ActionEvent actionEvent) {
        new Thread(() -> {
            try {
                btnRequestAnotherIP.setDisable(true);
                startClient(false);
                btnRequestAnotherIP.setDisable(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void onBtnRequestAnotherIP(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Input special IP");
        dialog.setHeaderText("Type what you want!");

        dialog.setContentText("Type Special IP:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                specialIP = Utils.strToIp(result.get());
                new Thread(() -> {
                    try {
                        btnRequestAnotherIP.setDisable(true);
                        startClient(true);
                        btnRequestAnotherIP.setDisable(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("Error Occur!");
                alert.setContentText("Bad IP Format");
                alert.show();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            mConfig = loadMyConfig("clientConfig.txt");
            displayConfig();
            socket = new DatagramSocket(CLIENT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
