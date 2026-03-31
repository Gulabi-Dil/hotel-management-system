package behaviors;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class buttonHovering {
    
    public static void notHovering(MouseEvent e) {
        Button btn = (Button) e.getSource();
        String behavior = "-fx-background-color: #6b061c; " +
                      "-fx-background-radius: 8; " +
                      "-fx-border-color: white; " +
                      "-fx-border-radius: 6; " +
                      "-fx-border-width: 1; " +
                      "-fx-text-fill: white;" + 
                      "-fx-cursor: hand;";
        btn.setStyle(behavior);
    }
    public static void hovering(MouseEvent e) {
        Button btn = (Button) e.getSource();
        String behavior = "-fx-background-color: white; " +
                      "-fx-background-radius: 8; " +
                      "-fx-border-color: white; " +
                      "-fx-border-radius: 6; " +
                      "-fx-border-width: 1; " +
                      "-fx-cursor: hand;" + 
                      "-fx-text-fill: black;";
        btn.setStyle(behavior);
    }
    public static void defaultButtonStyle(Button btn) {
        btn.setPrefSize(60, 25);
        btn.setStyle("-fx-background-color: #6b061c; " +
                      "-fx-background-radius: 8; " +
                      "-fx-border-color: white; " +
                      "-fx-border-radius: 6; " +
                      "-fx-border-width: 1; " +
                      "-fx-text-fill: white;" + 
                      "-fx-cursor: hand;"
                    );
    }
}
