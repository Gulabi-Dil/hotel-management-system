package models;

import java.time.LocalDate;

public class Payment {
    private int bookingId; private double extraService; private LocalDate payDate;
    private String mode; private String status;
    private double rating; private double bill; private String bookingStatus;

    public Payment(int bookingId, double extraService, LocalDate payDate, String mode, String status, double rating, double bill, String bookingStatus) {
        this.bookingId = bookingId; this.extraService = extraService;
        this.payDate = payDate; this.mode = mode;
        this.status = status; this.rating = rating;
        this.bill = bill; this.bookingStatus = bookingStatus;
    }

    public int getBookingId() { return bookingId; }
    public double getExtraService() { return extraService; }
    public LocalDate getPayDate() { return payDate; }
    public String getMode() { return mode; }
    public String getStatus() { return status; }
    public double getRating() { return rating; }
    public double getBill() { return bill; }
    public String getBookingStatus() { return bookingStatus; }
}