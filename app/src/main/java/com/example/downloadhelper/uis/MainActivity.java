package com.example.downloadhelper.uis;

import static com.example.downloadhelper.uis.download.DownloadFragment.REQUEST_CODE_SCAN;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.downloadhelper.R;
import com.example.downloadhelper.constant.FunType;
import com.example.downloadhelper.databinding.ActivityMainBinding;
import com.example.downloadhelper.databinding.LayoutTopTitleBinding;
import com.example.downloadhelper.uis.download.DownloadFragment;
import com.example.downloadhelper.uis.record.RecordFragment;
import com.example.downloadhelper.util.common.PermissionUtil;
import com.huawei.hms.hmsscankit.ScanUtil;

public class MainActivity extends BaseActivity {

    private com.example.downloadhelper.databinding.ActivityMainBinding mBinding;
    private com.example.downloadhelper.databinding.LayoutTopTitleBinding mTitleBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mTitleBinding = LayoutTopTitleBinding.bind(mBinding.getRoot());
        initData();
        initView();
    }

    @Override
    protected void initData() {
        checkAllPermission();
    }

    @Override
    protected void initView() {
        mTitleBinding.ivBack.setOnClickListener(view -> {
            switchPage(FunType.MAIN);
        });
        switchPage(FunType.MAIN);
    }

    private void checkAllPermission() {
        boolean isPermission = PermissionUtil.checkMultiPermission(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, 4096);
        if (!isPermission){
            Toast.makeText(this, "需要允许权限才能正常使用哦", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchPage(FunType type){
        switch (type){
            case MAIN:
                showTopBar(false,"下载小助手");
                getSupportFragmentManager().beginTransaction().replace(R.id.fcv_main,new MainFragment(),MainFragment.class.getSimpleName())
                        .commitAllowingStateLoss();
                break;
            case DOWNLOAD:
                showTopBar(true,"下载文件");
                getSupportFragmentManager().beginTransaction().replace(R.id.fcv_main,new DownloadFragment(),DownloadFragment.class.getSimpleName())
                        .commitAllowingStateLoss();
                break;
            case RECORD:
                showTopBar(true,"记录查找");
                getSupportFragmentManager().beginTransaction().replace(R.id.fcv_main,new RecordFragment(),RecordFragment.class.getSimpleName())
                        .commitAllowingStateLoss();
                break;
        }
    }

    private void showTopBar(boolean isShow,String title){
        if (isShow){
            mTitleBinding.ivBack.setVisibility(View.VISIBLE);
        }else{
            mTitleBinding.ivBack.setVisibility(View.GONE);
        }
        mTitleBinding.tvTitle.setText(title);
    }

    /**
     *重写dispatchTouchEvent
     * 点击软键盘外面的区域关闭软键盘
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 获得当前得到焦点的View，
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                //根据判断关闭软键盘
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判断用户点击的区域是否是输入框
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = { 0, 0 };
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        if (requestCode == REQUEST_CODE_SCAN) {
            // 导入图片扫描返回结果
            int errorCode = data.getIntExtra(ScanUtil.RESULT_CODE, ScanUtil.SUCCESS);
            if (errorCode == ScanUtil.SUCCESS) {
                Object obj = data.getParcelableExtra(ScanUtil.RESULT);
                if (obj != null) {
                    // 展示扫码结果
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(DownloadFragment.class.getSimpleName());
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
            if (errorCode == ScanUtil.ERROR_NO_READ_PERMISSION) {
                // 无文件权限，请求文件权限
                checkAllPermission();
            }
        }
    }
}