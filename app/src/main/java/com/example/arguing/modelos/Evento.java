package com.example.arguing.modelos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Evento {

    private String id;
    private String titulo;
    private String localizacion;
    private Map<String, Long> fecha;
    private String owner;
    private Map<String, Boolean> asistentes;

    public Evento(String id, String titulo, String localizacion, Map<String, Long> fecha, Map<String, Boolean> asistentes,String owner) {
        this.id = id;
        this.titulo = titulo;
        this.localizacion = localizacion;
        this.fecha = fecha;
        this.owner = owner;
        this.asistentes = asistentes;
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(String localizacion) {
        this.localizacion = localizacion;
    }

    public Map<String, Long> getFecha() {
        return fecha;
    }

    public void setFecha(Map<String, Long> fecha) {
        this.fecha = fecha;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Map<String, Boolean> getAsistentes() {
        return asistentes;
    }

    public List<String> getListFechas(){
        Object fechas[] = fecha.keySet().toArray();
        List<String> listaFechas = new ArrayList<String>();
        for (Object fecha:fechas) {
            listaFechas.add(fecha.toString());
        }
        return listaFechas;
    }

    public void setAsistentes(Map<String, Boolean> asistentes) {
        this.asistentes = asistentes;
    }

    public String getFechaSeleccionada(){
        Set<String> data = fecha.keySet();

        return "fecha.get(0)";
    }
}
