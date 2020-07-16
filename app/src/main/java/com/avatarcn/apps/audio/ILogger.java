package com.avatarcn.apps.audio;

public interface ILogger {

    void showLog(boolean isShowLog);

    void showStackTrace(boolean isShowStackTrace);

    void showMonitor(boolean showMonitor);

    void d(String message);

    void i(String message);

    void w(String message);

    void e(String message);

    void monitor(String message);

    String getDefaultTag();
}
