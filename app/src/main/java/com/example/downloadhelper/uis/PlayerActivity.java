package com.example.downloadhelper.uis;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.downloadhelper.R;
import com.example.downloadhelper.databinding.ActivityPlayerBinding;
import com.example.downloadhelper.databinding.LayoutTopTitleBinding;
import com.example.downloadhelper.util.common.FileUtil;
import com.example.downloadhelper.util.common.Utils;
import com.example.downloadhelper.widget.HintDialog;

import java.io.File;
import java.io.IOException;

public class PlayerActivity extends BaseActivity implements HintDialog.OnHintClickListener {

    private static final String TAG = "PlayerActivity";
    private com.example.downloadhelper.databinding.LayoutTopTitleBinding mTitleBinding;
    private com.example.downloadhelper.databinding.ActivityPlayerBinding mBinding;
    private String mSavePath;
    private String mFileName;
    private MediaPlayer mPlayer;
    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getWindow().setFormat(PixelFormat.TRANSLUCENT);
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_player);
        mBinding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mTitleBinding = LayoutTopTitleBinding.bind(mBinding.getRoot());
        initData();
        initView();
    }

    @Override
    protected void initData() {
        if (getIntent() != null) {
            mSavePath = getIntent().getStringExtra("savePath");
            mFileName = FileUtil.getSaveName(mSavePath);
        }
    }


    @Override
    protected void initView() {
        mTitleBinding.tvTitle.setText(mFileName);
        mTitleBinding.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
        mTitleBinding.ivBack.setOnClickListener(v->{
            onBackPressed();
        });
        openFile(mSavePath);
    }

    private void openFile(String filePath) {
        String fileType = FileUtil.getFileType(filePath);
        Log.i(TAG, "openFile: fileType="+fileType);
        if (fileType.equals("text")){
            showTextFile(filePath);
        }else if (fileType.equals("image")){
            showImageFile(filePath);
        }else if (fileType.equals("video")){ //视频
            showVideoFile(filePath);
        }else if (fileType.equals("audio")){ //音频
            showAudioFile(filePath);
        }else{
            //弹出一个弹窗表示该类型无法打开
            //Log.i(TAG, "openFile: 弹出弹框");
            HintDialog hintDialog = new HintDialog(this);
            hintDialog.setContent("提示","不支持打开该文件");
            hintDialog.setOnHintClickListener(this);
            hintDialog.show();
        }
    }

    private void showAudioFile(String filePath) {
         mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(filePath);
            mPlayer.prepare();
            mBinding.rvVideo.setBackgroundColor(Color.TRANSPARENT);
            mBinding.rvVideo.setVisibility(View.VISIBLE);
            mBinding.seekBar.setMax(mPlayer.getDuration());
            mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
                        mBinding.rvVideo.setBackground(new ColorDrawable(Color.TRANSPARENT));
                    }
                    return true;
                }
            });
            mBinding.videoView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (Utils.getScreenHeight(this)*0.5)));
            //setLayout(mPlayer);
            mBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser){
                        mPlayer.seekTo(progress);
                        updateCurrTime(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            startPlayer(); //开始播放
            playBtnClick();
        } catch (IOException e) {
            e.printStackTrace();
            //无法打开文件提示
            Toast.makeText(this, "无法打开该文件", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLayout(MediaPlayer player) {
        int vWidth = player.getVideoWidth();
        int vHeight = player.getVideoHeight();

        if (vWidth > mBinding.videoView.getWidth() || vHeight > mBinding.videoView.getHeight()) {
            //如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
            float wRatio = (float) vWidth / (float) mBinding.videoView.getWidth();
            float hRatio = (float) vHeight / (float) mBinding.videoView.getHeight();

            //选择大的一个进行缩放
            float ratio = Math.max(wRatio, hRatio);

            vWidth = (int) Math.ceil((float) vWidth / ratio);
            vHeight = (int) Math.ceil((float) vHeight / ratio);

            //设置surfaceView的布局参数
            mBinding.videoView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        }
    }

    private void playBtnClick() {
        mBinding.playButton.setOnClickListener(v->{
            if (isPlaying){
                pausePlayer();
            }else{
                startPlayer();
            }
        });
    }

    private void showVideoFile(String filePath) {
        //mBinding.videoView.setVisibility(View.VISIBLE);
        mBinding.rvVideo.setVisibility(View.VISIBLE);
        mBinding.videoView.setVideoURI(Uri.fromFile(new File(filePath)));
        MediaController controller = new MediaController(this);
        controller.setAnchorView(mBinding.videoView);
        mBinding.videoView.setMediaController(controller);
        mBinding.videoView.requestFocus();
        mBinding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mPlayer = mediaPlayer;
                mBinding.seekBar.setMax(mPlayer.getDuration()); //设置进度条最大值为音频文件的时长
                mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
                            mBinding.rvVideo.setBackground(new ColorDrawable(Color.TRANSPARENT));
                        }
                        return true;
                    }
                });
                mBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser){
                            mPlayer.seekTo(progress);
                            updateCurrTime(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                //开始播放
                startPlayer();
                playBtnClick();

            }
        });

    }

    private void startPlayer() {
        if (mPlayer!=null){
            mPlayer.start(); //开始播放
            isPlaying = true;
            mBinding.playButton.setText("暂停");
            updateSeekBar(); //更新进度条
        }
    }

    private void pausePlayer(){
        if(mPlayer!=null && mPlayer.isPlaying()){
            mPlayer.pause();
            isPlaying = false;
            mBinding.playButton.setText("播放");
        }
    }

    private void updateSeekBar() {
        if (mPlayer!=null && mPlayer.isPlaying()){
            mBinding.seekBar.setProgress(mPlayer.getCurrentPosition());
            updateCurrTime(mPlayer.getCurrentPosition());
            Runnable runnable = () -> updateSeekBar();
            mBinding.seekBar.postDelayed(runnable,1000);
        }
    }
    private void updateCurrTime(int progress) {
        int hours = progress / 1000 / 60 / 60;
        int minutes = (progress / 1000 / 60) % 60;
        int seconds = (progress / 1000) % 60;
        if (hours > 0) {
            mBinding.tvCurrTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        } else {
            mBinding.tvCurrTime.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }


    private void showImageFile(String filePath) {
        mBinding.imageView.setVisibility(View.VISIBLE);
        File file = new File(filePath);
        if (file.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            mBinding.imageView.setImageBitmap(bitmap);
        }

    }

    private void showTextFile(String filePath) {
        mBinding.webView.setVisibility(View.VISIBLE);
        mBinding.webView.setWebViewClient(new WebViewClient());
        mBinding.webView.getSettings().setJavaScriptEnabled(true);
        mBinding.webView.loadUrl("file://"+filePath);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mPlayer != null) {
            mPlayer.stop(); // 停止播放
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer!=null){
            mPlayer.release();
            mPlayer = null;
        }

    }

    @Override
    public void onQuit() {
        onBackPressed();
    }
}