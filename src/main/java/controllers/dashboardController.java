package controllers;

import java.util.ArrayList;
import java.util.List;
import utils.SceneSwitcher;
import behaviors.*;
import dao.hotelDao;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import models.Hotel;

public class dashboardController {

    @FXML
    private Button btnHotels, btnStats, addButton, btnCustomers, toggleDeletedButton;
    @FXML
    private AnchorPane root;
    @FXML
    private TextField searchField, hotelNameField, hotelStreetField, hotelLandmarkField, hotelPhonesField;
    @FXML
    private Label totalHotelsLabel, totalRoomsLabel, totalStaffLabel, totalBookingsLabel, messageLabel;
    @FXML
    private ScrollPane scrollPane;

    private final hotelDao dao = new hotelDao();
    private String editingHotelId = null; private boolean showingDeleted = false;

    @FXML
    public void initialize() {
        setActiveButton(btnHotels);
        loadStats();
        loadHotelCards();
        setupSearch();
        Platform.runLater(() -> root.requestFocus());
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            List<Hotel> filtered = new ArrayList<>();
            for (Hotel hotel : dao.getAllActiveHotels()) {
                if (!hotel.isActive())
                    continue;
                String lower = newText.toLowerCase();
                if (hotel.getName().toLowerCase().contains(lower)
                        || hotel.getStreet().toLowerCase().contains(lower)
                        || hotel.getLandmark().toLowerCase().contains(lower)) {
                    filtered.add(hotel);
                }
            }
            FlowPane flowPane = new FlowPane();
            // flowPane.setHgap(16);
            // flowPane.setVgap(16);
            flowPane.setHgap(20);//
            flowPane.setVgap(20);//
            flowPane.setPadding(new Insets(10));//
            flowPane.setStyle("-fx-background-color: transparent; -fx-padding: 4;");
            flowPane.setAlignment(javafx.geometry.Pos.TOP_CENTER);
            flowPane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(20));

            for (Hotel hotel : filtered) {
                flowPane.getChildren().add(createHotelCard(hotel));
            }

