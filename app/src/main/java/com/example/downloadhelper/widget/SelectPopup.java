package com.example.downloadhelper.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.downloadhelper.R;
import com.example.downloadhelper.util.common.Utils;

import java.lang.reflect.Type;

public class SelectPopup {

    private static final String TAG = "SelectPopup";
    private final Context context;
    private PopupWindow mPopupWindow;
    private LinearLayout mLayout;
    private OnItemClickListener onItemClickListener;
    private String title;
    private int callId;

    public SelectPopup(Context context){
        this.context = context;
        initPopupWindow();
    }

    private void initPopupWindow() {
        mLayout = new LinearLayout(context);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.setBackgroundResource(R.drawable.bg_rounded_corner);
        mLayout.setGravity(Gravity.CENTER);
        mLayout.setPadding(10,30,10,30);

        // 创建PopupWindow
        mPopupWindow = new PopupWindow(mLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void addTextView(String title,int callId){
        TextView tv_title = new TextView(context);
        tv_title.setText(title);
        tv_title.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        tv_title.setPadding(20,20,20,20);
        tv_title.setGravity(Gravity.CENTER);
        tv_title.setOnClickListener(v->{
            /*if (onItemClickListener!=null){
                *//*Log.i(TAG, "addTextView: selectItem点击="+ callId);
                onItemClickListener.onItemClick(title, callId);*//*
            }*/
            this.title = title;
            this.callId = callId;
            mPopupWindow.dismiss();
        });
        mLayout.addView(tv_title);
    }

    public void showPopupSelect(View rootView){
        // 计算弹窗的宽度和高度
        int popupWidth = (int) (Utils.getScreenWidth(context) * 0.9);
        int popupHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

        /*// 创建一个矩形，用于获取弹窗的宽度和高度
        Rect rect = new Rect();
        rootView.getWindowVisibleDisplayFrame(rect);

        // 计算弹窗的偏移量
        int offsetX = (Utils.getScreenWidth(context) - popupWidth) / 2;
        int offsetY = Utils.getScreenHeight(context) - rect.bottom - popupHeight;
        //int offsetY = Utils.getScreenHeight(context) - popupHeight;*/

        // 设置弹窗的宽度和高度
        mPopupWindow.setWidth(popupWidth);
        mPopupWindow.setHeight(popupHeight);

        // 显示弹窗
        //mPopupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, offsetX, offsetY);
        //mPopupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        mPopupWindow.showAsDropDown(rootView);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (onItemClickListener!=null){
                    onItemClickListener.onItemClick(title,callId);
                }
            }
        });
    }
    public interface OnItemClickListener{
        void onItemClick(String title,int callId);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}
