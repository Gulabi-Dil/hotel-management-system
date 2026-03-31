package models;

public class Hotel {
    private int id; private String name; private String street; private String landmark;
    private String phones; private String rating; private boolean is_Active;
    public Hotel(int id, String name, String street, String landmark, String phones, String rating, boolean is_Active) {
        this.id=id; this.name=name; this.street=street; this.is_Active = is_Active;
        this.landmark=landmark; this.phones=phones; this.rating=rating;
    }

    public int getId() {return id;}
    public String getName() {return name;}
    public String getStreet() {return street;}
    public String getLandmark() {return landmark;}
    public String getPhones() {return phones;}
    public String getRating() {return rating;}
    public boolean isActive() {return is_Active;}
}

