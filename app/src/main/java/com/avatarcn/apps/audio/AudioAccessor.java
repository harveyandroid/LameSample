package com.avatarcn.apps.audio;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by hanhui on 2019/7/26 0026 17:46
 */
public class AudioAccessor {
    private static final int MAX_BUF_LEN = 5242880;
    private static final int MIN_OUTPUT_TIME = 3000;
    private final String FILE_FMT;
    private final int SIZE_OF_WAVE_HEADER;
    private final int DATA_LENGTH_OFFSET;
    private final int FILE_LENGTH_OFFSET;
    private String mFilePath = null;
    private AudioAccessor.AccesserType mAccesserType;
    private int mOffset;
    private ByteBuffer mByteBuffer;
    private byte[] mBuffer;
    private int mBufLen;
    private int mDataCount;
    private long mLastFlushTime;
    private File mAudioFile;
    private RandomAccessFile mAccessFile;
    private FileChannel mFileChannel;
    private Object mFileSyncObj;
    private short mNumChannels;
    private short mFormat;
    private short mBitsPerSample;
    private int mSampleRate;
    private ILogger logger;

    protected AudioAccessor() {
        this.logger = AudioCentre.logger;
        this.mAccesserType = AudioAccessor.AccesserType.BUFFER;
        this.mOffset = 0;
        this.mByteBuffer = ByteBuffer.allocate(MAX_BUF_LEN);
        this.mBuffer = new byte[MAX_BUF_LEN];
        this.mBufLen = 0;
        this.mDataCount = 0;
        this.mLastFlushTime = System.currentTimeMillis();
        this.mAudioFile = null;
        this.mAccessFile = null;
        this.mFileChannel = null;
        this.mFileSyncObj = new Object();
        this.FILE_FMT = ".wav";
        this.SIZE_OF_WAVE_HEADER = 44;
        this.DATA_LENGTH_OFFSET = 40;
        this.FILE_LENGTH_OFFSET = 4;
        this.mNumChannels = 1;
        this.mFormat = 1;
        this.mBitsPerSample = 16;
        this.mSampleRate = 16000;
    }

    protected AudioAccessor(String filePath) throws IOException {
        this.logger = AudioCentre.logger;
        this.mAccesserType = AudioAccessor.AccesserType.READ_ONLY;
        this.mOffset = 0;
        this.mByteBuffer = ByteBuffer.allocate(MAX_BUF_LEN);
        this.mBuffer = new byte[MAX_BUF_LEN];
        this.mBufLen = 0;
        this.mDataCount = 0;
        this.mLastFlushTime = System.currentTimeMillis();
        this.mAudioFile = null;
        this.mAccessFile = null;
        this.mFileChannel = null;
        this.mFileSyncObj = new Object();
        this.FILE_FMT = ".wav";
        this.SIZE_OF_WAVE_HEADER = 44;
        this.DATA_LENGTH_OFFSET = 40;
        this.FILE_LENGTH_OFFSET = 4;
        this.mNumChannels = 1;
        this.mFormat = 1;
        this.mBitsPerSample = 16;
        this.mSampleRate = 16000;
        this.mFilePath = filePath;
        this.initFile();
    }

    protected AudioAccessor(String filePath, int sampleRate) throws IOException {
        this.logger = AudioCentre.logger;
        this.mAccesserType = AudioAccessor.AccesserType.WRITE_READ;
        this.mOffset = 0;
        this.mByteBuffer = ByteBuffer.allocate(MAX_BUF_LEN);
        this.mBuffer = new byte[MAX_BUF_LEN];
        this.mBufLen = 0;
        this.mDataCount = 0;
        this.mLastFlushTime = System.currentTimeMillis();
        this.mAudioFile = null;
        this.mAccessFile = null;
        this.mFileChannel = null;
        this.mFileSyncObj = new Object();
        this.FILE_FMT = ".wav";
        this.SIZE_OF_WAVE_HEADER = 44;
        this.DATA_LENGTH_OFFSET = 40;
        this.FILE_LENGTH_OFFSET = 4;
        this.mNumChannels = 1;
        this.mFormat = 1;
        this.mBitsPerSample = 16;
        this.mFilePath = filePath;
        this.mSampleRate = sampleRate;
        this.initFile();
    }

