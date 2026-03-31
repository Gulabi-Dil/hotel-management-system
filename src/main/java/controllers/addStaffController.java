package controllers;

import models.Staff;
import javafx.scene.control.*;
import dao.staffDao;
import javafx.fxml.FXML;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class addStaffController {

    @FXML
    private TextField staffIDField, staffNameField, staffPhoneField, staffEmailField, staffSalaryField;
    @FXML
    private ComboBox<String> staffGenderField, staffRoleField;
    @FXML
    private DatePicker staffJoiningField;
    @FXML
    private Button backButton, actionButton;
    @FXML
    Label messageLabel;

    private int hotelId;
    private boolean editing = false;
    private Staff editStaff = null;

    public void setHotelId(int id) {
        this.hotelId = id;
    }

    public void setEdit(Staff staff) {
        editing = true;
        editStaff = staff;
        staffIDField.setText(String.valueOf(staff.getStaffId()));
        staffNameField.setText(staff.getName());
        staffSalaryField.setText(String.valueOf(staff.getSalary()));
        staffGenderField.setValue(staff.getGender());
        staffPhoneField.setText(staff.getPhone());
        staffEmailField.setText(staff.getEmail());
        staffJoiningField.setValue(staff.getJoined());
        staffRoleField.setValue(staff.getRole());
        actionButton.setText("Update");
    }

    @FXML
    public void initialize() {
        staffGenderField.getItems().addAll("Male", "Female", "Other");
        staffRoleField.getItems().addAll("Manager", "Receptionist", "Housekeeping", "Chef", "Waiter", "Maintenance","Security");
        System.out.println(staffPhoneField);
    }

    @FXML
    public void handleAddStaff() {
        try {

            String idText = staffIDField.getText().trim();
            String name = staffNameField.getText().trim();
            String phone = staffPhoneField.getText().trim();
            String email = staffEmailField.getText().trim();
            String gender = staffGenderField.getValue();
            String role = staffRoleField.getValue();
            String salary = staffSalaryField.getText().trim();
            LocalDate joining = staffJoiningField.getValue();

            if (idText.isEmpty() || name.isEmpty() || phone.isEmpty() || email.isEmpty() || gender == null ||
                    role == null || salary.isEmpty() || joining == null) {
                messageLabel.setText("Please fill in all fields.");
                return;
            }
            try {
                joining = staffJoiningField.getValue();
                if (joining == null) {
                    messageLabel.setText("Please enter valid dates for Joining.");
                    return;
                }
            } catch (DateTimeParseException e) {
                messageLabel.setText("Please enter valid dates in the Joining fields.");
                return;
            }

            int staffIdNum; double staffSala;
            try {
                staffIdNum = Integer.parseInt(idText); staffSala = Double.parseDouble(salary);
            } catch (NumberFormatException e) {
                messageLabel.setText("Staff ID and Salary must be numerical.");
                return;
            }
            if (!phone.matches("(\\+91)?\\d{10}")) {
                messageLabel.setText("Invalid phone: " + phone + ". Must be 10 digits.");
                return;
            }
            
            if(staffSala <=0) {
                messageLabel.setText("Salary must be greater than 0."); return;
            }

            Staff staff = new Staff(hotelId, staffIdNum, name, gender, phone, email, joining, staffSala, role);
            staffDao dao = new staffDao();

            if (!editing) {
                dao.addStaff(staff);
            } else {
                dao.updateStaff(staff, editStaff.getStaffId());
            }
            actionButton.getScene().getWindow().hide();

        } catch (SQLException e) {
            // Unique constraint violation (duplicate phone/email)
            if ("23505".equals(e.getSQLState())) {
                messageLabel.setText("Staff ID, Phone or Email already exists for this hotel.");
            }
            // Check constraint violation (gender/role constraints, DOB/Joining logic)
            else if ("23514".equals(e.getSQLState())) {
                messageLabel.setText("Invalid data entered. Please check all fields.");
            } else {
                messageLabel.setText("Database error: " + e.getMessage());
            }
        } catch (Exception e) {
            messageLabel.setText("Unexpected error occurred.");
            e.printStackTrace();
        }
    }

}
