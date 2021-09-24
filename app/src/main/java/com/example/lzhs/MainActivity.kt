package com.example.lzhs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.lzhs.mftunewheel.MFTuneWheel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mTextView = findViewById<TextView>(R.id.mTextView)
        mTextView.text = "112233"
        findViewById<MFTuneWheel>(R.id.mMFTuneWheel).setListener {
            mTextView.text = it
        }

    }
}