import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("./fxml/login.fxml"));
        stage.setTitle("Hotel Management System");
        stage.setResizable(false);
        stage.setScene(new Scene(root,1000,700));
        stage.show();
    }
    public static void main(String args[]) {
        launch(args);
    }
}