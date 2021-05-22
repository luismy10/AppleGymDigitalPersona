package applegymdigitalpersona;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AppleGymDigitalPersona extends Application {

    public static String DOMINIO_SERVER = "";

    @Override
    public void start(Stage stage) throws Exception {
        URL url = getClass().getResource("FxDashboard.fxml");
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        Scene scene = new Scene(parent);
        stage.getIcons().add(new Image("/applegymdigitalpersona/icon.jpg"));
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setOnShowing(event -> {
            String rutaVenta = "./rutas/servidor.properties";
            try (InputStream input = new FileInputStream(rutaVenta)) {
                Properties prop = new Properties();
                prop.load(input);
                AppleGymDigitalPersona.DOMINIO_SERVER = prop.getProperty("ruta");
            } catch (IOException ex) {
                AppleGymDigitalPersona.DOMINIO_SERVER = "";
            }
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
