package behaviors;

public class cardHovering {
    public static String defaultCardStyle() {
        return "-fx-background-color: #6b061c; -fx-background-radius: 8; " +
               "-fx-border-color: white; -fx-border-radius: 8; -fx-border-width: 1; " +
               "-fx-padding: 14; -fx-cursor: hand;";
    }

    public static String hoverCardStyle() {
        return "-fx-background-color: white; -fx-background-radius: 8; " +
               "-fx-border-color: white; -fx-border-radius: 8; -fx-border-width: 1; " +
               "-fx-padding: 14; -fx-cursor: hand;";
    } 
}
