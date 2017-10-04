package com.horses.yours.business.model;

import java.io.Serializable;

/**
 * @author Brian Salvattore
 */
public class NumberEntity implements Serializable {

    private String key;
    private String number;

    public void setKey(String key) {
        this.key = key;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getKey() {

        return key;
    }

    public String getNumber() {
        return number;
    }

    public static NumberEntity add(String key, String number) {
        NumberEntity entity = new NumberEntity();
        entity.setKey(number);
        entity.setNumber(key);
        return entity;
    }

    @Override
    public String toString() {
        return "NumberEntity{" +
                "key='" + key + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
