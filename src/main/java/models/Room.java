package models;

public class Room {
    private int hotelId; private int roomNum; private String type; private String status; private double pricePerDay;
    
    public Room(int hotelId, int roomNum, String type, double pricePerDay, String status) {
        this.hotelId = hotelId; this.roomNum=roomNum; this.type=type; this.pricePerDay = pricePerDay; this.status = status;
    }

    public int getHotelId() {return hotelId;} 
    public int getRoomNum() {return roomNum;} 
    public String getType() {return type;} 
    public double getPricePerDay() {return pricePerDay;}
    public String getStatus() {return status;}
}
