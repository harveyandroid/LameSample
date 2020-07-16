package com.avatarcn.apps.lamesample

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import com.avatarcn.apps.audio.RecordError
import com.avatarcn.lame.AvatarLameMp3
import kotlinx.coroutines.*
import java.io.FileOutputStream

/**
 * Created by hanhui on 2020/7/15 15:59
 */
class Mp3AudioRecord {
    var outBitrate = 32
    var audioSource = MediaRecorder.AudioSource.CAMCORDER
    var sampleRate = 16000
    var channelConfig = AudioFormat.CHANNEL_IN_MONO
    var audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val buffer: ByteArray
    private var mp3Buffer: ByteArray? = null
    private val audioRecord: AudioRecord
    private var recordJob: Job? = null
    private val outputStream: FileOutputStream

    init {
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        buffer = ByteArray(bufferSize)
        audioRecord = AudioRecord(
            audioSource, sampleRate, channelConfig, audioFormat, bufferSize
        )
        AvatarLameMp3.init(sampleRate, 1, sampleRate, outBitrate, 7)
        val filePath =
            Environment.getExternalStorageDirectory().absolutePath + "/audioRecordTest.mp3"
        outputStream = FileOutputStream(filePath)
    }


    fun start() {
        Log.e("Mp3AudioRecord", "start  ${Thread.currentThread()}")
        recordJob = CoroutineScope(Dispatchers.IO).launch {
            audioRecord.startRecording()
            Log.e("Mp3AudioRecord", "startRecording  ${Thread.currentThread()}")
            val state = audioRecord.recordingState
            if (state != AudioRecord.RECORDSTATE_RECORDING) {
                throw RecordError(String.format("recorder state is %d not recoding", state))
            }
            while (isActive) {
                val length = audioRecord.read(buffer, 0, buffer.size)
                Log.e("Mp3AudioRecord", "read  $length")
                if (length > 0) {
                    val audioBuf = BytesTransUtil.Bytes2Shorts(buffer, length)
                    val audioBufLength = audioBuf.size
                    val mpsLength = 7200.0 + audioBufLength * 1.25
                    mp3Buffer = ByteArray(mpsLength.toInt())
                    Log.e("Mp3AudioRecord", "mp3Buffer size: ${mp3Buffer?.size}")
                    val mp3BufLength =
                        AvatarLameMp3.encode(audioBuf, audioBuf, audioBufLength, mp3Buffer!!)
                    Log.e("Mp3AudioRecord", "encode  $mp3BufLength")
                    outputStream.write(mp3Buffer!!, 0, mp3BufLength)
                }
            }
        }

    }

    fun stop() {
        recordJob?.cancel()
        audioRecord.stop()
        if (mp3Buffer != null) {
            val bufLength = AvatarLameMp3.flush(mp3Buffer!!)
            Log.e("Mp3AudioRecord", "flush  $bufLength")
            outputStream.write(mp3Buffer, 0, bufLength)
        }
        AvatarLameMp3.close()
        outputStream.flush()
        outputStream.close()
        Log.e("Mp3AudioRecord", "stop ")
    }

}