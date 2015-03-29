package com.magicbusapp.magicbus.firebase;

/**
 * Created by giuseppe on 14/03/15.
 */
import java.util.Date;

public class MessageChat {

    private String messaggio;
    private Date dataInvio;
    private String idUtente;
    private String nickname;
    private String fbId;
    private String avatar;
    private double latitude;
    private double longitude;

    public MessageChat(String messaggio, Date dataInvio, String idUtente,
                       String nickname, String fbId, String avatar, double latitude, double longitude) {
        super();
        this.messaggio = messaggio;
        this.dataInvio = dataInvio;
        this.idUtente = idUtente;
        this.nickname = nickname;
        this.fbId = fbId;
        this.avatar = avatar;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @SuppressWarnings("unused")
    private MessageChat(){}

    public String getMessaggio() {
        return messaggio;
    }
    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }
    public Date getDataInvio() {
        return dataInvio;
    }
    public void setDataInvio(Date dataInvio) {
        this.dataInvio = dataInvio;
    }
    public String getIdUtente() {
        return idUtente;
    }
    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getFbId() {
        return fbId;
    }
    public void setFbId(String fbId) {
        this.fbId = fbId;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public double getLatitude() {
        return this.latitude;
    }
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return this.longitude;
    }
    public void setlongitude(float longitude) {
        this.longitude = longitude;
    }
}