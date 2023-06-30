package com.example.downloadhelper.listener;

import com.example.downloadhelper.database.entity.DownloadEntity;

public interface OnFinishClickListener {
    void onOpenClick(DownloadEntity entity,int position);
    void onItemClick(DownloadEntity entity,int position);
}
