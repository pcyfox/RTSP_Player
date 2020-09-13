package com.taike.rtspplayer.rtsp;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pedro on 10/02/17.
 */

public class RtspClient {
    private final String TAG = "RtspClient";
    private static final Pattern rtspUrlPattern = Pattern.compile("^rtsps?://([^/:]+)(?::(\\d+))*/([^/]+)/?([^*]*)$");
    private Pattern rtspAuthPatten = Pattern.compile("^rtsp?://(\\w{1,20}):(\\w{1,20}.*?@1)*");
    private ConnectCheckerRtsp connectCheckerRtsp;
    //sockets objects
    private Socket connectionSocket;
    private BufferedReader reader;
    private BufferedWriter writer;
    //for tcp
    private OutputStream outputStream;
    private volatile boolean streaming = false;
    //for secure transport
    private boolean tlsEnabled = false;
    private String url;
    private CommandsManager commandsManager;
    private int numRetry;
    private int reTries;
    private Handler handler;
    private Runnable runnable;
    private final Object lock = new Object();

    public RtspClient(ConnectCheckerRtsp connectCheckerRtsp) {
        this.connectCheckerRtsp = connectCheckerRtsp;
        commandsManager = new CommandsManager();
        handler = new Handler(Looper.getMainLooper());
    }

    public void setOnlyAudio(boolean onlyAudio) {
        commandsManager.setOnlyAudio(onlyAudio);
    }

    public void setProtocol(Protocol protocol) {
        commandsManager.setProtocol(protocol);
    }

    public void setAuthorization(String user, String password) {
        Log.d(TAG, "setAuthorization() called with: user = [" + user + "], password = [" + password + "]");
        commandsManager.setAuth(user, password);
    }

    public void setReTries(int reTries) {
        numRetry = reTries;
        this.reTries = reTries;
    }

