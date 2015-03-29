package com.magicbusapp.magicbus.model;

/**
 * Created by giuseppe on 14/03/15.
 */

public class Fermata {

    private String id;
    private String nome;
    private double latitude;
    private double longitude;
    private String magicUser;
    private String distanzaString;
    private double distanza;
    private boolean validata;
    private boolean haOrari;

    public Fermata(String id, String nome, double latitude, double longitude,
                   String magicUser, String distanzaString, double distanza, boolean validata) {
        super();
        this.id = id;
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.magicUser = magicUser;
        this.distanzaString = distanzaString;
        this.distanza = distanza;
        this.validata = validata;

        this.haOrari = false;

    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public String getMagicUser() {
        return magicUser;
    }
    public void setMagicUser(String magicUser) {
        this.magicUser = magicUser;
    }

    public String getDistanzaString(){
        return this.distanzaString;
    }

    public void setDistanzaString(String distanza){
        this.distanzaString = distanza;
    }

    public void setDistanza(double distanza) {
        this.distanza = distanza;
    }

    public double getDistanza() {
        return distanza;
    }

    public boolean isValidata() {
        return validata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean haOrari() {
        return haOrari;
    }

    public void setHaOrari(boolean haOrari) {
        this.haOrari = haOrari;
    }

}