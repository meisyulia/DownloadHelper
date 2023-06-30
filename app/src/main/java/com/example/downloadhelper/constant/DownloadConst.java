package com.example.downloadhelper.constant;

public class DownloadConst {
    public static final int NONE = 0x1000; //无状态 4096
    public static final int START = 0x1001; //准备下载 4097
    public static final int PROGRESS = 0x1002; //下载中 4098
    public static final int PAUSE = 0x1003; //暂停 4099
    public static final int CANCEL = 0x1004; //取消下载 4100
    public static final int FINISH = 0x1005; //下载完成 4101
    public static final int ERROR = 0x1006; //下载出错 4102
    public static final int WAIT = 0x1007; //等待
    public static final int DESTROY = 0x1008; //释放资源
}
