package com.avatarcn.lame

/**
 * Created by hanhui on 2020/7/14 18:00
 */
object AvatarLameMp3 {
    init {
        System.loadLibrary("lamemp3")
    }

    external fun getVersion(): String

    /**
     *  @param inSampleRate ： 输入采样频率 Hz
     *  @param numChannel ： 输入声道数
     *  @param outSampleRate ： 输出采样频率 Hz
     *  @param outBitrate ： Encoded bit rate. KHz
     *  @param quality ： MP3音频质量。0~9。 其中0是最好，非常慢，9是最差。
     *推荐：
     *2 ：near-best quality, not too slow
     *5 ：good quality, fast
     *7 ：ok quality, really fast
     */
    external fun init(
        inSampleRate: Int,
        numChannel: Int,
        outSampleRate: Int,
        outBitrate: Int,
        quality: Int
    ): Int

    /**
     *  @param buffer_l ： 左声道数据
     * @param buffer_r：右声道数据
     * @param samples ：每个声道输入数据大小
     * @param mp3buf ：用于接收转换后的数据。7200 + (1.25 * buffer_l.length)
     */
    external fun encode(
        buffer_l: ShortArray,
        buffer_r: ShortArray,
        samples: Int,
        mp3buf: ByteArray
    ): Int

    /**
     * 将MP3结尾信息写入buffer中。
     */
    external fun flush(mp3buf: ByteArray): Int

    /**
     * 关闭释放Lame
     */
    external fun close(): Int
}