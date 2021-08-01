
package applegymdigitalpersona;

import java.util.ArrayList;

public class Finger {
    
    private String idCliente;
    private String opcion;
    private String tipo;
    private String dni;
    private String apellidos;
    private String nombres;
    private String huella;
    private String imageHuella;
    private String texto;
    private String fecha;
    private String hora;
    private ArrayList<Membresia> membresias;

    public Finger() {
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getOpcion() {
        return opcion;
    }

    public void setOpcion(String opcion) {
        this.opcion = opcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getHuella() {
        return huella;
    }

    public void setHuella(String huella) {
        this.huella = huella;
    }

    public String getImageHuella() {
        return imageHuella;
    }

    public void setImageHuella(String imageHuella) {
        this.imageHuella = imageHuella;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public ArrayList<Membresia> getMembresias() {
        return membresias;
    }

    public void setMembresias(ArrayList<Membresia> membresias) {
        this.membresias = membresias;
    }
    
}
