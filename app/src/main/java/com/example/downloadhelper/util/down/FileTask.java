package com.example.downloadhelper.util.down;

import static com.example.downloadhelper.constant.DownloadConst.CANCEL;
import static com.example.downloadhelper.constant.DownloadConst.DESTROY;
import static com.example.downloadhelper.constant.DownloadConst.ERROR;
import static com.example.downloadhelper.constant.DownloadConst.FINISH;
import static com.example.downloadhelper.constant.DownloadConst.PAUSE;
import static com.example.downloadhelper.constant.DownloadConst.PROGRESS;
import static com.example.downloadhelper.constant.DownloadConst.START;
import static com.example.downloadhelper.constant.DownloadType.HTTP_URL_CONNECTION;
import static com.example.downloadhelper.constant.DownloadType.OK_HTTP;
import static com.example.downloadhelper.constant.DownloadType.RETROFIT;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.database.helper.DownloadHelper;
import com.example.downloadhelper.util.common.DateUtil;
import com.example.downloadhelper.util.common.UrlUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public class FileTask implements Runnable {

    private static final int BUFFER_SIZE = 1024 * 4;
    private static final String TAG = "FileTask";
    private int downloadType;
    private boolean IS_SAVE = false;
    private final DownloadEntity entity;
    private final Handler handler;
    private DownloadHelper mHelper;
    private long id;
    private String downloadUrl;
    private final Context context;
    private int status;
    private String fileName;
    private String savePath;
    private long downloadedBytes;
    private long totalBytes;
    private boolean IS_CANCEL;
    private boolean IS_PAUSE;
    private boolean IS_DESTROY;

    public FileTask(Context context, DownloadEntity entity, Handler handler){
        this.context = context;
        this.entity = entity;
        this.handler = handler;
        id = entity.getId();
        downloadType = entity.getDownloadType();
        totalBytes = entity.getTotalBytes();
        downloadedBytes = entity.getDownloadedBytes();
        status = entity.getStatus();
        downloadUrl = entity.getUrl();
        mHelper = DownloadHelper.newInstance();
        savePath = entity.getSavePath();
    }
    @Override
    public void run() {
        if (entity.getDownloadType() == HTTP_URL_CONNECTION){
            if (entity.getTotalBytes() == 0 && !TextUtils.isEmpty(downloadUrl)){
                prepareFile();
            }
        /*Log.i(TAG, "run: downloadedBytes="+downloadedBytes+",id="+id);
        Log.i(TAG, "run: entity.getDownloadedBytes()="+entity.getDownloadedBytes());*/
            //进行断续下载
            if (entity.getTotalBytes() != 0){
                if (entity.getTotalBytes()>=entity.getDownloadedBytes()) {
                    onProgress(entity.getTotalBytes(), entity.getDownloadedBytes(), false);
                } else{
                    onStart(entity.getTotalBytes(),entity.getDownloadedBytes());
                }
                saveRangeFile();
            }
        }else if (entity.getDownloadType() == OK_HTTP){

            if (entity.getTotalBytes() == 0 && !TextUtils.isEmpty(downloadUrl)){
                okHttpPrepareFile();
            }
            //进行断续下载
            if (entity.getTotalBytes() != 0){
                if (entity.getTotalBytes()>=entity.getDownloadedBytes()) {
                    onProgress(entity.getTotalBytes(), entity.getDownloadedBytes(), false);
                } else{
                    onStart(entity.getTotalBytes(),entity.getDownloadedBytes());
                }
                okHttpSaveRangeFile();
            }

        }else if (entity.getDownloadType() == RETROFIT){
            if (entity.getTotalBytes() == 0 && !TextUtils.isEmpty(downloadUrl)){
                retrofitPreFile();
            }
            if (entity.getTotalBytes() != 0){
                if (entity.getTotalBytes()>=entity.getDownloadedBytes()) {
                    onProgress(entity.getTotalBytes(), entity.getDownloadedBytes(), false);
                } else{
                    onStart(entity.getTotalBytes(),entity.getDownloadedBytes());
                }
                Log.i(TAG, "retrofitRangeFile: 进来这里了吗2");
                retrofitRangeFile();
            }
        }

    }

    public interface RetrofitDownService{
        @GET
        Call<ResponseBody> prepare(@Url String url);

        @Streaming
        @GET
        Call<ResponseBody> saveRange(@Url String url, @Header("Range") String range);
    }


    private synchronized  void retrofitPreFile() {
        Map<String, String> urlMap = UrlUtil.splitUrl(downloadUrl);
        String baseUrl = urlMap.get("baseUrl");
        String url = urlMap.get("path");
        if (TextUtils.isEmpty(baseUrl)){
            onError("路径不正确");
            return;
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // 设置连接超时时间为10秒
                .readTimeout(60, TimeUnit.SECONDS) // 设置读取超时时间为10秒
                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(client).build();
        RetrofitDownService service = retrofit.create(RetrofitDownService.class);
        Call<ResponseBody> call = service.prepare(url);
        try {
            retrofit2.Response<ResponseBody> response = call.execute();
            if (response.isSuccessful()){
                // 从响应头部获取文件大小
                String contentLength = response.headers().get("Content-Length");
                if (contentLength != null) {
                    totalBytes = Long.parseLong(contentLength);
                    //Log.i(TAG, "onResponse: totalBytes1="+totalBytes);
                    entity.setTotalBytes(totalBytes);
                    entity.setDownloadedBytes(downloadedBytes);
                    entity.setStartTime(DateUtil.getNowDateTimeFormat());
                    mHelper.save(entity);
                    //Log.i(TAG, "onResponse: totalBytes2="+entity.getTotalBytes());
                    onStart(totalBytes,downloadedBytes);
                }
            }else {
                onError("响应码为："+response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
            onError(e.toString());
        }
    }
        /*new Thread(new Runnable() {
            @Override
            public void run() {

        }).start();*/
        /*Call<ResponseBody> call = service.prepare(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    // 从响应头部获取文件大小
                    String contentLength = response.headers().get("Content-Length");
                    if (contentLength != null) {
                        totalBytes = Long.parseLong(contentLength);
                        Log.i(TAG, "onResponse: totalBytes1="+totalBytes);
                        entity.setTotalBytes(totalBytes);
                        entity.setDownloadedBytes(downloadedBytes);
                        entity.setStartTime(DateUtil.getNowDateTimeFormat());
                        mHelper.save(entity);
                        Log.i(TAG, "onResponse: totalBytes2="+entity.getTotalBytes());
                        onStart(totalBytes,downloadedBytes);
                        //要在回调中执行方法
                        retrofitRangeFile();
                    }
                }else {
                    onError("响应码为："+response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 请求失败
                // TODO: 处理请求失败的情况
                onError(t.toString());
            }
        });*/


    private synchronized void retrofitRangeFile() {
        Map<String, String> urlMap = UrlUtil.splitUrl(downloadUrl);
        String baseUrl = urlMap.get("baseUrl");
        String url = urlMap.get("path");
        if (TextUtils.isEmpty(baseUrl)){
            onError("路径不正确");
            return;
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // 设置连接超时时间为10秒
                .readTimeout(60, TimeUnit.SECONDS) // 设置读取超时时间为10秒
                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(client).build();
        RetrofitDownService service = retrofit.create(RetrofitDownService.class);
        Call<ResponseBody> call = service.saveRange(url, "bytes=" + downloadedBytes + "-");
        try {
            retrofit2.Response<ResponseBody> response = call.execute();
            if (response.isSuccessful()){
                try {
                    File file = new File(entity.getSavePath());
                    FileOutputStream fos = new FileOutputStream(file, true);
                    //读取并写入
                    InputStream inputStream = response.body().byteStream();
                    //开始下载文件
                    byte[] bytes = new byte[BUFFER_SIZE];
                    int bytesRead;
                    long totalDownloadedBytes = downloadedBytes;
                    //Log.i(TAG, "saveRangeFile: totalDownloadedBytes="+ Formatter.formatFileSize(context,totalDownloadedBytes));
                    while ((bytesRead = inputStream.read(bytes))!=-1){
                        //判断任务状态，是否要写入文件
                        if (IS_CANCEL){
                            handler.sendEmptyMessage(CANCEL);
                            break;
                        }
                        fos.write(bytes,0,bytesRead);
                        totalDownloadedBytes += bytesRead;
                        //更新已下载的字节数
                        downloadedBytes = totalDownloadedBytes;
                        onProgress(totalBytes,totalDownloadedBytes,IS_SAVE);
                        //计算下载进度
                        //判断是否下载完成
                        if (totalDownloadedBytes==totalBytes){
                            //修改状态：成功
                            handler.sendEmptyMessage(FINISH);
                            break;
                        }
                        if (IS_PAUSE){
                            handler.sendEmptyMessage(PAUSE);
                            break;
                        }
                        if (IS_DESTROY){
                            handler.sendEmptyMessage(DESTROY);
                            break;
                        }
                        //保存完要把置回去
                        if (IS_SAVE){
                            IS_SAVE = false;
                            break;
                        }
                    }
                    inputStream.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    onError(e.toString());
                }
            }else{
                onError("响应码为："+response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
            onError(e.toString());
        }
        /*new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();*/

        /*call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        File file = new File(entity.getSavePath());
                        FileOutputStream fos = new FileOutputStream(file, true);
                        //读取并写入
                        InputStream inputStream = response.body().byteStream();
                        //开始下载文件
                        byte[] bytes = new byte[BUFFER_SIZE];
                        int bytesRead;
                        long totalDownloadedBytes = downloadedBytes;
                        //Log.i(TAG, "saveRangeFile: totalDownloadedBytes="+ Formatter.formatFileSize(context,totalDownloadedBytes));
                        while ((bytesRead = inputStream.read(bytes))!=-1){
                            //判断任务状态，是否要写入文件
                            if (IS_CANCEL){
                                handler.sendEmptyMessage(CANCEL);
                                break;
                            }
                            fos.write(bytes,0,bytesRead);
                            totalDownloadedBytes += bytesRead;
                            //更新已下载的字节数
                            downloadedBytes = totalDownloadedBytes;
                            onProgress(totalBytes,totalDownloadedBytes,IS_SAVE);
                            //计算下载进度
                            //判断是否下载完成
                            if (totalDownloadedBytes==totalBytes){
                                //修改状态：成功
                                handler.sendEmptyMessage(FINISH);
                                break;
                            }
                            if (IS_PAUSE){
                                handler.sendEmptyMessage(PAUSE);
                                break;
                            }
                            if (IS_DESTROY){
                                handler.sendEmptyMessage(DESTROY);
                                break;
                            }
                            //保存完要把置回去
                            if (IS_SAVE){
                                IS_SAVE = false;
                                break;
                            }
                        }
                        inputStream.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        onError(e.toString());
                    }
                }else{
                    onError("响应码为："+response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                onError(t.toString());
            }
        });*/
    }

    private void okHttpPrepareFile() {
        if (TextUtils.isEmpty(downloadUrl)){
            onError("路径为空");
            return;
        }
        //Log.i(TAG, "okHttpPrepareFile: downloadUrl="+downloadUrl);
        try {
            //OkHttpClient client = new OkHttpClient();
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS) // 设置连接超时时间为10秒
                    .readTimeout(60, TimeUnit.SECONDS) // 设置读取超时时间为10秒
                    .build();
            Request request = new Request.Builder().url(downloadUrl).head().build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                //从响应头获取文件大小
                String contentLength = response.header("Content-Length");
                if (contentLength!=null){
                    totalBytes = Long.parseLong(contentLength);
                    entity.setTotalBytes(totalBytes);
                    entity.setDownloadedBytes(downloadedBytes);
                    entity.setStartTime(DateUtil.getNowDateTimeFormat());
                    mHelper.save(entity);
                    onStart(totalBytes,downloadedBytes);
                }
            }else{
                onError("响应码为："+response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
            onError(e.toString());
        }
    }

    private void okHttpSaveRangeFile() {
        if (TextUtils.isEmpty(downloadUrl)){
            onError("路径为空");
            return;
        }
        try {
            //OkHttpClient client = new OkHttpClient();
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS) // 设置连接超时时间为10秒
                    .readTimeout(60, TimeUnit.SECONDS) // 设置读取超时时间为10秒
                    .build();
            Request request = new Request.Builder().url(downloadUrl)
                    .header("Range", "bytes=" + downloadedBytes + "-")
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                File file = new File(entity.getSavePath());
                FileOutputStream fos = new FileOutputStream(file, true);
                //读取并写入
                InputStream inputStream = response.body().byteStream();
                //开始下载文件
                byte[] bytes = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalDownloadedBytes = downloadedBytes;
                //Log.i(TAG, "saveRangeFile: totalDownloadedBytes="+ Formatter.formatFileSize(context,totalDownloadedBytes));
                while ((bytesRead = inputStream.read(bytes))!=-1){
                    //判断任务状态，是否要写入文件
                    if (IS_CANCEL){
                        handler.sendEmptyMessage(CANCEL);
                        break;
                    }
                    fos.write(bytes,0,bytesRead);
                    totalDownloadedBytes += bytesRead;
                    //更新已下载的字节数
                    downloadedBytes = totalDownloadedBytes;
                    onProgress(totalBytes,totalDownloadedBytes,IS_SAVE);
                    //计算下载进度
                    //判断是否下载完成
                    if (totalDownloadedBytes==totalBytes){
                        //修改状态：成功
                        handler.sendEmptyMessage(FINISH);
                        break;
                    }
                    if (IS_PAUSE){
                        handler.sendEmptyMessage(PAUSE);
                        break;
                    }
                    if (IS_DESTROY){
                        handler.sendEmptyMessage(DESTROY);
                        break;
                    }
                    //保存完要把置回去
                    if (IS_SAVE){
                        IS_SAVE = false;
                        break;
                    }
                }
                inputStream.close();
                fos.close();
            }else{
                onError("响应码为："+response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
            onError(e.toString());
        }
    }


    private void prepareFile() {
        HttpURLConnection conn = null;
        try {
            //初始化获取文件信息
            conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Encoding", "identity");
            /*conn.setRequestProperty("Charset", "UTF-8");    //设置客户端编码
            //设置用户代理
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; " +
                    "Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727;" +
                    " .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");

            conn.setRequestProperty("Connection", "Keep-Alive");  //设置connection的方式*/
            conn.connect();
            Log.i(TAG, "prepareFile: conn.getResponseCode="+conn.getResponseCode());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                /*//创建保存路径
                fileName = getFileName(conn);
                //savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
                savePath = context.getFilesDir()+"/"+fileName;
                //判断文件名是否有一样的,有则更新文件名
                if (FileUtil.isFileExists(savePath)){
                    savePath = FileUtil.addSerialNumber(savePath, fileName, 1);
                    fileName = savePath.substring(savePath.lastIndexOf('/')+1);
                }*/
                //获取文件总大小
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    totalBytes = conn.getContentLengthLong();
                }else{
                    totalBytes = conn.getContentLength();
                }
                Log.i(TAG, "prepareFile: totalBytes="+totalBytes);
               /* entity.setFileName(fileName);
                entity.setSavePath(savePath);*/
                entity.setTotalBytes(totalBytes);
                entity.setDownloadedBytes(downloadedBytes);
                entity.setStartTime(DateUtil.getNowDateTimeFormat());
                mHelper.save(entity);
                onStart(totalBytes,downloadedBytes);
            }else{
                onError("响应码为："+conn.getResponseCode());
            }

        } catch (IOException e) {
            e.printStackTrace();
            //下载失败
            onError(e.toString());
        }finally {
            if (conn!=null){
                conn.disconnect();
            }
        }
    }

    private void saveRangeFile() {
        HttpURLConnection conn = null;
        try {
            //初始化获取文件信息
            conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            /*conn.setRequestProperty("Charset", "UTF-8");    //设置客户端编码
            //设置用户代理
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; " +
                    "Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727;" +
                    " .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");

            conn.setRequestProperty("Connection", "Keep-Alive");  //设置connection的方式*/
            //设置请求头部，指定文件的起始字节和结束字节
            //获取已经下载的字节大小
            downloadedBytes = entity.getDownloadedBytes();
            totalBytes = entity.getTotalBytes();
            //设置
            conn.setRequestProperty("Range","bytes="+ downloadedBytes +"-");
            conn.connect();
            Log.i(TAG, "saveRangeFile: code="+conn.getResponseCode());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL){
                //Log.i(TAG, "saveRangeFile: totalBytes="+totalBytes);
                InputStream inputStream = conn.getInputStream();
                RandomAccessFile rw = new RandomAccessFile(savePath, "rw");
                rw.seek(downloadedBytes);
                //开始下载文件
                byte[] bytes = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalDownloadedBytes = downloadedBytes;
                //Log.i(TAG, "saveRangeFile: totalDownloadedBytes="+ Formatter.formatFileSize(context,totalDownloadedBytes));
                while ((bytesRead = inputStream.read(bytes))!=-1){
                    //判断任务状态，是否要写入文件
                    if (IS_CANCEL){
                        handler.sendEmptyMessage(CANCEL);
                        break;
                    }
                    rw.write(bytes,0,bytesRead);
                    totalDownloadedBytes += bytesRead;
                    //更新已下载的字节数
                    downloadedBytes = totalDownloadedBytes;
                    onProgress(totalBytes,totalDownloadedBytes,IS_SAVE);
                    //计算下载进度
                    //判断是否下载完成
                    if (totalDownloadedBytes==totalBytes){
                        //修改状态：成功
                        handler.sendEmptyMessage(FINISH);
                        break;
                    }
                    if (IS_PAUSE){
                        handler.sendEmptyMessage(PAUSE);
                        break;
                    }
                    if (IS_DESTROY){
                        handler.sendEmptyMessage(DESTROY);
                        break;
                    }
                    //保存完要把置回去
                    if (IS_SAVE){
                        IS_SAVE = false;
                        break;
                    }
                }
                inputStream.close();
                rw.close();
            }else{
                onError("响应码为："+conn.getResponseCode());
            }

        } catch (IOException e) {
            e.printStackTrace();
            //下载失败
            onError(e.toString());
        }finally {
            if (conn!=null){
                conn.disconnect();
            }
        }
    }

    private void onStart(long totalBytes, long downloadedBytes) {
        Message message = Message.obtain();
        message.what = START;
        Bundle bundle = new Bundle();
        bundle.putLong("totalBytes",totalBytes);
        bundle.putLong("downloadedBytes",downloadedBytes);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private void onError(String msg) {
        Message message = Message.obtain();
        message.what = ERROR;
        message.obj = msg;
        handler.sendMessage(message);
    }

    private void onProgress(long totalBytes, long totalDownloadedBytes,boolean needSave){
        Message message = Message.obtain();
        message.what = PROGRESS;
        Bundle bundle = new Bundle();
        bundle.putLong("totalBytes",totalBytes);
        bundle.putLong("totalDownloadedBytes",totalDownloadedBytes);
        bundle.putBoolean("needSave",needSave);
        message.setData(bundle);
        handler.sendMessage(message);
    }
    public void pause(){
        IS_PAUSE = true;
    }
    public void cancel(){
        IS_CANCEL = true;
    }
    public void destroy(){
        IS_DESTROY = true;
    }
    public void save(){IS_SAVE = true;}
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