    public static AudioAccessor createBufferAccessor() {
        return new AudioAccessor();
    }

    public static AudioAccessor createReadOnlyAccessor(String var0) throws IOException {
        return new AudioAccessor(var0);
    }

    public static AudioAccessor createWriteReadAccessor(String var0, int var1) throws IOException {
        return new AudioAccessor(var0, var1);
    }

    public String getFilePath() {
        String var1 = null;
        synchronized (this.mFileSyncObj) {
            if (null != this.mAudioFile) {
                var1 = this.mAudioFile.getAbsolutePath();
            }
            return var1;
        }
    }

    public long getDataLength() {
        long var1 = 0L;
        synchronized (this.mFileSyncObj) {
            var1 = (long) this.mDataCount;
            return var1;
        }
    }

    public String getAudioInfo(AudioAccessor.AudioKeys var1) {
        String var2 = null;
        switch (var1) {
            case CHANNEL:
                var2 = String.valueOf(this.mNumChannels);
                break;
            case FORMAT:
                var2 = String.valueOf(this.mFormat);
                break;
            case BIT:
                var2 = String.valueOf(this.mBitsPerSample);
                break;
            case RATE:
                var2 = String.valueOf(this.mSampleRate);
        }

        return var2;
    }

    public int getBufferLength() {
        return MAX_BUF_LEN;
    }

    public int getCacheLeft() {
        boolean var1 = false;
        synchronized (this.mFileSyncObj) {
            int var5 = 2621440 - this.mBufLen;
            return var5;
        }
    }

    public int getAudio(byte[] var1) throws IOException {
        logger.d("getAudioData enter");
        int var2 = 0;
        if (AudioAccessor.AccesserType.BUFFER == this.mAccesserType) {
            if (null != var1 && var1.length == this.getBufferLength()) {
                synchronized (this.mFileSyncObj) {
                    if (null == this.mBuffer) {
                        throw new IOException("Data array is null!");
                    }

                    if (this.mBufLen > 0) {
                        System.arraycopy(this.mBuffer, 0, var1, 0, this.mBufLen);
                        var2 = this.mBufLen;
                        this.mBufLen = 0;
                        logger.d("getAudioData len:" + var2);
                    }
                }
            } else {
                var2 = -1;
                logger.e("getAudioData buffer is null or length is error !");
            }
        } else if (null != var1 && var1.length == this.getBufferLength()) {
            synchronized (this.mFileSyncObj) {
                if (null == this.mFileChannel) {
                    throw new IOException("File is null!");
                }

                if (this.getFileLength() > 44L) {
                    this.mByteBuffer.clear();
                    int var4 = (int) Math.min((long) this.mByteBuffer.capacity(), this.getFileLength() - (long) this.mOffset);
                    logger.d("getAudioData buffer len:" + var4);
                    if (var4 > 0) {
                        this.mFileChannel.position((long) this.mOffset);
                        if (var4 != this.readBytes(this.mOffset, this.mByteBuffer)) {
                            throw new IOException("Read audio length error:" + var4);
                        }

                        this.mByteBuffer.position(0);
                        this.mByteBuffer.get(var1, 0, var4);
                        this.mOffset += var4;
                        var2 = var4;
                        logger.d("getAudioData read len:" + var4);
                    }
                }
            }
        } else {
            var2 = -1;
            logger.e("getAudioData buffer is null or length is not enough !");
        }

        logger.d("getAudioData leave");
        return var2;
    }

