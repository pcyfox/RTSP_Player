package com.taike.rtspplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.taike.rtspplayer.rtsp.PlayerThreadPool;

import java.util.ArrayList;
import java.util.List;

public class MultiVideoActivity extends AppCompatActivity {
    final static String[] urls = {
            "rtsp://admin:123456@192.168.28.124/mpeg4cif",
            "rtsp://admin:123456@192.168.28.93/mpeg4cif",
            "rtsp://admin:123456@192.168.28.91/mpeg4cif",
            "rtsp://admin:123456@192.168.28.81/mpeg4cif",
            "rtsp://admin:123456@192.168.28.77/mpeg4cif",
            "rtsp://admin:123456@192.168.28.5/mpeg4cif",
            "rtsp://admin:123456@192.168.28.47/mpeg4cif",
            "rtsp://admin:123456@192.168.28.28/mpeg4cif",
            "rtsp://admin:123456@192.168.28.251/mpeg4cif",
            "rtsp://admin:123456@192.168.28.248/mpeg4cif",
            "rtsp://admin:123456@192.168.28.16/mpeg4cif",
            "rtsp://admin:123456@192.168.28.131/mpeg4cif",
            "rtsp://admin:123456@192.168.28.125/mpeg4cif",
            "rtsp://admin:123456@192.168.28.124/mpeg4cif",
            "rtsp://admin:123456@192.168.28.89/mpeg4cif",
            "rtsp://admin:123456@192.168.28.93/mpeg4cif",
            "rtsp://admin:123456@192.168.28.91/mpeg4cif",
            "rtsp://admin:123456@192.168.28.81/mpeg4cif",
            "rtsp://admin:123456@192.168.28.77/mpeg4cif",
            "rtsp://admin:123456@192.168.28.5/mpeg4cif",
            "rtsp://admin:123456@192.168.28.47/mpeg4cif",
            "rtsp://admin:123456@192.168.28.28/mpeg4cif",
            "rtsp://admin:123456@192.168.28.251/mpeg4cif",
            "rtsp://admin:123456@192.168.28.248/mpeg4cif",
            "rtsp://admin:123456@192.168.28.16/mpeg4cif",
            "rtsp://admin:123456@192.168.28.131/mpeg4cif",
            "rtsp://admin:123456@192.168.28.125/mpeg4cif",
            "rtsp://admin:123456@192.168.28.112/mpeg4cif",
            "rtsp://admin:123456@192.168.28.112/mpeg4cif",
    };

    private List<XVideoView> videoViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_video);
        LinearLayout linearLayout = findViewById(R.id.ll_xvideos);

        int count = linearLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            if (linearLayout.getChildAt(i) instanceof XVideoView) {
                XVideoView xVideo = (XVideoView) linearLayout.getChildAt(i);
                videoViews.add(xVideo);
                if (i < urls.length) {
                    xVideo.setUrl(urls[i]);
                    int por1 = 5002 + i;
                    int port2 = 5004 + i;
                    int[] ports = {por1, port2};
                    xVideo.setVideoClientPorts(ports);
                }
                // xVideo.setUrl("rtsp://admin:taike@2020@192.168.8.86:554/Streaming/Channels/203");
            }
        }

    }

    @Override
    protected void onResume() {
        for (XVideoView xVideoView : videoViews) {
            //  xVideoView.start();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        for (XVideoView xVideoView : videoViews) {
            xVideoView.stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (XVideoView xVideoView : videoViews) {
            xVideoView.release();
        }
        PlayerThreadPool.getInstance().shutDown();
    }
}