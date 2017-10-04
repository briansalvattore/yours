package com.horses.yours.business.model;

import java.io.Serializable;

/**
 * @author Brian Salvattore
 */
@SuppressWarnings("unused")
public class DetailEntity implements Serializable {

    private boolean online;
    private long last;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getLast() {
        return last;
    }

    public void setLast(long last) {
        this.last = last;
    }
}
