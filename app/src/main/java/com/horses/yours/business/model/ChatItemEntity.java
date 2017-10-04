package com.horses.yours.business.model;

import android.net.Uri;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Brian Salvattore
 */
public class ChatItemEntity implements Serializable {

    private String message;
    private String type;
    private int id;
    private String user;

    private long time;
    private HashMap<String, Object> deleted;

    private String key;
    private int duration;
    private String audioPath;
    private String audioDownload;
    private String imagePath;
    private String imageDownload;
    private String imageUpload;
    private String room;

    public HashMap<String, Object> getDeleted() {
        return deleted;
    }

    public void setDeleted(HashMap<String, Object> deleted) {
        this.deleted = deleted;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public int getDuration() {
        return duration;
    }

    public String getAudioPath() {
        return audioPath == null ? "" : audioPath;
    }

    public void setAudioDownload(String audioDownload) {
        this.audioDownload = audioDownload;
    }

    public String getAudioDownload() {
        return audioDownload == null ? "" : audioDownload;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath == null ? "" : imagePath;
    }

    public void setImageDownload(String imageDownload) {
        this.imageDownload = imageDownload;
    }

    public String getImageDownload() {
        return imageDownload == null ? "" : imageDownload;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getImageUpload() {
        return imageUpload;
    }

    public void setImageUpload(String imageUpload) {
        this.imageUpload = imageUpload;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
