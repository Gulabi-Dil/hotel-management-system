package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import models.*;
import utils.TableUtils;
import dao.bookingDao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class hotelBookingPageController {

    @FXML
    private TextField searchField;
    @FXML
    private Button backButton, addButton;
    @FXML
    private ComboBox<String> roomTypeField, statusField, aadharField;
    @FXML
    private ComboBox<Integer> roomNumField;
    @FXML
    private DatePicker checkInField, checkOutField;
    @FXML
    private Label messageLabel, hotelNameLabel;
    @FXML
    private TableView<Booking> bookingTable;
    @FXML
    private TableColumn<Booking, Integer> colBookingId, colRoomNumber;
    @FXML
    private TableColumn<Booking, String> colCustomerAadhar, colRoomType, colBookingStatus;
    @FXML
    private TableColumn<Booking, LocalDate> colCheckIn, colCheckOut;
    @FXML
    private TableColumn<Booking, Void> colActions;

    private AnchorPane parentRoot;
    private Hotel currentHotel;
    private boolean editing = false;
    private Booking editingBooking = null;
    private bookingDao dao = new bookingDao();
    private List<Booking> allBookings;

    public void setHotel(Hotel hotel) {
        this.currentHotel = hotel;
        hotelNameLabel.setText("#" + currentHotel.getId() + " " + currentHotel.getName() + " / Bookings");

        setupTable();
        populateComboBoxes();
        loadBookings();
        setupAadharComboBox();
        setupSearch();
    }

    public void setParentRoot(AnchorPane root) {
        this.parentRoot = root;
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                loadBookings();
                return;
            }

            String lower = newText.toLowerCase();
            List<Booking> filteredBookings = new ArrayList<>();

            // Filter the bookings based on the search query
            for (Booking booking : allBookings) {
                if (booking.getAadhar().toLowerCase().contains(lower)
                        || booking.getRoomType().toLowerCase().contains(lower)
                        || String.valueOf(booking.getRoomNum()).contains(lower)
                        || booking.getStatus().toLowerCase().contains(lower)) {
                    filteredBookings.add(booking);
                }
            }
            // Update the table with the filtered list
            bookingTable.getItems().setAll(filteredBookings);
        });
    }

    private void populateComboBoxes() {
        statusField.getItems().setAll("confirmed", "checked_in", "checked_out", "cancelled");

        // Listen to date changes to reload room types
        checkInField.valueProperty().addListener((obs, oldVal, newVal) -> refreshRoomTypes());
        checkOutField.valueProperty().addListener((obs, oldVal, newVal) -> refreshRoomTypes());

        roomNumField.getItems().clear();
        roomTypeField.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                LocalDate in = checkInField.getValue();
                LocalDate out = checkOutField.getValue();
                if (in == null || out == null) {
                    messageLabel.setText("Please select check-in and check-out dates first.");
                    roomNumField.getItems().clear();
                    return;
                }
                List<Integer> roomNumbers = dao.getAvailableRoomNumbers(currentHotel.getId(), newVal, in, out);
                if (editing && editingBooking != null && editingBooking.getRoomType().equals(newVal)) {
                    if (!roomNumbers.contains(editingBooking.getRoomNum())) {
                        roomNumbers.add(editingBooking.getRoomNum());
                    }
                    roomNumField.setValue(editingBooking.getRoomNum());
                } else {
                    roomNumField.getSelectionModel().clearSelection();
                }
                roomNumField.getItems().setAll(roomNumbers);
            } else {
                roomNumField.getItems().clear();
            }
        });
    }

    private void refreshRoomTypes() {
        LocalDate in = checkInField.getValue();
        LocalDate out = checkOutField.getValue();
        if (in == null || out == null || !out.isAfter(in)) return;

        List<String> roomTypes = dao.getAvailableRoomTypesByHotel(currentHotel.getId(), in, out);
        roomTypeField.getItems().setAll(roomTypes);
        roomTypeField.getSelectionModel().clearSelection();
        roomNumField.getItems().clear();
    }

    private void setupAadharComboBox() {
        aadharField.setEditable(true);
        List<String> allAadhars = new ArrayList<>(dao.getAllAadharNumbers());
        aadharField.getItems().setAll(allAadhars);

        aadharField.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                String typed = aadharField.getEditor().getText();
                List<String> filtered;
                if (typed == null || typed.isEmpty()) {
                    filtered = new ArrayList<>(allAadhars);
                } else {
                    String lower = typed.toLowerCase();
                    filtered = allAadhars.stream()
                            .filter(a -> a.toLowerCase().startsWith(lower))
                            .collect(Collectors.toCollection(ArrayList::new));
                }
                aadharField.getItems().setAll(filtered);
                aadharField.getEditor().setText(typed.trim());
                aadharField.getEditor().positionCaret(typed.length());
                if (!aadharField.isShowing() && !filtered.isEmpty()) {
                    aadharField.show();
                }
            });
        });
    }

    private void loadBookings() {
        allBookings = dao.getBookingsByHotel(currentHotel.getId());
        bookingTable.getItems().setAll(allBookings);
        parentRoot.requestFocus();
    }

    private void setupTable() {
        colBookingId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getBookingId()).asObject());
        colCustomerAadhar.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAadhar()));
        colRoomType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomType()));
        colRoomNumber.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRoomNum()).asObject());
        colCheckIn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCheckIn()));
        colBookingStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colCheckOut.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCheckOut()));

        TableUtils.centerColumns(colBookingId, colCustomerAadhar, colRoomType, colRoomNumber, colCheckIn,
                colBookingStatus, colCheckOut, colActions);
        TableUtils.fixColumns(bookingTable, colBookingId, colCustomerAadhar, colRoomType, colRoomNumber, colCheckIn,
                colBookingStatus, colCheckOut, colActions);
        TableUtils.styleTable(bookingTable);

        TableUtils.addActionButtons(
                colActions,
                booking -> {
                    setEdit(booking);
                    messageLabel.setText("Editing booking...");
                    addButton.setText("Update");
                },
                null);
    }

    public void initialize() {
        setupTable();
        roomNumField.getSelectionModel().clearSelection();
        bookingTable.setFocusTraversable(false);
        checkInField.setEditable(false);
        checkOutField.setEditable(false);
        messageLabel.setText("NOTE: Always enter check-in and check-out dates first during input.");
    }

    public void setEdit(Booking booking) {
        editing = true;
        editingBooking = booking;
        roomTypeField.setValue(booking.getRoomType());
        checkInField.setValue(booking.getCheckIn());
        checkOutField.setValue(booking.getCheckOut());
        statusField.setValue(booking.getStatus());

        // Room number will be set in listener of roomTypeField
        roomNumField.setValue(booking.getRoomNum());
        aadharField.getEditor().setText(booking.getAadhar());
        parentRoot.requestFocus();
    }

    private void clearFields() {
        aadharField.setValue(null);
        roomTypeField.setValue(null);
        roomNumField.setValue(null);
        checkInField.setValue(null);
        statusField.setValue(null);
        checkOutField.setValue(null);
    }

    public void handleAddBooking() {
        try {
            String aadhar = aadharField.getEditor().getText().trim();
            String roomType = roomTypeField.getValue();
            Integer roomNumber = roomNumField.getValue();
            LocalDate inDate = checkInField.getValue();
            LocalDate outDate = checkOutField.getValue();
            String status = statusField.getValue();

            if (aadhar.isEmpty() || roomType == null || roomNumber == null || inDate == null || outDate == null
                    || status == null) {
                messageLabel.setText("Please fill in all fields.");
                return;
            }

            if (outDate.isBefore(inDate)) {
                messageLabel.setText("Invalid dates.");
                return;
            }
            int rn = roomNumber;
            Booking booking = new Booking(0, aadhar, currentHotel.getId(), roomType, rn, inDate, status, outDate);

            if (!editing) {
                dao.addBooking(booking);
                messageLabel.setText("Booking done successfully!");
            } else {
                editingBooking.setAadhar(aadhar);
                editingBooking.setRoomType(roomType);
                editingBooking.setRoomNum(roomNumber);
                editingBooking.setCheckIn(inDate);
                editingBooking.setCheckOut(outDate);
                editingBooking.setStatus(status);

                dao.updateBooking(editingBooking);
                messageLabel.setText("Booking updated successfully!");
                addButton.setText("Add");
                editing = false;
                editingBooking = null;
            }

            clearFields();
            loadBookings();
            populateComboBoxes();

        } catch (SQLException e) {
            if (e.getMessage().contains("booking_aadhar_num_fkey")) {
                messageLabel.setText("Customer Aadhar not found!");
            } else if (e.getMessage().contains("booking_hotel_id_room_number_fkey")) {
                messageLabel.setText("Room number not available!");
            } else if (e.getMessage().contains("booking_check")) {
                messageLabel.setText("Check-in must be before check-out!");
            } else if (e.getMessage().contains("booking_status_check")) {
                messageLabel.setText("Invalid booking status!");
            } else {
                messageLabel.setText(e.getMessage());
            }
        } catch (Exception e) {
            messageLabel.setText("Unexpected error occurred.");
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
        } catch (IOException e) {
            messageLabel.setText("Failed to load page.");
            e.printStackTrace();
        }
    }
}