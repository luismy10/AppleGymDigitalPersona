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
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.javafx.application.PlatformImpl;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.AudioClip;

public class FxRegistrarMarcaController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblDni;
    @FXML
    private Label lblDatos;
    @FXML
    private Label lblFecha;
    @FXML
    private ImageView ivImage;
    @FXML
    private TextArea txtDescripcion;
    @FXML
    private HBox hbBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMensaje;
    @FXML
    private TableView<Membresia> tvLista;
    @FXML
    private TableColumn<Membresia, String> tcPlan;
    @FXML
    private TableColumn<Membresia, String> tcFecha;
    @FXML
    private TableColumn<Membresia, String> tcEstado;
    @FXML
    private ImageView ivEstado;
    @FXML
    private RadioButton rbCliente;
    @FXML
    private RadioButton rbPersonal;
    @FXML
    private Label lblProcesando;

    private final DPFPCapture lector = DPFPGlobal.getCaptureFactory().createCapture();

    private final DPFPEnrollment recalculador = DPFPGlobal.getEnrollmentFactory().createEnrollment();

    private final DPFPVerification verificador = DPFPGlobal.getVerificationFactory().createVerification();

    public static String TEMPLATE_PROPERTY = "template";

    private DPFPFeatureSet featuresinscripcion;

    private DPFPFeatureSet featuresverificacion;

    private final javafx.scene.image.Image imageQuestion = new javafx.scene.image.Image("/applegymdigitalpersona/confirmation.png");

    private final javafx.scene.image.Image imageWarning = new javafx.scene.image.Image("/applegymdigitalpersona/warning.png");

    private final javafx.scene.image.Image imageError = new javafx.scene.image.Image("/applegymdigitalpersona/error.png");

    private final javafx.scene.image.Image imageExit = new javafx.scene.image.Image("/applegymdigitalpersona/exit.png");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcPlan.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNombre()));
        tcFecha.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFechaInicio() + "\n" + cellData.getValue().getFechaFinal()));
        tcEstado.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMembresia() == 1 ? "ACTIVA" : "POR VENCER"));
        ivEstado.setImage(imageQuestion);
        ToggleGroup toggleGroup = new ToggleGroup();
        rbCliente.setToggleGroup(toggleGroup);
        rbPersonal.setToggleGroup(toggleGroup);
    }

    protected void Iniciar() {
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

    private void ProcesarCaptura(DPFPSample sample) {
        featuresinscripcion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
        featuresverificacion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
        if (featuresinscripcion != null) {
            try {
                EnviarTexto("Las Caracteristicas de la Huella han sido creada");
                recalculador.addFeatures(featuresinscripcion);
                Image image = CrearImagenHuella(sample);
                DibujarHuella(image);
                identificarHuella();
            } catch (DPFPImageQualityException ex) {
                lblDni.setText("--");
                lblDatos.setText("--");
                lblFecha.setText("--");
                lblMensaje.setText(ex.getLocalizedMessage());
                ivImage.setImage(new javafx.scene.image.Image("/applegymdigitalpersona/noimage.jpg"));
                onEventRehacer();
            }
        }
    }

    public void identificarHuella() {
        if (rbCliente.isSelected()) {
            loadHuellasClientes();
        } else {
            loadHuellasEmpleados();
        }
    }

    private void loadHuellasClientes() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                try {
                    URL obj = new URL(AppleGymDigitalPersona.DOMINIO_SERVER + "/app/cliente/ApiFinger.php?type=listaclientes");
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Accept-Charset", "UTF-8");

                    int responseCode = con.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        StringBuilder buffer;
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                            buffer = new StringBuilder();
                            int read;
                            char[] chars = new char[1024];
                            while ((read = reader.read(chars)) != -1) {
                                buffer.append(chars, 0, read);
                            }
                        }

                        JsonElement jsonElementData = new JsonParser().parse(buffer.toString());
                        JsonObject jsonObjectData = jsonElementData.getAsJsonObject();
                        if (jsonObjectData.get("estado").toString().equalsIgnoreCase("1")) {

                            JsonParser parser = new JsonParser();
                            JsonArray gsonArr = parser.parse(jsonObjectData.get("data").toString()).getAsJsonArray();

                            boolean valdiateIngreso = false;
                            String idCliente = "";
                            String dni = "";
                            String apellidos = "";
                            String nombres = "";
                            ArrayList<Membresia> membresias = new ArrayList();
                            for (JsonElement jsonElement : gsonArr) {
                                JsonObject gsonObj = jsonElement.getAsJsonObject();
                                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate();
                                byte[] templateBuffer = Base64.getDecoder().decode(gsonObj.get("huella").getAsString());
                                referenceTemplate.deserialize(templateBuffer);
                                referenceTemplate.deserialize(templateBuffer);
                                DPFPVerificationResult result = verificador.verify(featuresverificacion, referenceTemplate);

                                if (result.isVerified()) {
                                    valdiateIngreso = true;
                                    idCliente = gsonObj.get("idCliente").getAsString();
                                    dni = gsonObj.get("dni").getAsString();
                                    apellidos = gsonObj.get("apellidos").getAsString();
                                    nombres = gsonObj.get("nombres").getAsString();
                                    JsonArray gsonArrMem = parser.parse(gsonObj.get("membresias").toString()).getAsJsonArray();

                                    for (JsonElement jsonElementMem : gsonArrMem) {
                                        JsonObject gsonObjMem = jsonElementMem.getAsJsonObject();
                                        LocalDate dateInicio = LocalDate.parse(gsonObjMem.get("fechaInicio").getAsString());
                                        LocalDate dateFin = LocalDate.parse(gsonObjMem.get("fechaFin").getAsString());

                                        Membresia membresia = new Membresia();
                                        membresia.setNombre(gsonObjMem.get("nombre").getAsString());
                                        membresia.setFechaInicio("Inicio: " + dateInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                        membresia.setFechaFinal("Término: " + dateFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                        membresia.setMembresia(gsonObjMem.get("membresia").getAsInt());
                                        membresias.add(membresia);
                                    }
                                    break;
                                }
                            }

                            if (valdiateIngreso) {
                                Date date = new Date();
                                SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
                                String fecha = dt.format(date);
                                dt = new SimpleDateFormat("hh:mm:ss");
                                String hora = dt.format(date);

                                Finger finger = new Finger();
                                finger.setIdCliente(idCliente);
                                finger.setDni(dni);
                                finger.setApellidos(apellidos);
                                finger.setNombres(nombres);
                                finger.setFecha(fecha);
                                finger.setHora(hora);
                                finger.setMembresias(membresias);
                                return finger;
                            } else {
                                return "Datos no encontrados, intente nuevamente o consulte al encargado sobre su información";
                            }
                        } else {
                            return jsonObjectData.get("message").getAsString();
                        }
                    } else {
                        return "No se puedo establecer una conexión con el servidor intente nuevamente.";
                    }
                } catch (IOException | JsonSyntaxException | IllegalArgumentException ex) {
                    return ex.getLocalizedMessage();
                }
            }
        };

        task.setOnScheduled(w -> {
            lblDni.setText("--");
            lblDatos.setText("--");
            lblFecha.setText("--");
            ivEstado.setImage(imageQuestion);
            lblMensaje.setText("Mensaje");
            lblProcesando.setText("Procesando petición...");
            hbBody.setDisable(true);
            hbLoad.setVisible(true);
            tvLista.getItems().clear();
        });

        task.setOnFailed(w -> {
            lblDni.setText("--");
            lblDatos.setText("--");
            lblFecha.setText("--");
            ivEstado.setImage(imageError);
            lblMensaje.setText(task.getException().getLocalizedMessage());
            ivImage.setImage(new javafx.scene.image.Image("/applegymdigitalpersona/noimage.jpg"));
            hbBody.setDisable(false);
            hbLoad.setVisible(false);
            PlatformImpl.startup(() -> {
                AudioClip clip = new AudioClip(getClass().getResource("error.mp3").toString());
                clip.play();
            });
            onEventRehacer();
        });

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Finger) {
                Finger finger = (Finger) object;
                lblDni.setText(finger.getDni());
                lblDatos.setText(finger.getApellidos() + " - " + finger.getNombres());
                lblFecha.setText(finger.getFecha() + " - " + finger.getHora());
                if (finger.getMembresias().isEmpty()) {
                    ivEstado.setImage(imageWarning);
                    lblMensaje.setText("NO TIENE NINGUNA MEMBRESIA ACTIVA");
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    PlatformImpl.startup(() -> {
                        AudioClip clip = new AudioClip(getClass().getResource("error.mp3").toString());
                        clip.play();
                    });
                    onEventRehacer();
                } else {
                    finger.getMembresias().forEach((mem) -> {
                        tvLista.getItems().add(mem);
                    });
                    registrarEntredaSalida(finger.getIdCliente(), 0, 1);
                }
            } else {
                lblDni.setText("--");
                lblDatos.setText("--");
                lblFecha.setText("--");
                ivEstado.setImage(imageWarning);
                lblMensaje.setText((String) object);
                ivImage.setImage(new javafx.scene.image.Image("/applegymdigitalpersona/noimage.jpg"));
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                PlatformImpl.startup(() -> {
                    AudioClip clip = new AudioClip(getClass().getResource("error.mp3").toString());
                    clip.play();
                });
                onEventRehacer();
            }
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void loadHuellasEmpleados() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                try {
                    URL obj = new URL(AppleGymDigitalPersona.DOMINIO_SERVER + "/app/cliente/ApiFinger.php?type=listapersonal");
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Accept-Charset", "UTF-8");

                    int responseCode = con.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        StringBuilder buffer;
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                            buffer = new StringBuilder();
                            int read;
                            char[] chars = new char[1024];
                            while ((read = reader.read(chars)) != -1) {
                                buffer.append(chars, 0, read);
                            }
                        }

                        JsonElement jsonElementData = new JsonParser().parse(buffer.toString());
                        JsonObject jsonObjectData = jsonElementData.getAsJsonObject();
                        if (jsonObjectData.get("estado").toString().equalsIgnoreCase("1")) {

                            JsonParser parser = new JsonParser();
                            JsonArray gsonArr = parser.parse(jsonObjectData.get("data").toString()).getAsJsonArray();

                            boolean valdiateIngreso = false;
                            String idCliente = "";
                            String dni = "";
                            String apellidos = "";
                            String nombres = "";
                            for (JsonElement jsonElement : gsonArr) {
                                JsonObject gsonObj = jsonElement.getAsJsonObject();
                                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate();
                                byte[] templateBuffer = Base64.getDecoder().decode(gsonObj.get("huella").getAsString());
                                referenceTemplate.deserialize(templateBuffer);
                                referenceTemplate.deserialize(templateBuffer);
                                DPFPVerificationResult result = verificador.verify(featuresverificacion, referenceTemplate);

                                if (result.isVerified()) {
                                    valdiateIngreso = true;
                                    idCliente = gsonObj.get("idCliente").getAsString();
                                    dni = gsonObj.get("dni").getAsString();
                                    apellidos = gsonObj.get("apellidos").getAsString();
                                    nombres = gsonObj.get("nombres").getAsString();
                                    break;
                                }
                            }

                            if (valdiateIngreso) {
                                Date date = new Date();
                                SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
                                String fecha = dt.format(date);
                                dt = new SimpleDateFormat("hh:mm:ss");
                                String hora = dt.format(date);

                                Finger finger = new Finger();
                                finger.setIdCliente(idCliente);
                                finger.setDni(dni);
                                finger.setApellidos(apellidos);
                                finger.setNombres(nombres);
                                finger.setFecha(fecha);
                                finger.setHora(hora);
                                return finger;
                            } else {
                                return "Datos no encontrados, intente nuevamente o consulte al encargado sobre su información";
                            }
                        } else {
                            return jsonObjectData.get("message").getAsString();
                        }
                    } else {
                        return "No se puedo establecer una conexión con el servidor intente nuevamente.";
                    }
                } catch (IOException | JsonSyntaxException | IllegalArgumentException ex) {
                    return ex.getLocalizedMessage();
                }
            }
        };

        task.setOnScheduled(w -> {
            lblDni.setText("--");
            lblDatos.setText("--");
            lblFecha.setText("--");
            ivEstado.setImage(imageQuestion);
            lblMensaje.setText("Mensaje");
            lblProcesando.setText("Procesando petición...");
            hbBody.setDisable(true);
            hbLoad.setVisible(true);
            tvLista.getItems().clear();
        });

        task.setOnFailed(w -> {
            lblDni.setText("--");
            lblDatos.setText("--");
            lblFecha.setText("--");
            ivEstado.setImage(imageError);
            lblMensaje.setText("Se produjo un error interno, comuníquese con su proveedor.");
            ivImage.setImage(new javafx.scene.image.Image("/applegymdigitalpersona/noimage.jpg"));
            hbBody.setDisable(false);
            hbLoad.setVisible(false);
            PlatformImpl.startup(() -> {
                AudioClip clip = new AudioClip(getClass().getResource("error.mp3").toString());
                clip.play();
            });
            onEventRehacer();
        });

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Finger) {
                Finger finger = (Finger) object;
                lblDni.setText(finger.getDni());
                lblDatos.setText(finger.getApellidos() + " - " + finger.getNombres());
                lblFecha.setText(finger.getFecha() + " - " + finger.getHora());
                lblMensaje.setText("GRACIAS POR ELEGIRNOS");
                registrarEntredaSalida(finger.getIdCliente(), 1, 2);
            } else {
                lblDni.setText("--");
                lblDatos.setText("--");
                lblFecha.setText("--");
                ivEstado.setImage(imageWarning);
                lblMensaje.setText((String) object);
                ivImage.setImage(new javafx.scene.image.Image("/applegymdigitalpersona/noimage.jpg"));
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                PlatformImpl.startup(() -> {
                    AudioClip clip = new AudioClip(getClass().getResource("error.mp3").toString());
                    clip.play();
                });
                onEventRehacer();
            }
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void registrarEntredaSalida(String idCliente, int estadomarca, int tipopersona) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                try {
                    URL obj = new URL(AppleGymDigitalPersona.DOMINIO_SERVER + "/app/cliente/ApiFinger.php?estado=" + estadomarca + "&persona=" + tipopersona + "&type=searchCliente&idCliente=" + idCliente);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Accept-Charset", "UTF-8");

                    int responseCode = con.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        StringBuilder buffer;
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                            buffer = new StringBuilder();
                            int read;
                            char[] chars = new char[1024];
                            while ((read = reader.read(chars)) != -1) {
                                buffer.append(chars, 0, read);
                            }
                        }
                        JsonElement jsonElementData = new JsonParser().parse(buffer.toString());
                        JsonObject jsonObjectData = jsonElementData.getAsJsonObject();
                        if (jsonObjectData.get("estado").toString().equalsIgnoreCase("1")) {
                            return "1";
                        } else if (jsonObjectData.get("estado").toString().equalsIgnoreCase("2")) {
                            return "2";
                        } else if (jsonObjectData.get("estado").toString().equalsIgnoreCase("3")) {
                            return "3";
                        } else {
                            return jsonObjectData.get("message").getAsString();
                        }
                    } else {
                        return "No se puedo establecer una conexión con el servidor intente nuevamente.";
                    }
                } catch (IOException | JsonSyntaxException | IllegalArgumentException ex) {
                    return ex.getLocalizedMessage();
                }
            }
        };

        task.setOnScheduled(event -> {
            lblProcesando.setText("Quite el dedo, para continuar...");
        });
        task.setOnFailed(event -> {
            ivEstado.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            ivEstado.setImage(imageError);
            lblMensaje.setText("Se produjo un error al marcar, comuníquese con su proveedor.");
            ivImage.setImage(new javafx.scene.image.Image("/applegymdigitalpersona/noimage.jpg"));
            hbBody.setDisable(false);
            hbLoad.setVisible(false);
            PlatformImpl.startup(() -> {
                AudioClip clip = new AudioClip(getClass().getResource("error.mp3").toString());
                clip.play();
            });
            onEventRehacer();
        });
        task.setOnSucceeded(event -> {
            String result = task.getValue();
            if (result.equalsIgnoreCase("1")) {
                ivEstado.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                ivEstado.setImage(imageExit);
                lblMensaje.setText("Se registró su salida correctamente.");
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                PlatformImpl.startup(() -> {
                    AudioClip clip = new AudioClip(getClass().getResource("acept.mp3").toString());
                    clip.play();
                });
                onEventRehacer();
            } else if (result.equalsIgnoreCase("2")) {
                ivEstado.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                ivEstado.setImage(imageExit);
                lblMensaje.setText("Se registró su entreda correctamente.");
                PlatformImpl.startup(() -> {
                    AudioClip clip = new AudioClip(getClass().getResource("acept.mp3").toString());
                    clip.play();
                });
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                onEventRehacer();
            } else if (result.equalsIgnoreCase("3")) {
                ivEstado.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                ivEstado.setImage(imageWarning);
                lblMensaje.setText("El cliente ya marco un ingreso.");
                PlatformImpl.startup(() -> {
                    AudioClip clip = new AudioClip(getClass().getResource("error.mp3").toString());
                    clip.play();
                });
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                onEventRehacer();
            } else {
                ivEstado.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                ivEstado.setImage(imageWarning);
                lblMensaje.setText(result);
                PlatformImpl.startup(() -> {
                    AudioClip clip = new AudioClip(getClass().getResource("error.mp3").toString());
                    clip.play();
                });
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                onEventRehacer();
            }
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onEventRehacer() {
        Platform.runLater(() -> {
            txtDescripcion.clear();
            if (lector.isStarted()) {
                recalculador.clear();
                stop();
                start();
            } else {
                recalculador.clear();
                start();
            }
        });
    }

    private DPFPFeatureSet extraerCaracteristicas(DPFPSample sample, DPFPDataPurpose purpose) {
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

    public void start() {
        lector.startCapture();
        EnviarTexto("Utilizando el Lector de Huella Dactilar ");
    }

    public void stop() {
        lector.stopCapture();
        EnviarTexto("No se está usando el Lector de Huella Dactilar ");
    }

    private void EnviarTexto(String string) {
        txtDescripcion.appendText(string + "\n");
    }
}
