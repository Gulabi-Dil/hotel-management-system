package dao;

import utils.DBConnection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.sql.Connection;
import java.sql.ResultSet;

public class userDao {

    public boolean validateLogin(String usernameOrEmail, String password) {
        String query = "SELECT * FROM Appuser WHERE (username = ? OR email = ?) AND password = crypt(?, password)";
        try (Connection conn = DBConnection.getConnection();PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            stmt.setString(3, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createAccount(String name, String gender, java.time.LocalDate dob, String username, String password,
            String email) throws SQLException {
        String query = "INSERT INTO Appuser (name,gender,dob,username,password,email) VALUES(?,?,?,?,?,?)";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, gender);
            stmt.setDate(3, java.sql.Date.valueOf(dob));
            stmt.setString(4, username);
            stmt.setString(5, password);
            stmt.setString(6, email);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (DateTimeException e) {
            throw new RuntimeException("Invalid date format.", e);
        } catch (SQLException e) {
            // Identify constraint violation
            switch (e.getSQLState()) {
                case "23505": // unique violation
                    if (e.getMessage().contains("appuser_email_key"))
                        throw new SQLException("Email already exists.", e);
                    if (e.getMessage().contains("appuser_username_key"))
                        throw new SQLException("Username already exists.", e);
                    break;

                case "23514": // check violation
                    if (e.getMessage().contains("appuser_gender_check"))
                        throw new SQLException("Gender must be 'Male', 'Female', or 'Other'.", e);
                    if (e.getMessage().contains("appuser_username_check"))
                        throw new SQLException("Username format invalid.", e);
                    break;

                case "23502": // not null
                    throw new SQLException("All fields are required.", e);

                default:
                    throw e; // unknown exceptions
            }
        }
        return false;
    }
}
