package com.example.downloadhelper.adapter;

import static com.example.downloadhelper.constant.DownloadConst.CANCEL;
import static com.example.downloadhelper.constant.DownloadConst.DESTROY;
import static com.example.downloadhelper.constant.DownloadConst.ERROR;
import static com.example.downloadhelper.constant.DownloadConst.FINISH;
import static com.example.downloadhelper.constant.DownloadConst.NONE;
import static com.example.downloadhelper.constant.DownloadConst.PAUSE;
import static com.example.downloadhelper.constant.DownloadConst.PROGRESS;
import static com.example.downloadhelper.constant.DownloadConst.START;
import static com.example.downloadhelper.constant.DownloadConst.WAIT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.downloadhelper.R;
import com.example.downloadhelper.constant.DownloadConst;
import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.database.helper.DownloadHelper;
import com.example.downloadhelper.databinding.ItemDownloadListBinding;
import com.example.downloadhelper.listener.OnHandleClickListener;
import com.example.downloadhelper.util.common.ClickUtil;
import com.example.downloadhelper.util.down.DownloadCallback;
import com.example.downloadhelper.util.down.DownloadManager;

import java.io.File;
import java.text.Format;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

public class DownloadAdapter extends RecyclerView.Adapter {

    private static final String TAG = "DownloadAdapter";
    private final Context context;
    private ArrayList<DownloadEntity> listArray;
    private int MAX_PRO = 100;
    private OnHandleClickListener onHandleClickListener;

