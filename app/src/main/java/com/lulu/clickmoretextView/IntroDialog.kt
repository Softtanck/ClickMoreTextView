package com.lulu.clickmoretextView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.title_page_intro_dialog_layout.*

/**
 * @author zhanglulu on 2020/9/10.
 * for
 */
class IntroDialog: BaseDialogFragment() {
    public var introText = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.title_page_intro_dialog_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true
        tvIntroText.text = introText
        ivClose.setOnClickListener { dismiss() }
    }

}