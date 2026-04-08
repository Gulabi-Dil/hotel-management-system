package controllers;

import java.sql.SQLException;
import java.util.List;
import models.Customer;
import utils.TableUtils;
import dao.customerDao;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;
import behaviors.buttonHovering;

public class customerController {

    @FXML
    private TextField customerNameField, aadharField, phoneField, emailField, stateField, cityField, streetField,
            searchField;
    @FXML
    private ComboBox<String> genderField;
    @FXML
    private Button addButton, toggleDeletedButton;
    @FXML
    private Label messageLabel;
    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, String> colName, colAadhar, colGender, colPhone, colEmail, colState, colCity,
            colStreet;
    @FXML
    private TableColumn<Customer, Void> colActions;
    @FXML
    private AnchorPane root;
    private boolean editing = false;
    private boolean showingDeleted = false;
    private Customer editingCustomer = null;
    private List<Customer> allCustomers;

    @FXML
    public void hoverBehavior(MouseEvent e) {
        buttonHovering.hovering(e);
    }

    @FXML
    public void defaultBehavior(MouseEvent e) {
        buttonHovering.notHovering(e);
    }

    private void addTooltipToColumn(TableColumn<Customer, String> column) { // for attribute which appear partially due
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

    public void initialize() {
        setupTable();
        loadAllCustomers();
        genderField.getItems().addAll("Male", "Female", "Other");
        addTooltipToColumn(colName);
        addTooltipToColumn(colPhone);
        addTooltipToColumn(colEmail);
        addTooltipToColumn(colState);
        addTooltipToColumn(colCity);
        addTooltipToColumn(colStreet);
        customerTable.getSelectionModel().clearSelection();
        customerTable.setFocusTraversable(false);
        root.requestFocus();
    }

    private void loadAllCustomers() {
        allCustomers = new customerDao().getCustomers();
        customerTable.getItems().setAll(allCustomers);
        setupSearch();
    }

    private void settingText(Customer customer) {
        customerNameField.setText(customer.getName());
        aadharField.setText(customer.getAadhar());
        genderField.setValue(customer.getGender());
        phoneField.setText(customer.getPhone());
        emailField.setText(customer.getEmail());
        stateField.setText(customer.getState());
        cityField.setText(customer.getCity());
        streetField.setText(customer.getStreet());
    }

    private void clearFields() {
        customerNameField.clear();
        aadharField.clear();
        genderField.setValue(null);
        phoneField.clear();
        emailField.clear();
        stateField.clear();
        cityField.clear();
        streetField.clear();
    }

    private void setupTable() {

        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colAadhar.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAadhar()));
        colGender.setCellValueFactory(data -> {
            String genderWord = data.getValue().getGender();
            String genderLetter;
            switch (genderWord) {
                case "Male" -> genderLetter = "M";
                case "Female" -> genderLetter = "F";
                default -> genderLetter = "X";
            }
            return new SimpleStringProperty(genderLetter);
        });
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colState.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getState()));
        colCity.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCity()));
        colStreet.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStreet()));

        TableUtils.centerColumns(colName, colAadhar, colGender, colPhone, colEmail, colState, colCity, colStreet,
                colActions);
        TableUtils.fixColumns(customerTable, colName, colAadhar, colGender, colPhone, colEmail, colState, colCity,
                colStreet, colActions);
        TableUtils.styleTable(customerTable);

        TableUtils.addActionButtons(
                colActions,
                Customer -> {
                    editing = true;
                    editingCustomer = Customer;
                    settingText(Customer);
                    messageLabel.setText("Editing Customer...");
                    addButton.setText("Update");
                },
                customer -> {
                    try {
                        String error = new customerDao().softDeleteCustomer(customer.getAadhar());
                        if (error != null) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Cannot Delete Customer");
                            alert.setHeaderText(null);
                            alert.setContentText(error);
                            alert.showAndWait();
                        } else {
                            loadAllCustomers();
                        }
                    } catch (SQLException e) {
                        messageLabel.setText("Database error: " + e.getMessage());
                    }
                });
    }

    private void setupSearch() {
        TableUtils.setupSearch(
                searchField,
                allCustomers,
                customerTable,
                c -> {
                    String lower = searchField.getText().toLowerCase();
                    return c.getAadhar().startsWith(lower);
                    // return c.getName().toLowerCase().startsWith(lower) ||
                    // c.getAadhar().startsWith(lower) ||
                    // c.getPhone().startsWith(lower) ||
                    // c.getEmail().toLowerCase().startsWith(lower) ||
                    // c.getState().toLowerCase().startsWith(lower) ||
                    // c.getCity().toLowerCase().startsWith(lower);
                });
    }

    @FXML
    public void handleAddCustomer() {
        try {
            String name = customerNameField.getText().trim();
            String aadhar = aadharField.getText().trim();
            String gender = genderField.getValue();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String state = stateField.getText().trim();
            String city = cityField.getText().trim();
            String street = streetField.getText().trim();

            if (name.isEmpty() || aadhar.isEmpty() || gender == null || phone.isEmpty() || email.isEmpty()
                    || state.isEmpty() || city.isEmpty() || street.isEmpty()) {
                messageLabel.setText("Please fill all fields.");
                return;
            }

            if (!phone.matches("(\\+91)?\\d{10}")) {
                messageLabel.setText("Invalid phone: " + phone + ". Must be 10 digits.");
                return;
            }
            if (!aadhar.matches("\\d{12}")) {
                messageLabel.setText("Aadhaar must be exactly 12 digits.");
                return;
            }
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                messageLabel.setText("Invalid email format.");
                return;
            }
            Customer customer = new Customer(aadhar, name, gender, phone, email, state, city, street, true);
            customerDao daob = new customerDao();
            if (!editing) {
                String result = daob.addCustomer(customer);
                messageLabel.setText(result.equals("success") ? name + "'s details added successfully!" : result);
            } else {
                daob.updateCustomer(customer, editingCustomer.getAadhar());
                messageLabel.setText(name + "'s details updated successfully!");
                addButton.setText("Add");
                editing = false;
                editingCustomer = null;
            }
            clearFields();
            loadAllCustomers();
            root.requestFocus();
        } catch (SQLException e) {

            String code = e.getSQLState();

            switch (code) {
                case "22001" -> messageLabel.setText("Invalid value length");
                case "23505" ->
                    messageLabel.setText("Aadhar or phone number already exists.");

                case "23514" ->
                    messageLabel.setText("Invalid gender value. Use Male/Female/Other.");

                case "23502" ->
                    messageLabel.setText("Some required fields are missing.");

                case "22P02" ->
                    messageLabel.setText("Invalid input format.");

                default -> {
                    messageLabel.setText("Database error: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    public void handleToggleDeleted() {
        showingDeleted = !showingDeleted;
        if (showingDeleted) {
            List<Customer> deleted = new customerDao().getDeletedCustomers();
            customerTable.getItems().setAll(deleted);
            toggleDeletedButton.setText("Show Active");
            messageLabel.setText("Showing deleted customers.");
        } else {
            loadAllCustomers();
            toggleDeletedButton.setText("Show Deleted");
            messageLabel.setText("");
        }
    }

}
