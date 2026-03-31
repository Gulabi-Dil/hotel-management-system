package models;

public class Customer {
    private String name; private String aadhar; private String gender; private String phone; private String email; private String state;
    private String city; private String street; private boolean is_Active;

    public Customer(String aadhar, String name, String gender, String phone, String email, String state, String city, String street, boolean is_Active) {
        this.name=name; this.aadhar=aadhar; this.gender=gender; this.phone=phone; this.email=email; this.state = state; 
        this.city=city; this.street=street; this.is_Active = is_Active;
    }
    public String getName() {return name;}
    public String getAadhar() {return aadhar;}
    public String getGender() {return gender;}
    public String getPhone() {return phone;}
    public String getEmail() {return email;}
    public String getState() {return state;}
    public String getCity() {return city;}
    public String getStreet() {return street;}
    public boolean isActive() {return is_Active;}
}
