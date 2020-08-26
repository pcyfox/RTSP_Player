package com.taike.rtspplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;

public class MultiVideoActivity extends AppCompatActivity {
    String[] urls = {
            "rtsp://admin:123456@192.168.28.201/mpeg4",
            "rtsp://admin:123456@192.168.28.203/mpeg4",
            "rtsp://admin:123456@192.168.28.250/mpeg4",
            "rtsp://admin:123456@192.168.28.252/mpeg4",
            "rtsp://admin:123456@192.168.28.210/mpeg4"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_video);
        LinearLayout linearLayout = findViewById(R.id.ll_xvideos);
        int count = linearLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            if (linearLayout.getChildAt(i) instanceof XVideoView) {
                XVideoView xVideo = (XVideoView) linearLayout.getChildAt(i);
               // xVideo.setUrl("rtsp://admin:taike@2020@192.168.8.86:554/Streaming/Channels/203");
                xVideo.setUrl(urls[i]);
                int por1 = 5002 + i;
                int port2 = 5004 + i;
                int[] ports = {por1, port2};
                xVideo.setVideoClientPorts(ports);
            }
        }

    }
}