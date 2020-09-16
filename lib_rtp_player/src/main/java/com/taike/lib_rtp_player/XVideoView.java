package com.taike.lib_rtp_player;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.taike.lib_rtp_player.rtsp.CodecBufferInfoListener;


public class XVideoView extends LinearLayout {
    private RTSPPlayer player;
    private static final String TAG = "XVideo";
    private int[] videoClientPorts;
    private int[] audioClientPorts;
    private String url;
    private boolean isNeedStart = false;

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
        View tvPause = findViewById(R.id.tv_pause);
        if (BuildConfig.DEBUG) {
            findViewById(R.id.x_ll_buttons).setVisibility(View.VISIBLE);
        }
        SurfaceView svVideo = findViewById(R.id.sv_video);
        svVideo.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                player = new RTSPPlayer(holder.getSurface());
                player.setVideoClientPorts(videoClientPorts);
                player.setAudioClientPorts(audioClientPorts);
                setPlayUrl(url);
                if (isNeedStart) {
                    player.startPlay();
                    isNeedStart = false;
                }
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
                if (player != null) {
                    player.startPlay();
                }
            }
        });

        tvStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null) {
                    player.stopPlay();
                }
            }
        });
        tvPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
            }
        });
    }

    private void setPlayUrl(String url) {
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

    public void setVideoClientPorts(int[] videoClientPorts) {
        this.videoClientPorts = videoClientPorts;
    }

    public void setAudioClientPorts(int[] audioClientPorts) {
        this.audioClientPorts = audioClientPorts;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void release() {
        Log.d(TAG, "release() called");
        if (player != null) {
            player.release();
        }
    }


    public void start() {
        if (TextUtils.isEmpty(url) || player == null || player.isPlaying()) {
            return;
        }
        Log.d(TAG, "start() called  url=" + url);
        setPlayUrl(url);
        player.startPlay();
        isNeedStart = true;
    }

    public void stop() {
        if (player != null) {
            player.stopPlay();
        }
    }
}
