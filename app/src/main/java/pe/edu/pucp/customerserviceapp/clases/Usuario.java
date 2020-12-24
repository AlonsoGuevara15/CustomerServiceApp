package pe.edu.pucp.customerserviceapp.clases;

import java.io.Serializable;

public class Usuario implements Serializable {
    private String uid = "";
    private String nombre = "";
    private int rol = UsuarioManager.ROLE_CLIENT; // Por defecto, será rol cliente

    public Usuario(String uid, String nombre, int rol) {
        this.uid = uid;
        this.nombre = nombre;
        this.rol = rol;
    }

    public Usuario(String uid, String nombre) {
        this.uid = uid;
        this.nombre = nombre;
    }

    public Usuario() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getRol() {
        return rol;
    }

    public void setRol(int rol) {
        this.rol = rol;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
