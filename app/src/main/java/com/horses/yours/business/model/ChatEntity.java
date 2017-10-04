package com.horses.yours.business.model;

import java.io.Serializable;

/**
 * @author Brian Salvattore
 */
public class ChatEntity implements Serializable {

    private int id;
    private String name;
    private String chat_type;
    private String room;
    private String mute;
    /*private String break;*/
    private String offline;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChatType() {
        return chat_type;
    }

    public void setChatType(String chat_type) {
        this.chat_type = chat_type;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getMute() {
        return mute;
    }

    public void setMute(String mute) {
        this.mute = mute;
    }

    public String getOffline() {
        return offline;
    }

    public void setOffline(String offline) {
        this.offline = offline;
    }

    @Override
    public String toString() {
        return "ChatEntity{" +
                "room='" + room + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}