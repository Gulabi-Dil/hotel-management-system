package models;
import java.time.LocalDate;
public class Booking {
    private int bookingId; private int hotelId; private String roomType; private int roomNum; private String aadhar; private String status; 
    private LocalDate checkIn; private LocalDate checkOut; 

    public Booking(int bookingId,String aadhar, int hotelId, String roomType, int roomNum, LocalDate checkIn, String status, LocalDate checkOut) {
        this.bookingId = bookingId; this.aadhar=aadhar; this.hotelId=hotelId; this.roomType=roomType; 
        this.roomNum=roomNum; this.checkIn=checkIn; this.status=status; this.checkOut=checkOut;

    }

    public int getBookingId() {return bookingId;}    
    public String getAadhar() {return aadhar;}
    public int getHotelId() {return hotelId;}
    public String getRoomType() {return roomType;}
    public int getRoomNum() {return roomNum;}
    public LocalDate getCheckIn() {return checkIn;}
    public String getStatus() {return status;}
    public LocalDate getCheckOut() {return checkOut;}

    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public void setAadhar(String aadhar) { this.aadhar = aadhar; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setRoomNum(int roomNum) { this.roomNum = roomNum; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    public void setStatus(String status) { this.status = status; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

}
