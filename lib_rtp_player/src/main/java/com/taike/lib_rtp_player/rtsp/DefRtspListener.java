package com.taike.lib_rtp_player.rtsp;

/**
 * Created by pedro on 20/02/17.
 */

public abstract class DefRtspListener implements RtspListener {
    @Override
    public void onCanPlay(String sessionId, int[] ports) {

    }

    @Override
    public void onPlayError(String sessionId, int[] ports) {

    }

    @Override
    public void onPause(String sessionId, int[] ports) {

    }

    @Override
    public void onPauseError(String sessionId, int[] ports) {

    }

    @Override
    public void onConnectionSuccessRtsp() {

    }

    @Override
    public void onConnectionFailedRtsp(String reason) {

    }

    @Override
    public void onNewBitrateRtsp(long bitrate) {

    }

    @Override
    public void onDisconnectRtsp() {

    }

    @Override
    public void onAuthErrorRtsp() {

    }

    @Override
    public void onAuthSuccessRtsp() {

    }
}
