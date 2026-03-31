package controllers;

import models.BillDetails;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class billPopupController {

    @FXML
    private Label bookingIdLabel;
    @FXML
    private Label customerNameLabel;
    @FXML
    private Label customerPhoneLabel;
    @FXML
    private Label roomInfoLabel;
    @FXML
    private Label checkInLabel;
    @FXML
    private Label checkOutLabel;
    @FXML
    private Label daysLabel;
    @FXML
    private Label rateLabel;
    @FXML
    private Label extraLabel;
    @FXML
    private Label taxLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private ImageView qrImageView;

    @FXML
    public void handleClose() {
        Stage stage = (Stage) totalLabel.getScene().getWindow();
        stage.close();
    }

    public void setBillDetails(BillDetails bill) {
        bookingIdLabel.setText("Booking #" + bill.getBookingId());
        customerNameLabel.setText(bill.getCustomerName());
        customerPhoneLabel.setText(bill.getCustomerPhone());
        roomInfoLabel.setText(bill.getRoomType() + " — #" + bill.getRoomNumber());
        checkInLabel.setText(bill.getCheckIn().toString());
        checkOutLabel.setText(bill.getCheckOut().toString());
        daysLabel.setText(String.valueOf(bill.getDaysStayed()));
        rateLabel.setText("₹" + bill.getPricePerDay() + " × " + bill.getDaysStayed() + " nights");
        extraLabel.setText("₹" + String.format("%.2f", bill.getExtraServices()));
        double tax = bill.getPricePerDay() * bill.getDaysStayed() * 0.2;
        taxLabel.setText("₹" + String.format("%.2f", tax));
        totalLabel.setText("₹" + String.format("%.2f", bill.getTotalBill()));

        // Load QR
        try {
            Image qr = new Image(getClass().getResource("/static/qr.png").toExternalForm());
            qrImageView.setImage(qr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}