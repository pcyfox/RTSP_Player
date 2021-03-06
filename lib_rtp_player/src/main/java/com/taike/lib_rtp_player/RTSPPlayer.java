package com.taike.lib_rtp_player;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;


import com.taike.lib_rtp_player.rtsp.CodecBufferInfoListener;
import com.taike.lib_rtp_player.rtsp.PlayerThreadPool;
import com.taike.lib_rtp_player.rtsp.RtspClient;
import com.taike.lib_rtp_player.rtsp.RtspListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Auser on 2018/5/28.
 */

public class RTSPPlayer {
    private CodecBufferInfoListener codecBufferInfoListener;
    private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private static final String TAG = "RTSPPlayer";
    private RtspClient client;
    private BlockingQueue<byte[]> video_data_Queue = new ArrayBlockingQueue<>(1000);
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    //MediaCodec variable
    private volatile boolean isPlaying = false;
    private boolean isPause = false;
    private Surface surface;
    private MediaCodec mediaCodec;
    private long lastTime = 0;
    private long frameTime = 0;
    private int videoPort;
    private String url = null;
    private DatagramSocket dataSocket;
    private RtspListener rtspListener;


    public RTSPPlayer(Surface surface) {
        this.surface = surface;
        initMediaCodec();
        initRtspClient();
    }


