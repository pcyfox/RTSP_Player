package com.taike.rtspplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;

import com.taike.rtspplayer.rtsp.CodecBufferInfoListener;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private SurfaceView mSurfaceView;
    private RTSPPlayer player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.surfaceView);
        findViewById(R.id.btn_play).setOnClickListener(this);
        findViewById(R.id.btn_stopplay).setOnClickListener(this);
        initSurface();
        int permisson = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permisson != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == 0 && grantResults[1] == 0) {
            Log.d("-----", "permissions is good");
        } else {
            Intent MyIntent = new Intent(Intent.ACTION_MAIN);
            MyIntent.addCategory(Intent.CATEGORY_HOME);
            startActivity(MyIntent);
            System.exit(0);
        }
    }

    //初始化播放相关
    private void initSurface() {
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                initPlayer(holder);
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (player != null) {
                    player.stopPlay();
                }
            }
        });
    }

    private void initPlayer(SurfaceHolder surfaceHolder) {
        player = new RTSPPlayer(surfaceHolder.getSurface());
        player.setPlayUrl("rtsp://admin:taike@2020@192.168.8.86:554/Streaming/Channels/203");
        player.setCodecBufferInfoListener(new CodecBufferInfoListener() {
            @Override
            public void onDecodeStart(byte[] data) {
                Log.d(TAG, "onDecodeStart() called with: data = [" + data + "]");
            }

            @Override
            public void onDecodeOver(byte[] data) {
                Log.d(TAG, "onDecodeOver() called with: data = [" + data + "]");
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                player.startPlay();
                break;
            case R.id.btn_stopplay:
                if (player != null) {
                    player.stopPlay();
                }
                break;
        }
    }
}