            scrollPane.setContent(flowPane);
            scrollPane.setFitToWidth(true);
        });
    }

    private void loadStats() {
        totalHotelsLabel.setText(String.valueOf(dao.getTotalHotels()));
        totalRoomsLabel.setText(String.valueOf(dao.getTotalRooms()));
        totalStaffLabel.setText(String.valueOf(dao.getTotalStaff()));
        totalBookingsLabel.setText(String.valueOf(dao.getTotalBookings()));
    }

    private void loadHotelCards() {
        List<Hotel> hotels = dao.getAllActiveHotels();

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(16);
        flowPane.setVgap(16);
        flowPane.setStyle("-fx-background-color: transparent; -fx-padding: 4;");
        flowPane.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        flowPane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(20));

        for (Hotel hotel : hotels) {
            flowPane.getChildren().add(createHotelCard(hotel));
        }

        scrollPane.setContent(flowPane);
        scrollPane.setFitToWidth(true);
    }

    private AnchorPane createHotelCard(Hotel hotel) {
        AnchorPane card = new AnchorPane();
        card.prefWidthProperty().bind(scrollPane.widthProperty().divide(3).subtract(25)); //
        card.setStyle(cardHovering.defaultCardStyle());
        // card.setPrefWidth(240);
        // card.setPrefHeight(170);
        card.setPrefHeight(200);
        // card.setMaxWidth(240);
        // card.setPrefWidth(320); // base size
        card.setMinWidth(280); //
        card.setMaxWidth(Double.MAX_VALUE); //
        // card.setMaxHeight(170);

        Label name = new Label(hotel.getName());
        name.setStyle(
                "-fx-text-fill: white; -fx-font-family: 'Roboto Medium'; -fx-font-size: 15px; -fx-font-weight: bold;");
        name.setWrapText(true);
        name.setMaxWidth(200);
        AnchorPane.setTopAnchor(name, 14.0);
        AnchorPane.setLeftAnchor(name, 14.0);

        if (!hotel.isActive()) {
            name.setStyle(
                    "-fx-text-fill: grey; -fx-font-family: 'Roboto Medium'; -fx-font-size: 15px; -fx-font-weight: bold;");
            Label deletedLabel = new Label("Deleted");
            deletedLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-font-weight: bold;");
            deletedLabel.setAlignment(Pos.CENTER);
            AnchorPane.setTopAnchor(deletedLabel, 150.0);
            AnchorPane.setLeftAnchor(deletedLabel, 120.0);
            card.getChildren().add(deletedLabel);
        }

        Label id = new Label("#" + hotel.getId());
        id.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-opacity: 0.7;");
        AnchorPane.setTopAnchor(id, 53.0);
        AnchorPane.setLeftAnchor(id, 14.0);

        Label landmark = new Label("🏛  " + hotel.getLandmark());
        landmark.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-opacity: 0.85;");
        landmark.setWrapText(true);
        landmark.setWrapText(true);
        landmark.maxWidthProperty().bind(card.widthProperty().subtract(28)); // 14 left + 14 padding
        AnchorPane.setTopAnchor(landmark, 80.0);
        AnchorPane.setLeftAnchor(landmark, 14.0);

        String ratingText = (hotel.getRating() == null || hotel.getRating().equals("null"))
                ? "No ratings yet"
                : "⭐ " + hotel.getRating();
        Label rating = new Label(ratingText);
        rating.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-opacity: 0.85;");
        AnchorPane.setTopAnchor(rating, 120.0);
        AnchorPane.setLeftAnchor(rating, 14.0);

        Button editBtn = new Button("🖍️");
        editBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-border-color: transparent; -fx-font-size: 16px;");
        editBtn.setPadding(new javafx.geometry.Insets(0));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-border-color: transparent; -fx-font-size: 16px;");
        deleteBtn.setPadding(new javafx.geometry.Insets(0));

        HBox btnBox = new HBox(14, editBtn, deleteBtn);
        AnchorPane.setTopAnchor(btnBox, 6.0);
        AnchorPane.setRightAnchor(btnBox, 2.0);

        card.getChildren().addAll(name, id, landmark, rating, btnBox);

        List<Label> labels = List.of(name, id, landmark, rating);

        card.setOnMouseEntered(e -> {
            card.setStyle(cardHovering.hoverCardStyle());
            labels.forEach(l -> l.setStyle(l.getStyle().replace("white", "black")));
            editBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: black; -fx-cursor: hand; -fx-border-color: transparent; -fx-font-size: 16px;");
            deleteBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: black; -fx-cursor: hand; -fx-border-color: transparent; -fx-font-size: 16px;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle(cardHovering.defaultCardStyle());
            labels.forEach(l -> l.setStyle(l.getStyle().replace("black", "white")));
            editBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-border-color: transparent; -fx-font-size: 16px;");
            deleteBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-border-color: transparent; -fx-font-size: 16px;");
        });

        card.setOnMouseClicked(e -> handleHotelCardClick(hotel));
        editBtn.setOnMouseClicked(e -> {
            e.consume();
            handleEditHotel(hotel);
        });

        deleteBtn.setOnMouseClicked(e -> {
            e.consume();
            handleDeleteHotel(hotel.getId());
        });

        return card;
    }

    private void setActiveButton(Button active) {
        for (Button btn : new Button[] { btnHotels, btnStats, btnCustomers }) {
            btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white; " +
                            "-fx-font-family: 'Roboto'; -fx-font-size: 13px; " +
                            "-fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 16; " +
                            "-fx-cursor: hand; -fx-border-color: transparent;");
        }
        active.setStyle(
                "-fx-background-color: white; -fx-text-fill: #6b061c; " +
                        "-fx-font-family: 'Roboto Medium'; -fx-font-size: 13px; " +
                        "-fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 16; " +
                        "-fx-cursor: hand; -fx-border-color: transparent;");
    }

    private void handleHotelCardClick(Hotel hotel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hotelPage.fxml"));
            AnchorPane hotelPage = loader.load();

            hotelPageController controller = loader.getController();
            controller.setHotelData(hotel);
            controller.setParentRoot(root);

            root.getChildren().clear();
            root.getChildren().add(hotelPage);

            AnchorPane.setTopAnchor(hotelPage, 0.0);
            AnchorPane.setBottomAnchor(hotelPage, 0.0);
            AnchorPane.setLeftAnchor(hotelPage, 0.0);
            AnchorPane.setRightAnchor(hotelPage, 0.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddHotel() {
        String name = hotelNameField.getText().trim();
        String street = hotelStreetField.getText().trim();
        String landmark = hotelLandmarkField.getText().trim();
        String phonesList = hotelPhonesField.getText().trim();
        if (name.isEmpty() || street.isEmpty() || landmark.isEmpty()) {
            messageLabel.setText("Please fill all fields.");
            return;
        }
        List<String> phones = new ArrayList<>();
        for (String p : phonesList.split(",")) {
            String phone = p.trim();
            if (!phone.isEmpty())
                phones.add(phone);
        }
        for (String phone : phones) {
            if (!phone.matches("(\\+91)?\\d{10}")) {
                messageLabel.setText("Invalid phone: " + phone + ". Must be 10 digits.");
                return;
            }
        }
        if (editingHotelId != null) {
            boolean success = dao.updateHotel(Integer.parseInt(editingHotelId), name, street, landmark, phones);
            if (success) {
                messageLabel.setText("Hotel updated successfully!");
                editingHotelId = null;
                addButton.setText("Add");
                clearFields();
                loadStats();
                loadHotelCards();
            } else {
                messageLabel.setText("Failed to update hotel. Please try again!");
            }
        } else {
            String result = dao.addHotel(name, street, landmark, phones);
            boolean isSuccess = result.equals("success") || result.equals("Hotel reactivated successfully!");

            messageLabel.setText(result.equals("success") ? "Hotel added successfully!" : result);

            if (isSuccess) {
                clearFields();
                loadStats();
                loadHotelCards();
            }
        }
    }

    private void handleDeleteHotel(int hotelId) {
        boolean success = dao.softDeleteHotel(hotelId);
        if (success) {
            messageLabel.setText("Deleted Hotel #" + hotelId + " successfully!");
            loadStats();
            loadHotelCards();
        } else {
            messageLabel.setText("Failed to delete Hotel #" + hotelId + ". Please try again!");
        }
    }

    private void handleEditHotel(Hotel hotel) {
        editingHotelId = String.valueOf(hotel.getId());
        hotelNameField.setText(hotel.getName());
        hotelStreetField.setText(hotel.getStreet());
        hotelLandmarkField.setText(hotel.getLandmark());
        hotelPhonesField.setText(hotel.getPhones() != null ? hotel.getPhones() : "");
        addButton.setText("Update");
        messageLabel.setText("Editing hotel #" + hotel.getId());
    }

    @FXML
    public void handleToggleDeleted() {
        showingDeleted = !showingDeleted;
        if (showingDeleted) {
            List<Hotel> deleted = dao.getDeletedHotels();
            FlowPane flowPane = new FlowPane();
            flowPane.setHgap(16);
            flowPane.setVgap(16);
            flowPane.setStyle("-fx-background-color: transparent; -fx-padding: 4;");
            flowPane.setAlignment(javafx.geometry.Pos.TOP_CENTER);
            flowPane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(20));
            for (Hotel hotel : deleted) {
                flowPane.getChildren().add(createHotelCard(hotel));
            }
            scrollPane.setContent(flowPane);
            toggleDeletedButton.setText("Show Active");
            messageLabel.setText("Showing deleted hotels.");
        } else {
            loadHotelCards();
            toggleDeletedButton.setText("Show Deleted");
            messageLabel.setText("");
        }
    }

    private void clearFields() {
        hotelNameField.clear();
        hotelStreetField.clear();
        hotelLandmarkField.clear();
        hotelPhonesField.clear();
        root.requestFocus();
    }

    @FXML
    public void handleHotels() {
        setActiveButton(btnHotels);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard2.fxml"));
            Parent dashboard = loader.load();
            root.getScene().setRoot(dashboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStats() {
        setActiveButton(btnStats);
        SceneSwitcher.switchPage(root, "/fxml/stats.fxml");
    }

    @FXML
    public void handleCustomers() {
        setActiveButton(btnCustomers);
        SceneSwitcher.switchPage(root, "/fxml/customer.fxml");
    }

}