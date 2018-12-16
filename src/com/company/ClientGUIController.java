package com.company;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientGUIController extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("dhcpclient/ClientGUI.fxml"));

        primaryStage.setTitle("my_dhcp");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void onBtnRequestIPClick(ActionEvent actionEvent) {

    }

    public void onBtnRequestAnotherIP(ActionEvent actionEvent) {

    }
}