    public synchronized boolean putAudio(byte[] var1, int var2) throws IOException {
        logger.d("putAudio enter");
        if (null == var1) {
            logger.e("data is null !");
            throw new NullPointerException();
        } else if (AudioAccessor.AccesserType.BUFFER == this.mAccesserType && MAX_BUF_LEN < this.mBufLen + var2) {
            logger.e("Buffer is not enough ! " + this.mBufLen);
            throw new IOException("Buffer is not enough ! " + this.mBufLen);
        } else if (AudioAccessor.AccesserType.READ_ONLY == this.mAccesserType) {
            logger.e("Current type is " + this.mAccesserType);
            throw new IOException("Current type is " + this.mAccesserType);
        } else {
            if (null != var1 && var2 > 0) {
                synchronized (this.mFileSyncObj) {
                    logger.d("putAudio data len=" + var2);
                    System.arraycopy(var1, 0, this.mBuffer, this.mBufLen, var2);
                    this.mBufLen += var2;
                    this.mDataCount += var2;
                    logger.d("putAudio buf len=" + this.mBufLen);
                }
            }

            if (AudioAccessor.AccesserType.WRITE_READ == this.mAccesserType) {
                this.saveAudio();
            }

            logger.d("putAudio leave");
            return true;
        }
    }

    public synchronized void flush() throws IOException {
        if (AudioAccessor.AccesserType.WRITE_READ != this.mAccesserType) {
            throw new IOException("Current type is " + this.mAccesserType);
        } else {
            synchronized (this.mFileSyncObj) {
                this.mFileChannel.force(true);
                this.mLastFlushTime = System.currentTimeMillis();
            }
        }
    }

    public void close() throws IOException {
        logger.d("AudioAccesser close enter");
        synchronized (this.mFileSyncObj) {
            if (AudioAccessor.AccesserType.WRITE_READ == this.mAccesserType) {
                this.saveAudio();
            }

            if (AudioAccessor.AccesserType.BUFFER != this.mAccesserType) {
                if (null != this.mFileChannel) {
                    this.mFileChannel.force(true);
                    this.mFileChannel.close();
                    this.mFileChannel = null;
                }

                if (null != this.mAccessFile) {
                    this.mAccessFile.close();
                    this.mAccessFile = null;
                }
            }

            this.mBuffer = null;
            this.mByteBuffer.clear();
            this.mByteBuffer = null;
        }

        logger.d("AudioAccesser close leave");
    }

    private void saveAudio() throws IOException {
        logger.d("saveAudioData enter");
        synchronized (this.mFileSyncObj) {
            if (null != this.mFileChannel) {
                logger.d("saveAudio write audio len:" + this.mBufLen + ", file length=" + this.getFileLength());
                if (0 < this.mBufLen) {
                    this.mByteBuffer.clear();
                    int var2 = this.mByteBuffer.capacity() - this.mBufLen;
                    this.mByteBuffer.position(var2);
                    this.mByteBuffer.put(this.mBuffer, 0, this.mBufLen);
                    this.writeBytes((int) this.getFileLength(), this.mByteBuffer, var2);
                    this.mBufLen = 0;
                    this.updateAudioFileHeader();
                }

                if (AudioAccessor.AccesserType.WRITE_READ == this.mAccesserType && this.isTimeToFlush()) {
                    logger.d("saveAudio flush to device.");
                    this.flush();
                }
            }
        }

        logger.d("saveAudioData leave");
    }

