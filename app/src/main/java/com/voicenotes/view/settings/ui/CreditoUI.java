package com.voicenotes.view.settings.ui;

public class CreditoUI {
    private String autor;
    private String url;
    private  String iconName;

    public CreditoUI(String autor, String url, String iconName) {
        this.autor = autor;
        this.url = url;
        this.iconName = iconName;
    }

    public String getAutor() {
        return autor;
    }

    public String getUrl() {
        return url;
    }

    public String getIconName() {
        return iconName;
    }
}
