package com.avatarcn.apps.audio.template;

import android.media.AudioRecord;

/**
 * Created by hanhui on 2019/7/26 0026 17:14
 */
public interface AudioRecordListener extends AudioDataCallback {

    void onRecordError(String msg);

    void onRecordStarted(AudioRecord audioRecord);

    void onRecordReleased();
}
