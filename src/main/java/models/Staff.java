package models;
import java.time.LocalDate;

public class Staff {
    private int hotelId; private int staffId; private String name; private double salary; private String gender; private String phone; private String email;
    private LocalDate joinDate; private String role;

    public Staff(int hotelId, int staffId, String name, String gender, String phone, String email, LocalDate joinDate,double salary, String role) {
        this.hotelId = hotelId; this.staffId = staffId; this.name = name; this.salary = salary; this.joinDate = joinDate;
        this.gender = gender; this.phone = phone; this.email = email; this.role = role;
    }
    public int getHotelId() {return hotelId;}
    public int getStaffId() {return staffId;}
    public String getName() {return name;}
    public double getSalary() {return salary;}
    public String getGender() {return gender;}
    public String getPhone() {return phone;}
    public String getEmail() {return email;}
    public LocalDate getJoined() {return joinDate;}
    public String getRole() {return role;}
}
