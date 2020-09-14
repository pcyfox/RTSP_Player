package com.taike.rtspplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.taike.rtspplayer.rtsp.ThreadPool;

import java.util.ArrayList;
import java.util.List;

public class MultiVideoActivity extends AppCompatActivity {
    final static String[] urls = {
            "rtsp://admin:123456@192.168.28.75/mpeg4cif",
            "rtsp://admin:123456@192.168.28.77/mpeg4cif",
            "rtsp://admin:123456@192.168.28.79/mpeg4cif",
            "rtsp://admin:123456@192.168.28.80/mpeg4cif",
            "rtsp://admin:123456@192.168.28.85/mpeg4cif",
            "rtsp://admin:123456@192.168.28.252/mpeg4cif",
            "rtsp://admin:123456@192.168.28.84/mpeg4cif",
            "rtsp://admin:123456@192.168.28.85/mpeg4cif",
            "rtsp://admin:123456@192.168.28.86/mpeg4cif",
            "rtsp://admin:123456@192.168.28.87/mpeg4cif",
            "rtsp://admin:123456@192.168.28.88/mpeg4cif",
            "rtsp://admin:123456@192.168.28.90/mpeg4cif",
            "rtsp://admin:123456@192.168.28.91/mpeg4cif",
            "rtsp://admin:123456@192.168.28.92/mpeg4cif",
            "rtsp://admin:123456@192.168.28.93/mpeg4cif",
            "rtsp://admin:123456@192.168.28.94/mpeg4cif",
            "rtsp://admin:123456@192.168.28.95/mpeg4cif",
            "rtsp://admin:123456@192.168.28.96/mpeg4cif",
            "rtsp://admin:123456@192.168.28.97/mpeg4cif",
            "rtsp://admin:123456@192.168.28.98/mpeg4cif",
            "rtsp://admin:123456@192.168.28.9/mpeg4cif",
            "rtsp://admin:123456@192.168.28.8/mpeg4cif"
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
        ThreadPool.getInstance().shutDown();
    }
}