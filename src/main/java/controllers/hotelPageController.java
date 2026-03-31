package controllers;

import behaviors.buttonHovering;
import models.Hotel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class hotelPageController {
    @FXML
    private Button hotelRooms, hotelBookings, hotelStaff, backButton;
    @FXML
    private Label hotelNameLabel, hotelIDLabel, hotelStreetLabel, hotelLandmarkLabel, hotelContactsLabel;
    @FXML
    AnchorPane root;
    private AnchorPane parentRoot; private Hotel currentHotel;

    
    public void setHotelId(Hotel hotel) {
        this.currentHotel = hotel;
    }

    @FXML
    public void hoverBehavior(MouseEvent e) {
        buttonHovering.hovering(e);
    }

    @FXML
    public void defaultBehavior(MouseEvent e) {
        buttonHovering.notHovering(e);
    }

    public void setParentRoot(AnchorPane root) {
        this.parentRoot = root;
    }

    public void setHotelData(Hotel hotel) {
        if(hotel==null) return;
        this.currentHotel = hotel;
        hotelNameLabel.setText(hotel.getName());
        hotelIDLabel.setText("Hotel ID:  #" + hotel.getId());
        hotelStreetLabel.setText("Street Name:  " + hotel.getStreet());
        hotelLandmarkLabel.setText("Landmark:  " + hotel.getLandmark());
        hotelContactsLabel.setText("Contacts:  " + hotel.getPhones());
    }

    @FXML
    public void handleHotelRooms() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hotelRoomsPage.fxml"));
            AnchorPane hotelRoomsPage = loader.load();
            hotelRoomsPageController controller = loader.getController();
            controller.setParentRoot(root);
            controller.setHotel(currentHotel);
            root.getChildren().setAll(hotelRoomsPage);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void handleHotelBookings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hotelBookingPage.fxml"));
            AnchorPane hotelBookingPage = loader.load();
            hotelBookingPageController controller = loader.getController();
            controller.setParentRoot(root);
            controller.setHotel(currentHotel);
            root.getChildren().setAll(hotelBookingPage);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleHotelStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hotelStaffPage.fxml"));
            AnchorPane hotelStaffPage = loader.load();
            hotelStaffPageController controller = loader.getController();
            controller.setParentRoot(root);
            controller.setHotel(currentHotel);
            root.getChildren().setAll(hotelStaffPage);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleHotelPayments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hotelPaymentsPage.fxml"));
            AnchorPane hotelPaymentsPage = loader.load();
            hotelPaymentsPageController controller = loader.getController();
            controller.setParentRoot(root);
            controller.setHotel(currentHotel);
            root.getChildren().setAll(hotelPaymentsPage);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard2.fxml"));
            BorderPane dashboard = loader.load();
            backButton.getScene().setRoot(dashboard);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
