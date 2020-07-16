package com.avatarcn.apps.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by hanhui on 2019/11/18 0018 16:32
 */
public class AudioParams {
    /**
     * 默认读取比特率
     */
    public static final int DEFAULT_READ_RATE = 40;

    /**
     * 默认比特率
     */
    public static final int DEFAULT_BIT_RATE = 16000;
    /**
     * 默认采样率
     */
    public static final int DEFAULT_SAMPLE_RATE = 16000;
    /**
     * 通道数为1
     */
    public static final int CHANNEL_COUNT_MONO = 1;
    /**
     * 通道数为2
     */
    public static final int CHANNEL_COUNT_STEREO = 2;
    /**
     * 单声道
     */
    public static final int CHANNEL_IN_MONO = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 立体声
     */
    public static final int CHANNEL_IN_STEREO = AudioFormat.CHANNEL_IN_STEREO;
    /**
     * 16位采样精度
     */
    public static final int ENCODING_PCM_16BIT = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * 8位采样精度
     */
    public static final int ENCODING_PCM_8BIT = AudioFormat.ENCODING_PCM_8BIT;
    /**
     * 音频源为MIC
     */
    public static final int SOURCE_MIC = MediaRecorder.AudioSource.MIC;
    /**
     * 音频源为Default
     */
    public static final int SOURCE_DEFAULT = MediaRecorder.AudioSource.DEFAULT;
    /**
     * 音频源为蓝牙
     */
    public static final int SOURCE_COMMUNICATION = MediaRecorder.AudioSource.VOICE_COMMUNICATION;

    /**
     * 设定录音来源于同方向的相机麦克风相同，若相机无内置相机或无法识别，则使用预设的麦克风
     */
    public static final int SOURCE_CAMCORDER = MediaRecorder.AudioSource.CAMCORDER;
    /**
     * 音频编码比特率
     */
    int audioBitrate;
    /**
     * 音频来源
     */
    int audioSource;
    /**
     * 音频流采样率
     */
    int sampleRate;

    /**
     * 读音频流的采样率（单位时间内采样多少点数据，控制读取音频流的大小）
     */
    int readRate;
    /**
     * 通道数量
     */
    int channelCount;
    /**
     * 采样精度
     */
    int audioFormat;

    public AudioParams() {
        this.audioBitrate = DEFAULT_BIT_RATE;
        this.audioSource = SOURCE_CAMCORDER;
        this.sampleRate = DEFAULT_SAMPLE_RATE;
        this.readRate = DEFAULT_READ_RATE;
        this.channelCount = CHANNEL_COUNT_MONO;
        //默认采样精度16
        this.audioFormat = ENCODING_PCM_16BIT;

    }

    public AudioParams audioBitrate(int audioBitrate) {
        this.audioBitrate = audioBitrate;
        return this;
    }

    public AudioParams audioSource(int audioSource) {
        this.audioSource = audioSource;
        return this;
    }

    public AudioParams sampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public AudioParams readRate(int readRate) {
        this.readRate = readRate;
        if (this.readRate < 40 || this.readRate > 100) {
            this.readRate = 40;
        }
        return this;
    }

    public AudioParams channelCount(int channelCount) {
        this.channelCount = channelCount;
        return this;
    }

    public int getAudioBitrate() {
        return audioBitrate;
    }

    public int getAudioSource() {
        return audioSource;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getReadRate() {
        return readRate;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    public int getChannelConfig() {
        return channelCount == CHANNEL_COUNT_MONO ? CHANNEL_IN_MONO : CHANNEL_IN_STEREO;
    }

    public int getBufferSize() {
        byte unit = 16;
        int framePeriod = sampleRate * readRate / 1000;
        int bufferSize = framePeriod * 4 * unit * channelCount / 8;
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, getChannelConfig(), audioFormat);
        if (bufferSize < minBufferSize) {
            bufferSize = minBufferSize;
        }
        AudioCentre.logger.i("\nSampleRate:" + sampleRate +
                "\nChannelCount:" + channelCount +
                "\nFormat:" + audioFormat +
                "\nAudioSource:" + audioSource +
                "\nFramePeriod:" + framePeriod +
                "\nBufferSize:" + bufferSize +
                "\nMinBufferSize:" + minBufferSize + "\n");

        return bufferSize;
    }

}
