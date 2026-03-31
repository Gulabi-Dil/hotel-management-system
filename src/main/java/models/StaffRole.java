package models;

public class StaffRole {
    private String role; private int total;
    public StaffRole(String role, int total) {
        this.role = role; this.total = total;
    }

    public String getRole() {return role;}
    public int getTotal() {return total;}
}
