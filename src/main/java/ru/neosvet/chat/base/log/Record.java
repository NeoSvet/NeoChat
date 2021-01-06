package ru.neosvet.chat.base.log;

import java.io.Serializable;
import java.util.Date;

public class Record implements Serializable {
    private Date date;
    private String owner;
    private String msg;

    public Date getDate() {
        return date;
    }

    public void setTime(long time) {
        date = new Date(time);
    }

    public boolean hasOwner() {
        return owner != null && owner.length() > 0;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