    public boolean shouldRetry(String reason) {
        boolean validReason = !reason.contains("Endpoint malformed");
        return validReason && reTries > 0;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public void setUrl(String url) {
        Log.d(TAG, "setUrl() called with: url = [" + url + "]");
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "setUrl() called with: url = [" + url + "]");
            return;
        }
        Matcher rtspMatcher = rtspAuthPatten.matcher(url);
        if (rtspMatcher.find()) {
            String user = rtspMatcher.group(1);
            String psw = rtspMatcher.group(2);
            if (!TextUtils.isEmpty(psw)) {
                psw = psw.substring(0, psw.length() - 2);
            }
            if (!TextUtils.isEmpty(user)) {
                setAuthorization(user, psw);
            }
            this.url = url.replace(user + ":" + psw + "@", "");
            return;
        }
        this.url = url;
    }

    public void setSampleRate(int sampleRate) {
        commandsManager.setSampleRate(sampleRate);
    }

    public String getHost() {
        return commandsManager.getHost();
    }

    public int getPort() {
        return commandsManager.getPort();
    }

    public String getPath() {
        return commandsManager.getPath();
    }

    public ConnectCheckerRtsp getConnectCheckerRtsp() {
        return connectCheckerRtsp;
    }

    public void setSPSandPPS(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {
        commandsManager.setVideoInfo(sps, pps, vps);
    }

    public void setIsStereo(boolean isStereo) {
        commandsManager.setIsStereo(isStereo);
    }

    public void connect() {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (!streaming) {
            Matcher rtspMatcher = rtspUrlPattern.matcher(url);
            if (rtspMatcher.matches()) {
                tlsEnabled = rtspMatcher.group(0).startsWith("rtsps");
            } else {
                streaming = false;
                connectCheckerRtsp.onConnectionFailedRtsp("Endpoint malformed, should be: rtsp://ip:port/appname/streamname");
                return;
            }

            String host = rtspMatcher.group(1);
            int port = Integer.parseInt((rtspMatcher.group(2) != null) ? rtspMatcher.group(2) : "554");
            String group4 = rtspMatcher.group(4);

            if (!TextUtils.isEmpty(group4)) {
                String path = "/" + rtspMatcher.group(3) + "/" + rtspMatcher.group(4);
                commandsManager.setUrl(host, port, path);
            } else {
                String path = "/" + rtspMatcher.group(3);
                commandsManager.setUrl(host, port, path);
            }
            ThreadPool.getInstance().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!tlsEnabled) {
                            connectionSocket = new Socket();
                            SocketAddress socketAddress =
                                    new InetSocketAddress(commandsManager.getHost(), commandsManager.getPort());
                            connectionSocket.connect(socketAddress, 5000);
                        } else {
//                            connectionSocket = CreateSSLSocket.createSSlSocket(commandsManager.getHost(),
//                                    commandsManager.getPort());
//                            if (connectionSocket == null)
//                                throw new IOException("Socket creation failed");
                        }
                        connectionSocket.setSoTimeout(5000);
                        reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                        outputStream = connectionSocket.getOutputStream();
                        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                        String options = commandsManager.createOptions();
                        writer.write(options);
                        writer.flush();
                        commandsManager.getResponse(reader, connectCheckerRtsp, false, false);

                        String describe = commandsManager.createDescribe();
                        writer.write(describe);
                        writer.flush();
                        String dpResp = commandsManager.getResponse(reader, connectCheckerRtsp, false, false);
                        int status = commandsManager.getResponseStatus(dpResp);
                        if (status == 403) {
                            connectCheckerRtsp.onConnectionFailedRtsp("Error configure stream, access denied");
                            Log.e(TAG, "Response 403, access denied");
                            return;
                        } else if (status == 401) {
                            if (commandsManager.getUser() == null || commandsManager.getPassword() == null) {
                                connectCheckerRtsp.onAuthErrorRtsp();
                                return;
                            } else {
                                String describeWithAuth = commandsManager.createDescribeWithAuth(dpResp);
                                writer.write(describeWithAuth);
                                writer.flush();
                                String authResp = commandsManager.getResponse(reader, connectCheckerRtsp, false, false);
                                int statusAuth = commandsManager.getResponseStatus(authResp);
                                if (statusAuth == 401) {
                                    connectCheckerRtsp.onAuthErrorRtsp();
                                    return;
                                } else if (statusAuth == 200) {
                                    connectCheckerRtsp.onAuthSuccessRtsp();
                                    writer.write(commandsManager.createSetup(commandsManager.getTrackVideo()));
                                    writer.flush();
                                    String setupResp = commandsManager.getResponse(reader, connectCheckerRtsp, false, true);
                                    int setUpStatus = commandsManager.getResponseStatus(setupResp);
                                    if (setUpStatus == 200) {
                                        writer.write(commandsManager.sendPlay());
                                        writer.flush();
                                        String sendPlayResp = commandsManager.getResponse(reader, connectCheckerRtsp, false, true);
                                        int sendPlayStatus = commandsManager.getResponseStatus(sendPlayResp);
                                        if (sendPlayStatus == 200) {
                                            connectCheckerRtsp.onCanPlay(commandsManager.getSessionId(), commandsManager.getVideoClientPorts());
                                        }
                                    }
                                }
                            }
                        }
                        streaming = true;
                        reTries = numRetry;
                        connectCheckerRtsp.onConnectionSuccessRtsp();
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                        connectCheckerRtsp.onConnectionFailedRtsp("Error configure stream Exception:, " + e.getMessage());
                        streaming = false;
                    }
                }
            });
        }
    }


    public void write(String param) {
        if (TextUtils.isEmpty(param) || writer == null) {
            return;
        }
        try {
            writer.write(param);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendGetParam() {
        write(commandsManager.createGetParameter());
    }

    public void disconnect() {
        handler.removeCallbacks(runnable);
        disconnect(true);
    }

    private void disconnect(final boolean clear) {
        streaming = false;
        ThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (writer != null) {
                        writer.write(commandsManager.createTeardown());
                        writer.flush();
                        if (clear) {
                            commandsManager.clear();
                        } else {
                            commandsManager.retryClear();
                        }
                    }

                    if (connectionSocket != null && !connectionSocket.isClosed()) {
                        connectionSocket.close();
                    }
                    writer = null;
                    connectionSocket = null;
                } catch (IOException e) {
                    if (clear) {
                        commandsManager.clear();
                    } else {
                        commandsManager.retryClear();
                    }
                    Log.e(TAG, "disconnect error", e);
                }
            }
        });
        if (clear) {
            reTries = 0;
            connectCheckerRtsp.onDisconnectRtsp();
        }
    }

    public void setVideoClientPorts(int[] videoClientPorts) {
        commandsManager.setVideoClientPorts(videoClientPorts);
    }

    public void setAudioClientPorts(int[] audioClientPorts) {
        commandsManager.setAudioClientPorts(audioClientPorts);
    }


    public void reConnect(long delay) {
        reTries--;
        disconnect(false);
        runnable = new Runnable() {
            @Override
            public void run() {
                connect();
            }
        };
        handler.postDelayed(runnable, delay);
    }
}

