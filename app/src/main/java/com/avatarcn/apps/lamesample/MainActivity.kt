package com.avatarcn.apps.lamesample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avatarcn.lame.AvatarLameMp3
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mp3AudioRecord: Mp3AudioRecord

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mp3AudioRecord = Mp3AudioRecord()
        // Example of a call to a native method
        sample_text.text = AvatarLameMp3.getVersion()
        startRecord.setOnClickListener {
            mp3AudioRecord.start()
        }

        stopRecord.setOnClickListener {
            mp3AudioRecord.stop()
        }
    }
}
