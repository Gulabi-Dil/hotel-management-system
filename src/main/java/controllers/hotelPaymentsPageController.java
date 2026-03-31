package controllers;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import models.*;
import utils.TableUtils;
import dao.paymentDao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.*;
import javafx.beans.property.*;

public class hotelPaymentsPageController {

    @FXML
    private TableView<Payment> paymentTable;
    @FXML
    private TableColumn<Payment, Integer> colBookingId;
    @FXML
    private TableColumn<Payment, Double> colExtraCharge, colBill, colRating;
    @FXML
    private TableColumn<Payment, String> colMode, colPaymentStatus;
    @FXML
    private TableColumn<Payment, Void> colActions;
    @FXML
    private TableColumn<Payment, LocalDate> colDate;

    @FXML
    private TextField extraChargeField;
    @FXML
    private ComboBox<String> modeField, statusField, bookingIdField;
    @FXML
    private Label paymentDateLabel, hotelNameLabel;
    @FXML
    private Button actionButton;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Double> ratingCombo;
    @FXML
    private Label messageLabel;

    private AnchorPane parentRoot;
    private Hotel currentHotel;
    private List<Payment> allPayments;
    private Payment editingPayment = null;
    private final paymentDao dao = new paymentDao();

    public void setHotel(Hotel hotel) {
        this.currentHotel = hotel;
        hotelNameLabel.setText("#" + hotel.getId() + " " + hotel.getName() + " / Payments");
        setupTable();
        loadPayments();
        setupBookingIdComboBox();
    }

    public void setParentRoot(AnchorPane root) {
        this.parentRoot = root;
    }

    public void initialize() {
        paymentTable.setFocusTraversable(false);
        modeField.getItems().addAll("upi", "card", "cash", "bank_transfer");
        statusField.getItems().addAll("pending", "completed");
        ratingCombo.getItems().addAll(0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0);
    }

    public void setEdit(Payment payment) {
        editingPayment = payment;
    }

