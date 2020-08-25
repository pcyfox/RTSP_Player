package com.taike.rtspplayer.rtsp;

public interface CodecBufferInfoListener {
    void onDecodeStart(byte[] data);

    void onDecodeOver(byte[] data);
}
