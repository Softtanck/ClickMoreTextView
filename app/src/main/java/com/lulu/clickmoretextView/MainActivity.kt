package com.lulu.clickmoretextView

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        showClick.setOnClickListener {
            textView.isShowMore = !textView.isShowMore
        }
        maxLinesClick.setOnClickListener {
//            if (textView.maxLines == 4) {
                textView.maxLines = Int.MAX_VALUE
//            } else {
//                textView.maxLines = 4
//            }
        }
        textView.setMoreTextOnClickListener(View.OnClickListener {
            val introDialog = IntroDialog()
            introDialog.introText = textView.text
            introDialog.show(supportFragmentManager)
        })
    }
}

private var SCALED_DENSITY = 0f
fun getScaledDensity(): Float {
    if (SCALED_DENSITY == 0f) {
        SCALED_DENSITY = Resources.getSystem().displayMetrics.scaledDensity
    }
    return SCALED_DENSITY
}

fun convertSpToPx(sp: Int): Int {
    return (sp * getScaledDensity() + 0.5f).toInt()
}

val Number.sp: Float get() = convertSpToPx(toInt()).toFloat()