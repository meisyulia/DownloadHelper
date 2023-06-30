package com.example.downloadhelper.util.common;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {
    private static final String TAG = "FileUtil";

    // 判断文件是否已经存在的方法
    public static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }
    //通过保存路径获取文件名
    public static String getSaveName(String savePath){
        if (TextUtils.isEmpty(savePath)){
            return "";
        }
        int index = savePath.lastIndexOf("/");
       if (index != -1 && index <savePath.length()-1){
           return savePath.substring(index+1);
       }else{
           return "";
       }
    }
    // 获取文件扩展名的方法
    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        } else {
            return "";
        }
    }
    // 在文件名和扩展名之间添加序号的方法
    public static String addSerialNumber(String filePath, String fileName, int number) {
        String extension = getFileExtension(fileName);
        //Log.i(TAG, "addSerialNumber: fileName="+fileName);
        // 判断文件名是否已经包含序号
        int index = fileName.lastIndexOf("(");
        String newFileName = null;
        if (index != -1) {
            // 获取文件名中的序号
            String numberStr = fileName.substring(index + 1, fileName.length() - 1);
            try {
                int existingNumber = Integer.parseInt(numberStr);
                // 判断获取的序号是否大于等于给定的序号
                if (existingNumber >= number) {
                    // 给定的序号加1
                    number = existingNumber + 1;
                }
            } catch (NumberFormatException e) {
                // 序号解析失败，不进行任何操作
            }
            // 构建新的文件名
            newFileName = fileName.substring(0, fileName.lastIndexOf("(") + 1) + number + ")" + extension;
        }else{
            //第一次重复
            newFileName = fileName.substring(0, fileName.lastIndexOf("."))+"(1)"+extension;

        }
        // 根据新的文件名构建新的文件路径
        String newFilePath = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + newFileName;
       // Log.i(TAG, "addSerialNumber: newFileName="+newFileName);
        // 判断新的文件名是否已经存在
        boolean isFileExists = isFileExists(newFilePath);
        if (isFileExists) {
            // 如果新的文件名已经存在，则递归调用addSerialNumber方法，将序号加1
            newFilePath = addSerialNumber(newFilePath, newFileName, number + 1);
        }

        return newFilePath;
    }

    /**
     * 从下载路径中获取文件名
     * @param downloadUrl
     * @return
     */
    public static String getFileName(String downloadUrl){
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
        if (fileName == null || "".equals(fileName.trim())){
            for (int i = 0; ; i++) {
                //初始化获取文件信息
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    conn.connect();
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                        String mine = conn.getHeaderField(i); //从返回的流中获取特定索引的头字段的值
                        if (mine==null) break;
                        //获取content-disposition返回字段，里面可能包含文件名
                        if ("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())){
                            Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                            if (m.find()) return m.group(1);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            fileName = UUID.randomUUID()+".tmp"; //如果都没有找到，默认取一个文件名
            //有网卡标识数字（每个网卡都有唯一的标识号）以及CPU时间的唯一数字生成的一个16字节的二进制作为文件名
        }
        return fileName;
    }

    //获取文件类型，主要文件类型有：text、image、audio、video、application
    public static String getFileType(String filePath) {
        if (TextUtils.isEmpty(filePath)){
            return "";
        }
        String fileType = "";
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath); // 获取文件扩展名
        if (extension != null) {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension); // 根据扩展名获取文件类型
            if (!TextUtils.isEmpty(mimeType)){
                int index = mimeType.indexOf("/");
                if (index != -1 && index<=mimeType.length()-1){
                    fileType = mimeType.substring(0,index);
                }
            }
        }
        return fileType;
    }
}