    public DownloadAdapter(Context context, ArrayList<DownloadEntity> listArray){
        this.context = context;
        this.listArray = listArray;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //ItemDownloadListBinding binding = ItemDownloadListBinding.inflate(LayoutInflater.from(context), parent, false);
        ItemDownloadListBinding binding = ItemDownloadListBinding.inflate(LayoutInflater.from(context));
        return new ItemHolder(binding);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (listArray.size()<=0){
            return;
        }
        DownloadEntity item = listArray.get(position);
        ItemHolder itemHolder = (ItemHolder) holder;
        if (item!=null && item.getId()>0){
            /*//先清空
            itemHolder.mBinding.tvFileName.setText("");
            itemHolder.mBinding.tvPercentage.setText("0.00%");
            itemHolder.mBinding.tvDownSize.setText("");
            itemHolder.mBinding.pbProgress.setProgress(0);*/
            Log.i(TAG, "onBindViewHolder: status="+item.getStatus()+",id="+item.getId());
            itemHolder.mBinding.pbProgress.setMax(MAX_PRO);
            DownloadManager.getInstance(context).setOnDownloadCallback(item, new DownloadCallback() {
                @Override
                public void onStart(long currentSize, long totalSize) {
                    itemHolder.mBinding.tvFileName.setText(item.getFileName()+":准备中");
                    itemHolder.mBinding.ivHandle.setVisibility(View.GONE);
                }

                @Override
                public void onProgress(long currentSize, long totalSize) {
                    //Log.i(TAG, "onProgress: currentSize="+currentSize);
                    double percent;
                    int progress;
                    itemHolder.mBinding.tvFileName.setText(item.getFileName()+":下载中");
                    itemHolder.mBinding.tvDownSize.setText(
                            Formatter.formatFileSize(context,currentSize)+"/"
                            +Formatter.formatFileSize(context,totalSize)
                    );
                    if (totalSize!=0){
                        percent = (double) currentSize / totalSize * 100;
                        progress =  (int) ((float)currentSize / totalSize * MAX_PRO);
                    }else{
                        percent=0.0f;
                        progress=0;
                    }

                    //Log.i(TAG, "onProgress: progress="+progress);
                    String desc = String.format("%.2f",percent);
                    //Log.i(TAG, "onProgress: desc="+desc);
                    itemHolder.mBinding.tvPercentage.setText(desc+"%");
                    itemHolder.mBinding.pbProgress.setProgress(progress);
                    itemHolder.mBinding.ivHandle.setVisibility(View.VISIBLE);
                    itemHolder.mBinding.ivHandle.setImageResource(R.drawable.pause_32);
                    itemHolder.mBinding.ivHandle.setOnClickListener(v->{
                        if (!ClickUtil.isFastClick(v)){
                            if (onHandleClickListener!=null){
                                onHandleClickListener.onPause(item,position);
                            }
                        }
                    });
                }

                @Override
                public void onPause() {
                    itemHolder.mBinding.tvFileName.setText(item.getFileName()+":已暂停");
                    itemHolder.mBinding.ivHandle.setVisibility(View.VISIBLE);
                    itemHolder.mBinding.ivHandle.setImageResource(R.drawable.start_32);
                    itemHolder.mBinding.ivHandle.setOnClickListener(v->{
                        if (!ClickUtil.isFastClick(v)){
                            if (onHandleClickListener!=null){
                                onHandleClickListener.onResume(item,position);
                            }
                        }

                        //DownloadManager.getInstance().resume(item.getId());
                    });
                }

                @Override
                public void onCancel() {
                    itemHolder.mBinding.tvFileName.setText(item.getFileName()+":已取消");
                    /*if (position<listArray.size()){
                        listArray.remove(position);
                        DownloadHelper.newInstance().delete(item);
                        notifyItemRemoved(position);
                        //虽然页面显示他们已经被移除了，但是对应的position没有根据列表进行更新，需要更新下标
                        notifyItemRangeChanged(position,listArray.size()-position);
                    }*/
                    if (onHandleClickListener!=null){
                        onHandleClickListener.onDelete(item,position);
                    }

                   /* //清空
                    itemHolder.mBinding.tvFileName.setText("");
                    itemHolder.mBinding.tvPercentage.setText("0.00%");
                    itemHolder.mBinding.tvDownSize.setText("");
                    itemHolder.mBinding.pbProgress.setProgress(0);*/
                }

                @Override
                public void onFinish(File file) {
                    itemHolder.mBinding.tvFileName.setText(item.getFileName()+":已完成");
                    itemHolder.mBinding.ivHandle.setVisibility(View.GONE);
                    if (onHandleClickListener!=null){
                        onHandleClickListener.onFinish(item,position);
                    }
                }

                @Override
                public void onWait() {
                    itemHolder.mBinding.tvFileName.setText(item.getFileName()+":等待中");
                    itemHolder.mBinding.ivHandle.setVisibility(View.GONE);
                }

                @Override
                public void onError(String error) {
                    itemHolder.mBinding.tvFileName.setText(item.getFileName()+":下载失败");
                    itemHolder.mBinding.ivHandle.setVisibility(View.VISIBLE);
                    itemHolder.mBinding.ivHandle.setImageResource(R.drawable.start_32);
                    itemHolder.mBinding.ivHandle.setOnClickListener(v->{
                        if (!ClickUtil.isFastClick(v)){
                            if (onHandleClickListener!=null){
                                onHandleClickListener.onResume(item,position);
                            }
                        }
                    });
                }
            });
            itemHolder.mBinding.ivDelete.setOnClickListener(v->{
                if (!ClickUtil.isFastClick(v)){
                    if (onHandleClickListener!=null){
                        onHandleClickListener.onCancel(item,position);
                    }
                }
                //DownloadManager.getInstance(context).cancel(item.getId());
            });
            //DownloadManager.getInstance(context).start(item.getId());
            long downloadedBytes = item.getDownloadedBytes();
            long totalBytes = item.getTotalBytes();
            double percent = 0;
            int progress = 0;
            itemHolder.mBinding.tvDownSize.setText(
                    Formatter.formatFileSize(context,downloadedBytes)+"/"
                            +Formatter.formatFileSize(context,totalBytes)
            );
            if (totalBytes>0) {
                percent = (double) downloadedBytes / totalBytes * 100;
                progress = (int) ((float) downloadedBytes / totalBytes * MAX_PRO);
            }
            String desc = String.format("%.2f",percent);
            itemHolder.mBinding.tvPercentage.setText(desc+"%");
            itemHolder.mBinding.pbProgress.setProgress(progress);
            itemHolder.mBinding.tvFileName.setText(item.getFileName());
            switch (item.getStatus()){
                case PAUSE:
                    Log.i(TAG, "onBindViewHolder: onPause");
                    itemHolder.mBinding.tvFileName.setText(item.getFileName()+":已暂停");
                    itemHolder.mBinding.ivHandle.setVisibility(View.VISIBLE);
                    itemHolder.mBinding.ivHandle.setImageResource(R.drawable.start_32);
                    itemHolder.mBinding.ivHandle.setOnClickListener(v->{
                        if (!ClickUtil.isFastClick(v)){
                            if (onHandleClickListener!=null){
                                onHandleClickListener.onResume(item,position);
                            }
                        }
                    });
                    break;
                case PROGRESS:
                    Log.i(TAG, "onBindViewHolder: onProgress");
                    itemHolder.mBinding.tvFileName.setText(item.getFileName()+":下载中");
                    itemHolder.mBinding.ivHandle.setVisibility(View.VISIBLE);
                    itemHolder.mBinding.ivHandle.setImageResource(R.drawable.pause_32);
                    itemHolder.mBinding.ivHandle.setOnClickListener(v->{
                        if (!ClickUtil.isFastClick(v)){
                            if (onHandleClickListener!=null){
                                onHandleClickListener.onPause(item,position);
                            }
                        }
                    });
                    if (onHandleClickListener!=null){
                        onHandleClickListener.onResume(item,position);
                    }
                    break;
                case ERROR:
                    itemHolder.mBinding.tvFileName.setText(item.getFileName()+":下载失败");
                    itemHolder.mBinding.ivHandle.setVisibility(View.VISIBLE);
                    itemHolder.mBinding.ivHandle.setImageResource(R.drawable.start_32);
                    itemHolder.mBinding.ivHandle.setOnClickListener(v->{
                        if (!ClickUtil.isFastClick(v)){
                            if (onHandleClickListener!=null){
                                onHandleClickListener.onResume(item,position);
                            }
                        }
                    });
                    break;
                case CANCEL:
                    itemHolder.mBinding.tvFileName.setText(item.getFileName()+":已取消");
                    /*listArray.remove(position);
                    DownloadHelper.newInstance().delete(item);
                    notifyItemRemoved(position);
                    //虽然页面显示他们已经被移除了，但是对应的position没有根据列表进行更新，需要更新下标
                    notifyItemRangeChanged(position,listArray.size()-position);*/
                    if (onHandleClickListener!=null){
                        onHandleClickListener.onDelete(item,position);
                    }
                    /*//清空
                    itemHolder.mBinding.tvFileName.setText("");
                    itemHolder.mBinding.tvPercentage.setText("0.00%");
                    itemHolder.mBinding.tvDownSize.setText("");
                    itemHolder.mBinding.pbProgress.setProgress(0);*/
                    break;
                default:
                    if (onHandleClickListener!=null){
                        onHandleClickListener.onStart(item,position);
                    }
                    break;
            }
        }
    }


    @Override
    public int getItemCount() {
        if (listArray!=null && listArray.size()>0){
            return listArray.size();
        }else{
            return 0;
        }

    }

    public void setDataList(ArrayList<DownloadEntity> list){
        listArray.clear();
        listArray.addAll(list);
        Log.i(TAG, "setDataList: listArray.size="+listArray.size());
        new Handler().post(() -> notifyDataSetChanged());
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        private com.example.downloadhelper.databinding.ItemDownloadListBinding mBinding;

        public ItemHolder(ItemDownloadListBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

    public void setOnHandleClickListener(OnHandleClickListener onHandleClickListener){
        this.onHandleClickListener = onHandleClickListener;
    }

}
