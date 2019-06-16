package com.voicenotes.view.library.adapter;

import java.util.Date;

public class CustomAdapterElement {
    private String name;
    private Boolean checked;
    private Date date;
    private Date duration;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDuration() {
        return duration;
    }

    public void setDuration(Date duration) {
        this.duration = duration;
    }

    public CustomAdapterElement(String name,Date date, Date duration){
        this.name=name;
        this.checked=false;
        this.date=date;
        this.duration=duration;

    }
    public String getName(){
        return this.name;
    }
    public Boolean getChecked(){
        return this.checked;
    }
    public void setChecked(Boolean checked){
        this.checked=checked;
    }

    @Override
    public String toString(){
        return this.name + ". check: " + checked;
    }
}
