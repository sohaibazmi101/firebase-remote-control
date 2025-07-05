package com.sohaibazmi.remote;

public class MenuItem {
    private String title;
    private int iconResId;

    public MenuItem(String title, int iconResId) {
        this.title = title;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }
}
