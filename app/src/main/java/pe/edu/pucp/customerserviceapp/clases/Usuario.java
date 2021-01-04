package pe.edu.pucp.customerserviceapp.clases;

import java.io.Serializable;

public class Usuario implements Serializable {
    private String uid = "";
    private String nombre = "";
    private String rol = UsuarioManager.ROLE_STUDENT; // Por defecto, será rol cliente
    private String correo;
    private String currently="idle";
    private String chatWith="";

    public Usuario(String uid, String nombre, String correo) {
        this.uid = uid;
        this.nombre = nombre;
        this.correo = correo;
    }

    public Usuario() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCurrently() {
        return currently;
    }

    public void setCurrently(String currently) {
        this.currently = currently;
    }

    public String getChatWith() {
        return chatWith;
    }

    public void setChatWith(String chatWith) {
        this.chatWith = chatWith;
    }
}
