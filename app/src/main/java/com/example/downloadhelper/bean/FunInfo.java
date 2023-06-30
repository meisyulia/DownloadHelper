package com.example.downloadhelper.bean;

import com.example.downloadhelper.constant.FunType;

public class FunInfo {
    private String title;
    private int resId;
    private FunType type;

    public FunInfo(String title, int resId, FunType type) {
        this.title = title;
        this.resId = resId;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public FunType getType() {
        return type;
    }

    public void setType(FunType type) {
        this.type = type;
    }
}
