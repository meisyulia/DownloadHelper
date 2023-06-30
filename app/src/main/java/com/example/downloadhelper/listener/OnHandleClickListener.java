package com.example.downloadhelper.listener;

import com.example.downloadhelper.database.entity.DownloadEntity;

public interface OnHandleClickListener {
    void onPause(DownloadEntity data,int position);
    void onResume(DownloadEntity data,int position);
    void onStart(DownloadEntity data,int position);
    void onCancel(DownloadEntity data,int position);
    void onDelete(DownloadEntity data,int position);
    void onFinish(DownloadEntity data,int position);
}
