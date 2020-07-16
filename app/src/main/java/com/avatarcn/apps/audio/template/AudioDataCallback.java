package com.avatarcn.apps.audio.template;

/**
 * Created by hanhui on 2019/7/29 0029 14:13
 */
public interface AudioDataCallback {

    void onRecordBuffer(byte[] buffer, int offset, int length);
}
