package models;

import java.time.LocalDate;

public class BillDetails {
    private int bookingId;
    private String customerName;
    private String customerPhone;
    private String roomType;
    private int roomNumber;
    private double pricePerDay;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int daysStayed;
    private double extraServices;
    private double totalBill;

    public BillDetails(int bookingId, String customerName, String customerPhone, String roomType, int roomNumber, double pricePerDay,
                       LocalDate checkIn, LocalDate checkOut, int daysStayed, double extraServices, double totalBill) {
        
        this.bookingId = bookingId; this.customerName = customerName; this.customerPhone = customerPhone; this.roomType = roomType;
        this.roomNumber = roomNumber; this.pricePerDay = pricePerDay; this.checkIn = checkIn; this.checkOut = checkOut;
        this.daysStayed = daysStayed; this.extraServices = extraServices; this.totalBill = totalBill;
    }

    public int getBookingId() { return bookingId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getRoomType() { return roomType; }
    public int getRoomNumber() { return roomNumber; }
    public double getPricePerDay() { return pricePerDay; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getDaysStayed() { return daysStayed; }
    public double getExtraServices() { return extraServices; }
    public double getTotalBill() { return totalBill; }
}