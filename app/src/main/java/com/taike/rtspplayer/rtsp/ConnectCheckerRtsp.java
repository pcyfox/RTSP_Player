package com.taike.rtspplayer.rtsp;

/**
 * Created by pedro on 20/02/17.
 */

public interface ConnectCheckerRtsp {

    void onCanPlay(String sessionId,int[] ports);

    void onConnectionSuccessRtsp();

    void onConnectionFailedRtsp(String reason);

    void onNewBitrateRtsp(long bitrate);

    void onDisconnectRtsp();

    void onAuthErrorRtsp();

    void onAuthSuccessRtsp();
}
