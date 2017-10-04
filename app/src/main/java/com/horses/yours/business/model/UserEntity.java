package com.horses.yours.business.model;

import java.io.Serializable;

/**
 * @author Brian Salvattore
 */
public class UserEntity implements Serializable {

    private String fullname;
    private String number;

    private String mail;

    private boolean showOnline;

    private String key;

    public boolean isShowOnline() {
        return showOnline;
    }

    public void setShowOnline(boolean showOnline) {
        this.showOnline = showOnline;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /*public String getNumberYours() {
        return "+51" + number;
    }

    public String getMailYours() {
        return getNumberYours() + "@yours.com";
    }*/

    @Override
    public String toString() {
        return "UserEntity{" +
                "fullname='" + fullname + '\'' +
                ", number='" + number + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
