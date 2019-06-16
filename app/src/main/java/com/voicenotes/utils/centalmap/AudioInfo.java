package com.voicenotes.utils.centalmap;

import java.io.Serializable;
import java.util.Date;

public class AudioInfo implements Serializable {

    private String name;
    private String wavPath;
    private String textPath;
    private Date duration;
    private Date fechaCreacion;
    private boolean inQueue;
    private String tag;
    private String idioma;
    static final long serialVersionUID =6747595817576289122L;


    public AudioInfo(String name, String wavPath, String textPath,boolean inQueue, String tag,Date duration,Date fechaCreacion,String idioma) {
        this.name = name;
        this.wavPath = wavPath;
        this.textPath = textPath;
        this.inQueue = inQueue;
        this.tag=tag;
        this.duration=duration;
        this.fechaCreacion = fechaCreacion;
        this.idioma=idioma;

    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getWavPath() {
        return wavPath;
    }
    public void setWavPath(String wavPath) {
        this.wavPath = wavPath;
    }
    public String getTextPath() {
        return textPath;
    }
    public void setTextPath(String textPath) {
        this.textPath = textPath;
    }
    public boolean isInQueue() {
        return inQueue;
    }
    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public Date getDuration() {
        return duration;
    }
    public void setDuration(Date duration) {
        this.duration = duration;
    }
    public Date getFechaCreacion() {
        return fechaCreacion;
    }
    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getIdioma() {
        return idioma;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((textPath == null) ? 0 : textPath.hashCode());
        result = prime * result + ((wavPath == null) ? 0 : wavPath.hashCode());

        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AudioInfo other = (AudioInfo) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (textPath == null) {
            if (other.textPath != null)
                return false;
        } else if (!textPath.equals(other.textPath))
            return false;

        if (wavPath == null) {
            if (other.wavPath != null)
                return false;
        } else if (!wavPath.equals(other.wavPath))
            return false;

        if (duration == null) {
            if (other.duration != null)
                return false;
        } else if (!duration.equals(other.duration))
            return false;

        if (fechaCreacion == null) {
            if (other.fechaCreacion != null)
                return false;
        } else if (!fechaCreacion.equals(other.fechaCreacion))
            return false;

        return true;
    }


    @Override
    public String toString() {
        return "AudioInfo [name=" + name + ", wavPath=" + wavPath + ", textPath=" + textPath + "]";
    }


}
