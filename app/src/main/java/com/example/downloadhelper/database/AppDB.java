package com.example.downloadhelper.database;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = AppDB.DB_NAME,version = AppDB.DB_VERSION)
public class AppDB {
    public static final String DB_NAME = "AppDB";
    public static final int DB_VERSION = 1;
}
