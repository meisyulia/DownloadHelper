package com.example.downloadhelper.presenters;

import static com.example.downloadhelper.constant.DownloadConst.CANCEL;
import static com.example.downloadhelper.constant.DownloadConst.DESTROY;
import static com.example.downloadhelper.constant.DownloadConst.FINISH;
import static com.example.downloadhelper.constant.DownloadConst.PAUSE;

import android.text.TextUtils;
import android.util.Log;

import com.example.downloadhelper.util.common.UrlUtil;
import com.example.downloadhelper.util.down.FileTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class RetrofitInterruptDownImpl extends BaseDownloadImpl{

    private static final String TAG = "retrofit";
    private static RetrofitInterruptDownImpl down;
    private Call<ResponseBody> call;
    private InputStream inputStream;
    private FileOutputStream fos;

    public static RetrofitInterruptDownImpl getInstance(){
        if (down == null) {
            synchronized (RetrofitInterruptDownImpl.class){
                down = new RetrofitInterruptDownImpl();
            }
        }
        return down;
    }

    @Override
    protected void initData() {

    }

    @Override
    public void startDownload(String downloadUrl, String filePath) {
        super.startDownload(downloadUrl, filePath);
        Map<String, String> urlMap = UrlUtil.splitUrl(downloadUrl);
        String baseUrl = urlMap.get("baseUrl");
        String url = urlMap.get("path");
        boolean isFinish = true;
        if (TextUtils.isEmpty(baseUrl)){
            if (downloadCallback != null) {
                downloadCallback.onError("路径不正确",downloadUrl,filePath);
                return;
            }
        }
        File downFile = new File(filePath);
        long downloadedBytes = 0;
        if (downFile.exists()){
            downloadedBytes = downFile.length();
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // 设置连接超时时间为10秒
                .readTimeout(60, TimeUnit.SECONDS) // 设置读取超时时间为10秒
                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(client).build();
        FileTask.RetrofitDownService service = retrofit.create(FileTask.RetrofitDownService.class);
        call = service.saveRange(url, "bytes=" + downloadedBytes + "-");
        try {
            retrofit2.Response<ResponseBody> response = call.execute();
            if (response.isSuccessful()){
                try {
                    //从响应头获取要下载的大小
                    Long totalBytes = Long.parseLong(response.headers().get("Content-Length"))+downloadedBytes; //获取的不是总大小，而是看你从哪里读起
                    fos = new FileOutputStream(downFile, true);
                    //读取并写入
                    inputStream = response.body().byteStream();
                    //开始下载文件
                    byte[] bytes = new byte[2048];
                    int bytesRead;
                    long totalDownloadedBytes = downloadedBytes;
                    //Log.i(TAG, "saveRangeFile: totalDownloadedBytes="+ Formatter.formatFileSize(context,totalDownloadedBytes));
                    while ((bytesRead = inputStream.read(bytes))!=-1){
                        fos.write(bytes,0,bytesRead);
                        totalDownloadedBytes += bytesRead;
                        if (downloadCallback != null) {
                            downloadCallback.onProgress(totalDownloadedBytes,totalBytes,downloadUrl,filePath);
                        }
                    }
                    inputStream.close();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    isFinish = false;
                    if (!TextUtils.equals(e.getMessage(),"Socket closed") && !TextUtils.equals(e.getMessage(),"Socket is closed")){
                        if(downloadCallback!=null){
                            //下载失败
                            downloadCallback.onError(e.toString(),downloadUrl,filePath);
                        }
                    }
                }
            }else{
                isFinish = false;
                if (downloadCallback != null) {
                    downloadCallback.onError("响应码为："+response.code(),downloadUrl,filePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isFinish = false;
            Log.i(TAG, "startDownload: e.getMessage="+e.getMessage());
            if (!TextUtils.equals(e.getMessage(),"Socket closed") && !TextUtils.equals(e.getMessage(),"Socket is closed")){
                if(downloadCallback!=null){
                    //下载失败
                    downloadCallback.onError(e.toString(),downloadUrl,filePath);
                }
            }
        }finally {
            if (isFinish){
                if (downloadCallback != null) {
                    downloadCallback.onFinish(downloadUrl,filePath);
                }
            }
        }
    }

    @Override
    public void startDownload(String downloadUrl, String filePath, long startPoint, long totalSize) {
        super.startDownload(downloadUrl, filePath, startPoint, totalSize);
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        try {
           /* if (fos != null) {
                fos.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }*/
            call.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopDownload(){
        releaseRes();
    }
}
