package com.example.downloadhelper.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.downloadhelper.databinding.DialogHintBinding;
import com.example.downloadhelper.util.common.Utils;

public class HintDialog extends Dialog {
    private final Context context;
    private OnHintClickListener onHintClickListener;
    private com.example.downloadhelper.databinding.DialogHintBinding mBinding;

    /*public HintDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        mBinding = DialogHintBinding.inflate(LayoutInflater.from(context));
        setContentView(mBinding.getRoot());
        initWindow();
        initView();
    }*/

    public HintDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        mBinding = DialogHintBinding.inflate(LayoutInflater.from(context));
        setContentView(mBinding.getRoot());
        initWindow();
        initView();
    }



    private void initWindow() {
        Window window = getWindow();
        if (window!=null){
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.width = (int)(Utils.getScreenWidth(context)*0.8);
            window.setAttributes(layoutParams);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        //设置弹窗四角为圆弧
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.WHITE);
        gradientDrawable.setCornerRadius(20);
        getWindow().setBackgroundDrawable(gradientDrawable);
        setCanceledOnTouchOutside(false);
    }

    private void initView() {
        mBinding.tvQuit.setOnClickListener(v->{

            if (onHintClickListener!=null){
                onHintClickListener.onQuit();
            }
            dismiss();
        });
    }

    public void setContent(String title,String content){
        mBinding.tvTitle.setText(title);
        mBinding.tvContent.setText(content);
    }

    public interface OnHintClickListener{
        void onQuit();
    }

    public void setOnHintClickListener(OnHintClickListener onHintClickListener){
        this.onHintClickListener = onHintClickListener;
    }
}
