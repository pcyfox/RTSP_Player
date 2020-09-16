package com.taike.rtspplayer.rtsp;

/**
 * Created by pedro on 20/02/17.
 */

public interface RtspListener {

    void onCanPlay(String sessionId, int[] ports);

    void onPlayError(String sessionId, int[] ports);

    void onPause(String sessionId, int[] ports);
    void onPauseError(String sessionId, int[] ports);

    void onConnectionSuccessRtsp();

    void onConnectionFailedRtsp(String reason);

    void onNewBitrateRtsp(long bitrate);

    void onDisconnectRtsp();

    void onAuthErrorRtsp();

    void onAuthSuccessRtsp();
}
