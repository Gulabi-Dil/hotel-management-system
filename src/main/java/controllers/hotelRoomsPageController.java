package controllers;

import java.sql.SQLException;
import java.util.List;
import models.*;
import utils.TableUtils;
import dao.roomDao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.*;

public class hotelRoomsPageController {
    @FXML
    private Button backButton, addRoom, toggleDeletedButton;
    @FXML
    private Label hotelNameLabel;
    @FXML
    private HBox roomTypeContainer;
    @FXML
    private TableView<Room> roomTable;
    @FXML
    private TableColumn<Room, Integer> colRoomNumber;
    @FXML
    private TableColumn<Room, String> colRoomType, colStatus;
    @FXML
    private TableColumn<Room, Double> colPrice;
    @FXML
    private TableColumn<Room, Void> colActions;
    @FXML
    private TextField searchField;
    private AnchorPane parentRoot;
    private Hotel currentHotel;
    private List<Room> allRooms;
    private boolean showingDeleted = false;

    public void setHotel(Hotel hotel) {
        this.currentHotel = hotel;
        hotelNameLabel.setText("# " + currentHotel.getId() + " " + currentHotel.getName() + " / Rooms");
        loadRoomTypes();
        setupTable();
        loadRooms();
    }

    public void setParentRoot(AnchorPane root) {
        this.parentRoot = root;
    }

    private void loadRoomTypes() {
        roomTypeContainer.getChildren().clear();
        roomDao daob = new roomDao();
        List<RoomType> types = daob.getRoomTypesByHotel(currentHotel.getId());
        if (types == null || types.isEmpty()) {
            Label message = new Label("There are no rooms in this hotel as of now.");
            roomTypeContainer.setAlignment(Pos.CENTER);
            message.setStyle(
                    "-fx-text-fill: white; -fx-font-weight:bold; -fx-font-size:20px; -fx-font-family: sans-serif;");
            roomTypeContainer.getChildren().add(message);
            return;
        }
        roomTypeContainer.getChildren().clear();

        for (RoomType type : types) {
            roomTypeContainer.getChildren().add(createRoomTypeCard(type));
        }
    }

    private AnchorPane createRoomTypeCard(RoomType type) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(120, 103);
        card.setStyle("-fx-background-color: #6b061c; -fx-background-radius: 8;");

        Label count = new Label(type.getAvailable() + "/" + type.getTotal());
        count.setPrefSize(111, 71);
        count.setStyle(
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-font-family: sans-serif;");
        count.setAlignment(Pos.CENTER);

        AnchorPane.setTopAnchor(count, 0.0);
        AnchorPane.setLeftAnchor(count, 0.0);
        AnchorPane.setRightAnchor(count, 0.0);

        Label typeL = new Label(type.getType());
        typeL.setWrapText(true);
        typeL.setMaxWidth(110);
        typeL.setAlignment(Pos.CENTER);
        typeL.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        typeL.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        typeL.setAlignment(Pos.CENTER);

        AnchorPane.setTopAnchor(typeL, 60.0);
        AnchorPane.setLeftAnchor(typeL, 0.0);
        AnchorPane.setRightAnchor(typeL, 0.0);

        card.getChildren().addAll(count, typeL);
        return card;
    }

    @FXML
    public void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hotelPage.fxml"));
            AnchorPane hotelPage = loader.load();
            hotelPageController controller = loader.getController();
            controller.setHotelData(currentHotel);
            parentRoot.getChildren().setAll(hotelPage);

            AnchorPane.setTopAnchor(hotelPage, 0.0);
            AnchorPane.setBottomAnchor(hotelPage, 0.0);
            AnchorPane.setLeftAnchor(hotelPage, 0.0);
            AnchorPane.setRightAnchor(hotelPage, 0.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {

        colRoomNumber.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRoomNum()).asObject());
        colRoomType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPricePerDay()).asObject());
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        TableUtils.centerColumns(colRoomNumber, colRoomType, colPrice, colStatus, colActions);
        TableUtils.fixColumns(roomTable, colRoomNumber, colRoomType, colPrice, colStatus, colActions);
        TableUtils.styleTable(roomTable);

        TableUtils.addActionButtons(
                colActions,
                room -> openEditPopup(room),
                room -> {
                    try {
                        roomDao dao = new roomDao();
                        dao.deleteRoom(room.getHotelId(), room.getRoomNum());
                        roomTable.getItems().remove(room);
                        loadRoomTypes();
                        loadRooms();
                    } catch (SQLException e) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Cannot Delete Room");
                        alert.setHeaderText(null);
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    }
                });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void openEditPopup(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addRoom.fxml"));
            Parent root = loader.load();

            addRoomController controller = loader.getController();
            controller.setHotelId(currentHotel.getId());
            controller.setEdit(room);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            stage.setOnHidden(e -> {
                loadRooms();
                loadRoomTypes();
            });

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        TableUtils.setupSearch(
                searchField,
                allRooms,
                roomTable,
                r -> {
                    String lower = searchField.getText().toLowerCase();
                    return String.valueOf(r.getRoomNum()).contains(lower) ||
                            r.getType().toLowerCase().contains(lower) ||
                            r.getStatus().toLowerCase().contains(lower);
                });
    }

    private void loadRooms() {
        allRooms = new roomDao().getRoomsByHotel(currentHotel.getId());
        roomTable.getItems().setAll(allRooms);
        parentRoot.requestFocus();
        setupSearch();
    }

    @FXML
    public void handleAddRoom() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addRoom.fxml"));
            Parent root = loader.load();

            addRoomController controller = loader.getController();
            controller.setHotelId(currentHotel.getId());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setOnHidden(e -> {
                loadRooms();
                loadRoomTypes();
            });

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleToggleDeleted() {
        showingDeleted = !showingDeleted;
        if (showingDeleted) {
            roomTable.getItems().setAll(new roomDao().getDeletedRooms(currentHotel.getId()));
            toggleDeletedButton.setText("Show Active");
        } else {
            loadRooms();
            toggleDeletedButton.setText("Show Deleted");
        }
    }
}