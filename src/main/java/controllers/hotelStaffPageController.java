package controllers;

import java.util.List;
import models.*;
import utils.TableUtils;
import dao.staffDao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.beans.property.*;
import java.time.LocalDate;

public class hotelStaffPageController {
    @FXML
    private Label hotelNameLabel, messageLabel;
    @FXML
    private Button backButton, addCustomer, toggleDeletedButton;
    @FXML
    private TableView<Staff> staffTable;
    @FXML
    private TableColumn<Staff, Integer> colStaffId;
    @FXML
    private TableColumn<Staff, Double> colStaffSalary;
    @FXML
    private TableColumn<Staff, String> colStaffName, colStaffGender, colStaffPhone, colStaffEmail, colStaffRole;
    @FXML
    private TableColumn<Staff, LocalDate> colStaffJoin;
    @FXML
    private TableColumn<Staff, Void> colActions;
    @FXML
    private TextField searchField;
    @FXML
    private HBox staffRoleContainer;

    private AnchorPane parentRoot;
    private Hotel currentHotel;
    private List<Staff> allStaff;
    private boolean showingDeleted = false;

    public void setHotel(Hotel hotel) {
        this.currentHotel = hotel;
        hotelNameLabel.setText("#" + currentHotel.getId() + " " + currentHotel.getName() + " / Staff");
        loadStaffRoles();
        setupTable();
        loadStaff();
    }

    private void setupSearch() {
        TableUtils.setupSearch(
                searchField,
                allStaff,
                staffTable,
                s -> {
                    String lower = searchField.getText().toLowerCase();
                    return s.getName().toLowerCase().contains(lower)
                            || s.getPhone().toLowerCase().contains(lower)
                            || s.getEmail().toLowerCase().contains(lower)
                            || s.getRole().toLowerCase().contains(lower);
                });
    }

    public void setParentRoot(AnchorPane root) {
        this.parentRoot = root;
    }

    private void loadStaffRoles() {
        staffRoleContainer.getChildren().clear();
        staffDao daob = new staffDao();
        List<StaffRole> types = daob.getRoles(currentHotel.getId());
        if (types == null || types.isEmpty()) {
            Label message = new Label("This hotel hasn't employed any staff yet.");
            staffRoleContainer.setAlignment(Pos.CENTER);
            message.setStyle(
                    "-fx-text-fill: white; -fx-font-weight:bold; -fx-font-size:20px; -fx-font-family: sans-serif;");
            staffRoleContainer.getChildren().add(message);
            return;
        }
        staffRoleContainer.getChildren().clear();

        for (StaffRole type : types) {
            staffRoleContainer.getChildren().add(createStaffRoleCard(type));
        }
    }

    private AnchorPane createStaffRoleCard(StaffRole roles) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(120, 103);
        card.setStyle("-fx-background-color: #6b061c; -fx-background-radius: 8;");

        Label count = new Label(String.valueOf(roles.getTotal()));
        count.setPrefSize(111, 71);
        count.setStyle(
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-font-family: sans-serif;");
        count.setAlignment(Pos.CENTER);

        AnchorPane.setTopAnchor(count, 0.0);
        AnchorPane.setLeftAnchor(count, 0.0);
        AnchorPane.setRightAnchor(count, 0.0);

        Label typeL = new Label(roles.getRole());
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

    private void addTooltipToColumn(TableColumn<Staff, String> column) { // for attribute which appear partially due
                                                                         // to length.
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    setTooltip(new Tooltip(item));
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void setupTable() {

        colStaffId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStaffId()).asObject());
        colStaffName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colStaffSalary.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSalary()).asObject());
        colStaffGender.setCellValueFactory(data -> {
            String gender = data.getValue().getGender();
            String display;
            switch (gender) {
                case "Male" -> display = "M";
                case "Female" -> display = "F";
                default -> display = "X";
            }
            return new SimpleStringProperty(display);
        });
        colStaffPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        colStaffEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colStaffJoin.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getJoined()));
        colStaffRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        TableUtils.centerColumns(colStaffId, colStaffName, colStaffSalary, colStaffJoin, colStaffGender, colStaffPhone,
                colStaffEmail,
                colStaffRole, colActions);
        TableUtils.fixColumns(staffTable, colStaffId, colStaffName, colStaffSalary, colStaffJoin, colStaffGender,
                colStaffPhone,
                colStaffEmail, colStaffRole, colActions);
        TableUtils.styleTable(staffTable);

        TableUtils.addActionButtons(
                colActions,
                staff -> openEditPopup(staff),

                staff -> {
                    try {
                        new staffDao().softDeleteStaff(staff.getHotelId(), staff.getStaffId());
                        staffTable.getItems().remove(staff);
                        loadStaffRoles();
                        loadStaff();
                    } catch (Exception e) {
                        // messageLabel.setText("Error! Could not delete staff's record!");
                        return;
                    }
                });
    }

    private void openEditPopup(Staff staff) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addStaff.fxml"));
            Parent root = loader.load();

            addStaffController controller = loader.getController();
            controller.setHotelId(currentHotel.getId());
            controller.setEdit(staff);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            stage.setOnHidden(e -> {
                loadStaff();
                loadStaffRoles();
            });

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStaff() {
        allStaff = new staffDao().getStaffByHotel(currentHotel.getId());
        staffTable.getItems().setAll(allStaff);
        parentRoot.requestFocus();
        setupSearch();
    }

    @FXML
    public void handleAddStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addStaff.fxml"));
            Parent root = loader.load();

            addStaffController controller = loader.getController();
            controller.setHotelId(currentHotel.getId());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setOnHidden(e -> {
                loadStaff();
                loadStaffRoles();
            });

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @FXML
    public void handleToggleDeleted() {
        showingDeleted = !showingDeleted;
        if (showingDeleted) {
            staffTable.getItems().setAll(new staffDao().getDeletedStaff(currentHotel.getId()));
            toggleDeletedButton.setText("Show Active");
        } else {
            loadStaff();
            toggleDeletedButton.setText("Show Deleted");
        }
    }

}
