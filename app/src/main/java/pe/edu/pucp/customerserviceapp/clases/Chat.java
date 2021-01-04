package pe.edu.pucp.customerserviceapp.clases;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.Date;

public class Chat implements Serializable, Comparable<Chat> {
    private String msg;
    private Date fecha;
    private Boolean attachedImg;
    private String senderId;
    private String receiverId;
    private Boolean readbyreceiver = false;

    public Chat(String msg, Date fecha, Boolean attachedImg, String senderId, String receiverId) {
        this.msg = msg;
        this.fecha = fecha;
        this.attachedImg = attachedImg;
        this.senderId = senderId;
        this.receiverId = receiverId;

    }

    public Chat() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getFechaStr() {
        LocalDateTime lc = fecha.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss 'el' yyyy-MM-dd");
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        return lc.format(formatter);
    }
    
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Boolean getAttachedImg() {
        return attachedImg;
    }

    public void setAttachedImg(Boolean attachedImg) {
        this.attachedImg = attachedImg;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Boolean getReadbyreceiver() {
        return readbyreceiver;
    }

    public void setReadbyreceiver(Boolean readbyreceiver) {
        this.readbyreceiver = readbyreceiver;
    }


    @Override
    public int compareTo(Chat chat) {
        return getFecha().compareTo(chat.getFecha());
    }
}
