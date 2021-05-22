package applegymdigitalpersona;

import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class FxRegistrarHuellaController implements Initializable {

    @FXML
    private ImageView ivImage;
    @FXML
    private TextArea txtDescripcion;
    @FXML
    private Button btnGuardar;
    @FXML
    private HBox hbBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private RadioButton rbCliente;
    @FXML
    private RadioButton rbPersonal;

    private final DPFPCapture lector = DPFPGlobal.getCaptureFactory().createCapture();

    private final DPFPEnrollment recalculador = DPFPGlobal.getEnrollmentFactory().createEnrollment();

    private DPFPTemplate template;

    private DPFPFeatureSet featuresinscripcion;

    private Image image;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnGuardar.setDisable(true);
        ToggleGroup toggleGroup = new ToggleGroup();
        rbCliente.setToggleGroup(toggleGroup);
        rbPersonal.setToggleGroup(toggleGroup);
    }

    public void Iniciar() {
        lector.addDataListener(new DPFPDataAdapter() {
            @Override
            public void dataAcquired(final DPFPDataEvent e) {
                Platform.runLater(() -> {
                    EnviarTexto("La Huella Digital ha sido Capturada");
                    ProcesarCaptura(e.getSample());
                });
            }
        });

        lector.addReaderStatusListener(new DPFPReaderStatusAdapter() {
            @Override
            public void readerConnected(final DPFPReaderStatusEvent e) {
                Platform.runLater(() -> {
                    EnviarTexto("El Sensor de Huella Digital esta Activado o Conectado");
                });
            }

            @Override
            public void readerDisconnected(final DPFPReaderStatusEvent e) {
                Platform.runLater(() -> {
                    EnviarTexto("El Sensor de Huella Digital esta Desactivado o no Conectado");
                });
            }
        });

        lector.addSensorListener(new DPFPSensorAdapter() {
            @Override
            public void fingerTouched(final DPFPSensorEvent e) {
                Platform.runLater(() -> {
                    EnviarTexto("El dedo ha sido colocado sobre el Lector de Huella");
                });
            }

            @Override
            public void fingerGone(final DPFPSensorEvent e) {
                Platform.runLater(() -> {
                    EnviarTexto("El dedo ha sido quitado del Lector de Huella");
                });
            }
        });

        lector.addErrorListener(new DPFPErrorAdapter() {
            public void errorReader(final DPFPErrorEvent e) {
                Platform.runLater(() -> {
                    EnviarTexto("Error: " + e.getError());
                });
            }
        });
    }

    public void ProcesarCaptura(DPFPSample sample) {
        featuresinscripcion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
        if (featuresinscripcion != null) {
            try {
                recalculador.addFeatures(featuresinscripcion);
                image = CrearImagenHuella(sample);
                DibujarHuella(image);
            } catch (DPFPImageQualityException ex) {
                System.err.println("Error: " + ex.getMessage());
            } finally {
                EstadoHuellas();
                switch (recalculador.getTemplateStatus()) {
                    case TEMPLATE_STATUS_READY:
                        stop();
                        setTemplate(recalculador.getTemplate());
                        EnviarTexto("Listo la huella ha sido capturado correctamente ahora puede guardarla");
                        setAlert(Alert.AlertType.INFORMATION, "Inscripcion de Huellas Dactilares", "Listo la huella ha sido capturado correctamente ahora puede guardarla");
                        btnGuardar.setDisable(false);
                        break;
                    case TEMPLATE_STATUS_FAILED:
                        recalculador.clear();
                        btnGuardar.setDisable(true);
                        stop();
                        EstadoHuellas();
                        setTemplate(null);
                        setAlert(Alert.AlertType.WARNING, "Inscripcion de Huellas Dactilares", "La Plantilla de la Huella no pudo ser creada, Repita el Proceso");
                        start();
                        break;
                }
            }
        }
    }

    public DPFPFeatureSet extraerCaracteristicas(DPFPSample sample, DPFPDataPurpose purpose) {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            return null;
        }
    }

    public Image CrearImagenHuella(DPFPSample sample) {
        return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }

    public void DibujarHuella(Image image) {
        BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();
        ivImage.setImage(SwingFXUtils.toFXImage(bimage, null));
    }

    public void setAlert(AlertType alertType, String title, String text) {
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

    private void onEventRehacer() {
        Platform.runLater(() -> {
            txtDescripcion.clear();
            ivImage.setImage(new javafx.scene.image.Image("/applegymdigitalpersona/noimage.jpg"));
            btnGuardar.setDisable(true);
            if (lector.isStarted()) {
                stop();
                recalculador.clear();
                EstadoHuellas();
                setTemplate(null);
                start();
            } else {
                recalculador.clear();
                EstadoHuellas();
                setTemplate(null);
                start();
            }
        });
    }

    private void guardarHuella() {
        final URL urlStyle = FxRegistrarHuellaController.class.getClass().getResource("/applegymdigitalpersona/light_theme.css");
        TextInputDialog dialog = new TextInputDialog("");
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image("/applegymdigitalpersona/icon.jpg"));
        dialog.getDialogPane().getStylesheets().add(urlStyle.toExternalForm());
        dialog.setTitle(null);
        dialog.setHeaderText("Ingrese el número de dni");
        dialog.setContentText("Preciona enter para continuar:");

        Optional<String> text = dialog.showAndWait();
        if (text.isPresent()) {
            if (text.get().trim().equalsIgnoreCase("") || text.get().trim() == null) {
                setAlert(Alert.AlertType.INFORMATION, "Guardar Huella", "No ingresó ningún dato para iniciar el proceso.");
                return;
            }

            ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
                Thread t = new Thread(runnable);
                t.setDaemon(true);
                return t;
            });

            Task<String> task = new Task<String>() {
                @Override
                public String call() {
                    try {
                        String encodedString = Base64.getEncoder().encodeToString(template.serialize());

                        Finger finger = new Finger();
                        finger.setTipo(rbCliente.isSelected() ? "cliente" : "personal");
                        finger.setDni(text.get().trim());
                        finger.setHuella(encodedString);
                        finger.setImageHuella(getEncodeImage(image));                 
                        String object = new Gson().toJson(finger);

                        URL url = new URL(AppleGymDigitalPersona.DOMINIO_SERVER + "/app/cliente/ApiFinger.php");
                        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                        httpCon.setRequestProperty("Accept", "*/*");
                        httpCon.setRequestProperty("Content-Type", "application/json");
                        httpCon.setDoOutput(true);
                        httpCon.setRequestMethod("POST");
                        try (OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream())) {
                            out.write(object);
                        }

                        int responseCode = httpCon.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {

                            StringBuilder buffer;
                            try (BufferedReader recv = new BufferedReader(new InputStreamReader(httpCon.getInputStream()))) {
                                buffer = new StringBuilder();
                                int read;
                                char[] chars = new char[1024];
                                while ((read = recv.read(chars)) != -1) {
                                    buffer.append(chars, 0, read);
                                }
                            }

                            //System.out.println(buffer.toString());
                            JsonElement jsonElementData = new JsonParser().parse(buffer.toString());
                            JsonObject jsonObjectData = jsonElementData.getAsJsonObject();
                            if (jsonObjectData.get("estado").getAsString().equalsIgnoreCase("1")) {
                                return "1";
                            } else {
                                return jsonObjectData.get("response").getAsString();
                            }
                        } else {
                            return "No se puedo establecer una conexión con el servidor intente nuevamente.";
                        }
                    } catch (IOException ex) {
                        return "Error catch: " + ex.getMessage();
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
                setAlert(Alert.AlertType.ERROR, "Guardar Huella", task.getException().getLocalizedMessage());
                onEventRehacer();
            });

            task.setOnSucceeded(w -> {
                String result = task.getValue();
                if (result.equalsIgnoreCase("1")) {
                    setAlert(Alert.AlertType.INFORMATION, "Guardar Huella", "Se actualizó correctamente la huella.");
                } else {
                    setAlert(Alert.AlertType.WARNING, "Guardar Huella", result);
                }
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                onEventRehacer();
            });

            exec.execute(task);
            if (!exec.isShutdown()) {
                exec.shutdown();
            }
        }
    }

    private String getEncodeImage(Image imageHuella) throws IOException {
        ImageIcon icon = new ImageIcon(imageHuella);
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        byte[] imageInByte;
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(icon.getImage(), 0, 0, icon.getImageObserver());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "jpg", baos);
            baos.flush();
            imageInByte = baos.toByteArray();
        }
        return Base64.getEncoder().encodeToString(imageInByte);
    }

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            guardarHuella();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        guardarHuella();
    }

    @FXML
    private void onKeyPressedRehacer(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventRehacer();
        }
    }

    @FXML
    private void onActionRehacer(ActionEvent event) {
        onEventRehacer();
    }

    public void EnviarTexto(String string) {
        txtDescripcion.appendText(string + "\n");
    }

    public void start() {
        lector.startCapture();
        EnviarTexto("Utilizando el Lector de Huella Dactilar ");
    }

    public void stop() {
        lector.stopCapture();
        EnviarTexto("No se está usando el Lector de Huella Dactilar ");
    }

    public void EstadoHuellas() {
        EnviarTexto("\nMuestra de Huellas Restantes para Guardar " + recalculador.getFeaturesNeeded());
    }

    public void setTemplate(DPFPTemplate template) {
        this.template = template;
    }

    public DPFPTemplate getTemplate() {
        return template;
    }

}
