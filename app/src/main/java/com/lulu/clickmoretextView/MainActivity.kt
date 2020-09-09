package com.lulu.clickmoretextView

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        showClick.setOnClickListener {
            textView.isShowMore = !textView.isShowMore
        }
        maxLinesClick.setOnClickListener {
            if (textView.maxLines == 4) {
                textView.maxLines = Int.MAX_VALUE
            } else {
                textView.maxLines = 4
            }
        }
        textView.setMoreTextOnClickListener(View.OnClickListener {
            Toast.makeText(this, "点击测试", Toast.LENGTH_SHORT).show()
        })
    }
}