package com.taike.rtspplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.taike.rtspplayer.rtsp.CodecBufferInfoListener;

public class XVideoView extends LinearLayout {
    private RTSPPlayer player;
    private static final String TAG = "XVideo";
    private int[] videoClientPorts;
    private int[] audioClientPorts;
    private String url;

    public XVideoView(@NonNull Context context) {
        super(context);
    }

    public XVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public XVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public XVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    {
        LayoutInflater.from(this.getContext()).inflate(R.layout.layout_xvideo, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View tvStart = findViewById(R.id.tv_start);
        View tvStop = findViewById(R.id.tv_stop);
        SurfaceView svVideo = findViewById(R.id.sv_video);
        svVideo.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                player = new RTSPPlayer(holder.getSurface());
                player.setVideoClientPorts(videoClientPorts);
                player.setAudioClientPorts(audioClientPorts);
                player.setPlayUrl(url);
                player.setCodecBufferInfoListener(new CodecBufferInfoListener() {
                    @Override
                    public void onDecodeStart(byte[] data) {
                       // Log.d(TAG, "onDecodeStart() called with: data = [" + data + "]");
                    }

                    @Override
                    public void onDecodeOver(byte[] data) {
                     //   Log.d(TAG, "onDecodeOver() called with: data = [" + data + "]");
                    }
                });
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

        tvStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                player.startPlay();
            }
        });

        tvStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                player.stopPlay();
            }
        });
    }

    public void setVideoClientPorts(int[] videoClientPorts) {
        this.videoClientPorts = videoClientPorts;
    }

    public void setAudioClientPorts(int[] audioClientPorts) {
        this.audioClientPorts = audioClientPorts;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
