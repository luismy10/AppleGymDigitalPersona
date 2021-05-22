package applegymdigitalpersona;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FxDashboardController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private VBox vbContent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    private void eventRegistrarHuella() {
        try {
            URL url = getClass().getResource("FxRegistrarHuella.fxml");
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            FxRegistrarHuellaController controller = fXMLLoader.getController();
            Stage stage = WindowStage.StageLoaderModal(parent, "Registrar Huella", apWindow.getScene().getWindow());
            stage.setOnHiding(event -> {
                controller.stop();
            });
            stage.setOnShown(event -> {
                controller.Iniciar();
                controller.start();
                controller.EstadoHuellas();
            });
            stage.show();
        } catch (IOException ex) {
            System.out.println("Error en la view configuración:" + ex.getLocalizedMessage());
        }
    }

    private void eventMarcaEntreda() {
        try {
            URL url = getClass().getResource("FxRegistrarMarca.fxml");
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            FxRegistrarMarcaController controller = fXMLLoader.getController();
            Stage stage = WindowStage.StageLoaderModal(parent, "Marcar Entreda/Salida", apWindow.getScene().getWindow());
            stage.setOnHiding(event -> {
                controller.stop();
            });
            stage.setOnShown(event -> {
                controller.Iniciar();
                controller.start();
            });
            stage.show();
        } catch (IOException ex) {
            System.out.println("Error en la view configuración:" + ex.getLocalizedMessage());
        }
    }

    private void eventRutaServidor() {
        try {
            URL url = getClass().getResource("FxRutaServidor.fxml");
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            FxRutaServidorController controller = fXMLLoader.getController();
            Stage stage = WindowStage.StageLoaderModal(parent, "Ruta del Servidor", apWindow.getScene().getWindow());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Error en la view configuración:" + ex.getLocalizedMessage());
        }
    }

    @FXML
    private void onKeyPressedRegistrarHuella(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventRegistrarHuella();
        }
    }

    @FXML
    private void onActionRegistrarHuella(ActionEvent event) {
        eventRegistrarHuella();
    }

    @FXML
    private void onkeyPressedMarcarEntreda(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventMarcaEntreda();
        }
    }

    @FXML
    private void onActionMarcarEntreda(ActionEvent event) {
        eventMarcaEntreda();
    }

    @FXML
    private void onkeyPressedRutaServidor(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventRutaServidor();
        }
    }

    @FXML
    private void onActionRutaServidor(ActionEvent event) {
        eventRutaServidor();
    }

}