    private void setupBookingIdComboBox() {
        bookingIdField.setEditable(true);
        List<String> allBookingIds = dao.getBookingIdsByHotel(currentHotel.getId())
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        bookingIdField.getItems().setAll(allBookingIds);

        bookingIdField.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                String typed = bookingIdField.getEditor().getText();
                List<String> filtered;
                if (typed == null || typed.isEmpty()) {
                    filtered = new ArrayList<>(allBookingIds);
                } else {
                    filtered = allBookingIds.stream()
                            .filter(id -> id.startsWith(typed))
                            .collect(Collectors.toList());
                }
                bookingIdField.getItems().setAll(filtered);
                bookingIdField.getEditor().setText(typed);
                bookingIdField.getEditor().positionCaret(typed.length());
                if (!bookingIdField.isShowing() && !filtered.isEmpty()) {
                    bookingIdField.show();
                }
            });
        });
    }

    private void setupTable() {
        colBookingId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getBookingId()).asObject());
        colExtraCharge
                .setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getExtraService()).asObject());
        colMode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMode()));
        colPaymentStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colRating.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getRating()).asObject());
        colDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPayDate()));
        colBill.setCellFactory(col -> new TableCell<Payment, Double>() {
            private final Button viewBillBtn = new Button("View Bill");

            {
                viewBillBtn.setStyle(
                        "-fx-background-color: white; -fx-text-fill: #8a0824; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold; " +
                                "-fx-background-radius: 4; -fx-cursor: hand;");
                viewBillBtn.setOnAction(e -> {
                    Payment payment = getTableView().getItems().get(getIndex());
                    showBillPopup(payment.getBookingId());
                });
            }

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Payment payment = getTableView().getItems().get(getIndex());
                if ("checked_out".equals(payment.getBookingStatus())) {
                    setGraphic(viewBillBtn);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText("-");
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: white;");
                }
            }
        });

        TableUtils.centerColumns(colBookingId, colDate, colExtraCharge, colBill, colMode, colPaymentStatus, colRating,
                colActions);
        TableUtils.fixColumns(paymentTable, colDate, colBookingId, colExtraCharge, colBill, colMode, colPaymentStatus,
                colRating, colActions);
        TableUtils.styleTable(paymentTable);

        TableUtils.addActionButtons(
                colActions,
                payment -> {
                    editingPayment = payment;
                    bookingIdField.getEditor().setText(String.valueOf(payment.getBookingId()));
                    extraChargeField.setText(String.valueOf(payment.getExtraService()));
                    ratingCombo.setValue(payment.getRating());
                    modeField.setValue(payment.getMode());
                    statusField.setValue(payment.getStatus());
                    paymentDateLabel.setText(payment.getPayDate() != null ? payment.getPayDate().toString() : "N/A");
                    actionButton.setText("Update");
                    messageLabel.setText("Editing payment #" + payment.getBookingId());
                },
                null);
    }

    private void setupSearch() {
        TableUtils.setupSearch(
                searchField,
                allPayments,
                paymentTable,
                p -> String.valueOf(p.getBookingId()).startsWith(searchField.getText().toLowerCase()));
    }

    private void loadPayments() {
        allPayments = dao.getPaymentsByHotel(currentHotel.getId());
        paymentTable.getItems().setAll(allPayments);
        setupSearch();
        parentRoot.requestFocus();
    }

    @FXML
    public void handleUpdatePayment() {
        if (editingPayment == null) {
            messageLabel.setText("Select a payment to edit first.");
            return;
        }

        String extraText = extraChargeField.getText().trim();
        Double rating = ratingCombo.getValue();
        String mode = modeField.getValue();
        String status = statusField.getValue();

        if (extraText.isEmpty() || rating == null || mode == null || status == null) {
            messageLabel.setText("Please fill all fields.");
            return;
        }

        double extraService;

        try {
            extraService = Double.parseDouble(extraText);
        } catch (NumberFormatException e) {
            messageLabel.setText("Extra service cost must be a valid number.");
            return;
        }

        if (extraService < 0) {
            messageLabel.setText("Extra service cost cannot be negative.");
            return;
        }

        if (rating < 0 || rating > 5) {
            messageLabel.setText("Rating must be between 0 and 5.");
            return;
        }

        try {
            dao.updatePayment(editingPayment.getBookingId(), extraService, mode, status, rating);
            messageLabel.setText("Payment #" + editingPayment.getBookingId() + " updated successfully!");
            clearFields();
            loadPayments();
        } catch (SQLException e) {
            switch (e.getSQLState()) {
                case "23514" -> messageLabel.setText("Invalid value — check rating or mode.");
                default -> messageLabel.setText("Database error: " + e.getMessage());
            }
        }
    }

    private void clearFields() {
        bookingIdField.getEditor().clear();
        bookingIdField.getItems().setAll(dao.getBookingIdsByHotel(currentHotel.getId()).stream().map(String::valueOf)
                .collect(Collectors.toList()));
        bookingIdField.setPromptText("Booking ID");
        extraChargeField.clear();
        extraChargeField.setPromptText("Extra Services Charge");
        ratingCombo.setValue(null);
        ratingCombo.setPromptText("Rating");
        modeField.setValue(null);
        modeField.setPromptText("Payment Mode");
        statusField.setValue(null);
        statusField.setPromptText("Payment Status");
        paymentDateLabel.setText("");
        actionButton.setText("Update");
        editingPayment = null;
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

    private void showBillPopup(int bookingId) {
        BillDetails bill = dao.getBillDetails(bookingId);
        if (bill == null) {
            messageLabel.setText("Could not load bill details.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/billPopup.fxml"));
            AnchorPane pane = loader.load();

            billPopupController controller = loader.getController();
            controller.setBillDetails(bill);

            javafx.stage.Stage popup = new javafx.stage.Stage();
            popup.setTitle("Invoice — Booking #" + bookingId);
            popup.setResizable(false);
            popup.setScene(new javafx.scene.Scene(pane));
            popup.show();

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Could not open bill.");
        }
    }
}