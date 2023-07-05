package com.example.downloadhelper.listener;

import com.example.downloadhelper.database.entity.DownloadEntity;

public interface OnDownClickListener {
    void onItemStop(int position, DownloadEntity entity);
    void onItemStart(int position,DownloadEntity entity);
    void onItemDelete(int position,DownloadEntity entity);
}