    private void initRtspClient() {
        client = new RtspClient(new RtspListener() {
            @Override
            public void onCanPlay(String sessionId, int[] ports) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, url + " onCanPlay() called with: sessionId = [" + sessionId + "], ports = [" + Arrays.toString(ports) + "]");
                if (client != null && client.isStreaming()) {
                    isPause = false;
                } else {
                    videoPort = ports[0];
                    startDecode();
                    startHandleData();
                }
                if (rtspListener != null) {
                    rtspListener.onCanPlay(sessionId, ports);
                }
            }

            @Override
            public void onPlayError(String sessionId, int[] ports) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, url + " onPlayError() called with: sessionId = [" + sessionId + "], ports = [" + Arrays.toString(ports) + "]");
                isPlaying = false;
                if (rtspListener != null) {
                    rtspListener.onPlayError(sessionId, ports);
                }
            }

            @Override
            public void onPause(String sessionId, int[] ports) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, url + " onPause() called with: sessionId = [" + sessionId + "], ports = [" + Arrays.toString(ports) + "]");
                isPause = true;
                if (rtspListener != null) {
                    rtspListener.onPause(sessionId, ports);
                }
            }

            @Override
            public void onPauseError(String sessionId, int[] ports) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, url + " onPauseError() called with: sessionId = [" + sessionId + "], ports = [" + Arrays.toString(ports) + "]");
                isPause = false;
                if (rtspListener != null) {
                    rtspListener.onPlayError(sessionId, ports);
                }
            }

            @Override
            public void onConnectionSuccessRtsp() {
                if (rtspListener != null) {
                    rtspListener.onConnectionSuccessRtsp();
                }
                if (BuildConfig.DEBUG) Log.d(TAG, url + " onConnectionSuccessRtsp() called");
            }

            @Override
            public void onConnectionFailedRtsp(String reason) {
                if (rtspListener != null) {
                    rtspListener.onConnectionFailedRtsp(reason);
                }
                if (BuildConfig.DEBUG)
                    Log.e(TAG, url + " onConnectionFailedRtsp() called with: reason = [" + reason + "]");
            }

            @Override
            public void onNewBitrateRtsp(long bitrate) {
                if (rtspListener != null) {
                    rtspListener.onNewBitrateRtsp(bitrate);
                }
                if (BuildConfig.DEBUG)
                    Log.e(TAG, url + " onNewBitrateRtsp() called with: bitrate = [" + bitrate + "]");
            }

            @Override
            public void onDisconnectRtsp() {
                if (rtspListener != null) {
                    rtspListener.onDisconnectRtsp();
                }
                if (BuildConfig.DEBUG) Log.e(TAG, url + " onDisconnectRtsp() called");
            }

            @Override
            public void onAuthErrorRtsp() {
                if (BuildConfig.DEBUG) Log.d(TAG, url + " onAuthErrorRtsp() called");
                if (rtspListener != null) {
                    rtspListener.onAuthErrorRtsp();
                }

            }

            @Override
            public void onAuthSuccessRtsp() {
                if (BuildConfig.DEBUG) Log.d(TAG, url + "onAuthSuccessRtsp() called");
                if (rtspListener != null) {
                    rtspListener.onAuthSuccessRtsp();
                }
            }
        });
    }

    /*
    设置视频流
     */
    public void setPlayUrl(String playUrl) {
        client.setUrl(playUrl);
        url = playUrl;
    }

    public void setAuthorization(String user, String psw) {
        client.setAuthorization(user, psw);
    }


    /*
    开始播放
     */
    public void startPlay() {
        if (isPlaying) {
            Log.e(TAG, "start play failed.  player is playing");
        } else {
            initMediaCodec();
            isPlaying = true;
            client.connect();
        }
    }

    /*
    停止播放
     */
    public void stopPlay() {
        isPlaying = false;
    }


    public void pause() {
        if (client != null) {
            client.pause();
        }
    }

    /*
    初始化MediaCodec
     */
    private void initMediaCodec() {
        if (mediaCodec != null) {
            return;
        }
        try {
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, 1920, 1080);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
//            byte[] header_sps = new byte[]{0, 0, 0, 1, 103, 66, 0, 42, -106, 53, 64, -16, 4, 79, -53, 55};
//            byte[] header_pps = {0, 0, 0, 1, 104, -50, 60, -128};
//            mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
//            mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
            mediaCodec = MediaCodec.createDecoderByType(MIME_TYPE);
            mediaCodec.configure(mediaFormat, surface, null, 0);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    开启解码线程
     */
    private void startDecode() {
        Runnable decoder = new Runnable() {
            @Override
            public void run() {
                while (isPlaying) {
                    try {
                        int inIndex = mediaCodec.dequeueInputBuffer(5);
                        if (inIndex >= 0) {
                            ByteBuffer buffer = mediaCodec.getInputBuffer(inIndex);
                            if (buffer != null) {
                                buffer.clear();
                                if (!video_data_Queue.isEmpty()) {
                                    byte[] data;
                                    data = video_data_Queue.take();
                                    buffer.put(data);
                                    //输入解码数据
                                    if (codecBufferInfoListener != null) {
                                        codecBufferInfoListener.onDecodeStart(data);
                                    }
                                    mediaCodec.queueInputBuffer(inIndex, 0, data.length, SystemClock.currentThreadTimeMillis(), 0);
                                } else {
                                    mediaCodec.queueInputBuffer(inIndex, 0, 0, SystemClock.currentThreadTimeMillis(), 0);
                                }
                            }
                        } /*else {
                            mediaCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        }*/
                        //TODO:使用异步方法可以提高效率
                        int outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        if (outIndex > 0) {
                            if (codecBufferInfoListener != null) {
                                ByteBuffer outputBuffers = mediaCodec.getOutputBuffer(outIndex);
                                if (outputBuffers != null) {
                                    byte[] data = decodeValue(outputBuffers);
                                    codecBufferInfoListener.onDecodeOver(data);
                                    outputBuffers.clear();
                                }
                            }
                            mediaCodec.releaseOutputBuffer(outIndex, true);
                            lastTime = System.currentTimeMillis();
                        }
                        if (lastTime != 0) {
                            frameTime = System.currentTimeMillis() - lastTime;
                            if (frameTime < 20) {
                                Thread.sleep(20 - frameTime);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isPlaying = false;
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            }
        };
        submit(decoder);
    }

    private void submit(Runnable runnable) {
        PlayerThreadPool.getInstance().submit(runnable);
    }

    public byte[] decodeValue(ByteBuffer bytes) {
        int len = bytes.limit() - bytes.position();
        byte[] bytes1 = new byte[len];
        bytes.get(bytes1);
        return bytes1;
    }

    /*
    开启RTSP收包线程
     */
    private void startHandleData() {
        submit(new Runnable() {
            @Override
            public void run() {
                try {
                    keepConnect();
                    receiveData();
                } catch (Exception e) {
                    e.printStackTrace();
                    isPlaying = false;
                    Log.d(TAG, "receive data from socket failed.");
                } finally {
                    video_data_Queue.clear();
                    if (dataSocket != null) {
                        try {
                            client.disconnect();
                            dataSocket.close();
                            Log.e(TAG, "dataSocket close ok.");
                        } catch (Exception e) {
                            //dataSocket.close();
                            Log.e(TAG, "dataSocket close failed.", e);
                        }
                    }
                }
            }
        });
    }

    private void keepConnect() {
        submit(new Runnable() {
            @Override
            public void run() {
                while (isPlaying) {
                    try {
                        client.sendGetParam();
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        isPlaying = false;
                        videoPort = videoPort + 2;
                    }
                }
            }
        });
    }

    private void receiveData() throws IOException, InterruptedException {
        byte frame_head_1 = (byte) 0x00;
        byte frame_head_2 = (byte) 0x00;
        byte frame_head_3 = (byte) 0x00;
        byte frame_head_4 = (byte) 0x01;
        byte frame_head_I = (byte) 0x65;
        byte frame_head_P = (byte) 0x61;

        int nal_unit_type;

        long lastSq = 0;
        long currSq = 0;

        dataSocket = new DatagramSocket(videoPort);
        dataSocket.setSoTimeout(3000);
        byte[] receiveByte = new byte[48 * 1024];//96
        //从udp读取的数据长度
        int offHeadSize = 0;
        //当前帧长度
        int frameLen = 0;
        //完整帧筛选用缓冲区
        int FRAME_MAX_LEN = 300 * 1024;
        byte[] frame = new byte[FRAME_MAX_LEN];

        DatagramPacket dataPacket = new DatagramPacket(receiveByte, receiveByte.length);

        //Log.d(TAG, "start receive data from socket.");
        while (isPlaying) {
            //Log.d(TAG, "T=" + test);
            dataSocket.receive(dataPacket);
            offHeadSize = dataPacket.getLength() - 12;
            if (offHeadSize > 2) {
                lastSq = currSq;
                currSq = ((receiveByte[2] & 0xFF) << 8) + (receiveByte[3] & 0xFF);
                if (lastSq != 0) {
                    if (lastSq != currSq - 1) {
                        // if (BuildConfig.DEBUG)
                        //  Log.d(TAG, "frame data maybe lost.last=" + lastSq + ",curr=" + currSq);
                    }
                }

                if (frameLen + offHeadSize < FRAME_MAX_LEN) {
                    nal_unit_type = receiveByte[12] & 0xFF;
/*
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "receiveData() called  type=" + Integer.toHexString(nal_unit_type) + " currSq:" + currSq);
*/
                    if (nal_unit_type == 0x67 /*SPS*/
                            || nal_unit_type == 0x68 /*PPS*/
                            || nal_unit_type == 0x6 /*SEI*/) {

                        // if (BuildConfig.DEBUG) Log.d(TAG, " PPS | SPS SEI--------------------<");

                        //加上头部
                        receiveByte[8] = frame_head_1;
                        receiveByte[9] = frame_head_2;
                        receiveByte[10] = frame_head_3;
                        receiveByte[11] = frame_head_4;
                        //Log.d(TAG, "ppp=" + Arrays.toString(receiveByte));
                        video_data_Queue.put(Arrays.copyOfRange(receiveByte, 8, offHeadSize + 12));
//                                    if(isFirstPacket){
//                                        header_sps = Arrays.copyOfRange(receiveByte, 8, offHeadSize + 12);
//                                        mediaCodec = null;
//                                        initMediaCodec();
//                                        startDecodecThread();
//                                        isFirstPacket = false;
//                                    }
                        //修改frameLen
                        frameLen = 0;
                    } else if ((nal_unit_type & 0x1F) == 28) {//分片NAL包，可能是I或者P帧
                        if ((receiveByte[13] & 0xFF) == 0x85) {
                            //I帧的第一包
//                           Log.e(TAG, "I1=" + System.currentTimeMillis());
                            // if (BuildConfig.DEBUG)
                            //        Log.e(TAG, "receiveData() called  ---------------I---------- 帧来了！");
                            receiveByte[9] = frame_head_1;
                            receiveByte[10] = frame_head_2;
                            receiveByte[11] = frame_head_3;
                            receiveByte[12] = frame_head_4;
                            receiveByte[13] = frame_head_I;
                            System.arraycopy(receiveByte, 9, frame, frameLen, offHeadSize + 3);
                            frameLen += offHeadSize + 3;
                        } else if ((receiveByte[13] & 0xFF) == 0x81) {
                            //P帧的第一包
                            //if (BuildConfig.DEBUG) Log.e(TAG, "receiveData() called  P 帧来了！");
//                           Log.e(TAG, "P1=" + System.currentTimeMillis());
                            receiveByte[9] = frame_head_1;
                            receiveByte[10] = frame_head_2;
                            receiveByte[11] = frame_head_3;
                            receiveByte[12] = frame_head_4;
                            receiveByte[13] = frame_head_P;
                            System.arraycopy(receiveByte, 9, frame, frameLen, offHeadSize + 3);
                            frameLen += offHeadSize + 3;
                        } else {
                            System.arraycopy(receiveByte, 14, frame, frameLen, offHeadSize - 2);
                            //修改frameLen
                            frameLen += offHeadSize - 2;
                        }

                        if (((receiveByte[13] & 0xFF) == 0x45)) {
//                          Log.e(TAG, "II1=" + System.currentTimeMillis());
                            video_data_Queue.put(Arrays.copyOfRange(frame, 0, frameLen));
                            frameLen = 0;
                        } else if (((receiveByte[13] & 0xFF) == 0x41)) {
//                          Log.e(TAG, "PP2=" + System.currentTimeMillis());
                            video_data_Queue.put(Arrays.copyOfRange(frame, 0, frameLen));
                            frameLen = 0;
                        }
                    }
                    //Log.d(TAG, "SQ:" + (((receiveByte[2] & 0xFF)<<8) + (receiveByte[3] & 0xFF)));
                    //Log.d(TAG, "udp data=" + Arrays.toString(dataPacket.getData()));
                    //Log.d(TAG, "data=" + Arrays.toString(receiveByte));
//                                Log.d(TAG, "-------");
//                                Log.d(TAG, "rtp V:" + ((receiveByte[0] & 0xC0)>>6));
//                                Log.d(TAG, "rtp P:" + ((receiveByte[0] & 0x20)>>5));
//                                Log.d(TAG, "rtp X:" + ((receiveByte[0] & 0x10)>>4));
//                                Log.d(TAG, "rtp M:" + ((receiveByte[1] & 0x80)>>7));
//                                Log.d(TAG, "rtp PT:" + (receiveByte[1] & 0x7F));
//                                Log.d(TAG, "rtp SQ:" + (((receiveByte[2] & 0xFF)<<8) + (receiveByte[3] & 0xFF)));
//                                Log.d(TAG, "rtp TS:" + (((receiveByte[7] & 0xFF)<<24) + ((receiveByte[6] & 0xFF)<<16) + ((receiveByte[5] & 0xFF)<<8) + (receiveByte[4] & 0xFF)));
//                                Log.d(TAG, "-------");
                }
            } else {
                isPlaying = false;
                Log.e(TAG, "udp port receive stream failed.");
            }
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }


    public void release() {
        isPlaying = false;
        if (surface != null) {
            surface.release();
        }
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
        if (client != null) {
            client.disconnect();
        }
        video_data_Queue.clear();
    }

    public void setVideoClientPorts(int[] videoClientPorts) {
        client.setVideoClientPorts(videoClientPorts);
    }

    public void setAudioClientPorts(int[] audioClientPorts) {
        client.setAudioClientPorts(audioClientPorts);
    }

    public void setCodecBufferInfoListener(CodecBufferInfoListener codecBufferInfoListener) {
        this.codecBufferInfoListener = codecBufferInfoListener;
    }

    public void setRtspListener(RtspListener rtspListener) {
        this.rtspListener = rtspListener;
    }
}
