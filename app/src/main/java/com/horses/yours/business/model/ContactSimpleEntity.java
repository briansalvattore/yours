package com.horses.yours.business.model;

import java.io.Serializable;

/**
 * @author Brian Salvattore
 */
@SuppressWarnings("unused")
public class ContactSimpleEntity implements Serializable {

    private String number;
    private String name;
    private String account;

    private NumberEntity numberEntity;

    public NumberEntity getNumberEntity() {
        return numberEntity;
    }

    public void setNumberEntity(NumberEntity numberEntity) {
        this.numberEntity = numberEntity;
    }

    public String getNumber() {
        return number == null ? "" : number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
