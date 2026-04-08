package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import dao.roomDao;
import models.Room;
import java.sql.*;

public class addRoomController {

    @FXML
    private TextField roomNumberField, roomTypeField, priceField;
    @FXML
    private ComboBox<String> statusBox;
    @FXML
    private Button actionButton;
    @FXML
    private Label messageLabel;
    private int hotelId;

    private boolean editing = false;
    private Room editRoom;

    public void setHotelId(int id) {
        this.hotelId = id;
    }

    @FXML
    public void initialize() {
        statusBox.getItems().addAll("available", "booked", "occupied", "maintenance");
    }

    public void setEdit(Room room) {
        editing = true;
        editRoom = room;
        roomNumberField.setText(String.valueOf(room.getRoomNum()));
        roomTypeField.setText(room.getType());
        priceField.setText(String.valueOf(room.getPricePerDay()));
        statusBox.setValue(room.getStatus());
        actionButton.setText("Update");
    }

    @FXML
    public void handleAddRoom() {
        try {
            String roomNumText = roomNumberField.getText();
            String type = roomTypeField.getText();
            String priceText = priceField.getText();
            String status = statusBox.getValue();

            if (roomNumText.isEmpty() || type.isEmpty() || priceText.isEmpty() || status == null) {
                messageLabel.setText("Please enter all fields.");
                return;
            }
            int roomNum = Integer.parseInt(roomNumText);
            double price = Double.parseDouble(priceText);

            roomDao daob = new roomDao();
            Room room = new Room(hotelId, roomNum, type, price, status);

            if (!editing) {
                daob.addRoom(room);
            } else {
                String error = daob.updateRoom(room, editRoom.getRoomNum());
                if (error != null) {
                    messageLabel.setText(error);
                    return;
                }
            }

            roomNumberField.getScene().getWindow().hide();

        } catch (SQLException e) {
            // Unique violation: duplicate room number
            if ("23505".equals(e.getSQLState())) {
                messageLabel.setText("Room number " + roomNumberField.getText() + " already exists for this hotel.");
            }
            // Check constraint violation: invalid price or status
            else if ("23514".equals(e.getSQLState())) {
                messageLabel.setText("Invalid room price. Price cannot be <=0");
            } else {
                messageLabel.setText("Database error: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Room number and price must be numeric.");
        } catch (Exception e) {
            messageLabel.setText("Unexpected error occurred.");
            e.printStackTrace();
        }
    }
}