    private void initFile() throws IOException {
        if (AudioAccessor.AccesserType.BUFFER != this.mAccesserType) {
            synchronized (this.mFileSyncObj) {
                if (null == this.mFilePath) {
                    throw new IOException("File path is null");
                } else {
                    if (AudioAccessor.AccesserType.WRITE_READ == this.mAccesserType) {
                        String var2 = this.mFilePath;
                        String var3 = var2;
                        if (var2.endsWith("/")) {
                            var3 = var2.substring(0, var2.lastIndexOf("/"));
                        }

                        File var4 = new File(var3);
                        if ((!var4.isDirectory() || !var4.exists()) && !var4.mkdirs()) {
                            IOException var12 = new IOException("create file path failed");
                            throw var12;
                        }

                        if (!var2.endsWith(".wav") && !var2.endsWith(".pcm")) {
                            if (!var2.endsWith("/")) {
                                var2 = var2.concat("/");
                            }

                            Date var5 = new Date(System.currentTimeMillis());
                            SimpleDateFormat var6 = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.CHINA);
                            String var7 = var6.format(var5);
                            var2 = var2.concat(var7);
                            this.mAudioFile = new File(var2 + FILE_FMT);

                            String var9;
                            for (int var8 = 0; this.mAudioFile.exists(); this.mAudioFile = new File(var9)) {
                                ++var8;
                                var9 = var2 + "_" + var8 + FILE_FMT;
                            }
                        } else {
                            this.mAudioFile = new File(var2);
                            if (this.mAudioFile.exists()) {
                                throw new IOException("File is exists:" + var2);
                            }
                        }

                        logger.d("initFile createNewFile:" + var2);
                        if (!this.mAudioFile.createNewFile()) {
                            throw new IOException("create new file \"" + this.mAudioFile.getAbsolutePath() + "\" failed.");
                        }

                        this.mAccessFile = new RandomAccessFile(this.mAudioFile, "rw");
                        this.mFileChannel = this.mAccessFile.getChannel();
                        this.initAudioFileHeader();
                    } else if (AudioAccessor.AccesserType.READ_ONLY == this.mAccesserType) {
                        this.mAudioFile = new File(this.mFilePath);
                        if (!this.mAudioFile.exists()) {
                            throw new IOException("File is not exist:" + this.mFilePath);
                        }
                        this.mAccessFile = new RandomAccessFile(this.mAudioFile, "rw");
                        this.mFileChannel = this.mAccessFile.getChannel();
                        this.readAudioInfo();
                    }

                }
            }
        }
    }

    protected void initAudioFileHeader() throws IOException {
        String var1 = "RIFF";
        String var2 = "WAVE";
        String var3 = "fmt ";
        String var4 = "data";
        byte var5 = 0;
        this.writeBytes(var5, (byte[]) "RIFF".getBytes());
        int var8 = var5 + "RIFF".length();
        this.writeInt(var8, SIZE_OF_WAVE_HEADER);
        var8 += 4;
        this.writeBytes(var8, "WAVE".getBytes());
        var8 += "WAVE".length();
        this.writeBytes(var8, "fmt ".getBytes());
        var8 += "fmt ".length();
        this.writeInt(var8, 16);
        var8 += 4;
        this.writeShort(var8, this.mFormat);
        var8 += 2;
        this.writeShort(var8, this.mNumChannels);
        var8 += 2;
        this.writeInt(var8, this.mSampleRate);
        var8 += 4;
        int var6 = this.mNumChannels * this.mSampleRate * this.mBitsPerSample / 8;
        short var7 = (short) (this.mNumChannels * this.mBitsPerSample / 8);
        logger.d("writeAudioFileHeader NumChannels=" + this.mNumChannels + "SampleRate=" + this.mSampleRate + ", transferRate=" + var6 + ", adjustValue=" + var7 + ", bit=" + this.mBitsPerSample);
        this.writeInt(var8, var6);
        var8 += 4;
        this.writeShort(var8, var7);
        var8 += 2;
        this.writeShort(var8, this.mBitsPerSample);
        var8 += 2;
        this.writeBytes(var8, "data".getBytes());
        var8 += "data".length();
        this.writeInt(var8, 0);
    }

    protected void readAudioInfo() throws IOException {
        String var1 = "RIFF";
        ByteBuffer var2 = ByteBuffer.allocate(4);
        this.readBytes(0, var2);
        String var3 = new String(var2.array());
        if ("RIFF".equalsIgnoreCase(var3)) {
            byte var4 = 20;
            this.mFormat = this.readShort(var4);
            int var5 = var4 + 2;
            this.mNumChannels = this.readShort(var5);
            var5 += 2;
            this.mSampleRate = this.readInt(var5);
            var5 += 4;
            var5 += 4;
            var5 += 2;
            this.mBitsPerSample = this.readShort(var5);
        }
    }

    protected void writeBytes(int var1, ByteBuffer var2) throws IOException {
        logger.d("writeBytes buffer len=" + var2.capacity());
        var2.rewind();
        this.mFileChannel.position((long) var1);
        int var3 = this.mFileChannel.write(var2);
        logger.d("writeBytes writen len=" + var3);
    }

    protected void writeBytes(int var1, ByteBuffer var2, int var3) throws IOException {
        logger.d("writeBytes buffer len=" + (var2.capacity() - var3));
        var2.position(var3);
        this.mFileChannel.position((long) var1);
        int var4 = this.mFileChannel.write(var2);
        logger.d("writeBytes writen len=" + var4);
    }

    protected void writeBytes(int var1, byte[] var2) throws IOException {
        ByteBuffer var3 = ByteBuffer.allocate(var2.length);
        var3.put(var2);
        this.writeBytes(var1, var3);
    }

    protected void writeInt(int var1, int var2) throws IOException {
        ByteBuffer var3 = ByteBuffer.allocate(4);
        var3.put(0, (byte) (var2 >> 0));
        var3.put(1, (byte) (var2 >> 8));
        var3.put(2, (byte) (var2 >> 16));
        var3.put(3, (byte) (var2 >> 24));
        this.writeBytes(var1, var3);
    }

    protected void writeShort(int var1, short var2) throws IOException {
        ByteBuffer var3 = ByteBuffer.allocate(2);
        var3.put(0, (byte) (var2 >> 0));
        var3.put(1, (byte) (var2 >> 8));
        this.writeBytes(var1, var3);
    }

    protected int readInt(int var1) throws IOException {
        ByteBuffer var2 = ByteBuffer.allocate(4);
        this.mFileChannel.position((long) var1);
        this.mFileChannel.read(var2);
        return var2.getInt(0) << 0 | var2.getInt(1) << 8 | var2.getInt(2) << 16 | var2.getInt(3) << 24;
    }

    protected int readBytes(int var1, ByteBuffer var2) throws IOException {
        this.mFileChannel.position((long) var1);
        return this.mFileChannel.read(var2);
    }

    protected short readShort(int var1) throws IOException {
        ByteBuffer var2 = ByteBuffer.allocate(4);
        this.mFileChannel.position((long) var1);
        this.mFileChannel.read(var2);
        return (short) (var2.getShort(0) << 0 | var2.getShort(1) << 8);
    }

    protected void updateAudioFileHeader() throws IOException {
        logger.d("updateHeader File length:" + this.getDataLength() + ", mem file length:" + this.mFileChannel.size());
        this.writeInt(FILE_LENGTH_OFFSET, (int) this.getDataLength());
        logger.d("updateHeader data length:" + (this.getDataLength() - SIZE_OF_WAVE_HEADER));
        this.writeInt(DATA_LENGTH_OFFSET, (int) this.getDataLength() - SIZE_OF_WAVE_HEADER);
    }

    private boolean isTimeToFlush() {
        return MIN_OUTPUT_TIME <= System.currentTimeMillis() - this.mLastFlushTime;
    }

    private long getFileLength() throws IOException {
        int var1 = 0;
        if (null != this.mFileChannel) {
            var1 = (int) this.mFileChannel.size();
        }

        logger.d("getFileLength:" + var1);
        return (long) var1;
    }

    public enum AudioKeys {
        CHANNEL,
        FORMAT,
        BIT,
        RATE;

        AudioKeys() {
        }
    }

    enum AccesserType {
        WRITE_READ,
        READ_ONLY,
        BUFFER;

        AccesserType() {
        }
    }
}
