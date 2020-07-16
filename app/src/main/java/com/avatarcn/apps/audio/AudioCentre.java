package com.avatarcn.apps.audio;


import android.media.AudioRecord;

import androidx.collection.ArraySet;

import com.avatarcn.apps.audio.template.AudioDataCallback;
import com.avatarcn.apps.audio.template.AudioRecordListener;

import java.util.Set;


/**
 * Created by hanhui on 2019/7/26 0026 16:55
 */
public class AudioCentre implements AudioRecordListener {
    static ILogger logger = new DefaultLogger("AudioCentre");
    private static AudioCentre instance = null;
    private final Set<AudioDataCallback> audioDataCallbacks;
    private AudioParams audioParams;
    private RecordThread mRecorder = null;
    private volatile boolean mIsRecording = false;
    private AudioRecord mAudioRecord;
    private Object lock = new Object();

    private AudioCentre(AudioParams params) {
        this.audioDataCallbacks = new ArraySet<>();
        this.audioParams = params;
    }

    public static AudioCentre getInstance() {
        if (instance == null) {
            synchronized (AudioCentre.class) {
                if (instance == null) {
                    instance = new AudioCentre(new AudioParams());
                }
            }
        }
        return instance;
    }

    public static void showLog(boolean isShowLog) {
        logger.showLog(isShowLog);
    }

    public static synchronized void printStackTrace() {
        logger.showStackTrace(true);
    }

    /**
     * start 之前可以先配置
     *
     * @param params
     */
    public void init(AudioParams params) {
        this.audioParams = params;
    }

    public AudioParams getAudioParams() {
        return audioParams;
    }

    public void start() {
        synchronized (lock) {
            if (mIsRecording)
                return;
            if (this.mRecorder != null) {
                this.mRecorder.stopRecord(true);
                this.mRecorder = null;
                this.mIsRecording = false;
            }
            this.mRecorder = new RecordThread(audioParams);
            this.mRecorder.startRecording(this);
            this.mIsRecording = true;
            logger.i("----start--");
        }
    }

    public void start(AudioDataCallback callback) {
        addAudioDataCallback(callback);
        start();
        logger.i("start--" + callback);
    }

    public void stop(AudioDataCallback callback) {
        removeAudioDataCallback(callback);
        stop();
        logger.i("stop--" + callback);
    }

    public void forceStop() {
        if (this.mRecorder != null) {
            this.mRecorder.stopRecord(false);
            this.mRecorder = null;
            this.mIsRecording = false;
        }
        logger.i("----forceStop--");
    }

    public void recover() {
        if (haveBindCallback()) {
            start();
        }
        logger.i("----recover--");
    }

    public void stop() {
        synchronized (lock) {
            if (haveBindCallback()) {
                return;
            }
            if (this.mRecorder != null) {
                this.mRecorder.stopRecord(false);
                this.mRecorder = null;
                this.mIsRecording = false;
            }
            logger.i("----stop--");
        }
    }

    public int getChannelCount() {
        if (mAudioRecord != null) {
            return mAudioRecord.getChannelCount();
        }
        return -1;
    }

    public int getSampleRate() {
        if (mAudioRecord != null) {
            return mAudioRecord.getSampleRate();
        }
        return -1;
    }

    public void addAudioDataCallback(AudioDataCallback callback) {
        synchronized (lock) {
            this.audioDataCallbacks.add(callback);
        }
    }

    public void removeAudioDataCallback(AudioDataCallback callback) {
        synchronized (lock) {
            this.audioDataCallbacks.remove(callback);
        }
    }

    public boolean haveBindCallback() {
        return audioDataCallbacks.size() != 0;
    }

    @Override
    public void onRecordBuffer(byte[] var1, int var2, int var3) {
        final int count = audioDataCallbacks.size();
        if (count > 0) {
            final AudioDataCallback[] callbacks = new AudioDataCallback[count];
            audioDataCallbacks.toArray(callbacks);
            for (AudioDataCallback callback : callbacks) {
                if (callback != null)
                    callback.onRecordBuffer(var1, var2, var3);
            }
        }

    }

    @Override
    public void onRecordError(String var1) {
        logger.i("---onRecordError---" + var1);
        this.mIsRecording = false;
    }

    @Override
    public void onRecordStarted(AudioRecord audioRecord) {
        logger.i("---onRecordStarted---");
        this.mAudioRecord = audioRecord;
        this.mIsRecording = true;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public AudioRecord getAudioRecord() {
        return mAudioRecord;
    }

    @Override
    public void onRecordReleased() {
        logger.i("---onRecordReleased:---");
        this.mIsRecording = false;
        this.mAudioRecord = null;
    }

}
