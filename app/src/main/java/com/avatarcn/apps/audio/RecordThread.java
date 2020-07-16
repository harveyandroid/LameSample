package com.avatarcn.apps.audio;

import android.media.AudioRecord;

import com.avatarcn.apps.audio.template.AudioRecordListener;


/**
 * Created by hanhui on 2019/7/26 0026 17:17
 */
public class RecordThread extends Thread {
    private static final int READ_INTERVAL40MS = 40;
    private byte[] buffer;
    private AudioRecord audioRecord;
    private AudioRecordListener mListener;
    private AudioRecordListener mReleaseListener;
    private volatile boolean setToStop;
    private double mReadSumSize;
    private double mVarianceSum;
    private AudioParams audioParams;
    private int mReadIntervalTime;
    private ILogger mLogger;


    public RecordThread(AudioParams params) {
        super(Thread.currentThread().getThreadGroup(), "AudioCentre");
        this.mLogger = AudioCentre.logger;
        this.buffer = null;
        this.audioRecord = null;
        this.mListener = null;
        this.mReleaseListener = null;
        this.setToStop = false;
        this.mReadSumSize = 0.0D;
        this.mVarianceSum = 0.0D;
        this.audioParams = params;
        this.mReadIntervalTime = READ_INTERVAL40MS;
    }

    protected void initRecord() throws RecordError {
        if (this.audioRecord != null) {
            mLogger.i("[initRecord] recoder release first");
            this.release();
        }
        int bufferSize = audioParams.getBufferSize();
        int channel = audioParams.getChannelConfig();
        this.audioRecord = new AudioRecord(
                audioParams.audioSource,
                audioParams.sampleRate,
                channel,
                audioParams.audioFormat,
                bufferSize);
        this.buffer = new byte[bufferSize];
        if (this.audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            mLogger.i("create AudioRecord error");
            throw new RecordError("create AudioRecord error");
        }
        mLogger.i("[initRecord] ");
    }

    private int readAudio() throws RecordError {
        if (this.audioRecord != null) {
            int length = this.audioRecord.read(this.buffer, 0, this.buffer.length);
            if (length > 0 && this.mListener != null) {
                this.mListener.onRecordBuffer(this.buffer, 0, length);
            } else if (0 > length) {
                throw new RecordError("Record read data error: " + length);
            }
            return length;
        } else {
            return 0;
        }
    }

    private double checkAudioData(byte[] data, int length) {
        if (null != data && length > 0) {
            double averageValue = 0.0D;
            double valueSum = 0.0D;
            byte[] tempData = data;
            for (int i = 0; i < length; ++i) {
                byte value = tempData[i];
                valueSum += (double) value;
            }
            averageValue = valueSum / (double) length;
            //离差平方和
            double deviationsSquaresSum = 0.0D;
            for (int i = 0; i < length; ++i) {
                byte value = tempData[i];
                deviationsSquaresSum += Math.pow((double) value - averageValue, 2.0D);
            }
            //方差
            return Math.sqrt(deviationsSquaresSum / (double) (length - 1));
        } else {
            return 0.0D;
        }
    }

    public void stopRecord(boolean force) {
        this.setToStop = true;
        if (null == this.mReleaseListener) {
            this.mReleaseListener = this.mListener;
        }
        this.mListener = null;
        if (force) {
            try {
                mLogger.i("stopRecord...release");
                if (this.audioRecord != null) {
                    if (AudioRecord.RECORDSTATE_RECORDING == this.audioRecord.getRecordingState()
                            && AudioRecord.STATE_INITIALIZED == this.audioRecord.getState()) {
                        mLogger.i("stopRecord releaseRecording ing...");
                        this.audioRecord.release();
                        mLogger.i("stopRecord releaseRecording end...");
                        this.audioRecord = null;
                    }
                    if (null != this.mReleaseListener) {
                        this.mReleaseListener.onRecordReleased();
                        this.mReleaseListener = null;
                    }
                }
            } catch (Exception var5) {
                mLogger.i(var5.toString());
            }
        }
        mLogger.i("stop record");
    }

    public void startRecording(AudioRecordListener listener) {
        this.mListener = listener;
        this.setPriority(10);
        this.start();
    }

    @Override
    public void run() {
        try {
            int time = 0;

            while (!this.setToStop) {
                try {
                    this.initRecord();
                    break;
                } catch (Exception var7) {
                    ++time;
                    if (time >= 10) {
                        throw new RecordError(var7.toString());
                    }
                    sleep(40L);
                }
            }

            time = 0;

            while (!this.setToStop) {
                try {
                    this.audioRecord.startRecording();
                    int state = this.audioRecord.getRecordingState();
                    if (state != AudioRecord.RECORDSTATE_RECORDING) {
                        throw new RecordError(String.format("recorder state is %d not recoding", state));
                    }
                    break;
                } catch (Exception var8) {
                    ++time;
                    if (time >= 10) {
                        throw new RecordError("recoder start failed " + var8.getMessage());
                    }
                    sleep(40L);
                }
            }
            if (null != this.mListener) {
                this.mListener.onRecordStarted(audioRecord);
            }
            long startTime = System.currentTimeMillis();
            boolean punctual = true;
            while (!this.setToStop) {
                int readSize = this.readAudio();
                if (punctual) {
                    this.mReadSumSize += (double) readSize;
                    this.mVarianceSum += this.checkAudioData(this.buffer, this.buffer.length);
                    if (System.currentTimeMillis() - startTime >= 1000L) {
                        punctual = false;
                        if (this.mReadSumSize == 0.0D || this.mVarianceSum == 0.0D) {
                            throw new RecordError("cannot get record permission, get invalid audio data.");
                        }
                    }
                }

                if (this.buffer.length > readSize) {
                    mLogger.i("current record read size is less than buffer size: " + readSize);
                    sleep((long) this.mReadIntervalTime);
                }
            }
        } catch (Exception var9) {
            mLogger.i(var9.toString());
            if (this.mListener != null) {
                this.mListener.onRecordError(var9.toString());
            }
        }
        this.release();
    }

    private void release() {
        try {
            if (this.audioRecord != null) {
                mLogger.i("release record begin");
                this.audioRecord.release();
                this.audioRecord = null;
                if (null != this.mReleaseListener) {
                    this.mReleaseListener.onRecordReleased();
                    this.mReleaseListener = null;
                }
                mLogger.i("release record over");
            }
        } catch (Exception var4) {
            mLogger.i(var4.toString());
        }
    }
}