package applegymdigitalpersona;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class FxRutaServidorController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtRuta;
    @FXML
    private HBox hbBody;
    @FXML
    private HBox hbLoad;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtRuta.setText(AppleGymDigitalPersona.DOMINIO_SERVER);
    }

    private void onEventGuardar() {
        if (txtRuta.getText().trim().equalsIgnoreCase("")) {
            txtRuta.requestFocus();
        } else {
            ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
                Thread t = new Thread(runnable);
                t.setDaemon(true);
                return t;
            });

            Task<String> task = new Task<String>() {
                @Override
                public String call() {
                    String ruta = "./rutas/servidor.properties";
                    File file = new File(ruta);
                    Properties prop = new Properties();
                    try {
                        if (file.exists()) {
                            try (InputStream input = new FileInputStream(ruta)) {
                                prop.load(input);
                                try (OutputStream output = new FileOutputStream(ruta)) {
                                    prop.setProperty("ruta", txtRuta.getText().trim());
                                    prop.store(output, "Se creó la ruta del servidor.");
                                    AppleGymDigitalPersona.DOMINIO_SERVER = txtRuta.getText().trim();
                                }
                            }
                            return "1";
                        } else {
                            try (OutputStream output = new FileOutputStream(ruta)) {
                                prop.setProperty("ruta", txtRuta.getText().trim());
                                prop.store(output, "Se creó la ruta del servidor.");
                                AppleGymDigitalPersona.DOMINIO_SERVER = txtRuta.getText().trim();
                            }
                            return "1";
                        }
                    } catch (IOException ex) {
                        return ex.getLocalizedMessage();
                    }
                }
            };

            task.setOnScheduled(w -> {
                hbBody.setDisable(true);
                hbLoad.setVisible(true);
            });

            task.setOnFailed(w -> {
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                setAlert(Alert.AlertType.ERROR, "Ruta Servidor", "Se produjo un error interno, comuníquese con su proveedor.");
            });

            task.setOnSucceeded(w -> {
                String result = task.getValue();
                if (result.equalsIgnoreCase("1")) {
                    setAlert(Alert.AlertType.INFORMATION, "Ruta Servidor", "Se registro correctamtente la ruta");
                    Stage stage = (Stage) apWindow.getScene().getWindow();
                    stage.close();
                } else {
                    setAlert(Alert.AlertType.WARNING, "Ruta Servidor", result);
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                }
            });

            exec.execute(task);
            if (!exec.isShutdown()) {
                exec.shutdown();
            }
        }
    }

    public void setAlert(Alert.AlertType alertType, String title, String text) {
        Platform.runLater(() -> {
            final URL url = FxRegistrarHuellaController.class.getClass().getResource("/applegymdigitalpersona/light_theme.css");
            Alert alert = new Alert(alertType);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new javafx.scene.image.Image("/applegymdigitalpersona/icon.jpg"));
            alert.getDialogPane().getStylesheets().add(url.toExternalForm());
            alert.setHeaderText(null);
            alert.setTitle(title);
            alert.setContentText(text);
            alert.showAndWait();
        });
    }

    @FXML

    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventGuardar();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        onEventGuardar();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Stage stage = (Stage) apWindow.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Stage stage = (Stage) apWindow.getScene().getWindow();
        stage.close();
    }

}
