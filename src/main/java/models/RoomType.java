package models;

public class RoomType {
    private String type; private int available; private int total; 
    public RoomType(String type, int available, int total) {
        this.type=type; this.available=available; this.total=total;
    }

    public String getType() {return type;}
    public int getAvailable() {return available;}
    public int getTotal() {return total;}
}
