package com.example.downloadhelper.util.present;

import android.os.Build;
import android.os.Environment;

import com.example.downloadhelper.util.common.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpURLDown {

    private static final int BUFFER_SIZE = 1024*4;
    private final String downloadUrl;
    private String fileName;
    private String savePath;
    private long downloadedBytes;
    private long totalBytes;

    public HttpURLDown(String downloadUrl){
        this.downloadUrl = downloadUrl;
        HttpURLConnection conn = null;
        try {
            //初始化获取文件信息
            conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            //设置请求头部，指定文件的起始字节和结束字节
            downloadedBytes = 0;
            //获取已经下载的字节大小

            //设置
            //conn.setRequestProperty("Range","bytes="+ downloadedBytes +"-");
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                //创建保存路径
                fileName = getFileName(conn);
                savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
                //判断文件名是否有一样的,有则更新文件名
                if (FileUtil.isFileExists(savePath)){
                    savePath = FileUtil.addSerialNumber(savePath, fileName, 1);
                    fileName = savePath.substring(savePath.lastIndexOf('/')+1);
                }
                //获取文件总大小
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    totalBytes = conn.getContentLengthLong();
                }else{
                    totalBytes = conn.getContentLength();
                }
                //初始化完通知可以进行下载了（是进行下载还是进行等待列表）
            }

            InputStream inputStream = conn.getInputStream();
            RandomAccessFile rw = new RandomAccessFile(savePath, "rw");
            rw.seek(downloadedBytes);
            //开始下载文件
            byte[] bytes = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalDownloadedBytes = downloadedBytes;
            while ((bytesRead = inputStream.read(bytes))!=-1){
                //判断任务状态，是否要写入文件
                rw.write(bytes,0,bytesRead);
                totalDownloadedBytes += bytesRead;
                //计算下载进度
                //判断是否下载完成
                if (totalDownloadedBytes==totalBytes){
                    //修改状态：成功

                    break;
                }
                //更新已下载的字节数
                downloadedBytes = totalDownloadedBytes;
            }
            inputStream.close();
            rw.close();
            //下载完成后写入数据库？移动到完成列表？

        } catch (IOException e) {
            e.printStackTrace();
            //下载失败
            throw new RuntimeException("无法连接url");
        }finally {
            if (conn!=null){
                conn.disconnect();
            }
        }



    }
    //public void download()
    //获取文件名
    public String getFileName(HttpURLConnection conn){
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
        if (fileName == null || "".equals(fileName.trim())){
            for (int i = 0; ; i++) {
                String mine = conn.getHeaderField(i); //从返回的流中获取特定索引的头字段的值
                if (mine==null) break;
                //获取content-disposition返回字段，里面可能包含文件名
                if ("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())){
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                    if (m.find()) return m.group(1);
                }
            }
            fileName = UUID.randomUUID()+".tmp"; //如果都没有找到，默认取一个文件名
            //有网卡标识数字（每个网卡都有唯一的标识号）以及CPU时间的唯一数字生成的一个16字节的二进制作为文件名
        }
        return fileName;
    }



}
